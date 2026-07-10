package com.incidentanalyzer.model;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("incidents")
public class Incident {

    @Id
    private String incidentId;

    @TextIndexed(weight = 5)
    private String title;

    @TextIndexed(weight = 4)
    private String applicationName;

    private String environment;

    private IncidentSeverity severity;

    private IncidentPriority priority;

    private IncidentStatus status;

    @TextIndexed(weight = 5)
    private String errorMessage;

    private String logRefId;

    private String stackTrace;

    @TextIndexed(weight = 3)
    private String rootCause;

    @TextIndexed(weight = 3)
    private String solution;

    private String engineerId;

    private List<String> affectedServices;

    @TextIndexed(weight = 2)
    private List<String> tags;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant updatedDate;

    private Instant resolvedDate;

    @Version
    private Long version;
}
