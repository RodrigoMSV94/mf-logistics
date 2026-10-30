package com.mflogistica.application.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.DataBinder;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceRequestDtoBindingTest {

    @Test
    void shouldBindIndexedInvoiceLinesIntoMutableRequest() {
        InvoiceRequestDto request = new InvoiceRequestDto();
        DataBinder binder = new DataBinder(request);
        binder.setConversionService(new DefaultFormattingConversionService());
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("invoiceNumber", "FAC-2026-0001");
        values.add("issueDate", LocalDate.of(2026, 10, 30));
        values.add("clientName", "Cliente Demo");
        values.add("clientNif", "B12345678");
        values.add("clientAddress", "Calle Mayor 1");
        values.add("lines[0].description", "Servicio de reparto");
        values.add("lines[0].quantity", new BigDecimal("1"));
        values.add("lines[0].unitPrice", new BigDecimal("100"));

        binder.bind(values);

        assertEquals("FAC-2026-0001", request.invoiceNumber());
        assertEquals("Servicio de reparto", request.lines().getFirst().description());
        assertEquals(new BigDecimal("100"), request.lines().getFirst().unitPrice());
    }
}
