package com.contextflow.learning.dto;

import java.time.Instant;

public record AgentSenseFeedbackResponse(
        Long id,
        String status,
        Instant createdAt
) {
}
