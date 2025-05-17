package org.example.models;


import org.example.enums.FeatureStatus;


public class ClientFeature {
    private Long id;
    private Long clientId;
    private Long featureFlagId;
    private boolean enabled;

    public ClientFeature() {}

    public ClientFeature(Long id, Long clientId, Long featureFlagId, boolean enabled) {
        this.id = id;
        this.clientId = clientId;
        this.featureFlagId = featureFlagId;
        this.enabled = enabled;
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getFeatureFlagId() { return featureFlagId; }
    public void setFeatureFlagId(Long featureFlagId) { this.featureFlagId = featureFlagId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
