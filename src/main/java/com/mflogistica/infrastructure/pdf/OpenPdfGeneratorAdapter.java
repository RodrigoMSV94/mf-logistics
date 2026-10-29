package com.mflogistica.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.model.Invoice;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component
public class OpenPdfGeneratorAdapter implements PdfGeneratorPort {

    private static final Color BLACK = new Color(0x00, 0x00, 0x00);
    private static final Color BEIGE = new Color(0xFD, 0xF9, 0xF4);
    private static final Color PINK = new Color(0xE4, 0x4B, 0x7C);

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BLACK);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PINK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BLACK);

            Paragraph title = new Paragraph("MF Logistica - Factura", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(16f);
            document.add(title);

            document.add(new Paragraph("Numero: " + invoice.invoiceNumber(), bodyFont));
            document.add(new Paragraph("Fecha: " + invoice.issueDate(), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Datos del cliente", sectionFont));
            document.add(new Paragraph("Nombre: " + invoice.clientName(), bodyFont));
            document.add(new Paragraph("NIF/CIF: " + invoice.clientNif(), bodyFont));
            document.add(new Paragraph("Direccion: " + invoice.clientAddress(), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Detalle", sectionFont));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(8f);
            table.setSpacingAfter(12f);

            addRow(table, "Concepto", invoice.concept(), bodyFont);
            addRow(table, "Base imponible", euro(invoice.subtotal()), bodyFont);
            addRow(table, "IVA (" + invoice.taxRate() + "%)", euro(invoice.taxAmount()), bodyFont);
            addRow(table, "Total", euro(invoice.totalAmount()), bodyFont);

            document.add(table);

            Paragraph footer = new Paragraph("Gracias por confiar en MF Logistica", bodyFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBackgroundColor(BEIGE);
        labelCell.setPadding(8f);
        labelCell.setBorderColor(BLACK);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(8f);
        valueCell.setBorderColor(BLACK);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String euro(java.math.BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(value) + " EUR";
    }
}

