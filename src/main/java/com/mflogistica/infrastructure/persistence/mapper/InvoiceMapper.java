package com.mflogistica.infrastructure.persistence.mapper;

import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import com.mflogistica.domain.model.FiscalData;
import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import com.mflogistica.infrastructure.persistence.entity.InvoiceLineEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class InvoiceMapper {

    public Invoice toDomain(InvoiceEntity entity) {
        List<InvoiceLine> lines = entity.getLines().stream()
                .map(line -> new InvoiceLine(line.getId(), line.getDescription(), line.getQuantity(),
                        line.getUnitPrice(), line.getTotalAmount()))
                .toList();
        return new Invoice(entity.getId(), entity.getInvoiceNumber(), entity.getClientName(), entity.getClientNif(),
                entity.getClientAddress(), entity.getIssueDate(), entity.getSubtotal(), entity.getTaxRate(),
                entity.getTaxAmount(), valueOrZero(entity.getIrpfRate()), valueOrZero(entity.getTotalRetentions()),
                entity.getTotalAmount(), lines, new FiscalData(entity.getIssuerName(), entity.getIssuerNif(),
                entity.getIssuerAddress(), entity.getIssuerPhone(), entity.getIssuerEmail(), entity.getIssuerIban()));
    }

    public InvoiceEntity toEntity(Invoice domain) {
        return updateEntity(domain, new InvoiceEntity());
    }

    public InvoiceEntity updateEntity(Invoice domain, InvoiceEntity entity) {
        entity.setId(domain.id());
        entity.setInvoiceNumber(domain.invoiceNumber());
        entity.setClientName(domain.clientName());
        entity.setClientNif(domain.clientNif());
        entity.setClientAddress(domain.clientAddress());
        entity.setIssueDate(domain.issueDate());
        entity.setSubtotal(domain.subtotal());
        entity.setTaxRate(domain.taxRate());
        entity.setTaxAmount(domain.taxAmount());
        entity.setIrpfRate(domain.irpfRate());
        entity.setTotalRetentions(domain.totalRetentions());
        entity.setTotalAmount(domain.totalAmount());
        entity.setIssuerName(domain.issuerSnapshot().issuerName());
        entity.setIssuerNif(domain.issuerSnapshot().nif());
        entity.setIssuerAddress(domain.issuerSnapshot().address());
        entity.setIssuerPhone(domain.issuerSnapshot().phone());
        entity.setIssuerEmail(domain.issuerSnapshot().email());
        entity.setIssuerIban(domain.issuerSnapshot().iban());
        entity.setConcept(domain.lines().isEmpty() ? "Factura sin líneas" : domain.lines().getFirst().description());
        entity.getLines().clear();
        domain.lines().forEach(line -> entity.addLine(toEntity(line)));
        return entity;
    }

    private InvoiceLineEntity toEntity(InvoiceLine line) {
        InvoiceLineEntity entity = new InvoiceLineEntity();
        entity.setId(line.id());
        entity.setDescription(line.description());
        entity.setQuantity(line.quantity());
        entity.setUnitPrice(line.unitPrice());
        entity.setTotalAmount(line.totalAmount());
        return entity;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
