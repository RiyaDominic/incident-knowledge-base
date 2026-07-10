package com.incidentanalyzer.repository;

import com.incidentanalyzer.dto.incident.IncidentSearchResponse;
import com.incidentanalyzer.dto.incident.IncidentSummaryResponse;
import com.incidentanalyzer.model.Incident;
import com.incidentanalyzer.model.IncidentSeverity;
import com.incidentanalyzer.model.IncidentStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class IncidentRepositoryImpl implements IncidentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<IncidentSearchResponse> searchIncidents(String query, String applicationName, IncidentSeverity severity, IncidentStatus status, String tag, Pageable pageable) {
        Query baseQuery = buildQuery(query, applicationName, severity, status, tag);
        long total = mongoTemplate.count(Query.of(baseQuery).limit(-1).skip(-1), Incident.class);
        List<Incident> incidents = mongoTemplate.find(baseQuery.with(pageable), Incident.class);
        List<IncidentSearchResponse> responses = incidents.stream()
                .map(incident -> new IncidentSearchResponse(toSummary(incident), score(query, incident), matchReason(query, applicationName, severity, status, tag, incident)))
                .toList();
        return new PageImpl<>(responses, pageable, total);
    }

    private Query buildQuery(String query, String applicationName, IncidentSeverity severity, IncidentStatus status, String tag) {
        Query mongoQuery;
        if (StringUtils.hasText(query)) {
            mongoQuery = TextQuery.queryText(TextCriteria.forDefaultLanguage().matchingAny(query)).sortByScore();
        } else {
            mongoQuery = new Query();
            mongoQuery = mongoQuery.with(Sort.by(Sort.Direction.DESC, "createdDate"));
        }

        List<Criteria> criteria = new ArrayList<>();
        if (StringUtils.hasText(applicationName)) {
            criteria.add(Criteria.where("applicationName").regex(Pattern.compile("^" + Pattern.quote(applicationName) + "$", Pattern.CASE_INSENSITIVE)));
        }
        if (severity != null) {
            criteria.add(Criteria.where("severity").is(severity));
        }
        if (status != null) {
            criteria.add(Criteria.where("status").is(status));
        }
        if (StringUtils.hasText(tag)) {
            criteria.add(Criteria.where("tags").regex(Pattern.compile("^" + Pattern.quote(tag) + "$", Pattern.CASE_INSENSITIVE)));
        }
        if (!criteria.isEmpty()) {
            mongoQuery.addCriteria(new Criteria().andOperator(criteria.toArray(Criteria[]::new)));
        }
        return mongoQuery;
    }

    private IncidentSummaryResponse toSummary(Incident incident) {
        return new IncidentSummaryResponse(
                incident.getIncidentId(),
                incident.getTitle(),
                incident.getApplicationName(),
                incident.getSeverity(),
                incident.getPriority(),
                incident.getStatus(),
                null,
                incident.getTags(),
                incident.getCreatedDate());
    }

    private double score(String query, Incident incident) {
        if (!StringUtils.hasText(query)) {
            return 0.0d;
        }
        String normalized = query.toLowerCase();
        double score = 0.0d;
        score += weightMatch(incident.getTitle(), normalized, 3.0d);
        score += weightMatch(incident.getApplicationName(), normalized, 2.0d);
        score += weightMatch(incident.getErrorMessage(), normalized, 4.0d);
        score += weightMatch(incident.getRootCause(), normalized, 3.0d);
        score += weightMatch(incident.getSolution(), normalized, 2.0d);
        if (incident.getTags() != null) {
            score += incident.getTags().stream().filter(Objects::nonNull).map(String::toLowerCase).filter(tag -> normalized.contains(tag)).count();
        }
        return score;
    }

    private double weightMatch(String value, String normalizedQuery, double weight) {
        if (value == null) {
            return 0.0d;
        }
        return value.toLowerCase().contains(normalizedQuery) ? weight : 0.0d;
    }

    private String matchReason(String query, String applicationName, IncidentSeverity severity, IncidentStatus status, String tag, Incident incident) {
        List<String> reasons = new ArrayList<>();
        if (StringUtils.hasText(query) && containsAny(incident, query)) {
            reasons.add("text match");
        }
        if (StringUtils.hasText(applicationName) && applicationName.equalsIgnoreCase(incident.getApplicationName())) {
            reasons.add("application");
        }
        if (severity != null && severity == incident.getSeverity()) {
            reasons.add("severity");
        }
        if (status != null && status == incident.getStatus()) {
            reasons.add("status");
        }
        if (StringUtils.hasText(tag) && incident.getTags() != null && incident.getTags().stream().anyMatch(value -> value.equalsIgnoreCase(tag))) {
            reasons.add("tag");
        }
        return reasons.isEmpty() ? "relevance" : String.join(", ", reasons);
    }

    private boolean containsAny(Incident incident, String query) {
        String normalized = query.toLowerCase();
        return matches(incident.getTitle(), normalized)
                || matches(incident.getApplicationName(), normalized)
                || matches(incident.getErrorMessage(), normalized)
                || matches(incident.getRootCause(), normalized)
                || matches(incident.getSolution(), normalized)
                || (incident.getTags() != null && incident.getTags().stream().filter(Objects::nonNull).map(String::toLowerCase).anyMatch(normalized::contains));
    }

    private boolean matches(String value, String normalizedQuery) {
        return value != null && value.toLowerCase().contains(normalizedQuery);
    }
}
