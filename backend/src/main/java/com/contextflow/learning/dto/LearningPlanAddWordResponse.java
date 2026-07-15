package com.contextflow.learning.dto;

public record LearningPlanAddWordResponse(
        Long learningUnitId,
        int addedSenseCount,
        int skippedOutOfLevelCount,
        String message
) {
}
