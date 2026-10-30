package com.mflogistica.application.service;

import com.mflogistica.application.dto.InvoiceRequestDto;
import com.mflogistica.application.dto.InvoiceLineRequestDto;
import com.mflogistica.application.dto.InvoiceResponseDto;
import com.mflogistica.application.port.InvoiceRepositoryPort;
import com.mflogistica.application.port.PdfGeneratorPort;
import com.mflogistica.application.port.ClientRepositoryPort;
import com.mflogistica.domain.model.Client;
import com.mflogistica.domain.model.Invoice;
import com.mflogistica.domain.model.InvoiceLine;
import com.mflogistica.domain.model.FiscalData;
import com.mflogistica.domain.exception.BusinessException;
import com.mflogistica.application.dto.InvoiceSearchResultDto;
import com.mflogistica.application.port.FiscalDataRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceServiceTest {

    @Test
    void shouldCalculateTaxAndTotal() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        PdfGeneratorPort pdfGeneratorPort = invoice -> "pdf".getBytes();
        InvoiceService service = service(repo, pdfGeneratorPort);

        InvoiceRequestDto request = new InvoiceRequestDto(
                "FAC-" + LocalDate.now().getYear() + "-0001",
                LocalDate.now(),
                "Cliente Demo",
                "B12345678",
                "Calle Mayor 1",
                List.of(new InvoiceLineRequestDto("Servicio urgente", BigDecimal.ONE, new BigDecimal("5467.15")))
        );

        InvoiceResponseDto response = service.createInvoice(request);

        assertEquals(new BigDecimal("21.00"), response.taxRate());
        assertEquals(new BigDecimal("1148.10"), response.taxAmount());
        assertEquals(new BigDecimal("1.00"), response.irpfRate());
        assertEquals(new BigDecimal("-54.67"), response.totalRetentions());
        assertEquals(new BigDecimal("6560.58"), response.totalAmount());
        assertEquals("FAC-" + LocalDate.now().getYear() + "-0001", response.invoiceNumber());
    }

    @Test
    void shouldCreateInvoiceAndGeneratePdfWithCalculatedTotals() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        AtomicReference<Invoice> invoiceInPdf = new AtomicReference<>();
        PdfGeneratorPort pdfGeneratorPort = invoice -> {
            invoiceInPdf.set(invoice);
            return "pdf".getBytes();
        };
        InvoiceService service = service(repo, pdfGeneratorPort);
        InvoiceRequestDto request = new InvoiceRequestDto(
                "FAC-" + LocalDate.now().getYear() + "-PDF",
                LocalDate.now(),
                "Cliente Demo",
                "B12345678",
                "Calle Mayor 1",
                List.of(new InvoiceLineRequestDto("Servicio de reparto", BigDecimal.ONE, new BigDecimal("100.00")))
        );

        var result = service.createInvoicePdf(request);

        assertEquals(request.invoiceNumber(), result.invoiceNumber());
        assertEquals("pdf", new String(result.content()));
        assertEquals(new BigDecimal("100.00"), invoiceInPdf.get().subtotal());
        assertEquals(new BigDecimal("21.00"), invoiceInPdf.get().taxAmount());
        assertEquals(new BigDecimal("-1.00"), invoiceInPdf.get().totalRetentions());
        assertEquals(new BigDecimal("120.00"), invoiceInPdf.get().totalAmount());
    }

    @Test
    void shouldUpdateInvoiceAndRecalculateTotals() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        InvoiceService service = service(repo, invoice -> "pdf".getBytes());
        InvoiceResponseDto created = service.createInvoice(request("FAC-UPDATE", "100.00"));
        InvoiceRequestDto update = request("FAC-UPDATE", "200.00");
        update.setInvoiceId(created.id());
        update.setClientName("Cliente actualizado");

        InvoiceResponseDto updated = service.saveInvoice(update);

        assertEquals(created.id(), updated.id());
        assertEquals("Cliente actualizado", updated.clientName());
        assertEquals(new BigDecimal("200.00"), updated.subtotal());
        assertEquals(new BigDecimal("240.00"), updated.totalAmount());
    }

    @Test
    void shouldPreserveFiscalSnapshotOnExistingInvoicesAndUseCurrentDataForNewInvoices() {
        InMemoryInvoiceRepository invoiceRepository = new InMemoryInvoiceRepository();
        InMemoryFiscalDataRepository fiscalDataRepository = new InMemoryFiscalDataRepository();
        testClientService = new ClientService(new InMemoryClientRepository());
        InvoiceService service = new InvoiceService(invoiceRepository, invoice -> "pdf".getBytes(),
                testClientService, new FiscalDataService(fiscalDataRepository));
        InvoiceResponseDto created = service.createInvoice(request("FAC-SNAPSHOT-1", "100.00"));
        FiscalData updatedIssuer = new FiscalData("Nueva Empresa", "B87654321", "Nueva dirección",
                "600000000", "nueva@empresa.es", "ES0000000000000000000000");
        fiscalDataRepository.save(updatedIssuer);
        InvoiceRequestDto update = request("FAC-SNAPSHOT-1", "200.00");
        update.setInvoiceId(created.id());
        service.saveInvoice(update);
        InvoiceResponseDto next = service.createInvoice(request("FAC-SNAPSHOT-2", "100.00"));

        assertEquals("MARTHA DENISSE CARRILLO TOVAR",
                invoiceRepository.findById(created.id()).orElseThrow().issuerSnapshot().issuerName());
        assertEquals(updatedIssuer,
                invoiceRepository.findById(next.id()).orElseThrow().issuerSnapshot());
    }

    @Test
    void shouldLoadPersistedIssueDateForEditing() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        InvoiceService service = service(repo, invoice -> "pdf".getBytes());
        InvoiceRequestDto request = request("FAC-EDIT-DATE", "100.00");
        request.setIssueDate(LocalDate.of(2025, 6, 14));
        InvoiceResponseDto created = service.createInvoice(request);

        InvoiceRequestDto editRequest = service.findForEdit(created.id());

        assertEquals(LocalDate.of(2025, 6, 14), editRequest.issueDate());
    }

    @Test
    void shouldSuggestNumberAfterLatestInvoiceNumberInsteadOfCountingInvoices() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        InvoiceService service = service(repo, invoice -> "pdf".getBytes());
        service.createInvoice(request("FAC-" + LocalDate.now().getYear() + "-0001", "100.00"));
        service.createInvoice(request("FAC-" + LocalDate.now().getYear() + "-0007", "100.00"));

        assertEquals("FAC-" + LocalDate.now().getYear() + "-0008",
                service.suggestInvoiceNumber(LocalDate.now()));
    }

    @Test
    void shouldRejectDuplicateInvoiceNumberWhenUpdating() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        InvoiceService service = service(repo, invoice -> "pdf".getBytes());
        InvoiceResponseDto first = service.createInvoice(request("FAC-DUP-1", "100.00"));
        service.createInvoice(request("FAC-DUP-2", "100.00"));
        BusinessException createException = assertThrows(BusinessException.class,
                () -> service.createInvoice(request("FAC-DUP-2", "125.00")));
        assertEquals("Ya existe una factura con el número FAC-DUP-2", createException.getMessage());
        InvoiceRequestDto update = request("FAC-DUP-2", "125.00");
        update.setInvoiceId(first.id());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.saveInvoice(update));

        assertEquals("Ya existe una factura con el número FAC-DUP-2", exception.getMessage());
    }

    @Test
    void shouldReplaceLocalInvoicesWithSingleSelectableSampleClient() {
        InMemoryInvoiceRepository repo = new InMemoryInvoiceRepository();
        InvoiceService service = service(repo, invoice -> "pdf".getBytes());
        service.createInvoice(request("FAC-OLD-1", "100.00"));
        service.createInvoice(request("FAC-OLD-2", "200.00"));

        service.resetLocalDevelopmentInvoices();

        List<InvoiceResponseDto> invoices = service.findRecentInvoices(10);
        assertEquals(1, invoices.size());
        assertEquals("FAC-" + LocalDate.now().getYear() + "-0001", invoices.getFirst().invoiceNumber());
        assertEquals("SAÚL GUERRERO LÓPEZ", invoices.getFirst().clientName());
        assertEquals("73025558Y", invoices.getFirst().clientNif());
        assertEquals(1, testClientService.findAll().size());
        assertEquals("73025558Y", testClientService.findAll().getFirst().nif());
    }

    private ClientService testClientService;

    private InvoiceService service(InMemoryInvoiceRepository repository, PdfGeneratorPort pdfGeneratorPort) {
        testClientService = new ClientService(new InMemoryClientRepository());
        return new InvoiceService(repository, pdfGeneratorPort, testClientService,
                new FiscalDataService(new InMemoryFiscalDataRepository()));
    }

    private InvoiceRequestDto request(String number, String amount) {
        return new InvoiceRequestDto(number, LocalDate.now(), "Cliente Demo", "B12345678", "Calle Mayor 1",
                List.of(new InvoiceLineRequestDto("Servicio de reparto", BigDecimal.ONE, new BigDecimal(amount))));
    }

    private static final class InMemoryInvoiceRepository implements InvoiceRepositoryPort {

        private final List<Invoice> invoices = new ArrayList<>();

        @Override
        public Invoice save(Invoice invoice) {
            Invoice persisted = new Invoice(
                    invoice.id() == null ? (long) (invoices.size() + 1) : invoice.id(),
                    invoice.invoiceNumber(),
                    invoice.clientName(),
                    invoice.clientNif(),
                    invoice.clientAddress(),
                    invoice.issueDate(),
                    invoice.subtotal(),
                    invoice.taxRate(),
                    invoice.taxAmount(),
                    invoice.irpfRate(),
                    invoice.totalRetentions(),
                    invoice.totalAmount(),
                    invoice.lines().stream()
                            .map(line -> new InvoiceLine((long) (invoices.size() + 1), line.description(),
                                    line.quantity(), line.unitPrice(), line.totalAmount()))
                            .toList(),
                    invoice.issuerSnapshot()
            );
            if (invoice.id() == null) {
                invoices.add(persisted);
            } else {
                invoices.removeIf(existing -> existing.id().equals(invoice.id()));
                invoices.add(persisted);
            }
            return persisted;
        }

        @Override
        public void deleteAll() {
            invoices.clear();
        }

        @Override
        public Optional<Invoice> findById(Long id) {
            return invoices.stream().filter(i -> i.id().equals(id)).findFirst();
        }

        @Override
        public List<Invoice> findRecent(int limit) {
            return invoices.stream().limit(limit).toList();
        }

        @Override
        public Optional<String> findLatestInvoiceNumber(String yearPrefix) {
            return invoices.stream()
                    .filter(invoice -> invoice.invoiceNumber().startsWith(yearPrefix))
                    .max(java.util.Comparator.comparing(Invoice::id))
                    .map(Invoice::invoiceNumber);
        }

        @Override
        public boolean existsByInvoiceNumber(String invoiceNumber) {
            return invoices.stream().anyMatch(invoice -> Objects.equals(invoice.invoiceNumber(), invoiceNumber));
        }

        @Override
        public boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id) {
            return invoices.stream().anyMatch(invoice -> !invoice.id().equals(id)
                    && Objects.equals(invoice.invoiceNumber(), invoiceNumber));
        }

        @Override
        public InvoiceSearchResultDto search(String term, LocalDate from, LocalDate to, int page, int size) {
            return new InvoiceSearchResultDto(invoices, invoices.size());
        }

    }

    private static final class InMemoryClientRepository implements ClientRepositoryPort {

        private final Map<String, Client> clients = new LinkedHashMap<>();

        @Override
        public Client save(Client client) {
            clients.put(client.nif(), client);
            return client;
        }

        @Override
        public List<Client> findAll() {
            return List.copyOf(clients.values());
        }

        @Override
        public void deleteAll() {
            clients.clear();
        }

    }

    private static final class InMemoryFiscalDataRepository implements FiscalDataRepositoryPort {

        private FiscalData fiscalData = new FiscalData(
                "MARTHA DENISSE CARRILLO TOVAR",
                "Y8023499D",
                "Carrer del Pintor Huguet, 1\n17310 Lloret de Mar (Girona, España)",
                "+34 664 008 020",
                "denissecarrillo1986@gmail.com",
                "ES50 2100 8137 8902 0047 3817"
        );

        @Override
        public Optional<FiscalData> findCurrent() {
            return Optional.of(fiscalData);
        }

        @Override
        public FiscalData save(FiscalData updated) {
            fiscalData = updated;
            return updated;
        }
    }
}
