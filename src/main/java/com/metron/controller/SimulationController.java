package com.metron.controller;

import com.metron.dto.PurchaseSimulationRequest;
import com.metron.dto.PurchaseSimulationResponse;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.PurchaseSimulatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulate")
public class SimulationController {

    private final PurchaseSimulatorService simulatorService;
    private final CurrentUserContext userContext;

    public SimulationController(PurchaseSimulatorService simulatorService, CurrentUserContext userContext) {
        this.simulatorService = simulatorService;
        this.userContext = userContext;
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseSimulationResponse> simulatePurchase(@Valid @RequestBody PurchaseSimulationRequest request) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(simulatorService.simulatePurchase(user, request));
    }
}
