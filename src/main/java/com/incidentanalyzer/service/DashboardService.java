package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.dashboard.DashboardStatsResponse;
import com.incidentanalyzer.dto.dashboard.LabelValue;
import com.incidentanalyzer.dto.dashboard.SeverityCount;
import com.incidentanalyzer.model.Incident;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import com.incidentanalyzer.repository.IncidentRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncidentRepository incidentRepository;

    public DashboardStatsResponse buildDashboard() {
        List<Incident> incidents = incidentRepository.findAll();
                long total = incidentRepository.count();
                long resolved = incidentRepository.countByStatus(IncidentStatus.RESOLVED);
                long open = incidentRepository.countByStatus(IncidentStatus.OPEN) + incidentRepository.countByStatus(IncidentStatus.INVESTIGATING);
                long critical = incidentRepository.countBySeverity(IncidentSeverity.CRITICAL);

        List<SeverityCount> bySeverity = List.of(IncidentSeverity.values()).stream()
                .map(severity -> new SeverityCount(severity.name(), incidents.stream().filter(incident -> incident.getSeverity() == severity).count()))
                .toList();

        List<LabelValue> commonErrors = incidents.stream()
                .filter(incident -> incident.getErrorMessage() != null)
                .collect(Collectors.groupingBy(Incident::getErrorMessage, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> new LabelValue(entry.getKey(), entry.getValue()))
                .toList();

        List<LabelValue> resolutionTrend = incidents.stream()
                .filter(incident -> incident.getResolvedDate() != null)
                .collect(Collectors.groupingBy(incident -> LocalDate.ofInstant(incident.getResolvedDate(), ZoneId.systemDefault()), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new LabelValue(entry.getKey().toString(), entry.getValue()))
                .toList();

        List<LabelValue> applicationCounts = incidents.stream()
                .collect(Collectors.groupingBy(incident -> incident.getApplicationName() == null ? "Unknown" : incident.getApplicationName(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(8)
                .map(entry -> new LabelValue(entry.getKey(), entry.getValue()))
                .toList();

        return new DashboardStatsResponse(total, resolved, open, critical, bySeverity, commonErrors, resolutionTrend, applicationCounts);
    }
}
