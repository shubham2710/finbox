package org.example.service;


import org.example.models.ClientFeature;
import org.example.models.FeatureFlag;
import org.example.repository.ClientFeatureRepository;
import org.example.repository.FeatureFlagRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClientFeatureService {

    private final ClientFeatureRepository clientFeatureRepository;
    private final FeatureFlagRepository featureFlagRepository;

    public ClientFeatureService(ClientFeatureRepository clientFeatureRepository, FeatureFlagRepository featureFlagRepository) {
        this.clientFeatureRepository = clientFeatureRepository;
        this.featureFlagRepository = featureFlagRepository;
    }

    public ClientFeature enableFeature(Long clientId, Long featureFlagId) {
        FeatureFlag featureFlag = featureFlagRepository.findById(featureFlagId)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found"));

        if (featureFlag.getDependsOnFeatureId() != null) {
            Optional<ClientFeature> dependency = clientFeatureRepository
                    .findByClientIdAndFeatureFlagId(clientId, featureFlag.getDependsOnFeatureId());

            if (dependency.isEmpty() || !dependency.get().isEnabled()) {
                throw new IllegalStateException("Dependency feature flag not enabled for client");
            }
        }

        Optional<ClientFeature> existing = clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureFlagId);
        if (existing.isPresent()) {
            ClientFeature cf = existing.get();
            cf.setEnabled(true);
            return clientFeatureRepository.save(cf);
        } else {
            ClientFeature cf = new ClientFeature(null, clientId, featureFlagId, true);
            return clientFeatureRepository.save(cf);
        }
    }

    public void disableFeature(Long clientId, Long featureFlagId) {
        Optional<ClientFeature> existing = clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureFlagId);
        if (existing.isPresent()) {
            ClientFeature cf = existing.get();
            cf.setEnabled(false);
            clientFeatureRepository.save(cf);

            // Disable all dependent children recursively
            disableChildFeaturesRecursively(clientId, featureFlagId);
        }
    }

    private void disableChildFeaturesRecursively(Long clientId, Long parentFeatureId) {
        List<FeatureFlag> children = featureFlagRepository.findAll();
        for (FeatureFlag child : children) {
            if (Objects.equals(child.getDependsOnFeatureId(), parentFeatureId)) {
                Optional<ClientFeature> cfOpt = clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, child.getId());
                if (cfOpt.isPresent() && cfOpt.get().isEnabled()) {
                    ClientFeature cf = cfOpt.get();
                    cf.setEnabled(false);
                    clientFeatureRepository.save(cf);
                    disableChildFeaturesRecursively(clientId, child.getId());
                }
            }
        }
    }

    public boolean isFeatureEnabled(Long clientId, Long featureFlagId) {
        FeatureFlag featureFlag = featureFlagRepository.findById(featureFlagId)
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found"));

        Optional<ClientFeature> cfOpt = clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureFlagId);
        if (cfOpt.isEmpty() || !cfOpt.get().isEnabled()) {
            return false;
        }

        if (featureFlag.getDependsOnFeatureId() != null) {
            return isFeatureEnabled(clientId, featureFlag.getDependsOnFeatureId());
        }

        return true;
    }

    public List<FeatureFlag> listEnabledFeatures(Long clientId) {
        List<ClientFeature> clientFeatures = clientFeatureRepository.findByClientId(clientId);
        List<FeatureFlag> enabledFeatures = new ArrayList<>();

        for (ClientFeature cf : clientFeatures) {
            if (cf.isEnabled()) {
                featureFlagRepository.findById(cf.getFeatureFlagId()).ifPresent(enabledFeatures::add);
            }
        }
        return enabledFeatures;
    }
}
