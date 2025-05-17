package org.example.repository;

import org.example.models.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Optional<Client> findById(Long id);
    List<Client> findAll();
    Client save(Client client);
}

