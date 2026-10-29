package com.mflogistica.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        BigDecimal totalAmount,
        String concept
) {
}

