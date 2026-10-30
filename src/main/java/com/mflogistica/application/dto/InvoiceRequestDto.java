package com.mflogistica.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class InvoiceRequestDto {

    private Long invoiceId;

    @NotBlank(message = "El número de factura es obligatorio")
    @Size(max = 20, message = "El número de factura no puede superar 20 caracteres")
    private String invoiceNumber;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 120, message = "El nombre del cliente no puede superar 120 caracteres")
    private String clientName;

    @NotBlank(message = "El NIF/CIF del cliente es obligatorio")
    @Size(max = 20, message = "El NIF/CIF no puede superar 20 caracteres")
    private String clientNif;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String clientAddress;

    @NotEmpty(message = "Añade al menos una línea de factura")
    private List<@Valid InvoiceLineRequestDto> lines = new ArrayList<>();

    public InvoiceRequestDto() {
    }

    public InvoiceRequestDto(String invoiceNumber, LocalDate issueDate, String clientName, String clientNif,
                             String clientAddress, List<InvoiceLineRequestDto> lines) {
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.clientName = clientName;
        this.clientNif = clientNif;
        this.clientAddress = clientAddress;
        this.lines = new ArrayList<>(lines);
    }

    public String invoiceNumber() {
        return invoiceNumber;
    }

    public Long invoiceId() {
        return invoiceId;
    }

    public LocalDate issueDate() {
        return issueDate;
    }

    public String clientName() {
        return clientName;
    }

    public String clientNif() {
        return clientNif;
    }

    public String clientAddress() {
        return clientAddress;
    }

    public List<InvoiceLineRequestDto> lines() {
        return lines;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientNif(String clientNif) {
        this.clientNif = clientNif;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setLines(List<InvoiceLineRequestDto> lines) {
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
    }
}
