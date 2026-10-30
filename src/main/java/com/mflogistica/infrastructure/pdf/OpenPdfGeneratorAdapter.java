package com.mflogistica.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfWriter;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class OpenPdfGeneratorAdapter implements PdfGeneratorPort {

    private static final Color INK = new Color(0x11, 0x14, 0x18);
    private static final Color MUTED = new Color(0x64, 0x74, 0x8B);
    private static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);
    private static final Color SOFT = new Color(0xF8, 0xFA, 0xFC);
    private static final Color SAND = new Color(0xFB, 0xF9, 0xF6);
    private static final Color SAND_BORDER = new Color(0xF0, 0xEA, 0xE1);
    private static final Color PINK = new Color(0xEE, 0x52, 0x83);
    private static final Color RED = new Color(0xDC, 0x26, 0x26);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd / MM / yyyy");
    private static final float SUMMARY_TOP_FROM_BOTTOM = 367;
    private static final String LOGO_PATH = "static/images/mf-logo-transparent.png";
    private static final String ISSUER_NAME = "MARTHA DENISSE CARRILLO TOVAR";
    private static final String ISSUER_NIF = "Y8023499D";
    private static final String ISSUER_ADDRESS = "Carrer del Pintor Huguet, 1\n"
            + "17310 Lloret de Mar (Girona, España)";
    private static final String ISSUER_PHONE = "+34 664 008 020";
    private static final String ISSUER_EMAIL = "denissecarrillo1986@gmail.com";
    private static final String IBAN = "ES50 2100 8137 8902 0047 3817";

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 24, 24, 18, 72);

        try {
            Image logo = loadLogo();
            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(new InvoicePageEvent(logo));
            document.open();

            addHeader(document, invoice, logo);
            addParties(document, invoice);
            addLineItems(document, invoice);
            addFinancialSummary(document, writer, invoice);
            document.close();
            return output.toByteArray();
        } catch (Exception exception) {
            if (document.isOpen()) {
                document.close();
            }
            throw new IllegalStateException("No se pudo generar el PDF de la factura", exception);
        }
    }

    private Image loadLogo() throws IOException, DocumentException {
        ClassPathResource resource = new ClassPathResource(LOGO_PATH);
        try (var input = resource.getInputStream()) {
            return Image.getInstance(input.readAllBytes());
        }
    }

    private void addHeader(Document document, Invoice invoice, Image logo) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{1.25f, 6.35f, 5.4f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(16);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(0);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPTable logoFrame = new PdfPTable(1);
        logoFrame.setTotalWidth(52);
        logoFrame.setLockedWidth(true);
        logoFrame.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell logoFrameCell = new PdfPCell();
        logoFrameCell.setFixedHeight(52);
        logoFrameCell.setBorder(Rectangle.NO_BORDER);
        logoFrameCell.setPadding(4);
        logoFrameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoFrameCell.setCellEvent(new RoundedCellEvent(INK, PINK, 10));
        Image headerLogo = Image.getInstance(logo);
        headerLogo.scaleToFit(43, 43);
        headerLogo.setAlignment(Element.ALIGN_CENTER);
        logoFrameCell.addElement(headerLogo);
        logoFrame.addCell(logoFrameCell);
        logoCell.addElement(logoFrame);
        header.addCell(logoCell);

        PdfPCell brandCell = borderlessCell();
        brandCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph brand = new Paragraph("MF LOGÍSTICA", font(19, Font.BOLD, INK));
        brand.setSpacingAfter(3);
        brandCell.addElement(brand);
        brandCell.addElement(new Paragraph("Operatividad integral de paquetería", font(9, Font.NORMAL, MUTED)));
        header.addCell(brandCell);

        PdfPCell metaCell = borderlessCell();
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        addParagraph(metaCell, "FACTURA ORDINARIA DE SERVICIOS", font(8, Font.BOLD, MUTED),
                Element.ALIGN_RIGHT, 3);
        addParagraph(metaCell, invoice.invoiceNumber(), font(17, Font.BOLD, INK, FontFactory.COURIER),
                Element.ALIGN_RIGHT, 4);
        addParagraph(metaCell, "Fecha emisión: " + DATE_FORMAT.format(invoice.issueDate()),
                font(8.5f, Font.NORMAL, MUTED), Element.ALIGN_RIGHT, 1);
        addParagraph(metaCell, "Vencimiento: A la vista / Inmediato",
                font(8.5f, Font.NORMAL, MUTED), Element.ALIGN_RIGHT, 1);
        addParagraph(metaCell, "Régimen: General IVA (Península)",
                font(8.5f, Font.NORMAL, MUTED), Element.ALIGN_RIGHT, 0);
        header.addCell(metaCell);

        document.add(header);
        addDivider(document);
    }

    private void addParties(Document document, Invoice invoice) throws DocumentException {
        PdfPTable parties = new PdfPTable(new float[]{1, 0.05f, 1});
        parties.setWidthPercentage(100);
        parties.setSpacingBefore(13);
        parties.setSpacingAfter(16);
        parties.setKeepTogether(true);
        parties.addCell(partyCell(
                "EMISOR / PRESTADOR",
                "NIF " + ISSUER_NIF,
                ISSUER_NAME,
                "MF LOGÍSTICA\n" + ISSUER_ADDRESS
                        + "\nTeléfono: " + ISSUER_PHONE
                        + "\nEmail: " + ISSUER_EMAIL
        ));
        PdfPCell gap = new PdfPCell();
        gap.setBorder(Rectangle.NO_BORDER);
        parties.addCell(gap);
        parties.addCell(partyCell(
                "DESTINATARIO / CLIENTE",
                "NIF " + invoice.clientNif(),
                invoice.clientName(),
                invoice.clientAddress()
        ));
        document.add(parties);
    }

    private PdfPCell partyCell(String role, String nif, String name, String details) {
        PdfPCell card = new PdfPCell();
        card.setBackgroundColor(Color.WHITE);
        card.setBorder(Rectangle.NO_BORDER);
        card.setCellEvent(new RoundedCellEvent(SAND, SAND_BORDER, 9));
        card.setPadding(12);
        card.setMinimumHeight(116);

        PdfPTable cardHeader = new PdfPTable(new float[]{1, 1});
        cardHeader.setWidthPercentage(100);
        addTextCell(cardHeader, role, font(8, Font.BOLD, new Color(0x8A, 0x7B, 0x6C)),
                Element.ALIGN_LEFT, Rectangle.NO_BORDER, null, 0, 0, 4, 0);
        addTextCell(cardHeader, nif, font(8, Font.BOLD, INK, FontFactory.COURIER),
                Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null, 0, 0, 4, 0);
        card.addElement(cardHeader);

        Paragraph partyName = new Paragraph(name, font(10, Font.BOLD, INK));
        partyName.setSpacingAfter(4);
        card.addElement(partyName);
        card.addElement(new Paragraph(details == null ? "" : details, font(8.5f, Font.NORMAL, MUTED)));
        return card;
    }

    private void addLineItems(Document document, Invoice invoice) throws DocumentException {
        PdfPTable items = new PdfPTable(new float[]{0.55f, 4.9f, 1.05f, 1.35f, 1.4f});
        items.setWidthPercentage(100);
        items.setHeaderRows(1);
        items.setSpacingAfter(12);
        items.setWidths(new float[]{0.55f, 4.9f, 1.05f, 1.35f, 1.4f});
        addTextCell(items, "LÍN.", font(7.5f, Font.BOLD, MUTED), Element.ALIGN_CENTER,
                Rectangle.BOTTOM | Rectangle.TOP, BORDER, 8, 5, 8, 5);
        addTextCell(items, "DESCRIPCIÓN TÉCNICA DEL SERVICIO", font(7.5f, Font.BOLD, MUTED),
                Element.ALIGN_LEFT, Rectangle.BOTTOM | Rectangle.TOP, BORDER, 8, 5, 8, 5);
        addTextCell(items, "UNIDAD", font(7.5f, Font.BOLD, MUTED), Element.ALIGN_CENTER,
                Rectangle.BOTTOM | Rectangle.TOP, BORDER, 8, 5, 8, 5);
        addTextCell(items, "PRECIO UNIT.", font(7.5f, Font.BOLD, MUTED), Element.ALIGN_RIGHT,
                Rectangle.BOTTOM | Rectangle.TOP, BORDER, 8, 5, 8, 5);
        addTextCell(items, "BASE TOTAL", font(7.5f, Font.BOLD, MUTED), Element.ALIGN_RIGHT,
                Rectangle.BOTTOM | Rectangle.TOP, BORDER, 8, 5, 8, 5);

        int lineNumber = 1;
        for (InvoiceLine line : invoice.lines()) {
            Color rowBackground = lineNumber % 2 == 0 ? SOFT : Color.WHITE;
            addTextCell(items, String.format("%02d", lineNumber), font(8, Font.BOLD, MUTED, FontFactory.COURIER),
                    Element.ALIGN_CENTER, Rectangle.BOTTOM, BORDER, 9, 5, 9, 5, rowBackground);
            addTextCell(items, line.description(), font(8.5f, Font.BOLD, INK),
                    Element.ALIGN_LEFT, Rectangle.BOTTOM, BORDER, 9, 5, 9, 5, rowBackground);
            addTextCell(items, line.quantity().stripTrailingZeros().toPlainString(), font(8, Font.NORMAL, INK),
                    Element.ALIGN_CENTER, Rectangle.BOTTOM, BORDER, 9, 5, 9, 5, rowBackground);
            addTextCell(items, euro(line.unitPrice()), font(8, Font.BOLD, INK, FontFactory.COURIER),
                    Element.ALIGN_RIGHT, Rectangle.BOTTOM, BORDER, 9, 5, 9, 5, rowBackground);
            addTextCell(items, euro(line.totalAmount()), font(8, Font.BOLD, INK, FontFactory.COURIER),
                    Element.ALIGN_RIGHT, Rectangle.BOTTOM, BORDER, 9, 5, 9, 5, rowBackground);
            lineNumber++;
        }

        document.add(items);
    }

    private void addFinancialSummary(Document document, PdfWriter writer, Invoice invoice) throws DocumentException {
        float currentY = writer.getVerticalPosition(true);
        float spacerHeight = currentY - SUMMARY_TOP_FROM_BOTTOM;
        if (spacerHeight > 0) {
            PdfPTable spacer = new PdfPTable(1);
            spacer.setWidthPercentage(100);
            PdfPCell spacerCell = new PdfPCell(new Phrase(""));
            spacerCell.setFixedHeight(spacerHeight);
            spacerCell.setPadding(0);
            spacerCell.setBorder(Rectangle.NO_BORDER);
            spacer.addCell(spacerCell);
            document.add(spacer);
        }

        PdfPTable summary = new PdfPTable(new float[]{1.15f, 0.06f, 0.85f});
        summary.setWidthPercentage(100);
        summary.setSpacingAfter(14);
        summary.setKeepTogether(true);
        summary.addCell(paymentCard());
        PdfPCell gap = new PdfPCell();
        gap.setBorder(Rectangle.NO_BORDER);
        summary.addCell(gap);
        summary.addCell(totalsCard(invoice));
        document.add(summary);
    }

    private PdfPCell paymentCard() {
        PdfPCell card = new PdfPCell();
        card.setBorder(Rectangle.NO_BORDER);
        card.setBackgroundColor(SAND);
        card.setCellEvent(new RoundedCellEvent(null, SAND_BORDER, 9, false, SAND));
        card.setPadding(12);
        card.setPaddingTop(14);

        PdfPTable title = new PdfPTable(new float[]{1, 1});
        title.setWidthPercentage(100);
        addTextCell(title, "MODALIDAD DE PAGO", font(8, Font.BOLD, new Color(0x8A, 0x7B, 0x6C)),
                Element.ALIGN_LEFT, Rectangle.NO_BORDER, null, 0, 0, 6, 0);
        addTextCell(title, "CAIXABANK ESPAÑA", font(8.5f, Font.BOLD, INK),
                Element.ALIGN_RIGHT, Rectangle.NO_BORDER, null, 0, 0, 6, 0);
        card.addElement(title);

        PdfPCell iban = new PdfPCell();
        iban.setBackgroundColor(Color.WHITE);
        iban.setBorder(Rectangle.NO_BORDER);
        iban.setCellEvent(new RoundedCellEvent(Color.WHITE, new Color(0xDC, 0xD4, 0xC9), 6, true));
        iban.setPadding(9);
        iban.setPaddingTop(6);
        iban.setPaddingBottom(6);
        iban.addElement(new Paragraph("CÓDIGO INTERNACIONAL DE CUENTA (IBAN)",
                font(7, Font.BOLD, new Color(0x8A, 0x7B, 0x6C))));
        Paragraph ibanValue = new Paragraph(IBAN, font(10, Font.BOLD, INK, FontFactory.COURIER));
        ibanValue.setSpacingBefore(3);
        iban.addElement(ibanValue);
        PdfPTable ibanTable = new PdfPTable(1);
        ibanTable.setWidthPercentage(100);
        ibanTable.addCell(iban);
        card.addElement(ibanTable);

        Paragraph beneficiary = new Paragraph();
        beneficiary.setFont(font(7.5f, Font.NORMAL, MUTED));
        beneficiary.add(new Phrase("Beneficiario: ", font(7.5f, Font.BOLD, MUTED)));
        beneficiary.add(new Phrase(ISSUER_NAME, font(7.5f, Font.NORMAL, MUTED)));
        beneficiary.setSpacingBefore(5);
        card.addElement(beneficiary);
        PdfPTable paymentTable = new PdfPTable(1);
        paymentTable.setWidthPercentage(100);
        paymentTable.addCell(card);

        PdfPCell wrapper = new PdfPCell(paymentTable);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setPadding(0);
        wrapper.setVerticalAlignment(Element.ALIGN_BOTTOM);
        return wrapper;
    }

    private PdfPCell totalsCard(Invoice invoice) {
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(100);
        addTotalRow(totals, "BASE\nIMPONIBLE", euro(invoice.subtotal()), false);
        addTotalRow(totals, "IVA\n(" + percent(invoice.taxRate()) + ")", percent(invoice.taxRate()), false);
        addTotalRow(totals, "TOTAL IVA\n21%", "+" + euro(invoice.taxAmount()), false);
        addTotalRow(totals, "IRPF\n(-" + percent(invoice.irpfRate()) + ")",
                "-" + percent(invoice.irpfRate()), true);
        addTotalRow(totals, "TOTAL\nRETENCIONES", euro(invoice.totalRetentions()), true);

        PdfPTable grandTotalContent = new PdfPTable(new float[]{1, 1});
        grandTotalContent.setWidthPercentage(100);
        PdfPCell grandTotalLabel = new PdfPCell();
        grandTotalLabel.setBorder(Rectangle.NO_BORDER);
        grandTotalLabel.setPadding(0);
        grandTotalLabel.addElement(new Paragraph("TOTAL", font(8, Font.BOLD, Color.WHITE)));
        grandTotalLabel.addElement(new Paragraph("Total a liquidar", font(7, Font.BOLD, PINK)));
        grandTotalContent.addCell(grandTotalLabel);

        PdfPCell grandTotalAmount = new PdfPCell();
        grandTotalAmount.setBorder(Rectangle.NO_BORDER);
        grandTotalAmount.setPadding(0);
        grandTotalAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
        grandTotalAmount.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph grandTotalValue = new Paragraph(euro(invoice.totalAmount()),
                font(16, Font.BOLD, Color.WHITE, FontFactory.COURIER));
        grandTotalValue.setAlignment(Element.ALIGN_RIGHT);
        grandTotalAmount.addElement(grandTotalValue);
        grandTotalContent.addCell(grandTotalAmount);

        PdfPCell grandTotal = new PdfPCell(grandTotalContent);
        grandTotal.setColspan(2);
        grandTotal.setBorder(Rectangle.NO_BORDER);
        grandTotal.setBackgroundColor(INK);
        grandTotal.setPadding(12.65f);
        addStyledCell(totals, grandTotal);

        PdfPCell wrapper = new PdfPCell(totals);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setCellEvent(new RoundedCellEvent(null, BORDER, 9));
        wrapper.setPadding(0);
        return wrapper;
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean negative) {
        Color textColor = negative ? RED : MUTED;
        Color valueColor = negative ? RED : INK;
        addTextCell(table, label, font(8.5f, Font.NORMAL, textColor), Element.ALIGN_LEFT,
                Rectangle.BOTTOM, BORDER, 4.75f, 10, 4.75f, 10);
        addTextCell(table, value, font(8.5f, Font.BOLD, valueColor, FontFactory.COURIER), Element.ALIGN_RIGHT,
                Rectangle.BOTTOM, BORDER, 4.75f, 10, 4.75f, 10);
    }

    private void addDivider(Document document) throws DocumentException {
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER);
        cell.setBorderWidth(1);
        cell.setPadding(0);
        cell.setFixedHeight(1);
        divider.addCell(cell);
        document.add(divider);
    }

    private void addParagraph(PdfPCell cell, String text, Font font, int alignment, float spacingAfter) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(alignment);
        paragraph.setSpacingAfter(spacingAfter);
        cell.addElement(paragraph);
    }

    private void addTextCell(PdfPTable table, String text, Font font, int alignment, int border,
                             Color borderColor, float paddingTop, float paddingRight,
                             float paddingBottom, float paddingLeft) {
        addTextCell(table, text, font, alignment, border, borderColor,
                paddingTop, paddingRight, paddingBottom, paddingLeft, null);
    }

    private void addTextCell(PdfPTable table, String text, Font font, int alignment, int border,
                             Color borderColor, float paddingTop, float paddingRight,
                             float paddingBottom, float paddingLeft, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(border);
        if (borderColor != null) {
            cell.setBorderColor(borderColor);
        }
        if (background != null) {
            cell.setBackgroundColor(background);
        }
        cell.setPaddingTop(paddingTop);
        cell.setPaddingRight(paddingRight);
        cell.setPaddingBottom(paddingBottom);
        cell.setPaddingLeft(paddingLeft);
        addStyledCell(table, cell);
    }

    private void addStyledCell(PdfPTable table, PdfPCell cell) {
        table.addCell(cell);
    }

    private PdfPCell borderlessCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingLeft(10);
        cell.setPaddingRight(8);
        return cell;
    }

    private Font font(float size, int style, Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
    }

    private Font font(float size, int style, Color color, String family) {
        return FontFactory.getFont(family, size, style, color);
    }

    private String euro(BigDecimal value) {
        return number(value) + " €";
    }

    private String percent(BigDecimal value) {
        return number(value) + "%";
    }

    private String number(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-ES"));
        return new DecimalFormat("#,##0.00", symbols).format(value);
    }

    private static final class InvoicePageEvent extends PdfPageEventHelper {

        private final Image logo;

        private InvoicePageEvent(Image logo) {
            this.logo = logo;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            float pageWidth = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();
            float accentY = pageHeight - 3;
            float midpoint = pageWidth * 0.55f;
            var canvas = writer.getDirectContent();

            canvas.saveState();
            canvas.rectangle(0, accentY, midpoint, 3);
            canvas.clip();
            canvas.newPath();
            canvas.paintShading(PdfShading.simpleAxial(writer, 0, accentY, midpoint, accentY, INK, PINK));
            canvas.restoreState();

            canvas.saveState();
            canvas.rectangle(midpoint, accentY, pageWidth - midpoint, 3);
            canvas.clip();
            canvas.newPath();
            canvas.paintShading(PdfShading.simpleAxial(writer, midpoint, accentY, pageWidth, accentY, PINK, INK));
            canvas.restoreState();

            canvas.saveState();
            canvas.setColorFill(INK);
            canvas.rectangle(0, 0, pageWidth, 58);
            canvas.fill();
            Image footerLogo = Image.getInstance(logo);
            footerLogo.scaleToFit(48, 48);
            footerLogo.setAbsolutePosition((pageWidth - footerLogo.getScaledWidth()) / 2, 5);
            try {
                canvas.addImage(footerLogo);
            } catch (DocumentException exception) {
                throw new IllegalStateException("No se pudo dibujar el logo de la factura", exception);
            }
            canvas.restoreState();
        }
    }

    private static final class RoundedCellEvent implements com.lowagie.text.pdf.PdfPCellEvent {

        private final Color background;
        private final Color border;
        private final float radius;
        private final boolean dashed;
        private final Color cornerBackground;

        private RoundedCellEvent(Color background, Color border, float radius) {
            this(background, border, radius, false, null);
        }

        private RoundedCellEvent(Color background, Color border, float radius, boolean dashed) {
            this(background, border, radius, dashed, null);
        }

        private RoundedCellEvent(Color background, Color border, float radius, boolean dashed,
                                 Color cornerBackground) {
            this.background = background;
            this.border = border;
            this.radius = radius;
            this.dashed = dashed;
            this.cornerBackground = cornerBackground;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position,
                               com.lowagie.text.pdf.PdfContentByte[] canvases) {
            com.lowagie.text.pdf.PdfContentByte canvas =
                    canvases[PdfPTable.BACKGROUNDCANVAS];
            canvas.saveState();
            canvas.setColorStroke(border);
            canvas.setLineWidth(0.8f);
            canvas.setLineDash(dashed ? 2f : 0f, dashed ? 2f : 0f);
            if (background == null) {
                if (cornerBackground != null) {
                    float left = position.getLeft();
                    float bottom = position.getBottom();
                    float right = position.getRight();
                    float top = position.getTop();
                    canvas.setColorFill(Color.WHITE);
                    canvas.rectangle(left, top - radius, radius, radius);
                    canvas.rectangle(right - radius, top - radius, radius, radius);
                    canvas.rectangle(left, bottom, radius, radius);
                    canvas.rectangle(right - radius, bottom, radius, radius);
                    canvas.fill();
                    canvas.setColorFill(cornerBackground);
                    canvas.circle(left + radius, top - radius, radius);
                    canvas.circle(right - radius, top - radius, radius);
                    canvas.circle(left + radius, bottom + radius, radius);
                    canvas.circle(right - radius, bottom + radius, radius);
                    canvas.fill();
                }
                canvas.roundRectangle(position.getLeft(), position.getBottom(),
                        position.getWidth(), position.getHeight(), radius);
                canvas.stroke();
            } else {
                canvas.setColorFill(background);
                canvas.roundRectangle(position.getLeft(), position.getBottom(),
                        position.getWidth(), position.getHeight(), radius);
                canvas.fillStroke();
            }
            canvas.restoreState();
        }
    }
}
