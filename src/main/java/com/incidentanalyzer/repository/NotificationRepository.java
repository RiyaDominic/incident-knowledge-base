package com.incidentanalyzer.repository;

import com.incidentanalyzer.model.Notification;
import com.incidentanalyzer.model.NotificationStatus;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByTimestampDesc(String userId);

    List<Notification> findByIncidentId(String incidentId);

    List<Notification> findByStatus(NotificationStatus status);
}
