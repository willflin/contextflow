package com.contextflow.learning.dto;

import com.contextflow.learning.domain.LearningEventDirection;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record AgentLearningEventRequest(
        @NotNull Long learningUnitId,
        Long learningUnitSenseId,
        @NotNull LearningEventType eventType,
        @NotNull LearningEventDirection eventDirection,
        @NotNull LearningEventSourceType sourceType,
        @NotNull Long sourceId,
        @NotBlank String sourceText,
        @NotBlank String occurrenceText,
        String agentDecision,
        String agentReason,
        Map<String, Object> payload
) {
}
