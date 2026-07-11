package com.contextflow.content.dto;

public record LearningDataSourceSummaryResponse(
        String sourceName,
        String sourceVersion,
        String sourceUrl,
        String licenseName,
        String attribution,
        String attributeType,
        String sourceRecordId
) {
}
