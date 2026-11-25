package com.mflogistica.web.controller;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.application.service.AuthService;
import com.mflogistica.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class LoginTemplateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @MockBean
    private AuthService authService;

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

    @Test
    void shouldSetSecurityHeadersOnPublicPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"));
    }

    @Test
    void shouldRequireAuthenticationForPasswordSettings() throws Exception {
        mockMvc.perform(get("/dashboard/seguridad"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "mfadmin", roles = "ADMIN")
    void shouldRenderPasswordSettingsForAdministrator() throws Exception {
        mockMvc.perform(get("/dashboard/seguridad"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cambiar contraseña")));
    }

    @Test
    @WithMockUser(username = "mfadmin", roles = "ADMIN")
    void shouldRejectPasswordChangeWhenConfirmationDoesNotMatch() throws Exception {
        mockMvc.perform(post("/dashboard/seguridad")
                        .with(csrf())
                        .param("currentPassword", "old-password")
                        .param("newPassword", "New-Strong-Password-21!")
                        .param("confirmPassword", "different-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("no coinciden")));

        verify(authService, never()).changePassword("mfadmin", "old-password", "New-Strong-Password-21!");
    }

    @Test
    @WithMockUser(username = "mfadmin", roles = "ADMIN")
    void shouldEndSessionAfterSuccessfulPasswordChange() throws Exception {
        when(authService.changePassword("mfadmin", "old-password", "New-Strong-Password-21!"))
                .thenReturn(true);

        mockMvc.perform(post("/dashboard/seguridad")
                        .with(csrf())
                        .param("currentPassword", "old-password")
                        .param("newPassword", "New-Strong-Password-21!")
                        .param("confirmPassword", "New-Strong-Password-21!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?passwordChanged"));

        verify(authService).changePassword("mfadmin", "old-password", "New-Strong-Password-21!");
    }

    @Test
    void shouldRejectPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/login").param("username", "mfadmin").param("password", "password"))
                .andExpect(status().isForbidden());
    }
}
