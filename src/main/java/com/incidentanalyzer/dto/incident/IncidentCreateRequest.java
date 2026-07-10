package com.incidentanalyzer.dto.incident;

import com.incidentanalyzer.model.IncidentPriority;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record IncidentCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 120) String applicationName,
        @NotBlank @Size(max = 60) String environment,
        @NotNull IncidentSeverity severity,
        @NotNull IncidentPriority priority,
        @NotNull IncidentStatus status,
        @NotBlank @Size(max = 4000) String errorMessage,
        String logRefId,
        @Size(max = 12000) String stackTrace,
        String rootCause,
        String solution,
        String engineerId,
        List<String> affectedServices,
        List<String> tags) {
}
