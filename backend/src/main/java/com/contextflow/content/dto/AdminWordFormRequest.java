package com.contextflow.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminWordFormRequest(
        @NotBlank
        @Size(max = 255)
        String formText,
        String formType
) {
}
