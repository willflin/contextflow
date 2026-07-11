package com.contextflow.learning.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record AgentSenseFeedbackRequest(
        Long learningUnitId,
        @NotBlank String surfaceText,
        String sourceType,
        Long sourceId,
        String sourceText,
        String suggestedDefinitionEn,
        String suggestedDefinitionZh,
        String agentReason,
        Map<String, Object> payload
) {
}
