package com.contextflow.placement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlacementAnswerSubmission(
        @NotNull Long itemId,
        @NotNull @Min(0) Integer selectedOptionIndex
) {
}
