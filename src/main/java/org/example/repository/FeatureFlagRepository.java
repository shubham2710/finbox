package org.example.repository;

import org.example.models.FeatureFlag;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository {
    FeatureFlag save(FeatureFlag featureFlag);
    Optional<FeatureFlag> findById(Long id);
    Optional<FeatureFlag> findByName(String name);
    List<FeatureFlag> findAll();
    void deleteById(Long id);
}
