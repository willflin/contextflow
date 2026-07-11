package com.contextflow.content.dto;

public record LearningUnitFormResponse(
        Long id,
        String formText,
        String normalizedForm,
        String formType
) {
}
