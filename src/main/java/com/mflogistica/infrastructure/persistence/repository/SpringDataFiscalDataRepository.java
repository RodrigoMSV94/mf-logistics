package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.infrastructure.persistence.entity.FiscalDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFiscalDataRepository extends JpaRepository<FiscalDataEntity, Long> {
}
