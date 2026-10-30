package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.dto.InvoiceSearchResultDto;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import com.mflogistica.infrastructure.persistence.mapper.InvoiceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
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
        InvoiceEntity entity = invoice.id() == null
                ? invoiceMapper.toEntity(invoice)
                : springDataInvoiceRepository.findById(invoice.id())
                        .map(existing -> invoiceMapper.updateEntity(invoice, existing))
                        .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura a actualizar"));
        InvoiceEntity saved = springDataInvoiceRepository.save(entity);
        return invoiceMapper.toDomain(saved);
    }

    @Override
    public void deleteAll() {
        springDataInvoiceRepository.deleteAll();
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
    public Optional<String> findLatestInvoiceNumber(String yearPrefix) {
        return springDataInvoiceRepository.findFirstByInvoiceNumberStartingWithOrderByIdDesc(yearPrefix)
                .map(InvoiceEntity::getInvoiceNumber);
    }

    @Override
    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return springDataInvoiceRepository.existsByInvoiceNumber(invoiceNumber);
    }

    @Override
    public boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id) {
        return springDataInvoiceRepository.existsByInvoiceNumberAndIdNot(invoiceNumber, id);
    }

    @Override
    public InvoiceSearchResultDto search(String term, LocalDate from, LocalDate to, int page, int size) {
        Page<InvoiceEntity> result = springDataInvoiceRepository.search(term, from, to, PageRequest.of(page, size));
        return new InvoiceSearchResultDto(result.getContent().stream().map(invoiceMapper::toDomain).toList(),
                result.getTotalElements());
    }

}
