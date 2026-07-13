package com.contextflow.user.dto;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.user.domain.UserLevelProfileEntity;

import java.time.Instant;

public record UserLevelProfileResponse(
        CefrLevel cefrLevel,
        Integer vocabularySizeEstimate,
        String vocabularyBand,
        Integer vocabularyMeasurementError,
        String dimensionScoresJson,
        String weakScenariosJson,
        String weakAbilitiesJson,
        Long lastPlacementSessionId,
        Instant updatedAt
) {
    public static UserLevelProfileResponse from(UserLevelProfileEntity entity) {
        return new UserLevelProfileResponse(
                entity.getCefrLevel(),
                entity.getVocabularySizeEstimate(),
                entity.getVocabularyBand(),
                entity.getVocabularyMeasurementError(),
                entity.getDimensionScoresJson(),
                entity.getWeakScenariosJson(),
                entity.getWeakAbilitiesJson(),
                entity.getLastPlacementSessionId(),
                entity.getUpdatedAt()
        );
    }
}
