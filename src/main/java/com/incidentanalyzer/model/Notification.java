package com.incidentanalyzer.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("notifications")
public class Notification {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String incidentId;

    private NotificationType type;

    private String message;

    private NotificationStatus status;

    @CreatedDate
    private Instant timestamp;

    private Instant sentAt;
}
