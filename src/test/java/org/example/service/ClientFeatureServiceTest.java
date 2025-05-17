package org.example.service;

import org.example.models.ClientFeature;
import org.example.models.FeatureFlag;
import org.example.repository.ClientFeatureRepository;
import org.example.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientFeatureServiceTest {

    private ClientFeatureRepository clientFeatureRepository;
    private FeatureFlagRepository featureFlagRepository;
    private ClientFeatureService clientFeatureService;

    @BeforeEach
    void setup() {
        clientFeatureRepository = mock(ClientFeatureRepository.class);
        featureFlagRepository = mock(FeatureFlagRepository.class);
        clientFeatureService = new ClientFeatureService(clientFeatureRepository, featureFlagRepository);
    }

    @Test
    void enableFeature_shouldEnable_whenNoDependency() {
        Long clientId = 1L;
        Long featureId = 10L;
        FeatureFlag flag = new FeatureFlag(featureId, "Feature1", null);

        when(featureFlagRepository.findById(featureId)).thenReturn(Optional.of(flag));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureId)).thenReturn(Optional.empty());

        ClientFeature saved = new ClientFeature(100L, clientId, featureId, true);
        when(clientFeatureRepository.save(any(ClientFeature.class))).thenReturn(saved);

        ClientFeature result = clientFeatureService.enableFeature(clientId, featureId);

        assertTrue(result.isEnabled());
        verify(clientFeatureRepository).save(any(ClientFeature.class));
    }

    @Test
    void enableFeature_shouldEnable_whenDependencyEnabled() {
        Long clientId = 1L;
        Long featureId = 10L;
        Long dependsOnId = 5L;
        FeatureFlag flag = new FeatureFlag(featureId, "Feature1", dependsOnId);
        ClientFeature dependency = new ClientFeature(200L, clientId, dependsOnId, true);

        when(featureFlagRepository.findById(featureId)).thenReturn(Optional.of(flag));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, dependsOnId)).thenReturn(Optional.of(dependency));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureId)).thenReturn(Optional.empty());

        ClientFeature saved = new ClientFeature(101L, clientId, featureId, true);
        when(clientFeatureRepository.save(any(ClientFeature.class))).thenReturn(saved);

        ClientFeature result = clientFeatureService.enableFeature(clientId, featureId);

        assertTrue(result.isEnabled());
        verify(clientFeatureRepository).save(any(ClientFeature.class));
    }

    @Test
    void enableFeature_shouldThrow_whenDependencyNotEnabled() {
        Long clientId = 1L;
        Long featureId = 10L;
        Long dependsOnId = 5L;
        FeatureFlag flag = new FeatureFlag(featureId, "Feature1", dependsOnId);
        ClientFeature dependency = new ClientFeature(200L, clientId, dependsOnId, false);

        when(featureFlagRepository.findById(featureId)).thenReturn(Optional.of(flag));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, dependsOnId)).thenReturn(Optional.of(dependency));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> clientFeatureService.enableFeature(clientId, featureId));
        assertEquals("Dependency feature flag not enabled for client", ex.getMessage());
    }

    @Test
    void enableFeature_shouldEnableExistingFeature() {
        Long clientId = 1L;
        Long featureId = 10L;
        FeatureFlag flag = new FeatureFlag(featureId, "Feature1", null);
        ClientFeature existing = new ClientFeature(300L, clientId, featureId, false);

        when(featureFlagRepository.findById(featureId)).thenReturn(Optional.of(flag));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureId)).thenReturn(Optional.of(existing));
        when(clientFeatureRepository.save(existing)).thenReturn(existing);

        ClientFeature result = clientFeatureService.enableFeature(clientId, featureId);

        assertTrue(result.isEnabled());
        verify(clientFeatureRepository).save(existing);
    }

    @Test
    void disableFeature_shouldDisableFeatureAndDependentsRecursively() {
        Long clientId = 1L;
        Long parentFeatureId = 10L;
        Long childFeatureId = 20L;
        Long grandChildFeatureId = 30L;

        ClientFeature parentCf = new ClientFeature(1L, clientId, parentFeatureId, true);
        ClientFeature childCf = new ClientFeature(2L, clientId, childFeatureId, true);
        ClientFeature grandChildCf = new ClientFeature(3L, clientId, grandChildFeatureId, true);

        FeatureFlag parentFlag = new FeatureFlag(parentFeatureId, "ParentFeature", null);
        FeatureFlag childFlag = new FeatureFlag(childFeatureId, "ChildFeature", parentFeatureId);
        FeatureFlag grandChildFlag = new FeatureFlag(grandChildFeatureId, "GrandChildFeature", childFeatureId);

        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, parentFeatureId)).thenReturn(Optional.of(parentCf));
        when(featureFlagRepository.findAll()).thenReturn(Arrays.asList(parentFlag, childFlag, grandChildFlag));

        // Child enabled
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, childFeatureId)).thenReturn(Optional.of(childCf));
        // Grandchild enabled
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, grandChildFeatureId)).thenReturn(Optional.of(grandChildCf));

        clientFeatureService.disableFeature(clientId, parentFeatureId);

        assertFalse(parentCf.isEnabled());
        // Verify child disabled and saved
        assertFalse(childCf.isEnabled());
        // Verify grandchild disabled and saved
        assertFalse(grandChildCf.isEnabled());

        verify(clientFeatureRepository).save(parentCf);
        verify(clientFeatureRepository).save(childCf);
        verify(clientFeatureRepository).save(grandChildCf);
    }

    @Test
    void isFeatureEnabled_shouldReturnTrue_whenFeatureEnabledAndDependenciesEnabled() {
        Long clientId = 1L;
        Long featureId = 10L;
        Long dependsOnId = 5L;

        FeatureFlag featureFlag = new FeatureFlag(featureId, "Feature1", dependsOnId);
        FeatureFlag dependsOnFlag = new FeatureFlag(dependsOnId, "Dependency", null);

        ClientFeature featureCf = new ClientFeature(1L, clientId, featureId, true);
        ClientFeature dependsOnCf = new ClientFeature(2L, clientId, dependsOnId, true);

        when(featureFlagRepository.findById(featureId)).thenReturn(Optional.of(featureFlag));
        when(featureFlagRepository.findById(dependsOnId)).thenReturn(Optional.of(dependsOnFlag));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureId)).thenReturn(Optional.of(featureCf));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, dependsOnId)).thenReturn(Optional.of(dependsOnCf));

        boolean enabled = clientFeatureService.isFeatureEnabled(clientId, featureId);

        assertTrue(enabled);
    }

    @Test
    void isFeatureEnabled_shouldReturnFalse_whenFeatureNotEnabled() {
        Long clientId = 1L;
        Long featureId = 10L;

        FeatureFlag featureFlag = new FeatureFlag(featureId, "Feature1", null);

        when(featureFlagRepository.findById(featureId)).thenReturn(Optional.of(featureFlag));
        when(clientFeatureRepository.findByClientIdAndFeatureFlagId(clientId, featureId)).thenReturn(Optional.empty());

        boolean enabled = clientFeatureService.isFeatureEnabled(clientId, featureId);

        assertFalse(enabled);
    }

    @Test
    void listEnabledFeatures_shouldReturnOnlyEnabledFeatures() {
        Long clientId = 1L;

        ClientFeature cf1 = new ClientFeature(1L, clientId, 10L, true);
        ClientFeature cf2 = new ClientFeature(2L, clientId, 20L, false);
        ClientFeature cf3 = new ClientFeature(3L, clientId, 30L, true);

        FeatureFlag ff1 = new FeatureFlag(10L, "F1", null);
        FeatureFlag ff3 = new FeatureFlag(30L, "F3", null);

        when(clientFeatureRepository.findByClientId(clientId)).thenReturn(Arrays.asList(cf1, cf2, cf3));
        when(featureFlagRepository.findById(10L)).thenReturn(Optional.of(ff1));
        when(featureFlagRepository.findById(30L)).thenReturn(Optional.of(ff3));

        List<FeatureFlag> enabledFeatures = clientFeatureService.listEnabledFeatures(clientId);

        assertEquals(2, enabledFeatures.size());
        assertTrue(enabledFeatures.stream().anyMatch(f -> f.getId().equals(10L)));
        assertTrue(enabledFeatures.stream().anyMatch(f -> f.getId().equals(30L)));
    }
}
