package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.incident.IncidentSearchResponse;
import com.incidentanalyzer.model.IncidentStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final IncidentService incidentService;

    public List<IncidentSearchResponse> suggestSimilarIncidents(String query, String applicationName, List<String> tags) {
        String tag = tags == null || tags.isEmpty() ? null : tags.get(0);
        return incidentService.search(query, applicationName, null, IncidentStatus.RESOLVED, tag, PageRequest.of(0, 5))
                .getContent();
    }
}
