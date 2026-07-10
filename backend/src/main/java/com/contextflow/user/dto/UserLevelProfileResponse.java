package com.contextflow.user.dto;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.user.domain.UserLevelProfileEntity;

import java.time.Instant;

public record UserLevelProfileResponse(
        CefrLevel cefrLevel,
        String dimensionScoresJson,
        String weakScenariosJson,
        String weakAbilitiesJson,
        Long lastPlacementSessionId,
        Instant updatedAt
) {
    public static UserLevelProfileResponse from(UserLevelProfileEntity entity) {
        return new UserLevelProfileResponse(
                entity.getCefrLevel(),
                entity.getDimensionScoresJson(),
                entity.getWeakScenariosJson(),
                entity.getWeakAbilitiesJson(),
                entity.getLastPlacementSessionId(),
                entity.getUpdatedAt()
        );
    }
}
