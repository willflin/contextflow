package com.contextflow.ai.agent.dto;

public record AgentCorrection(
        String original,
        String suggestion,
        String reason
) {
}
