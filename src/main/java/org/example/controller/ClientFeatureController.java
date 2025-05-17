package org.example.controller;

import org.example.models.FeatureFlag;
import org.example.service.ClientFeatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client-feature")
public class ClientFeatureController {

    private final ClientFeatureService clientFeatureService;

    public ClientFeatureController(ClientFeatureService clientFeatureService) {
        this.clientFeatureService = clientFeatureService;
    }

    @PostMapping("/{flagId}/clients/{clientId}/enable")
    public ResponseEntity<String> enableFeatureForClient(@PathVariable Long flagId,
                                                         @PathVariable Long clientId) {
        clientFeatureService.enableFeature(clientId, flagId);
        return ResponseEntity.ok("Feature flag enabled for client");
    }

    @PostMapping("/{flagId}/clients/{clientId}/disable")
    public ResponseEntity<String> disableFeatureForClient(@PathVariable Long flagId,
                                                          @PathVariable Long clientId) {
        clientFeatureService.disableFeature(clientId, flagId);
        return ResponseEntity.ok("Feature flag disabled for client");
    }

    @GetMapping("/{flagId}/clients/{clientId}/enabled")
    public ResponseEntity<Boolean> isFeatureEnabledForClient(@PathVariable Long flagId,
                                                             @PathVariable Long clientId) {
        boolean enabled = clientFeatureService.isFeatureEnabled(clientId, flagId);
        return ResponseEntity.ok(enabled);
    }

    @GetMapping("/clients/{clientId}/enabled-features")
    public ResponseEntity<List<FeatureFlag>> listEnabledFeaturesForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientFeatureService.listEnabledFeatures(clientId));
    }
}
