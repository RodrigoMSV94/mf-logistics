package com.mflogistica.application.dto;

import com.mflogistica.domain.model.FiscalData;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FiscalDataRequestDto(
        @NotBlank(message = "El nombre del emisor es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String issuerName,
        @NotBlank(message = "El NIF es obligatorio")
        @Size(max = 20, message = "El NIF no puede superar 20 caracteres")
        String nif,
        @NotBlank(message = "La dirección fiscal es obligatoria")
        @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
        String address,
        @Size(max = 40, message = "El teléfono no puede superar 40 caracteres")
        String phone,
        @Email(message = "Introduce un correo electrónico válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String email,
        @NotBlank(message = "El IBAN es obligatorio")
        @Size(max = 34, message = "El IBAN no puede superar 34 caracteres")
        String iban
) {
    public static FiscalDataRequestDto from(FiscalData fiscalData) {
        return new FiscalDataRequestDto(fiscalData.issuerName(), fiscalData.nif(), fiscalData.address(),
                fiscalData.phone(), fiscalData.email(), fiscalData.iban());
    }

    public FiscalData toDomain() {
        return new FiscalData(issuerName.trim(), nif.trim(), address.trim(),
                phone == null ? "" : phone.trim(), email == null ? "" : email.trim(), iban.trim());
    }
}
