package com.mflogistica.application.port;

import com.mflogistica.domain.model.Invoice;

public interface PdfGeneratorPort {

    byte[] generateInvoicePdf(Invoice invoice);
}

