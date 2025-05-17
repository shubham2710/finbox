package org.example.repository;

import org.example.models.Client;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryClientRepository implements org.example.repository.ClientRepository {

    private final Map<Long, Client> clientMap = new ConcurrentHashMap<>();

    @Override
    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(clientMap.get(id));
    }

    @Override
    public List<Client> findAll() {
        return new ArrayList<>(clientMap.values());
    }

    @Override
    public Client save(Client client) {
        clientMap.put(client.getId(), client);
        return client;
    }
}
