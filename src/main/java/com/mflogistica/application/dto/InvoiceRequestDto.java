package com.mflogistica.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InvoiceRequestDto(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 120, message = "El nombre del cliente no puede superar 120 caracteres")
        String clientName,

        @NotBlank(message = "El NIF/CIF del cliente es obligatorio")
        @Size(max = 20, message = "El NIF/CIF no puede superar 20 caracteres")
        String clientNif,

        @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
        String clientAddress,

        @NotBlank(message = "El concepto es obligatorio")
        @Size(max = 255, message = "El concepto no puede superar 255 caracteres")
        String concept,

        @NotNull(message = "La base imponible es obligatoria")
        @DecimalMin(value = "0.01", message = "La base imponible debe ser mayor que 0")
        @Digits(integer = 12, fraction = 2, message = "La base imponible debe tener maximo 2 decimales")
        BigDecimal subtotal,

        @NotNull(message = "El tipo de IVA es obligatorio")
        @DecimalMin(value = "0.00", message = "El IVA no puede ser negativo")
        @Digits(integer = 3, fraction = 2, message = "El IVA no tiene formato valido")
        BigDecimal taxRate
) {
}

