package com.contextflow.placement.dto;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemGradingType;
import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.domain.PlacementItemType;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminPlacementItemResponse(
        Long id,
        PlacementItemType itemType,
        CefrLevel cefrLevel,
        Integer difficultyScore,
        Integer frequencyRank,
        String frequencyBand,
        BigDecimal itemDiscrimination,
        BigDecimal guessingFactor,
        String scenarioTag,
        String targetSkill,
        String abilityDimension,
        PlacementItemStatus status,
        PlacementItemGradingType gradingType,
        String contentJson,
        Instant createdAt
) {
    public static AdminPlacementItemResponse from(PlacementItemEntity entity) {
        return new AdminPlacementItemResponse(
                entity.getId(),
                entity.getItemType(),
                entity.getCefrLevel(),
                entity.getDifficultyScore(),
                entity.getFrequencyRank(),
                entity.getFrequencyBand(),
                entity.getItemDiscrimination(),
                entity.getGuessingFactor(),
                entity.getScenarioTag(),
                entity.getTargetSkill(),
                entity.getAbilityDimension(),
                entity.getStatus(),
                entity.getGradingType(),
                entity.getContentJson(),
                entity.getCreatedAt()
        );
    }
}
