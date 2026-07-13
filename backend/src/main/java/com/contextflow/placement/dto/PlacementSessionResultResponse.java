package com.contextflow.placement.dto;

import com.contextflow.placement.domain.CefrLevel;

import java.math.BigDecimal;

public record PlacementSessionResultResponse(
        Long sessionId,
        int itemCount,
        int answeredCount,
        int correctCount,
        BigDecimal scorePercent,
        CefrLevel estimatedLevel,
        int vocabularySizeEstimate,
        String vocabularyBand,
        int vocabularyMeasurementError
) {
}
