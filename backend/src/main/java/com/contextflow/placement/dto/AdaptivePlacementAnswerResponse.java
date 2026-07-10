package com.contextflow.placement.dto;

public record AdaptivePlacementAnswerResponse(
        Long sessionId,
        Long itemId,
        boolean correct,
        int answeredCount,
        int correctCount,
        int currentDifficultyScore,
        boolean finished,
        PlacementTestItemResponse nextItem,
        PlacementSessionResultResponse result
) {
}
