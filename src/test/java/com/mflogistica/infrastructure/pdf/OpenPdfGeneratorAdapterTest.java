package com.mflogistica.infrastructure.pdf;

import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;

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
                new BigDecimal("5467.15"),
                new BigDecimal("21.00"),
                new BigDecimal("1148.10"),
                new BigDecimal("1.00"),
                new BigDecimal("-54.67"),
                new BigDecimal("6560.58"),
                List.of(new InvoiceLine(1L, "Servicio de reparto\nRuta Girona", BigDecimal.ONE,
                        new BigDecimal("5467.15"), new BigDecimal("5467.15")))
        );

        byte[] pdf = new OpenPdfGeneratorAdapter().generateInvoicePdf(invoice);

        assertTrue(new String(pdf, java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        PdfReader reader = new PdfReader(pdf);
        assertEquals(1, reader.getNumberOfPages());
        ClassPathResource logoResource = new ClassPathResource("static/images/mf-logo-transparent.png");
        assertTrue(logoResource.exists());
        try (InputStream logoStream = logoResource.getInputStream()) {
            assertTrue(ImageIO.read(logoStream).getColorModel().hasAlpha());
        }
        PdfDictionary pageResources = reader.getPageN(1).getAsDict(PdfName.RESOURCES);
        PdfDictionary xObjects = pageResources.getAsDict(PdfName.XOBJECT);
        assertTrue(xObjects.getKeys().stream()
                .map(key -> PdfReader.getPdfObject(xObjects.get(key)))
                .filter(PdfDictionary.class::isInstance)
                .map(PdfDictionary.class::cast)
                .anyMatch(xObject -> PdfName.IMAGE.equals(xObject.getAsName(PdfName.SUBTYPE))));
        String text = new PdfTextExtractor(reader).getTextFromPage(1).replaceAll("\\s+", " ");
        assertTrue(text.contains("FACTURA ORDINARIA DE SERVICIOS"));
        assertTrue(text.contains("Cliente Demo"));
        assertTrue(text.contains("Ruta Girona"));
        assertTrue(text.contains("CAIXABANK ESPAÑA"));
        assertTrue(text.contains("BASE IMPONIBLE"));
        assertTrue(text.contains("IVA (21,00%)"), text);
        assertTrue(text.contains("21,00%"), text);
        assertTrue(text.contains("TOTAL IVA 21%"), text);
        assertTrue(text.contains("6.615,25 €"), text);
        assertTrue(text.contains("IRPF (1,00%)"), text);
        assertTrue(text.contains("TOTAL RETENCIONES"), text);
        assertTrue(text.contains("-54,67 €"), text);
        assertTrue(text.contains("6.560,58 €"));
        assertTrue(!text.contains("Vencimiento: A la vista / Inmediato"));
        assertTrue(!text.contains("Régimen: General IVA (Península)"));
    }
}
