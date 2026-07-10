package com.incidentanalyzer.dto.comment;

import java.time.Instant;

public record CommentResponse(
        String id,
        String incidentId,
        String userId,
        String userName,
        String message,
        Instant timestamp) {
}
