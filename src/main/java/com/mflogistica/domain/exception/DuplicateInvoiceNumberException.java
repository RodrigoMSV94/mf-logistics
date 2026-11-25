package com.mflogistica.domain.exception;

public class DuplicateInvoiceNumberException extends BusinessException {

    public DuplicateInvoiceNumberException(String message) {
        super(message);
    }
}
