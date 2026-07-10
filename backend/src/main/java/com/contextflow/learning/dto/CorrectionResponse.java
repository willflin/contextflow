package com.contextflow.learning.dto;

public record CorrectionResponse(
        String original,
        String suggestion,
        String reason
) {
}
