package com.incidentanalyzer.dto.dashboard;

import java.util.List;

public record DashboardStatsResponse(
        long totalIncidents,
        long resolvedIncidents,
        long openIncidents,
        long criticalIncidents,
        List<SeverityCount> incidentsBySeverity,
        List<LabelValue> mostCommonErrors,
        List<LabelValue> resolutionTrend,
        List<LabelValue> applicationFailureCounts) {
}
