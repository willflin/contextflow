package com.contextflow.learning.dto;

import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageStatus;

import java.time.Instant;

public record LearningPackageResponse(
        Long id,
        LearningPackageStatus status,
        String title,
        String scenarioName,
        String contentJson,
        Instant assignedAt
) {
    public static LearningPackageResponse from(LearningPackageEntity entity, String scenarioName) {
        return new LearningPackageResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getTitle(),
                scenarioName,
                entity.getContent(),
                entity.getAssignedAt()
        );
    }
}
