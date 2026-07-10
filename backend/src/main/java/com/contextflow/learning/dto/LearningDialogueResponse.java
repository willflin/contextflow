package com.contextflow.learning.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record LearningDialogueResponse(
        Long turnId,
        Long packageId,
        Integer turnIndex,
        String userMessage,
        String roleplayReply,
        String mentorFeedback,
        List<CorrectionResponse> corrections,
        String naturalExpression,
        Map<String, Object> scoringSignal,
        Instant createdAt
) {
}
