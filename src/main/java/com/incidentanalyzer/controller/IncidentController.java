package com.incidentanalyzer.controller;

import com.incidentanalyzer.dto.incident.IncidentCreateRequest;
import com.incidentanalyzer.dto.incident.IncidentResponse;
import com.incidentanalyzer.dto.incident.IncidentSearchResponse;
import com.incidentanalyzer.dto.incident.IncidentUpdateRequest;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import com.incidentanalyzer.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @Operation(summary = "Create an incident", description = "Creates a new incident with technical context, root cause notes, and tags.")
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentCreateRequest request) {
        return ResponseEntity.ok(incidentService.createIncident(request));
    }

    @GetMapping
    @Operation(summary = "Search incidents", description = "Searches incidents by text, application, severity, tag, and status.")
    public ResponseEntity<Page<IncidentSearchResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) String tag,
            @Parameter(description = "Page number starting at 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ResponseEntity.ok(incidentService.search(query, applicationName, severity, status, tag, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an incident", description = "Returns a single incident with engineer display name resolved.")
    public ResponseEntity<IncidentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getIncident(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an incident", description = "Updates the mutable incident fields and handles optimistic locking.")
    public ResponseEntity<IncidentResponse> update(@PathVariable String id, @Valid @RequestBody IncidentUpdateRequest request) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an incident", description = "Admin-only incident deletion endpoint.")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/log", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Attach logs", description = "Stores the full log dump separately and links it to the incident.")
    public ResponseEntity<IncidentResponse> attachLog(@PathVariable String id, @RequestBody String content) {
        return ResponseEntity.ok(incidentService.attachLog(id, content));
    }

    @GetMapping(value = "/{id}/log", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Fetch logs", description = "Retrieves the separately stored log content for an incident.")
    public ResponseEntity<String> getLog(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getLogContent(id));
    }
}
