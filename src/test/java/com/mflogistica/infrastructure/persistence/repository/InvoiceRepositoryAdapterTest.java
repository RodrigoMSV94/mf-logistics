package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.infrastructure.persistence.mapper.InvoiceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
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
    void shouldCountInvoicesWithinYearUsingDateRange() {
        when(springDataInvoiceRepository.countByIssueDateGreaterThanEqualAndIssueDateLessThan(
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1))).thenReturn(3L);

        assertEquals(3L, invoiceRepositoryAdapter.countByYear(2024));

        verify(springDataInvoiceRepository).countByIssueDateGreaterThanEqualAndIssueDateLessThan(
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1));
    }
}
