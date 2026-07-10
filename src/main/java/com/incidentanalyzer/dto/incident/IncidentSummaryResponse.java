package com.incidentanalyzer.dto.incident;

import com.incidentanalyzer.model.IncidentPriority;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import java.time.Instant;
import java.util.List;

public record IncidentSummaryResponse(
        String incidentId,
        String title,
        String applicationName,
        IncidentSeverity severity,
        IncidentPriority priority,
        IncidentStatus status,
        String engineerName,
        List<String> tags,
        Instant createdDate) {
}
