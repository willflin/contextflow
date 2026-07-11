package com.contextflow.ai.agent.dto;

import java.util.List;
import java.util.Map;

public record AgentDialogueOutput(
        String contractVersion,
        String reply,
        String feedback,
        List<AgentCorrection> corrections,
        String naturalExpression,
        List<AgentUnitMention> unitMentions,
        Map<String, Object> scoringSignal
) {
}
