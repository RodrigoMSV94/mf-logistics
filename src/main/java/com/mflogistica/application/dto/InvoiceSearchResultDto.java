package com.mflogistica.application.dto;

import com.mflogistica.domain.model.InvoiceSummary;

import java.util.List;

public record InvoiceSearchResultDto(List<InvoiceSummary> invoices, long totalElements) {
    public InvoiceSearchResultDto {
        invoices = List.copyOf(invoices);
    }
}
