package com.mflogistica.infrastructure.persistence.mapper;

import com.mflogistica.domain.model.Invoice;
import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public Invoice toDomain(InvoiceEntity entity) {
        return new Invoice(
                entity.getId(),
                entity.getInvoiceNumber(),
                entity.getClientName(),
                entity.getClientNif(),
                entity.getClientAddress(),
                entity.getIssueDate(),
                entity.getSubtotal(),
                entity.getTaxRate(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getConcept()
        );
    }

    public InvoiceEntity toEntity(Invoice domain) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(domain.id());
        entity.setInvoiceNumber(domain.invoiceNumber());
        entity.setClientName(domain.clientName());
        entity.setClientNif(domain.clientNif());
        entity.setClientAddress(domain.clientAddress());
        entity.setIssueDate(domain.issueDate());
        entity.setSubtotal(domain.subtotal());
        entity.setTaxRate(domain.taxRate());
        entity.setTaxAmount(domain.taxAmount());
        entity.setTotalAmount(domain.totalAmount());
        entity.setConcept(domain.concept());
        return entity;
    }
}

