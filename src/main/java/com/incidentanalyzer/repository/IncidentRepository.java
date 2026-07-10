package com.incidentanalyzer.repository;

import com.incidentanalyzer.model.Incident;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IncidentRepository extends MongoRepository<Incident, String>, IncidentRepositoryCustom {

    Optional<Incident> findByIncidentId(String incidentId);

    boolean existsByIncidentId(String incidentId);

    long countByStatus(com.incidentanalyzer.model.IncidentStatus status);

    long countBySeverity(com.incidentanalyzer.model.IncidentSeverity severity);

    Page<Incident> findByStatusOrderByCreatedDateDesc(com.incidentanalyzer.model.IncidentStatus status, Pageable pageable);

    List<Incident> findTop10ByStatusOrderByCreatedDateDesc(com.incidentanalyzer.model.IncidentStatus status);
}
