package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentOutputSanitizationResult(
        AgentDialogueOutput output,
        List<String> droppedUnitMentionErrors
) {
    public AgentOutputSanitizationResult {
        droppedUnitMentionErrors = droppedUnitMentionErrors == null ? List.of() : List.copyOf(droppedUnitMentionErrors);
    }
}
