package com.contextflow.ai.agent.dto;

import com.contextflow.learning.domain.LearningEventDirection;
import com.contextflow.learning.domain.LearningEventType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record AgentUnitMention(
        AgentRole agentRole,
        String sourceField,
        Long learningUnitId,
        Long learningUnitSenseId,
        String canonicalText,
        String senseKey,
        LearningEventType eventType,
        LearningEventDirection eventDirection,
        String occurrenceText,
        Integer occurrenceIndex,
        BigDecimal confidence,
        AgentUnitMentionDecision agentDecision,
        String agentReason,
        Map<String, Object> payload
) {
    public AgentUnitMention {
        payload = payload == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }
}
