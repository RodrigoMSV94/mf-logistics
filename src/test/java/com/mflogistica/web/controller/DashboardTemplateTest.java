package com.mflogistica.web.controller;

import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.dto.InvoicePageDto;
import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceLineRequestDto;
import com.mflogistica.application.service.ClientService;
import com.mflogistica.application.service.InvoiceService;
import com.mflogistica.domain.model.Client;
import com.mflogistica.domain.exception.DuplicateInvoiceNumberException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(DashboardController.class)
class DashboardTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private ClientService clientService;

    @Test
    void shouldRenderDashboardAndFormatEuroAmounts() throws Exception {
        InvoiceResponseDto invoice = sampleInvoice();
        when(invoiceService.suggestInvoiceNumber(LocalDate.now()))
                .thenReturn("FAC-" + LocalDate.now().getYear() + "-0001");
        when(invoiceService.findRecentInvoices(anyInt())).thenReturn(List.of(invoice));
        when(clientService.findAll()).thenReturn(List.of(
                new Client("SAÚL GUERRERO LÓPEZ", "73025558Y", "Carrer Comanegra, 3")
        ));
        when(clientService.findDefaultClient()).thenReturn(
                new Client("SAÚL GUERRERO LÓPEZ", "73025558Y", "Carrer Comanegra, 3")
        );

        mockMvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1.481,47 €")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"sidebarToggle\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("account_balance")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Datos fiscales")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(">logout</span>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("text-red-600")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Facturas emitidas")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SAÚL GUERRERO LÓPEZ")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "src=\"/images/mf-logo-transparent.png\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "mf-dashboard-sidebar-expanded")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "id=\"invoiceNumber\" name=\"invoiceNumber\" value=\"FAC-"
                                + LocalDate.now().getYear() + "-0001\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("clientName.value = '';")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("clientNif.value = '';")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("clientAddress.value = '';")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"" + LocalDate.now() + "\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("lines0.description")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("IVA (21,00%)")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("IRPF (1,00%)")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "format((baseCents + vatCents) / 100)")));
    }

    @Test
    void shouldRenderPersistedIssueDateWhenEditingInvoice() throws Exception {
        LocalDate persistedDate = LocalDate.of(2025, 6, 14);
        InvoiceRequestDto request = new InvoiceRequestDto(
                "FAC-2025-0003", persistedDate, "Cliente Demo", "B12345678", "Calle Mayor 1",
                List.of(new InvoiceLineRequestDto("Servicio", BigDecimal.ONE, BigDecimal.TEN))
        );
        request.setInvoiceId(3L);
        when(invoiceService.findForEdit(3L)).thenReturn(request);
        when(invoiceService.findRecentInvoices(anyInt())).thenReturn(List.of());
        when(clientService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/dashboard/facturas/3/editar").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "id=\"issueDate\" name=\"issueDate\" value=\"2025-06-14\"")));
    }

    @Test
    void shouldRenderInvoiceListAndFormatEuroAmounts() throws Exception {
        when(invoiceService.searchInvoices(null, null, null, 0, 20))
                .thenReturn(new InvoicePageDto(List.of(sampleInvoice()), 1, 0, 20));

        mockMvc.perform(get("/dashboard/facturas").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1.481,47 €")));
    }

    @Test
    void shouldShowInvoicePanelAndAutomaticallyDownloadNewInvoicePdf() throws Exception {
        when(invoiceService.searchInvoices(null, null, null, 0, 20))
                .thenReturn(new InvoicePageDto(List.of(sampleInvoice()), 1, 0, 20));

        mockMvc.perform(get("/dashboard/facturas")
                        .param("downloadInvoiceId", "1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Facturas emitidas")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"sidebarToggle\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Datos fiscales")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(">logout</span>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "mf-dashboard-sidebar-expanded")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nueva factura")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "/dashboard/invoices/1/pdf")));
    }

    @Test
    void shouldKeepInvoiceFormAndShowDuplicateNumberPopup() throws Exception {
        when(invoiceService.saveInvoice(any()))
                .thenThrow(new DuplicateInvoiceNumberException("Ya existe una factura con el número FAC-2026-0002"));
        when(invoiceService.findRecentInvoices(anyInt())).thenReturn(List.of());
        when(clientService.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/dashboard/invoices")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("invoiceNumber", "FAC-2026-0002")
                        .param("issueDate", "2026-10-30")
                        .param("clientName", "Cliente Prueba")
                        .param("clientNif", "B12345678")
                        .param("clientAddress", "Calle de prueba")
                        .param("lines[0].description", "Servicio conservado")
                        .param("lines[0].quantity", "1")
                        .param("lines[0].unitPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "value=\"FAC-2026-0002\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "border-red-500 ring-2 ring-red-100")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Servicio conservado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Ya existe una factura con el número FAC-2026-0002")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "id=\"duplicateInvoiceDialog\"")));
        verify(invoiceService).saveInvoice(any());
    }

    @Test
    void shouldRedirectToInvoicePanelAfterSavingInvoice() throws Exception {
        when(invoiceService.saveInvoice(any())).thenReturn(sampleInvoice());

        mockMvc.perform(post("/dashboard/invoices")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("invoiceNumber", "FAC-2026-0001")
                        .param("issueDate", "2026-10-30")
                        .param("clientName", "Cliente Prueba")
                        .param("clientNif", "B12345678")
                        .param("clientAddress", "Calle de prueba")
                        .param("lines[0].description", "Servicio")
                        .param("lines[0].quantity", "1")
                        .param("lines[0].unitPrice", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/dashboard/facturas?downloadInvoiceId=1"));
    }

    private InvoiceResponseDto sampleInvoice() {
        return new InvoiceResponseDto(
                1L, "FAC-2026-0001", "Cliente Demo", "B12345678", LocalDate.now(),
                new BigDecimal("1234.56"), new BigDecimal("21.00"), new BigDecimal("259.26"),
                new BigDecimal("1.00"), new BigDecimal("-12.35"), new BigDecimal("1481.47"), "Servicio"
        );
    }
}
