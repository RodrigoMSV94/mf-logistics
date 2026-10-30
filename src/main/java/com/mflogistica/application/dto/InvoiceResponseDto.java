package com.mflogistica.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponseDto(
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
