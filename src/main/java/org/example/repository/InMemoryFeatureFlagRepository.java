package org.example.repository;

import org.example.models.FeatureFlag;

import java.util.*;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryFeatureFlagRepository implements FeatureFlagRepository {

    private final Map<Long, FeatureFlag> store = new HashMap<>();
    private long idSequence = 1;

    @Override
    public FeatureFlag save(FeatureFlag featureFlag) {
        if (featureFlag.getId() == null) {
            featureFlag.setId(idSequence++);
        }
        store.put(featureFlag.getId(), featureFlag);
        return featureFlag;
    }

    @Override
    public Optional<FeatureFlag> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<FeatureFlag> findByName(String name) {
        return store.values().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<FeatureFlag> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
