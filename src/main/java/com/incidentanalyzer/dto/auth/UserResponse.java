package com.incidentanalyzer.dto.auth;

import com.incidentanalyzer.model.UserRole;
import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        String email,
        UserRole role,
        Instant createdDate) {
}
