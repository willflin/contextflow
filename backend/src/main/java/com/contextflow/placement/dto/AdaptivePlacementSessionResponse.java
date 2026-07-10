package com.contextflow.placement.dto;

import com.contextflow.placement.domain.PlacementSessionStatus;

public record AdaptivePlacementSessionResponse(
        Long sessionId,
        PlacementSessionStatus status,
        int answeredCount,
        int maxItemCount,
        int currentDifficultyScore,
        PlacementTestItemResponse currentItem
) {
}
