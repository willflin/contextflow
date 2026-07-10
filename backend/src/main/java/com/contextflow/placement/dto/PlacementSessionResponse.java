package com.contextflow.placement.dto;

import com.contextflow.placement.domain.PlacementSessionStatus;

import java.util.List;

public record PlacementSessionResponse(
        Long sessionId,
        PlacementSessionStatus status,
        List<PlacementItemResponse> items
) {
}
