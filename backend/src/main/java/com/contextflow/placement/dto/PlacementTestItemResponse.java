package com.contextflow.placement.dto;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemGradingType;
import com.contextflow.placement.domain.PlacementItemType;

public record PlacementTestItemResponse(
        Long id,
        PlacementItemType itemType,
        CefrLevel cefrLevel,
        int difficultyScore,
        String scenarioTag,
        String targetSkill,
        String abilityDimension,
        PlacementItemGradingType gradingType,
        String contentJson
) {
    public static PlacementTestItemResponse from(PlacementItemEntity entity, String publicContentJson) {
        return new PlacementTestItemResponse(
                entity.getId(),
                entity.getItemType(),
                entity.getCefrLevel(),
                entity.getDifficultyScore(),
                entity.getScenarioTag(),
                entity.getTargetSkill(),
                entity.getAbilityDimension(),
                entity.getGradingType(),
                publicContentJson
        );
    }
}
