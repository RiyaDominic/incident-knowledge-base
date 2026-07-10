package com.incidentanalyzer.repository;

import com.incidentanalyzer.model.IncidentLog;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IncidentLogRepository extends MongoRepository<IncidentLog, String> {

    Optional<IncidentLog> findByIncidentId(String incidentId);
}
