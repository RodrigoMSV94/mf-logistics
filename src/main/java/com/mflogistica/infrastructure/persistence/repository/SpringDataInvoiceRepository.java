package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    @Query("""
            select count(i) from InvoiceEntity i
            where function('year', i.issueDate) = :year
            """)
    long countByIssueYear(@Param("year") int year);

    List<InvoiceEntity> findAllByOrderByIssueDateDescIdDesc(Pageable pageable);
}

