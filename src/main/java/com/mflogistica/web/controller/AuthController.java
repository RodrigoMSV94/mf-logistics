package com.mflogistica.web.controller;

import com.mflogistica.application.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard/seguridad")
    public String securitySettings() {
        return "security-settings";
    }

    @PostMapping("/dashboard/seguridad")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "La nueva contraseña y su confirmación no coinciden.");
            return "security-settings";
        }

        if (!authService.changePassword(authentication.getName(), currentPassword, newPassword)) {
            model.addAttribute("error",
                    "No se pudo cambiar la contraseña. Comprueba la contraseña actual y que la nueva tenga al menos 16 caracteres.");
            return "security-settings";
        }

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login?passwordChanged";
    }
}
