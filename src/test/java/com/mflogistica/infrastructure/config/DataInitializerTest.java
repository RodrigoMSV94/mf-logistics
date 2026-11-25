package com.mflogistica.infrastructure.config;

import com.mflogistica.application.service.AuthService;
import com.mflogistica.application.service.FiscalDataService;
import com.mflogistica.application.service.InvoiceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DataInitializerTest {

    private final AuthService authService = mock(AuthService.class);
    private final InvoiceService invoiceService = mock(InvoiceService.class);
    private final FiscalDataService fiscalDataService = mock(FiscalDataService.class);

    @Test
    void shouldRequireStrongAdminPasswordBeforeInitializing() {
        DataInitializer initializer = initializer("short", "jdbc:postgresql://db.example/mf", false);

        assertThrows(IllegalStateException.class, () -> initializer.run());

        verify(authService, never()).ensureAdminUser("mfadmin", "short", "Admin");
        verify(fiscalDataService, never()).ensureDefaults();
    }

    @Test
    void shouldInitializeAdminWhenCredentialsAreConfigured() {
        String password = "Strong-Admin-Password-21!";
        DataInitializer initializer = initializer(password, "jdbc:postgresql://db.example/mf", false);

        initializer.run();

        verify(authService).ensureAdminUser("mfadmin", password, "Admin");
        verify(fiscalDataService).ensureDefaults();
        verify(invoiceService, never()).resetLocalDevelopmentInvoices();
    }

    @Test
    void shouldNotResetInvoicesOnRemoteDatabaseEvenWhenEnabled() {
        DataInitializer initializer = initializer("Strong-Admin-Password-21!",
                "jdbc:postgresql://db.example/mf", true);

        initializer.run();

        verify(invoiceService, never()).resetLocalDevelopmentInvoices();
    }

    @Test
    void shouldResetInvoicesWhenEnabledForLoopbackDatabase() {
        DataInitializer initializer = initializer("Strong-Admin-Password-21!",
                "jdbc:postgresql://localhost:5432/mf", true);

        initializer.run();

        verify(invoiceService).resetLocalDevelopmentInvoices();
    }

    private DataInitializer initializer(String password, String datasourceUrl, boolean localDataResetEnabled) {
        return new DataInitializer(authService, invoiceService, fiscalDataService, datasourceUrl,
                localDataResetEnabled, "mfadmin", password, "Admin");
    }
}
