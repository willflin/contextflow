package com.contextflow.placement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitAdaptivePlacementAnswerRequest(
        @NotNull Long itemId,
        @Min(0) Integer selectedOptionIndex,
        @Size(max = 500) String textAnswer
) {
}
