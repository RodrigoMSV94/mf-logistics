package com.mflogistica.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Invoice(
        Long id,
        String invoiceNumber,
        String clientName,
        String clientNif,
        String clientAddress,
        LocalDate issueDate,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal irpfRate,
        BigDecimal totalRetentions,
        BigDecimal totalAmount,
        List<InvoiceLine> lines,
        FiscalData issuerSnapshot
) {
}
