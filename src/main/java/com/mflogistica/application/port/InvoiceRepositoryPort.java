package com.mflogistica.application.port;

import com.mflogistica.domain.model.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findById(Long id);

    List<Invoice> findRecent(int limit);

    long countByYear(int year);
}

