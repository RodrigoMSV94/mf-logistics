package com.mflogistica.web.controller;

import com.mflogistica.application.service.FiscalDataService;
import com.mflogistica.domain.model.FiscalData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FiscalDataController.class)
class FiscalDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FiscalDataService fiscalDataService;

    @Test
    void shouldRenderFiscalSettingsWithHistoricalSnapshotNoticeAndSharedMenu() throws Exception {
        when(fiscalDataService.getCurrent()).thenReturn(defaultFiscalData());

        mockMvc.perform(get("/dashboard/datos-fiscales").with(user("mfadmin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Datos fiscales del emisor")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Los cambios no modifican las facturas emitidas anteriormente.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"sidebarToggle\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("logout")));
    }

    @Test
    void shouldPersistValidFiscalDataAndRedirect() throws Exception {
        mockMvc.perform(post("/dashboard/datos-fiscales")
                        .with(user("mfadmin").roles("ADMIN"))
                        .with(csrf())
                        .param("issuerName", "Empresa Nueva")
                        .param("nif", "B12345678")
                        .param("address", "Calle Nueva 1")
                        .param("phone", "600000000")
                        .param("email", "admin@empresa.es")
                        .param("iban", "ES1234567890123456789012"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/datos-fiscales?guardado=true"));

        verify(fiscalDataService).save(any(FiscalData.class));
    }

    private FiscalData defaultFiscalData() {
        return new FiscalData("MARTHA DENISSE CARRILLO TOVAR", "Y8023499D",
                "Carrer del Pintor Huguet, 1", "+34 664 008 020",
                "denissecarrillo1986@gmail.com", "ES50 2100 8137 8902 0047 3817");
    }
}
