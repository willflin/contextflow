package com.contextflow.content.dto;

import java.math.BigDecimal;
import java.util.List;

public record LearningUnitSenseResponse(
        Long id,
        String senseKey,
        String partOfSpeech,
        String definitionEn,
        String definitionZh,
        String difficultyLevel,
        BigDecimal difficultyConfidence,
        BigDecimal frequencyScore,
        String frequencyBand,
        String status,
        List<LearningDataSourceSummaryResponse> sources
) {
}
