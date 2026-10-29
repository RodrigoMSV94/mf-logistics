package com.mflogistica.web.controller;

import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

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
    public String createInvoice(@Valid @ModelAttribute("invoiceRequest") InvoiceRequestDto invoiceRequest,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("recentInvoices", invoiceService.findRecentInvoices(5));
            return "dashboard";
        }

        InvoiceResponseDto created = invoiceService.createInvoice(invoiceRequest);
        redirectAttributes.addFlashAttribute("successMessage",
                "Factura " + created.invoiceNumber() + " creada correctamente.");
        return "redirect:/dashboard";
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long id) {
        InvoiceResponseDto invoice = invoiceService.findById(id);
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(invoice.invoiceNumber() + ".pdf", StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private InvoiceRequestDto emptyRequest() {
        return new InvoiceRequestDto(
                "",
                "",
                "",
                "",
                BigDecimal.ONE,
                BigDecimal.valueOf(21)
        );
    }
}

