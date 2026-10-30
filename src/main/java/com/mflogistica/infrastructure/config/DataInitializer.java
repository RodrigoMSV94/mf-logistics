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
    private final AuthService authService;
    private final InvoiceService invoiceService;
    private final FiscalDataService fiscalDataService;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${app.local-data-reset.enabled:true}")
    private boolean localDataResetEnabled;

    @Value("${app.security.admin.username:mfadmin}")
    private String adminUsername;

    @Value("${app.security.admin.password:Admin1234!}")
    private String adminPassword;

    @Value("${app.security.admin.full-name:Administrador MF}")
    private String adminFullName;

    public DataInitializer(AuthService authService, InvoiceService invoiceService,
                           FiscalDataService fiscalDataService) {
        this.authService = authService;
        this.invoiceService = invoiceService;
        this.fiscalDataService = fiscalDataService;
    }

    @Override
    public void run(String... args) {
        authService.ensureAdminUser(adminUsername, adminPassword, adminFullName);
        fiscalDataService.ensureDefaults();
        if (localDataResetEnabled && isLoopbackPostgres(datasourceUrl)) {
            LOGGER.warn("Base de datos PostgreSQL local detectada: se reemplazarán las facturas por los datos de ejemplo.");
            invoiceService.resetLocalDevelopmentInvoices();
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
