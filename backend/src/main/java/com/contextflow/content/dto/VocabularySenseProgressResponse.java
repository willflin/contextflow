package com.contextflow.content.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VocabularySenseProgressResponse(
        Long senseId,
        String senseKey,
        String partOfSpeech,
        String definitionEn,
        String definitionZh,
        String difficultyLevel,
        String frequencyBand,
        boolean inLearningPlan,
        boolean learned,
        String masteryLevel,
        BigDecimal masteryScore,
        Integer exposureCount,
        Integer attemptCount,
        Instant lastSeenAt,
        Instant nextReviewAt
) {
}
