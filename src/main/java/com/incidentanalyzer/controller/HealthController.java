package com.incidentanalyzer.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Simple liveness endpoint for deployment and monitoring.")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
