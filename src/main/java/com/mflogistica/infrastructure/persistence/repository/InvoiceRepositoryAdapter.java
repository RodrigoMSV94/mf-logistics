package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import com.mflogistica.infrastructure.persistence.mapper.InvoiceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final SpringDataInvoiceRepository springDataInvoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceRepositoryAdapter(SpringDataInvoiceRepository springDataInvoiceRepository, InvoiceMapper invoiceMapper) {
        this.springDataInvoiceRepository = springDataInvoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity saved = springDataInvoiceRepository.save(invoiceMapper.toEntity(invoice));
        return invoiceMapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return springDataInvoiceRepository.findById(id).map(invoiceMapper::toDomain);
    }

    @Override
    public List<Invoice> findRecent(int limit) {
        return springDataInvoiceRepository.findAllByOrderByIssueDateDescIdDesc(PageRequest.of(0, limit)).stream()
                .map(invoiceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByYear(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        return springDataInvoiceRepository.countByIssueDateGreaterThanEqualAndIssueDateLessThan(
                startDate, startDate.plusYears(1));
    }

    @Override
    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return springDataInvoiceRepository.existsByInvoiceNumber(invoiceNumber);
    }
}
