package com.contextflow.review.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReviewPlanItemResponse(
        String pool,
        Long learningUnitId,
        Long learningUnitSenseId,
        String canonicalText,
        String senseKey,
        String partOfSpeech,
        String definitionEn,
        String definitionZh,
        String difficultyLevel,
        String frequencyBand,
        BigDecimal score,
        String scenarioCode,
        List<String> reasons
) {
}
