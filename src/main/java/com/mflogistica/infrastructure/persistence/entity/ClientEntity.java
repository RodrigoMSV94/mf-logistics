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
@Table(name = "clients")
public class ClientEntity {

    @Id
    @Column(name = "client_nif", nullable = false, length = 20)
    private String nif;

    @Column(name = "client_name", nullable = false, length = 120)
    private String name;

    @Column(name = "client_address", length = 255)
    private String address;
}
