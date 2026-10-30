package com.mflogistica.application.service;

import com.mflogistica.application.dto.InvoiceLineRequestDto;
import com.mflogistica.application.dto.InvoicePdfDto;
import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.exception.BusinessException;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
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

    public InvoiceService(InvoiceRepositoryPort invoiceRepositoryPort, PdfGeneratorPort pdfGeneratorPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
    }

    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceRequestDto request) {
        String invoiceNumber = request.invoiceNumber().trim();
        if (invoiceRepositoryPort.existsByInvoiceNumber(invoiceNumber)) {
            throw new BusinessException("Ya existe una factura con el número " + invoiceNumber);
        }

        List<InvoiceLine> lines = request.lines().stream().map(this::toLine).toList();
        BigDecimal subtotal = lines.stream().map(InvoiceLine::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(VAT_RATE).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalRetentions = subtotal.multiply(IRPF_RATE).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                .negate();
        BigDecimal totalAmount = subtotal.add(taxAmount).add(totalRetentions).setScale(2, RoundingMode.HALF_UP);

        Invoice saved = invoiceRepositoryPort.save(new Invoice(null, invoiceNumber,
                request.clientName().trim(), request.clientNif().trim(), trimOrEmpty(request.clientAddress()),
                request.issueDate(), subtotal, VAT_RATE, taxAmount, IRPF_RATE, totalRetentions, totalAmount, lines));
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
    public InvoiceResponseDto findById(Long id) {
        return toResponse(findInvoice(id));
    }

    @Transactional(readOnly = true)
    public String suggestInvoiceNumber(LocalDate issueDate) {
        return "FAC-" + issueDate.getYear() + "-" + String.format("%04d", invoiceRepositoryPort.countByYear(issueDate.getYear()) + 1);
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
