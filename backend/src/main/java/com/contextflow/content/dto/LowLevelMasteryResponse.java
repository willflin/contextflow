package com.contextflow.content.dto;

public record LowLevelMasteryResponse(
        int markedSenseCount,
        int skippedSenseCount,
        String message
) {
}
