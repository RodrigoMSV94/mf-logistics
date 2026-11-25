package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.application.port.FiscalDataRepositoryPort;
import com.mflogistica.domain.model.FiscalData;
import com.mflogistica.infrastructure.persistence.entity.FiscalDataEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FiscalDataRepositoryAdapter implements FiscalDataRepositoryPort {

    private static final long CURRENT_DATA_ID = 1L;
    private final SpringDataFiscalDataRepository repository;

    public FiscalDataRepositoryAdapter(SpringDataFiscalDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FiscalData> findCurrent() {
        return repository.findById(CURRENT_DATA_ID).map(this::toDomain);
    }

    @Override
    public FiscalData save(FiscalData fiscalData) {
        FiscalDataEntity entity = repository.findById(CURRENT_DATA_ID).orElseGet(FiscalDataEntity::new);
        entity.setId(CURRENT_DATA_ID);
        entity.setIssuerName(fiscalData.issuerName());
        entity.setNif(fiscalData.nif());
        entity.setAddress(fiscalData.address());
        entity.setPhone(fiscalData.phone());
        entity.setEmail(fiscalData.email());
        entity.setIban(fiscalData.iban());
        return toDomain(repository.save(entity));
    }

    private FiscalData toDomain(FiscalDataEntity entity) {
        return new FiscalData(entity.getIssuerName(), entity.getNif(), entity.getAddress(),
                entity.getPhone(), entity.getEmail(), entity.getIban());
    }
}
