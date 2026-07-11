package com.contextflow.ai.agent.dto;

import java.math.BigDecimal;

public record AgentTargetSenseContext(
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
        BigDecimal frequencyScore,
        BigDecimal reviewPriorityScore,
        String scenarioCode
) {
}
