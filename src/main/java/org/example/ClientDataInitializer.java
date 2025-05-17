package org.example;

import org.example.models.Client;
import org.example.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ClientDataInitializer implements CommandLineRunner {

    private final ClientRepository clientRepository;

    public ClientDataInitializer(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        clientRepository.save(new Client(1L, "Client A"));
        clientRepository.save(new Client(2L, "Client B"));
        clientRepository.save(new Client(3L, "Client C"));
        clientRepository.save(new Client(4L, "Client D"));

        System.out.println("Initialized 4 clients");
    }
}
