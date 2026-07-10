package com.incidentanalyzer.dto.notification;

import com.incidentanalyzer.model.NotificationStatus;
import com.incidentanalyzer.model.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        String id,
        String userId,
        String incidentId,
        NotificationType type,
        String message,
        NotificationStatus status,
        Instant timestamp,
        Instant sentAt) {
}
