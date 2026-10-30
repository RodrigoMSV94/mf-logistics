package com.mflogistica.infrastructure.pdf;

import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfGeneratorAdapterTest {

    @Test
    void shouldGeneratePdfForInvoiceWithVatAndRetention() throws Exception {
        Invoice invoice = new Invoice(
                1L,
                "FAC-2026-0001",
                "Cliente Demo",
                "B12345678",
                "Calle Mayor 1",
                LocalDate.of(2026, 10, 30),
                new BigDecimal("100.00"),
                new BigDecimal("21.00"),
                new BigDecimal("21.00"),
                new BigDecimal("1.00"),
                new BigDecimal("-1.00"),
                new BigDecimal("120.00"),
                List.of(new InvoiceLine(1L, "Servicio de reparto", BigDecimal.ONE,
                        new BigDecimal("100.00"), new BigDecimal("100.00")))
        );

        byte[] pdf = new OpenPdfGeneratorAdapter().generateInvoicePdf(invoice);

        assertTrue(new String(pdf, java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        PdfReader reader = new PdfReader(pdf);
        assertEquals(1, reader.getNumberOfPages());
        String text = new PdfTextExtractor(reader).getTextFromPage(1).replaceAll("\\s+", " ");
        assertTrue(text.contains("FACTURA ORDINARIA DE SERVICIOS"));
        assertTrue(text.contains("Cliente Demo"));
        assertTrue(text.contains("CAIXABANK ESPAÑA"));
        assertTrue(text.contains("BASE IMPONIBLE"));
        assertTrue(text.contains("120,00"));
    }
}
