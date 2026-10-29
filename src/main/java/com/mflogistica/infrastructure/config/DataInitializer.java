package com.mflogistica.infrastructure.config;

import com.mflogistica.application.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;

    @Value("${app.security.admin.email:admin@mflogistica.es}")
    private String adminEmail;

    @Value("${app.security.admin.password:Admin1234!}")
    private String adminPassword;

    @Value("${app.security.admin.full-name:Administrador MF}")
    private String adminFullName;

    public DataInitializer(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        authService.ensureAdminUser(adminEmail, adminPassword, adminFullName);
    }
}

