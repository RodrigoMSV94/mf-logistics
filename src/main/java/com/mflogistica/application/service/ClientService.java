package com.mflogistica.application.service;

import com.mflogistica.application.port.ClientRepositoryPort;
import com.mflogistica.domain.exception.BusinessException;
import com.mflogistica.domain.model.Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private static final Client DEFAULT_CLIENT = new Client(
            "SAÚL GUERRERO LÓPEZ",
            "73025558Y",
            "Contrato Logístico Operativo\nCarrer Comanegra, 3\n17252 Calonge (Girona, España)"
    );

    private final ClientRepositoryPort clientRepository;

    public ClientService(ClientRepositoryPort clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public void saveFromInvoice(String name, String nif, String address) {
        clientRepository.save(new Client(name, nif, address));
    }

    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Client findDefaultClient() {
        return clientRepository.findAll().stream()
                .filter(client -> DEFAULT_CLIENT.nif().equals(client.nif()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No se encontró el cliente de ejemplo configurado"));
    }

    @Transactional
    public void resetToDefaultClient() {
        clientRepository.deleteAll();
        clientRepository.save(DEFAULT_CLIENT);
    }
}
