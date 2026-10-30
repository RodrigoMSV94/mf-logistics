package com.mflogistica.application.port;

import com.mflogistica.domain.model.FiscalData;

import java.util.Optional;

public interface FiscalDataRepositoryPort {

    Optional<FiscalData> findCurrent();

    FiscalData save(FiscalData fiscalData);
}
