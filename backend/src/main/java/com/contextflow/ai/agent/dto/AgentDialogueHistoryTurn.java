package com.contextflow.ai.agent.dto;

import java.time.Instant;

public record AgentDialogueHistoryTurn(
        Long turnId,
        Integer turnIndex,
        String userMessage,
        String reply,
        String feedback,
        Instant createdAt
) {
}
