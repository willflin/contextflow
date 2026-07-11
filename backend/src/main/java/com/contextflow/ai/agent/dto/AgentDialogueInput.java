package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentDialogueInput(
        String contractVersion,
        AgentConversationContext conversation,
        AgentLearnerProfileContext learnerProfile,
        AgentLearningPackageContext learningPackage,
        List<AgentTargetSenseContext> targetSenses,
        List<AgentDialogueHistoryTurn> dialogueHistory,
        String userMessage,
        AgentToolAccessContext toolAccess
) {
    public AgentDialogueInput {
        targetSenses = targetSenses == null ? List.of() : List.copyOf(targetSenses);
        dialogueHistory = dialogueHistory == null ? List.of() : List.copyOf(dialogueHistory);
    }
}
