package org.example.controller;


import org.example.models.FeatureFlag;
import org.example.service.ClientFeatureService;
import org.example.service.FeatureFlagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

import java.util.List;


@RestController
@RequestMapping("/api/feature")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @PostMapping
    public ResponseEntity<FeatureFlag> createFeatureFlag(@RequestBody FeatureFlag featureFlag) {
        FeatureFlag created = featureFlagService.createFeatureFlag(featureFlag);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FeatureFlag>> listAllFeatureFlags() {
        return ResponseEntity.ok(featureFlagService.listAllFeatureFlags());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FeatureFlag> updateFeatureFlag(@PathVariable Long id,
                                                         @RequestBody FeatureFlag featureFlag) {
        FeatureFlag updated = featureFlagService.updateFeatureFlag(id, featureFlag);
        return ResponseEntity.ok(updated);
    }
}
