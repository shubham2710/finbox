package org.example.models;


import java.util.Objects;

public class FeatureFlag {
    private Long id;
    private String name;
    private Long dependsOnFeatureId; // nullable: parent flag id for dependency

    private boolean enabled; // if this flag is enabled globally (optional, could be used)

    public FeatureFlag() {}

    public FeatureFlag(Long id, String name, Long dependsOnFeatureId) {
        this.id = id;
        this.name = name;
        this.dependsOnFeatureId = dependsOnFeatureId;
        this.enabled = false;
    }

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDependsOnFeatureId() { return dependsOnFeatureId; }
    public void setDependsOnFeatureId(Long dependsOnFeatureId) { this.dependsOnFeatureId = dependsOnFeatureId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeatureFlag)) return false;
        FeatureFlag that = (FeatureFlag) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
