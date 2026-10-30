package com.mflogistica.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String invoiceNumber;

    @Column(nullable = false, length = 120)
    private String clientName;

    @Column(nullable = false, length = 20)
    private String clientNif;

    @Column(length = 255)
    private String clientAddress;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal irpfRate;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalRetentions;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 255)
    private String concept;

    @Column(nullable = false, length = 120)
    private String issuerName;

    @Column(nullable = false, length = 20)
    private String issuerNif;

    @Column(nullable = false, length = 255)
    private String issuerAddress;

    @Column(length = 40)
    private String issuerPhone;

    @Column(length = 150)
    private String issuerEmail;

    @Column(nullable = false, length = 34)
    private String issuerIban;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineEntity> lines = new ArrayList<>();

    public void setLines(List<InvoiceLineEntity> lines) {
        this.lines.clear();
        lines.forEach(this::addLine);
    }

    public void addLine(InvoiceLineEntity line) {
        line.setInvoice(this);
        this.lines.add(line);
    }
}
