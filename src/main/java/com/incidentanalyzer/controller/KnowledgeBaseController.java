package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.incident.IncidentSearchResponse;
import com.incidentanalyzer.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/similar")
    @Operation(summary = "Suggest similar incidents", description = "Finds historical incidents with similar text, tags, and application context.")
    public ResponseEntity<List<IncidentSearchResponse>> similar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) List<String> tags) {
        return ResponseEntity.ok(knowledgeBaseService.suggestSimilarIncidents(query, applicationName, tags));
    }
}
