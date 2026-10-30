package com.mflogistica.application.port;

import com.mflogistica.application.dto.InvoiceSearchResultDto;
import com.mflogistica.domain.model.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    void deleteAll();

    Optional<Invoice> findById(Long id);

    List<Invoice> findRecent(int limit);

    Optional<String> findLatestInvoiceNumber(String yearPrefix);

    boolean existsByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);

    InvoiceSearchResultDto search(String term, LocalDate from, LocalDate to, int page, int size);

}
