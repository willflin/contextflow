package com.contextflow.learning.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminDialogueTurnUpdateRequest(
        @NotBlank String userMessage,
        @NotBlank String roleplayReply,
        @NotBlank String mentorFeedback,
        @NotBlank String corrections,
        @NotBlank String naturalExpression,
        @NotBlank String scoringSignal
) {
}
