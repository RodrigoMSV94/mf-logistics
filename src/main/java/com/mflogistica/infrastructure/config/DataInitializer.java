package com.mflogistica.infrastructure.config;

import com.mflogistica.application.service.AuthService;
import com.mflogistica.application.service.FiscalDataService;
import com.mflogistica.application.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);
    private static final int MIN_ADMIN_PASSWORD_LENGTH = 16;
    private final AuthService authService;
    private final InvoiceService invoiceService;
    private final FiscalDataService fiscalDataService;
    private final String datasourceUrl;
    private final boolean localDataResetEnabled;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminFullName;

    public DataInitializer(AuthService authService, InvoiceService invoiceService,
                           FiscalDataService fiscalDataService,
                           @Value("${spring.datasource.url}") String datasourceUrl,
                           @Value("${app.local-data-reset.enabled:false}") boolean localDataResetEnabled,
                           @Value("${app.security.admin.username}") String adminUsername,
                           @Value("${app.security.admin.password}") String adminPassword,
                           @Value("${app.security.admin.full-name}") String adminFullName) {
        this.authService = authService;
        this.invoiceService = invoiceService;
        this.fiscalDataService = fiscalDataService;
        this.datasourceUrl = datasourceUrl;
        this.localDataResetEnabled = localDataResetEnabled;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminFullName = adminFullName;
    }

    @Override
    public void run(String... args) {
        validateAdminCredentials();
        authService.ensureAdminUser(adminUsername, adminPassword, adminFullName);
        fiscalDataService.ensureDefaults();
        if (localDataResetEnabled && isLoopbackPostgres(datasourceUrl)) {
            LOGGER.warn("Base de datos PostgreSQL local detectada: se reemplazarán las facturas por los datos de ejemplo.");
            invoiceService.resetLocalDevelopmentInvoices();
        }
    }

    private void validateAdminCredentials() {
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_USERNAME debe configurarse para inicializar el administrador");
        }
        if (adminPassword == null || adminPassword.length() < MIN_ADMIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD debe tener al menos 16 caracteres");
        }
        if (adminFullName == null || adminFullName.isBlank()) {
            throw new IllegalStateException("APP_ADMIN_FULL_NAME debe configurarse para inicializar el administrador");
        }
    }

    private boolean isLoopbackPostgres(String url) {
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            return false;
        }
        String host = URI.create(url.substring("jdbc:".length())).getHost();
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || "[::1]".equals(host);
    }
}
