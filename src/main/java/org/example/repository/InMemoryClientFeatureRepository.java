package org.example.repository;

import org.example.models.ClientFeature;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InMemoryClientFeatureRepository implements ClientFeatureRepository {

    private final Map<Long, ClientFeature> store = new HashMap<>();
    private long idSequence = 1;

    @Override
    public ClientFeature save(ClientFeature clientFeature) {
        if (clientFeature.getId() == null) {
            clientFeature.setId(idSequence++);
        }
        store.put(clientFeature.getId(), clientFeature);
        return clientFeature;
    }

    @Override
    public Optional<ClientFeature> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ClientFeature> findByClientId(Long clientId) {
        return store.values().stream()
                .filter(cf -> cf.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClientFeature> findByClientIdAndFeatureFlagId(Long clientId, Long featureFlagId) {
        return store.values().stream()
                .filter(cf -> cf.getClientId().equals(clientId) && cf.getFeatureFlagId().equals(featureFlagId))
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
