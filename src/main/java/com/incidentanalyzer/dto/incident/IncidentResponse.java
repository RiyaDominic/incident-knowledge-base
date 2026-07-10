package com.incidentanalyzer.dto.incident;

import com.incidentanalyzer.model.IncidentPriority;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import java.time.Instant;
import java.util.List;

public record IncidentResponse(
        String incidentId,
        String title,
        String applicationName,
        String environment,
        IncidentSeverity severity,
        IncidentPriority priority,
        IncidentStatus status,
        String errorMessage,
        String logRefId,
        String stackTrace,
        String rootCause,
        String solution,
        String engineerId,
        String engineerName,
        List<String> affectedServices,
        List<String> tags,
        Instant createdDate,
        Instant updatedDate,
        Instant resolvedDate,
        Long version) {
}
