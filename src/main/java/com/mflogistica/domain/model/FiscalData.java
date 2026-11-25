package com.mflogistica.domain.model;

public record FiscalData(
        String issuerName,
        String nif,
        String address,
        String phone,
        String email,
        String iban
) {
}
