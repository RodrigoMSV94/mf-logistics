package com.mflogistica.domain.model;

import java.math.BigDecimal;

public record InvoiceLine(
        Long id,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
