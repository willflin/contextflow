package com.contextflow.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record AdminWordCreateRequest(
        @NotBlank
        @Size(max = 255)
        String canonicalText,
        String partOfSpeech,
        @NotBlank
        @Size(max = 1000)
        String definitionEn,
        @Size(max = 1000)
        String definitionZh,
        String difficultyLevel,
        BigDecimal frequencyScore,
        String frequencyBand,
        List<AdminWordFormRequest> forms
) {
}
