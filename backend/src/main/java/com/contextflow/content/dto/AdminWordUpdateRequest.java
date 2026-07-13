package com.contextflow.content.dto;

import jakarta.validation.constraints.Size;

public record AdminWordUpdateRequest(
        @Size(max = 255)
        String canonicalText,
        String status
) {
}
