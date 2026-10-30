package com.mflogistica.application.service;

import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceLineRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceServiceTest {

    @Test
    void shouldCalculateTaxAndTotal() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        PdfGeneratorPort pdfGeneratorPort = invoice -> "pdf".getBytes();
        InvoiceService service = new InvoiceService(repo, pdfGeneratorPort);

        InvoiceRequestDto request = new InvoiceRequestDto(
                "FAC-" + LocalDate.now().getYear() + "-0001",
                LocalDate.now(),
                "Cliente Demo",
                "B12345678",
                "Calle Mayor 1",
                List.of(new InvoiceLineRequestDto("Servicio urgente", BigDecimal.ONE, new BigDecimal("5467.15")))
        );

        InvoiceResponseDto response = service.createInvoice(request);

        assertEquals(new BigDecimal("21.00"), response.taxRate());
        assertEquals(new BigDecimal("1148.10"), response.taxAmount());
        assertEquals(new BigDecimal("1.00"), response.irpfRate());
        assertEquals(new BigDecimal("-54.67"), response.totalRetentions());
        assertEquals(new BigDecimal("6560.58"), response.totalAmount());
        assertEquals("FAC-" + LocalDate.now().getYear() + "-0001", response.invoiceNumber());
    }

    @Test
    void shouldCreateInvoiceAndGeneratePdfWithCalculatedTotals() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        AtomicReference<Invoice> invoiceInPdf = new AtomicReference<>();
        PdfGeneratorPort pdfGeneratorPort = invoice -> {
            invoiceInPdf.set(invoice);
            return "pdf".getBytes();
        };
        InvoiceService service = new InvoiceService(repo, pdfGeneratorPort);
        InvoiceRequestDto request = new InvoiceRequestDto(
                "FAC-" + LocalDate.now().getYear() + "-PDF",
                LocalDate.now(),
                "Cliente Demo",
                "B12345678",
                "Calle Mayor 1",
                List.of(new InvoiceLineRequestDto("Servicio de reparto", BigDecimal.ONE, new BigDecimal("100.00")))
        );

        var result = service.createInvoicePdf(request);

        assertEquals(request.invoiceNumber(), result.invoiceNumber());
        assertEquals("pdf", new String(result.content()));
        assertEquals(new BigDecimal("100.00"), invoiceInPdf.get().subtotal());
        assertEquals(new BigDecimal("21.00"), invoiceInPdf.get().taxAmount());
        assertEquals(new BigDecimal("-1.00"), invoiceInPdf.get().totalRetentions());
        assertEquals(new BigDecimal("120.00"), invoiceInPdf.get().totalAmount());
    }

    private static final class InMemoryInvoiceRepository implements InvoiceRepositoryPort {

        private final List<Invoice> invoices = new ArrayList<>();

        @Override
        public Invoice save(Invoice invoice) {
            Invoice persisted = new Invoice(
                    (long) (invoices.size() + 1),
                    invoice.invoiceNumber(),
                    invoice.clientName(),
                    invoice.clientNif(),
                    invoice.clientAddress(),
                    invoice.issueDate(),
                    invoice.subtotal(),
                    invoice.taxRate(),
                    invoice.taxAmount(),
                    invoice.irpfRate(),
                    invoice.totalRetentions(),
                    invoice.totalAmount(),
                    invoice.lines().stream()
                            .map(line -> new InvoiceLine((long) (invoices.size() + 1), line.description(),
                                    line.quantity(), line.unitPrice(), line.totalAmount()))
                            .toList()
            );
            invoices.add(persisted);
            return persisted;
        }

        @Override
        public Optional<Invoice> findById(Long id) {
            return invoices.stream().filter(i -> i.id().equals(id)).findFirst();
        }

        @Override
        public List<Invoice> findRecent(int limit) {
            return invoices.stream().limit(limit).toList();
        }

        @Override
        public long countByYear(int year) {
            return invoices.stream().filter(i -> i.issueDate().getYear() == year).count();
        }

        @Override
        public boolean existsByInvoiceNumber(String invoiceNumber) {
            return invoices.stream().anyMatch(invoice -> Objects.equals(invoice.invoiceNumber(), invoiceNumber));
        }
    }
}
