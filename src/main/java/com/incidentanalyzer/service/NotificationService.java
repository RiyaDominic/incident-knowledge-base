package com.incidentanalyzer.service;

import com.incidentanalyzer.dto.notification.NotificationResponse;
import com.incidentanalyzer.model.Incident;
import com.incidentanalyzer.model.Notification;
import com.incidentanalyzer.model.NotificationStatus;
import com.incidentanalyzer.model.NotificationType;
import com.incidentanalyzer.model.User;
import com.incidentanalyzer.repository.NotificationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationDispatcher notificationDispatcher;

    public void notifyIncidentCreated(Incident incident) {
        if (incident.getSeverity() == com.incidentanalyzer.model.IncidentSeverity.CRITICAL) {
            dispatchToTargets(incident, NotificationType.CRITICAL_CREATED, "Critical incident created: " + incident.getTitle());
        }
    }

    public void notifyIncidentAssigned(Incident incident) {
        if (incident.getEngineerId() != null) {
            dispatchToTargets(incident, NotificationType.ASSIGNED, "Incident assigned: " + incident.getTitle());
        }
    }

    public void notifyIncidentResolved(Incident incident) {
        dispatchToTargets(incident, NotificationType.RESOLVED, "Incident resolved: " + incident.getTitle());
    }

    public List<NotificationResponse> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId).stream().map(this::toResponse).toList();
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getIncidentId(),
                notification.getType(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getTimestamp(),
                notification.getSentAt());
    }

    private void dispatchToTargets(Incident incident, NotificationType type, String message) {
        List<User> recipients = resolveRecipients(incident, type);
        for (User recipient : recipients) {
            Notification notification = notificationRepository.save(Notification.builder()
                    .userId(recipient.getId())
                    .incidentId(incident.getIncidentId())
                    .type(type)
                    .message(message)
                    .status(NotificationStatus.PENDING)
                    .timestamp(Instant.now())
                    .build());
            notificationDispatcher.send(notification, recipient);
        }
    }

    private List<User> resolveRecipients(Incident incident, NotificationType type) {
        if (type == NotificationType.ASSIGNED && incident.getEngineerId() != null) {
            try {
                return List.of(userService.findById(incident.getEngineerId()));
            } catch (Exception ex) {
                log.warn("Skipping notification recipient lookup for incident {}: {}", incident.getIncidentId(), ex.getMessage());
            }
        }
        if (type == NotificationType.RESOLVED && incident.getEngineerId() != null) {
            List<User> recipients = new ArrayList<>(userService.findAll().stream().filter(user -> user.getRole().name().equals("ADMIN")).toList());
            try {
                User engineer = userService.findById(incident.getEngineerId());
                if (recipients.stream().noneMatch(user -> user.getId().equals(engineer.getId()))) {
                    recipients.add(engineer);
                }
            } catch (Exception ex) {
                log.warn("Skipping resolved notification recipient lookup for incident {}: {}", incident.getIncidentId(), ex.getMessage());
            }
            return recipients;
        }
        return userService.findAll().stream().filter(user -> user.getRole().name().equals("ADMIN")).toList();
    }

}
