package com.mflogistica.application.service;

import com.mflogistica.application.port.FiscalDataRepositoryPort;
import com.mflogistica.domain.exception.BusinessException;
import com.mflogistica.domain.model.FiscalData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalDataService {

    private static final FiscalData DEFAULT_FISCAL_DATA = new FiscalData(
            "MARTHA DENISSE CARRILLO TOVAR",
            "Y8023499D",
            "Carrer del Pintor Huguet, 1\n17310 Lloret de Mar (Girona, España)",
            "+34 664 008 020",
            "denissecarrillo1986@gmail.com",
            "ES50 2100 8137 8902 0047 3817"
    );

    private final FiscalDataRepositoryPort repository;

    public FiscalDataService(FiscalDataRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public FiscalData getCurrent() {
        return repository.findCurrent()
                .orElseThrow(() -> new BusinessException("No se encontraron los datos fiscales del emisor"));
    }

    @Transactional
    public FiscalData save(FiscalData fiscalData) {
        return repository.save(fiscalData);
    }

    @Transactional
    public void ensureDefaults() {
        if (repository.findCurrent().isEmpty()) {
            repository.save(DEFAULT_FISCAL_DATA);
        }
    }
}
