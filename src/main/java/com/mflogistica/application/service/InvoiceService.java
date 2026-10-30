package com.mflogistica.application.service;

import com.mflogistica.application.dto.InvoiceLineRequestDto;
import com.mflogistica.application.dto.InvoicePdfDto;
import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.dto.InvoicePageDto;
import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.exception.BusinessException;
import com.mflogistica.domain.exception.DuplicateInvoiceNumberException;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import com.mflogistica.domain.model.FiscalData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal VAT_RATE = new BigDecimal("21.00");
    private static final BigDecimal IRPF_RATE = new BigDecimal("1.00");
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final PdfGeneratorPort pdfGeneratorPort;
    private final ClientService clientService;
    private final FiscalDataService fiscalDataService;

    public InvoiceService(InvoiceRepositoryPort invoiceRepositoryPort, PdfGeneratorPort pdfGeneratorPort,
                          ClientService clientService, FiscalDataService fiscalDataService) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
        this.clientService = clientService;
        this.fiscalDataService = fiscalDataService;
    }

    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceRequestDto request) {
        return saveInvoice(request);
    }

    @Transactional
    public void resetLocalDevelopmentInvoices() {
        invoiceRepositoryPort.deleteAll();
        clientService.resetToDefaultClient();
        LocalDate today = LocalDate.now();
        InvoiceRequestDto sampleInvoice = new InvoiceRequestDto(
                "FAC-" + today.getYear() + "-0001",
                today,
                "SAÚL GUERRERO LÓPEZ",
                "73025558Y",
                "Contrato Logístico Operativo\nCarrer Comanegra, 3\n17252 Calonge (Girona, España)",
                List.of(new InvoiceLineRequestDto("Servicio de reparto", BigDecimal.ONE, new BigDecimal("100.00")))
        );
        saveInvoice(sampleInvoice);
    }

    @Transactional
    public InvoiceResponseDto saveInvoice(InvoiceRequestDto request) {
        String invoiceNumber = request.invoiceNumber().trim();
        if (request.invoiceId() != null) {
            findInvoice(request.invoiceId());
        }
        if ((request.invoiceId() == null && invoiceRepositoryPort.existsByInvoiceNumber(invoiceNumber))
                || (request.invoiceId() != null
                && invoiceRepositoryPort.existsByInvoiceNumberAndIdNot(invoiceNumber, request.invoiceId()))) {
            throw new DuplicateInvoiceNumberException("Ya existe una factura con el número " + invoiceNumber);
        }

        List<InvoiceLine> lines = request.lines().stream().map(this::toLine).toList();
        BigDecimal subtotal = lines.stream().map(InvoiceLine::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(VAT_RATE).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalRetentions = subtotal.multiply(IRPF_RATE).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                .negate();
        BigDecimal totalAmount = subtotal.add(taxAmount).add(totalRetentions).setScale(2, RoundingMode.HALF_UP);

        FiscalData issuerSnapshot = request.invoiceId() == null
                ? fiscalDataService.getCurrent()
                : findInvoice(request.invoiceId()).issuerSnapshot();
        Invoice saved = invoiceRepositoryPort.save(new Invoice(request.invoiceId(), invoiceNumber,
                request.clientName().trim(), request.clientNif().trim(), trimOrEmpty(request.clientAddress()),
                request.issueDate(), subtotal, VAT_RATE, taxAmount, IRPF_RATE, totalRetentions, totalAmount, lines,
                issuerSnapshot));
        clientService.saveFromInvoice(saved.clientName(), saved.clientNif(), saved.clientAddress());
        return toResponse(saved);
    }

    @Transactional
    public InvoicePdfDto createInvoicePdf(InvoiceRequestDto request) {
        InvoiceResponseDto created = createInvoice(request);
        byte[] pdf = pdfGeneratorPort.generateInvoicePdf(findInvoice(created.id()));
        return new InvoicePdfDto(created.invoiceNumber(), pdf);
    }

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long invoiceId) {
        return pdfGeneratorPort.generateInvoicePdf(findInvoice(invoiceId));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> findRecentInvoices(int limit) {
        return invoiceRepositoryPort.findRecent(limit).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InvoicePageDto searchInvoices(String term, LocalDate from, LocalDate to, int page, int size) {
        String normalizedTerm = term == null || term.isBlank() ? null : term.trim();
        var result = invoiceRepositoryPort.search(normalizedTerm, from, to, page, size);
        return new InvoicePageDto(result.invoices().stream().map(this::toResponse).toList(),
                result.totalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDto findById(Long id) {
        return toResponse(findInvoice(id));
    }

    @Transactional(readOnly = true)
    public InvoiceRequestDto findForEdit(Long id) {
        Invoice invoice = findInvoice(id);
        InvoiceRequestDto request = new InvoiceRequestDto(invoice.invoiceNumber(), invoice.issueDate(),
                invoice.clientName(), invoice.clientNif(), invoice.clientAddress(),
                invoice.lines().stream()
                        .map(line -> new InvoiceLineRequestDto(line.description(), line.quantity(), line.unitPrice()))
                        .toList());
        request.setInvoiceId(invoice.id());
        return request;
    }

    @Transactional(readOnly = true)
    public String suggestInvoiceNumber(LocalDate issueDate) {
        String yearPrefix = "FAC-" + issueDate.getYear() + "-";
        long nextSequence = invoiceRepositoryPort.findLatestInvoiceNumber(yearPrefix)
                .map(this::sequenceFromInvoiceNumber)
                .orElse(0L) + 1;
        String candidate;
        do {
            candidate = yearPrefix + String.format("%04d", nextSequence++);
        } while (invoiceRepositoryPort.existsByInvoiceNumber(candidate));
        return candidate;
    }

    private long sequenceFromInvoiceNumber(String invoiceNumber) {
        String[] parts = invoiceNumber.split("-");
        if (parts.length != 3) {
            return 0;
        }
        try {
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepositoryPort.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontró la factura solicitada"));
    }

    private InvoiceLine toLine(InvoiceLineRequestDto line) {
        BigDecimal quantity = scale2(line.quantity());
        BigDecimal unitPrice = scale2(line.unitPrice());
        return new InvoiceLine(null, line.description().trim(), quantity, unitPrice,
                quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private InvoiceResponseDto toResponse(Invoice invoice) {
        return new InvoiceResponseDto(invoice.id(), invoice.invoiceNumber(), invoice.clientName(), invoice.clientNif(),
                invoice.issueDate(), invoice.subtotal(), invoice.taxRate(), invoice.taxAmount(), invoice.irpfRate(),
                invoice.totalRetentions(), invoice.totalAmount(),
                invoice.lines().isEmpty() ? "" : invoice.lines().getFirst().description());
    }
}
