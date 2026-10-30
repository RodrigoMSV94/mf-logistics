package com.mflogistica.infrastructure.persistence.mapper;

import com.mflogistica.domain.model.FiscalData;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceMapperTest {

    @Test
    void shouldPersistAndRestoreFiscalSnapshotWithInvoice() {
        FiscalData issuer = new FiscalData("Emisor Histórico", "B12345678", "Calle Antigua 1",
                "600000001", "historico@empresa.es", "ES1234567890123456789012");
        Invoice invoice = new Invoice(1L, "FAC-2026-0010", "Cliente", "B87654321", "Calle Cliente",
                LocalDate.of(2026, 10, 30), new BigDecimal("100.00"), new BigDecimal("21.00"),
                new BigDecimal("21.00"), new BigDecimal("1.00"), new BigDecimal("-1.00"),
                new BigDecimal("120.00"),
                List.of(new InvoiceLine(1L, "Servicio", BigDecimal.ONE, new BigDecimal("100.00"),
                        new BigDecimal("100.00"))),
                issuer);
        InvoiceMapper mapper = new InvoiceMapper();

        InvoiceEntity entity = mapper.toEntity(invoice);
        Invoice restored = mapper.toDomain(entity);

        assertEquals(issuer, restored.issuerSnapshot());
        assertEquals("Emisor Histórico", entity.getIssuerName());
        assertEquals("ES1234567890123456789012", entity.getIssuerIban());
    }
}
