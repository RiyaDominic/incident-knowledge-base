package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.dashboard.DashboardStatsResponse;
import com.incidentanalyzer.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Load dashboard stats", description = "Returns cards and chart data for the operational dashboard.")
    public ResponseEntity<DashboardStatsResponse> dashboard() {
        return ResponseEntity.ok(dashboardService.buildDashboard());
    }
}
