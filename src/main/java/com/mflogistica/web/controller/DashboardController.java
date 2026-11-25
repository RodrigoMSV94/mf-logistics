package com.mflogistica.web.controller;

import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.service.ClientService;
import com.mflogistica.application.service.InvoiceService;
import com.mflogistica.domain.exception.DuplicateInvoiceNumberException;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);
    private final InvoiceService invoiceService;
    private final ClientService clientService;

    public DashboardController(InvoiceService invoiceService, ClientService clientService) {
        this.invoiceService = invoiceService;
        this.clientService = clientService;
    }

    @GetMapping
    public String dashboard(Model model) {
        if (!model.containsAttribute("invoiceRequest")) {
            model.addAttribute("invoiceRequest", emptyRequest());
        }
        model.addAttribute("recentInvoices", invoiceService.findRecentInvoices(5));
        model.addAttribute("frequentClients", clientService.findAll());
        return "dashboard";
    }

    @GetMapping("/facturas")
    public String invoices(@RequestParam(required = false) String q,
                           @RequestParam(required = false) LocalDate from,
                           @RequestParam(required = false) LocalDate to,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) Long downloadInvoiceId,
                           Model model) {
        int safePage = Math.max(page, 0);
        var invoices = invoiceService.searchInvoices(q, from, to, safePage, 20);
        model.addAttribute("invoices", invoices);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("downloadInvoiceId", downloadInvoiceId);
        return "facturas";
    }

    @GetMapping("/facturas/{id}/editar")
    public String editInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("invoiceRequest", invoiceService.findForEdit(id));
        model.addAttribute("recentInvoices", invoiceService.findRecentInvoices(5));
        model.addAttribute("frequentClients", clientService.findAll());
        model.addAttribute("successMessage", "Editando factura. Al guardar se descargará el PDF actualizado.");
        return "dashboard";
    }

    @PostMapping("/invoices")
    public Object saveInvoice(@Valid @ModelAttribute("invoiceRequest") InvoiceRequestDto invoiceRequest,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            List<String> invalidFields = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getField)
                    .distinct()
                    .toList();
            LOGGER.warn("No se pudo guardar la factura; campos no válidos: {}", invalidFields);
            model.addAttribute("validationErrorMessage",
                    "No se pudo guardar la factura. Corrige los campos marcados e inténtalo de nuevo.");
            addFormSupportData(model);
            return "dashboard";
        }

        try {
            InvoiceResponseDto savedInvoice = invoiceService.saveInvoice(invoiceRequest);
            return "redirect:/dashboard/facturas?downloadInvoiceId=" + savedInvoice.id();
        } catch (DuplicateInvoiceNumberException exception) {
            bindingResult.rejectValue("invoiceNumber", "duplicate", exception.getMessage());
            model.addAttribute("duplicateInvoiceMessage", exception.getMessage());
            addFormSupportData(model);
            return "dashboard";
        }
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
        var defaultClient = clientService.findDefaultClient();
        return new InvoiceRequestDto(
                invoiceService.suggestInvoiceNumber(today),
                today,
                defaultClient.name(),
                defaultClient.nif(),
                defaultClient.address(),
                List.of(new com.mflogistica.application.dto.InvoiceLineRequestDto("Servicio de reparto", BigDecimal.ONE, BigDecimal.ZERO))
        );
    }

    private void addFormSupportData(Model model) {
        model.addAttribute("recentInvoices", invoiceService.findRecentInvoices(5));
        model.addAttribute("frequentClients", clientService.findAll());
    }
}
