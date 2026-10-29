package com.mflogistica.application.service;

import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.exception.BusinessException;
import com.mflogistica.domain.model.Invoice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final PdfGeneratorPort pdfGeneratorPort;

    public InvoiceService(InvoiceRepositoryPort invoiceRepositoryPort, PdfGeneratorPort pdfGeneratorPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
    }

    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceRequestDto requestDto) {
        BigDecimal subtotal = scale2(requestDto.subtotal());
        BigDecimal taxRate = scale2(requestDto.taxRate());

        BigDecimal taxAmount = subtotal.multiply(taxRate)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        LocalDate issueDate = LocalDate.now();
        String invoiceNumber = nextInvoiceNumber(issueDate.getYear());

        Invoice invoiceToPersist = new Invoice(
                null,
                invoiceNumber,
                requestDto.clientName().trim(),
                requestDto.clientNif().trim(),
                requestDto.clientAddress() == null ? "" : requestDto.clientAddress().trim(),
                issueDate,
                subtotal,
                taxRate,
                taxAmount,
                totalAmount,
                requestDto.concept().trim()
        );

        Invoice persisted = invoiceRepositoryPort.save(invoiceToPersist);
        return toResponse(persisted);
    }

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("No se encontro la factura solicitada"));
        return pdfGeneratorPort.generateInvoicePdf(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> findRecentInvoices(int limit) {
        return invoiceRepositoryPort.findRecent(limit).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDto findById(Long invoiceId) {
        Invoice invoice = invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("No se encontro la factura solicitada"));
        return toResponse(invoice);
    }

    private String nextInvoiceNumber(int year) {
        long next = invoiceRepositoryPort.countByYear(year) + 1;
        return "FAC-" + year + "-" + String.format("%04d", next);
    }

    private BigDecimal scale2(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private InvoiceResponseDto toResponse(Invoice invoice) {
        return new InvoiceResponseDto(
                invoice.id(),
                invoice.invoiceNumber(),
                invoice.clientName(),
                invoice.clientNif(),
                invoice.issueDate(),
                invoice.subtotal(),
                invoice.taxRate(),
                invoice.taxAmount(),
                invoice.totalAmount(),
                invoice.concept()
        );
    }
}

