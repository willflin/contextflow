package com.contextflow.placement.dto;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.domain.PlacementItemType;

import java.time.Instant;

public record PlacementItemResponse(
        Long id,
        PlacementItemType itemType,
        CefrLevel cefrLevel,
        String scenarioTag,
        String targetSkill,
        PlacementItemStatus status,
        String contentJson,
        Instant createdAt
) {
    public static PlacementItemResponse from(PlacementItemEntity entity) {
        return new PlacementItemResponse(
                entity.getId(),
                entity.getItemType(),
                entity.getCefrLevel(),
                entity.getScenarioTag(),
                entity.getTargetSkill(),
                entity.getStatus(),
                entity.getContentJson(),
                entity.getCreatedAt()
        );
    }
}
