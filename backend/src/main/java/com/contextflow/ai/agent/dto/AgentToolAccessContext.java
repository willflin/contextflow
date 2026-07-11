package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentToolAccessContext(
        String wordSensesEndpoint,
        String recordEventEndpoint,
        String senseFeedbackEndpoint,
        List<String> eventPolicy
) {
    public AgentToolAccessContext {
        eventPolicy = eventPolicy == null ? List.of() : List.copyOf(eventPolicy);
    }
}
