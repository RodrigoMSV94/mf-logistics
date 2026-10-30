package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    long countByIssueDateGreaterThanEqualAndIssueDateLessThan(LocalDate startDate, LocalDate endDate);

    List<InvoiceEntity> findAllByOrderByIssueDateDescIdDesc(Pageable pageable);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
