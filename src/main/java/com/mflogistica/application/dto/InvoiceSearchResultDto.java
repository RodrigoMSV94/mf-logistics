package com.mflogistica.application.dto;

import com.mflogistica.domain.model.Invoice;

import java.util.List;

public record InvoiceSearchResultDto(List<Invoice> invoices, long totalElements) {
    public InvoiceSearchResultDto {
        invoices = List.copyOf(invoices);
    }
}
