package com.mflogistica.application.dto;

import java.util.List;

public class InvoicePageDto {

    private final List<InvoiceResponseDto> content;
    private final long totalElements;
    private final int number;
    private final int size;

    public InvoicePageDto(List<InvoiceResponseDto> content, long totalElements, int number, int size) {
        this.content = List.copyOf(content);
        this.totalElements = totalElements;
        this.number = number;
        this.size = size;
    }

    public List<InvoiceResponseDto> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumber() {
        return number;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean hasPrevious() {
        return number > 0;
    }

    public boolean hasNext() {
        return number + 1 < getTotalPages();
    }
}
