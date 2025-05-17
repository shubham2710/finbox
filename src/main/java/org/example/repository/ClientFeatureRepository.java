package org.example.repository;

import org.example.models.ClientFeature;

import java.util.List;
import java.util.Optional;


public interface ClientFeatureRepository {
    ClientFeature save(ClientFeature clientFeature);
    Optional<ClientFeature> findById(Long id);
    List<ClientFeature> findByClientId(Long clientId);
    Optional<ClientFeature> findByClientIdAndFeatureFlagId(Long clientId, Long featureFlagId);
    void deleteById(Long id);
}
