package com.mflogistica.application.service;

import com.mflogistica.application.port.FiscalDataRepositoryPort;
import com.mflogistica.domain.model.FiscalData;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiscalDataServiceTest {

    @Test
    void shouldSaveAndReturnUpdatedFiscalData() {
        InMemoryFiscalDataRepository repository = new InMemoryFiscalDataRepository();
        FiscalDataService service = new FiscalDataService(repository);
        FiscalData updated = new FiscalData("Empresa Nueva", "B12345678", "Calle Nueva",
                "600000000", "admin@empresa.es", "ES123");

        service.save(updated);

        assertEquals(updated, service.getCurrent());
    }

    @Test
    void shouldInitializeDefaultFiscalDataOnlyWhenMissing() {
        InMemoryFiscalDataRepository repository = new InMemoryFiscalDataRepository();
        FiscalDataService service = new FiscalDataService(repository);

        service.ensureDefaults();

        assertEquals("MARTHA DENISSE CARRILLO TOVAR", service.getCurrent().issuerName());
        FiscalData before = service.getCurrent();
        service.ensureDefaults();
        assertEquals(before, service.getCurrent());
    }

    private static final class InMemoryFiscalDataRepository implements FiscalDataRepositoryPort {
        private FiscalData data;

        @Override
        public Optional<FiscalData> findCurrent() {
            return Optional.ofNullable(data);
        }

        @Override
        public FiscalData save(FiscalData fiscalData) {
            data = fiscalData;
            return fiscalData;
        }
    }
}
