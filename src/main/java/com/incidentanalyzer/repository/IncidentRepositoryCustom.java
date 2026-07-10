package com.incidentanalyzer.repository;

import com.incidentanalyzer.dto.incident.IncidentSearchResponse;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncidentRepositoryCustom {

    Page<IncidentSearchResponse> searchIncidents(
            String query,
            String applicationName,
            IncidentSeverity severity,
            IncidentStatus status,
            String tag,
            Pageable pageable);
}
