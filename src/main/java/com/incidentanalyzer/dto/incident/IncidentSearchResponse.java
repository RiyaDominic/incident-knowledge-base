package com.incidentanalyzer.dto.incident;

public record IncidentSearchResponse(
        IncidentSummaryResponse incident,
        double relevanceScore,
        String matchReason) {
}
