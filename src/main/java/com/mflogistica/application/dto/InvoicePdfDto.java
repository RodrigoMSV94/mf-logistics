package com.mflogistica.application.dto;

public record InvoicePdfDto(String invoiceNumber, byte[] content) {
}
