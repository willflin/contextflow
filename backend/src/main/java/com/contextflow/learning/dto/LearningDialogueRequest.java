package com.contextflow.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LearningDialogueRequest(
        @NotBlank
        @Size(max = 1000)
        String message
) {
}
