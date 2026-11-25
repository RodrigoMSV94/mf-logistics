package com.mflogistica.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummary(
        Long id,
        String invoiceNumber,
        String clientName,
        String clientNif,
        LocalDate issueDate,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal irpfRate,
        BigDecimal totalRetentions,
        BigDecimal totalAmount,
        String concept
) {
}
