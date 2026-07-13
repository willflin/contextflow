package com.contextflow.learning.dto;

import com.contextflow.learning.domain.LearningDialogueTurnEntity;

import java.time.Instant;

public record AdminDialogueTurnResponse(
        Long id,
        Long userId,
        Long learningPackageId,
        Integer turnIndex,
        String userMessage,
        String roleplayReply,
        String mentorFeedback,
        String corrections,
        String naturalExpression,
        String scoringSignal,
        long learningEventCount,
        Instant createdAt
) {
    public static AdminDialogueTurnResponse from(
            LearningDialogueTurnEntity entity,
            long learningEventCount
    ) {
        return new AdminDialogueTurnResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getLearningPackageId(),
                entity.getTurnIndex(),
                entity.getUserMessage(),
                entity.getRoleplayReply(),
                entity.getMentorFeedback(),
                entity.getCorrections(),
                entity.getNaturalExpression(),
                entity.getScoringSignal(),
                learningEventCount,
                entity.getCreatedAt()
        );
    }
}
