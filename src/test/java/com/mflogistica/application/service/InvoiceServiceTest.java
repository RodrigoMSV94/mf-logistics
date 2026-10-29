package com.mflogistica.application.service;

import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.model.Invoice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceServiceTest {

    @Test
    void shouldCalculateTaxAndTotal() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        PdfGeneratorPort pdfGeneratorPort = invoice -> "pdf".getBytes();
        InvoiceService service = new InvoiceService(repo, pdfGeneratorPort);

        InvoiceRequestDto request = new InvoiceRequestDto(
                "Cliente Demo",
                "B12345678",
                "Calle Mayor 1",
                "Servicio urgente",
                new BigDecimal("100.00"),
                new BigDecimal("21.00")
        );

        InvoiceResponseDto response = service.createInvoice(request);

        assertEquals(new BigDecimal("21.00"), response.taxAmount());
        assertEquals(new BigDecimal("121.00"), response.totalAmount());
        assertEquals("FAC-" + LocalDate.now().getYear() + "-0001", response.invoiceNumber());
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
                    invoice.totalAmount(),
                    invoice.concept()
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
    }
}

