package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.application.port.ClientRepositoryPort;
import com.mflogistica.domain.model.Client;
import com.mflogistica.infrastructure.persistence.entity.ClientEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClientRepositoryAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository repository;

    public ClientRepositoryAdapter(SpringDataClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = new ClientEntity();
        entity.setNif(client.nif());
        entity.setName(client.name());
        entity.setAddress(client.address());
        ClientEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Client> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    private Client toDomain(ClientEntity entity) {
        return new Client(entity.getName(), entity.getNif(), entity.getAddress());
    }
}
