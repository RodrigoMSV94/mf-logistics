package com.mflogistica.application.port;

import com.mflogistica.domain.model.Client;

import java.util.List;

public interface ClientRepositoryPort {

    Client save(Client client);

    List<Client> findAll();

    void deleteAll();
}
