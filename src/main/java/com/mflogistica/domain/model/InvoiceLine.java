package com.mflogistica.domain.model;

import java.math.BigDecimal;

public record InvoiceLine(
        Long id,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        boolean taxable
) {
    public InvoiceLine(Long id, String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalAmount) {
        this(id, description, quantity, unitPrice, totalAmount, true);
    }
}
