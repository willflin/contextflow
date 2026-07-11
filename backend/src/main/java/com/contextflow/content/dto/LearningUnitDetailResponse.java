package com.contextflow.content.dto;

import java.util.List;

public record LearningUnitDetailResponse(
        Long id,
        String unitType,
        String canonicalText,
        String normalizedText,
        String languageCode,
        String status,
        List<LearningUnitFormResponse> forms,
        List<LearningUnitSenseResponse> senses
) {
}
