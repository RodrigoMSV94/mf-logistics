package com.mflogistica.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fiscal_data")
public class FiscalDataEntity {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 120)
    private String issuerName;

    @Column(nullable = false, length = 20)
    private String nif;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 40)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 34)
    private String iban;
}
