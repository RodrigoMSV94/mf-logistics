package com.mflogistica.web.controller;

import com.mflogistica.application.dto.InvoicePdfDto;
import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.service.InvoiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);
    private final InvoiceService invoiceService;

    public DashboardController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public String dashboard(Model model) {
        if (!model.containsAttribute("invoiceRequest")) {
            model.addAttribute("invoiceRequest", emptyRequest());
        }
        model.addAttribute("recentInvoices", invoiceService.findRecentInvoices(5));
        return "dashboard";
    }

    @PostMapping("/invoices")
    public Object createInvoice(@Valid @ModelAttribute("invoiceRequest") InvoiceRequestDto invoiceRequest,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            List<String> invalidFields = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getField)
                    .distinct()
                    .toList();
            LOGGER.warn("No se pudo generar la factura; campos no válidos: {}", invalidFields);
            model.addAttribute("validationErrorMessage",
                    "No se pudo generar el PDF. Corrige los campos marcados e inténtalo de nuevo.");
            model.addAttribute("recentInvoices", invoiceService.findRecentInvoices(5));
            return "dashboard";
        }

        InvoicePdfDto pdf = invoiceService.createInvoicePdf(invoiceRequest);
        return pdfResponse(pdf.invoiceNumber(), pdf.content());
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long id) {
        InvoiceResponseDto invoice = invoiceService.findById(id);
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);

        return pdfResponse(invoice.invoiceNumber(), pdfBytes);
    }

    private ResponseEntity<byte[]> pdfResponse(String invoiceNumber, byte[] pdfBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(invoiceNumber + ".pdf", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private InvoiceRequestDto emptyRequest() {
        LocalDate today = LocalDate.now();
        return new InvoiceRequestDto(
                invoiceService.suggestInvoiceNumber(today),
                today,
                "SAÚL GUERRERO LÓPEZ",
                "73025558Y",
                "Contrato Logístico Operativo\nCarrer Comanegra, 3\n17252 Calonge (Girona, España)",
                List.of(new com.mflogistica.application.dto.InvoiceLineRequestDto("Servicio de reparto", BigDecimal.ONE, BigDecimal.ZERO))
        );
    }
}
