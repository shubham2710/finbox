package org.example.service;

import org.example.models.Client;
import org.example.models.ClientFeature;
import org.example.models.FeatureFlag;
import org.example.repository.ClientFeatureRepository;
import org.example.repository.ClientRepository;
import org.example.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import org.example.exception.CyclicDependencyException;
import org.example.models.FeatureFlag;
import org.example.repository.ClientFeatureRepository;
import org.example.repository.ClientRepository;
import org.example.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeatureFlagServiceTest {

    private FeatureFlagRepository featureFlagRepository;
    private ClientFeatureRepository clientFeatureRepository;
    private ClientRepository clientRepository;

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagRepository = mock(FeatureFlagRepository.class);
        clientFeatureRepository = mock(ClientFeatureRepository.class);
        clientRepository = mock(ClientRepository.class);
        featureFlagService = new FeatureFlagService(featureFlagRepository, clientFeatureRepository, clientRepository);
    }

    @Test
    void createFeatureFlag_shouldSucceed_whenNameIsUnique() {
        FeatureFlag flag = new FeatureFlag(null, "NewFeature", null);

        when(featureFlagRepository.findByName("NewFeature")).thenReturn(Optional.empty());
        when(featureFlagRepository.save(flag)).thenReturn(flag);

        FeatureFlag created = featureFlagService.createFeatureFlag(flag);

        assertEquals("NewFeature", created.getName());
        verify(featureFlagRepository).save(flag);
    }

    @Test
    void createFeatureFlag_shouldFail_whenNameAlreadyExists() {
        FeatureFlag flag = new FeatureFlag(null, "ExistingFeature", null);
        when(featureFlagRepository.findByName("ExistingFeature")).thenReturn(Optional.of(flag));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                featureFlagService.createFeatureFlag(flag));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void listAllFeatureFlags_shouldReturnList() {
        List<FeatureFlag> flags = Arrays.asList(
                new FeatureFlag(1L, "F1", null),
                new FeatureFlag(2L, "F2", null)
        );
        when(featureFlagRepository.findAll()).thenReturn(flags);

        List<FeatureFlag> result = featureFlagService.listAllFeatureFlags();

        assertEquals(2, result.size());
        assertEquals("F1", result.get(0).getName());
    }

    @Test
    void updateFeatureFlag_shouldUpdateNameAndDependency_whenValid() {
        FeatureFlag existing = new FeatureFlag(1L, "OldFeature", null);
        FeatureFlag updateRequest = new FeatureFlag(null, "UpdatedFeature", 2L);
        FeatureFlag parent = new FeatureFlag(2L, "ParentFeature", null);

        when(featureFlagRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(featureFlagRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(featureFlagRepository.findByName("UpdatedFeature")).thenReturn(Optional.empty());
        when(featureFlagRepository.save(any())).thenReturn(existing);

        FeatureFlag updated = featureFlagService.updateFeatureFlag(1L, updateRequest);

        assertEquals("UpdatedFeature", updated.getName());
        assertEquals(2L, updated.getDependsOnFeatureId());
    }

    @Test
    void updateFeatureFlag_shouldRemoveDependency_whenNullPassed() {
        FeatureFlag ff = new FeatureFlag(1L, "Feature", 2L);
        FeatureFlag updateRequest = new FeatureFlag(null, "Feature", null);

        when(featureFlagRepository.findById(1L)).thenReturn(Optional.of(ff));
        when(featureFlagRepository.save(any())).thenReturn(ff);

        FeatureFlag updated = featureFlagService.updateFeatureFlag(1L, updateRequest);

        assertNull(updated.getDependsOnFeatureId());
    }
}
