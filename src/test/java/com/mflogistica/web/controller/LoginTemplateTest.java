package com.mflogistica.web.controller;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class LoginTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @Test
    void shouldAskForUsernameRatherThanEmail() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nombre de usuario")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "id=\"username\" name=\"username\" type=\"text\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("placeholder=\"mfadmin\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("type=\"email\""))));
    }
}
