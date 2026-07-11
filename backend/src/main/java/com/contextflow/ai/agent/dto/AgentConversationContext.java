package com.contextflow.ai.agent.dto;

import java.time.Instant;

public record AgentConversationContext(
        Long learnerId,
        Long packageId,
        Integer nextTurnIndex,
        String locale,
        Instant requestedAt
) {
}
