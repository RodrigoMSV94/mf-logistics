package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import com.mflogistica.domain.model.InvoiceSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    @Query("""
            select new com.mflogistica.domain.model.InvoiceSummary(
                i.id, i.invoiceNumber, i.clientName, i.clientNif, i.issueDate, i.subtotal,
                i.taxRate, i.taxAmount, i.irpfRate, i.totalRetentions, i.totalAmount, i.concept)
            from InvoiceEntity i
            order by i.issueDate desc, i.id desc
            """)
    List<InvoiceSummary> findRecentSummaries(Pageable pageable);

    boolean existsByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);

    Optional<InvoiceEntity> findFirstByInvoiceNumberStartingWithOrderByIdDesc(String yearPrefix);

    @Query(value = """
            select new com.mflogistica.domain.model.InvoiceSummary(
                i.id, i.invoiceNumber, i.clientName, i.clientNif, i.issueDate, i.subtotal,
                i.taxRate, i.taxAmount, i.irpfRate, i.totalRetentions, i.totalAmount, i.concept)
            from InvoiceEntity i
            where (:term is null or :term = ''
                or lower(i.invoiceNumber) like lower(concat('%', :term, '%'))
                or lower(i.clientName) like lower(concat('%', :term, '%'))
                or lower(i.clientNif) like lower(concat('%', :term, '%')))
              and (:fromDate is null or i.issueDate >= :fromDate)
              and (:toDate is null or i.issueDate <= :toDate)
            order by i.issueDate desc, i.id desc
            """,
            countQuery = """
            select count(i) from InvoiceEntity i
            where (:term is null or :term = ''
                or lower(i.invoiceNumber) like lower(concat('%', :term, '%'))
                or lower(i.clientName) like lower(concat('%', :term, '%'))
                or lower(i.clientNif) like lower(concat('%', :term, '%')))
              and (:fromDate is null or i.issueDate >= :fromDate)
              and (:toDate is null or i.issueDate <= :toDate)
            """)
    Page<InvoiceSummary> search(@Param("term") String term,
                                @Param("fromDate") LocalDate fromDate,
                                @Param("toDate") LocalDate toDate,
                                Pageable pageable);

}
