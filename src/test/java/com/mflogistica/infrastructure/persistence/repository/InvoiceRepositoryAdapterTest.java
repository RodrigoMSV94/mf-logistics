package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.infrastructure.persistence.entity.InvoiceEntity;
import com.mflogistica.infrastructure.persistence.mapper.InvoiceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceRepositoryAdapterTest {

    @Mock
    private SpringDataInvoiceRepository springDataInvoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceRepositoryAdapter invoiceRepositoryAdapter;

    @Test
    void shouldFindLatestInvoiceNumberByYearPrefix() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setInvoiceNumber("FAC-2026-0007");
        when(springDataInvoiceRepository.findFirstByInvoiceNumberStartingWithOrderByIdDesc("FAC-2026-"))
                .thenReturn(java.util.Optional.of(invoice));

        assertEquals(java.util.Optional.of("FAC-2026-0007"),
                invoiceRepositoryAdapter.findLatestInvoiceNumber("FAC-2026-"));
    }
}
