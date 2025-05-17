package org.example.service;



import org.example.exception.CyclicDependencyException;
import org.example.models.Client;
import org.example.models.ClientFeature;
import org.example.models.FeatureFlag;
import org.example.repository.ClientFeatureRepository;
import org.example.repository.ClientRepository;
import org.example.repository.FeatureFlagRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final ClientFeatureRepository clientFeatureRepository;
    private final ClientRepository clientRepository;

    public FeatureFlagService(FeatureFlagRepository featureFlagRepository,
                              ClientFeatureRepository clientFeatureRepository,
                              ClientRepository clientRepository) {
        this.featureFlagRepository = featureFlagRepository;
        this.clientFeatureRepository = clientFeatureRepository;
        this.clientRepository = clientRepository;
    }

    // Create feature flag with optional dependency
    public FeatureFlag createFeatureFlag(FeatureFlag featureFlag) {

        Optional<FeatureFlag> existingFeature = featureFlagRepository.findByName(featureFlag.getName());
        if (existingFeature.isPresent()) {
            throw new IllegalArgumentException("Feature flag with name '" + featureFlag.getName() + "' already exists");
        }

        if (featureFlag.getDependsOnFeatureId() != null) {
            FeatureFlag parent = featureFlagRepository.findById(featureFlag.getDependsOnFeatureId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent feature flag not found"));

        }

        return featureFlagRepository.save(featureFlag);
    }

    private boolean detectCycle(Long currentFlagId, Long dependsOnFlagId) {
        Set<Long> visited = new HashSet<>();
        Long cur = dependsOnFlagId;

        while (cur != null) {
            if (Objects.equals(cur, currentFlagId)) {
                return true; // cycle detected
            }
            FeatureFlag ff = featureFlagRepository.findById(cur).orElse(null);
            if (ff == null || ff.getDependsOnFeatureId() == null) {
                break;
            }
            cur = ff.getDependsOnFeatureId();
            if (!visited.add(cur)) {
                break; // already visited, break to avoid infinite loop
            }
        }
        return false;
    }


    // List all feature flags (for admin)
    public List<FeatureFlag> listAllFeatureFlags() {
        return featureFlagRepository.findAll();
    }


    public FeatureFlag updateFeatureFlag(Long id, FeatureFlag updateRequest) {
        FeatureFlag existingFlag = featureFlagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found"));

        // If name is changed, check for duplicates
        if (!existingFlag.getName().equals(updateRequest.getName())) {
            featureFlagRepository.findByName(updateRequest.getName()).ifPresent(f -> {
                throw new IllegalArgumentException("Feature flag with name '" + updateRequest.getName() + "' already exists");
            });
            existingFlag.setName(updateRequest.getName());
        }

        Long newDependencyId = updateRequest.getDependsOnFeatureId();

        if (newDependencyId != null) {
            if (!newDependencyId.equals(existingFlag.getDependsOnFeatureId())) {
                // Check if new dependency exists
                featureFlagRepository.findById(newDependencyId)
                        .orElseThrow(() -> new IllegalArgumentException("Parent feature flag not found"));

                // Check cyclic dependency
                if (detectCycle(existingFlag.getId(), newDependencyId)) {
                    throw new CyclicDependencyException("Cyclic dependency detected");
                }

                existingFlag.setDependsOnFeatureId(newDependencyId);
            }
        } else {
            existingFlag.setDependsOnFeatureId(null);
        }

        return featureFlagRepository.save(existingFlag);

    }
}