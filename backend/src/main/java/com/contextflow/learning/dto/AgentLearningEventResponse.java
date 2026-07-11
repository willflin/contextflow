package com.contextflow.learning.dto;

public record AgentLearningEventResponse(
        boolean recorded,
        Long eventId,
        Long learningUnitSenseId,
        String eventDirection
) {
}
