package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.incident.IncidentCreateRequest;
import com.incidentanalyzer.dto.incident.IncidentResponse;
import com.incidentanalyzer.dto.incident.IncidentSearchResponse;
import com.incidentanalyzer.dto.incident.IncidentSummaryResponse;
import com.incidentanalyzer.dto.incident.IncidentUpdateRequest;
import com.incidentanalyzer.exception.BadOperationException;
import com.incidentanalyzer.exception.ResourceNotFoundException;
import com.incidentanalyzer.model.Incident;
import com.incidentanalyzer.model.IncidentLog;
import com.incidentanalyzer.model.IncidentStatus;
import com.incidentanalyzer.model.User;
import com.incidentanalyzer.repository.CommentRepository;
import com.incidentanalyzer.repository.IncidentLogRepository;
import com.incidentanalyzer.repository.IncidentRepository;
import com.incidentanalyzer.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentLogRepository incidentLogRepository;
    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public IncidentResponse createIncident(IncidentCreateRequest request) {
        if (request.status() == null) {
            throw new BadOperationException("Incident status is required");
        }
        Incident incident = Incident.builder()
                .incidentId(UUID.randomUUID().toString())
                .title(request.title())
                .applicationName(request.applicationName())
                .environment(request.environment())
                .severity(request.severity())
                .priority(request.priority())
                .status(request.status())
                .errorMessage(request.errorMessage())
                .logRefId(request.logRefId())
                .stackTrace(request.stackTrace())
                .rootCause(request.rootCause())
                .solution(request.solution())
                .engineerId(resolveEngineerId(request.engineerId()))
                .affectedServices(request.affectedServices())
                .tags(request.tags())
                .createdDate(Instant.now())
                .resolvedDate(request.status() == IncidentStatus.RESOLVED ? Instant.now() : null)
                .build();
        Incident saved = incidentRepository.save(incident);
        notificationService.notifyIncidentCreated(saved);
        if (saved.getEngineerId() != null) {
            notificationService.notifyIncidentAssigned(saved);
        }
        return toResponse(saved);
    }

    public IncidentResponse updateIncident(String incidentId, IncidentUpdateRequest request) {
        Incident existing = getIncidentEntity(incidentId);
        String previousEngineerId = existing.getEngineerId();
        IncidentStatus previousStatus = existing.getStatus();

        existing.setTitle(request.title());
        existing.setApplicationName(request.applicationName());
        existing.setEnvironment(request.environment());
        existing.setSeverity(request.severity());
        existing.setPriority(request.priority());
        existing.setStatus(request.status());
        existing.setErrorMessage(request.errorMessage());
        existing.setLogRefId(request.logRefId());
        existing.setStackTrace(request.stackTrace());
        existing.setRootCause(request.rootCause());
        existing.setSolution(request.solution());
        existing.setEngineerId(resolveEngineerId(request.engineerId()));
        existing.setAffectedServices(request.affectedServices());
        existing.setTags(request.tags());
        existing.setUpdatedDate(Instant.now());
        if (request.status() == IncidentStatus.RESOLVED && existing.getResolvedDate() == null) {
            existing.setResolvedDate(Instant.now());
        }

        Incident saved = incidentRepository.save(existing);
        if (saved.getEngineerId() != null && !saved.getEngineerId().equals(previousEngineerId)) {
            notificationService.notifyIncidentAssigned(saved);
        }
        if (previousStatus != IncidentStatus.RESOLVED && saved.getStatus() == IncidentStatus.RESOLVED) {
            notificationService.notifyIncidentResolved(saved);
        }
        return toResponse(saved);
    }

    public void deleteIncident(String incidentId) {
        if (!incidentRepository.existsByIncidentId(incidentId)) {
            throw new ResourceNotFoundException("Incident not found: " + incidentId);
        }
        incidentLogRepository.findByIncidentId(incidentId).ifPresent(incidentLogRepository::delete);
        commentRepository.findByIncidentIdOrderByTimestampAsc(incidentId).forEach(commentRepository::delete);
        notificationRepository.findByIncidentId(incidentId).forEach(notificationRepository::delete);
        incidentRepository.deleteById(incidentId);
    }

    public IncidentResponse getIncident(String incidentId) {
        return toResponse(getIncidentEntity(incidentId));
    }

    public Incident getIncidentEntity(String incidentId) {
        return incidentRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));
    }

    public Page<IncidentSearchResponse> search(String query, String applicationName, com.incidentanalyzer.model.IncidentSeverity severity, IncidentStatus status, String tag, Pageable pageable) {
        return incidentRepository.searchIncidents(query, applicationName, severity, status, tag, pageable)
            .map(result -> new IncidentSearchResponse(
                new IncidentSummaryResponse(
                    result.incident().incidentId(),
                    result.incident().title(),
                    result.incident().applicationName(),
                    result.incident().severity(),
                    result.incident().priority(),
                    result.incident().status(),
                    resolveEngineerName(getIncidentEntity(result.incident().incidentId()).getEngineerId()),
                    result.incident().tags(),
                    result.incident().createdDate()),
                result.relevanceScore(),
                result.matchReason()));
    }

    public List<IncidentSummaryResponse> listRecent(Pageable pageable) {
        return incidentRepository.searchIncidents(null, null, null, null, null, pageable)
                .stream()
                .map(IncidentSearchResponse::incident)
                .toList();
    }

    public IncidentResponse attachLog(String incidentId, String content) {
        Incident incident = getIncidentEntity(incidentId);
        IncidentLog log = incidentLogRepository.findByIncidentId(incidentId)
                .orElseGet(() -> IncidentLog.builder().incidentId(incidentId).build());
        log.setContent(content);
        IncidentLog savedLog = incidentLogRepository.save(log);
        incident.setLogRefId(savedLog.getId());
        incidentRepository.save(incident);
        return toResponse(incident);
    }

    public String getLogContent(String incidentId) {
        return incidentLogRepository.findByIncidentId(incidentId)
                .map(IncidentLog::getContent)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found for incident: " + incidentId));
    }

    private String resolveEngineerName(String engineerId) {
        if (engineerId == null) {
            return null;
        }
        try {
            return userService.findById(engineerId).getName();
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    private String resolveEngineerId(String engineerId) {
        if (engineerId == null || engineerId.isBlank()) {
            return null;
        }
        User engineer = userService.findById(engineerId);
        return engineer.getId();
    }

    public IncidentResponse toResponse(Incident incident) {
        String engineerName = null;
        if (incident.getEngineerId() != null) {
            try {
                engineerName = userService.findById(incident.getEngineerId()).getName();
            } catch (ResourceNotFoundException ignored) {
                engineerName = null;
            }
        }
        return new IncidentResponse(
                incident.getIncidentId(),
                incident.getTitle(),
                incident.getApplicationName(),
                incident.getEnvironment(),
                incident.getSeverity(),
                incident.getPriority(),
                incident.getStatus(),
                incident.getErrorMessage(),
                incident.getLogRefId(),
                incident.getStackTrace(),
                incident.getRootCause(),
                incident.getSolution(),
                incident.getEngineerId(),
                engineerName,
                incident.getAffectedServices(),
                incident.getTags(),
                incident.getCreatedDate(),
                incident.getUpdatedDate(),
                incident.getResolvedDate(),
                incident.getVersion());
    }
}
