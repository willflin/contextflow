package com.contextflow.content.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminSenseUpdateRequest(
        String partOfSpeech,
        @Size(max = 1000)
        String definitionEn,
        @Size(max = 1000)
        String definitionZh,
        String difficultyLevel,
        BigDecimal difficultyConfidence,
        BigDecimal frequencyScore,
        String frequencyBand,
        String status
) {
}
