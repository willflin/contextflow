package com.contextflow.ai.agent.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record AgentLearnerProfileContext(
        String cefrLevel,
        Map<String, Object> dimensionScores,
        List<String> weakScenarios,
        List<String> weakAbilities
) {
    public AgentLearnerProfileContext {
        dimensionScores = dimensionScores == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(dimensionScores));
        weakScenarios = weakScenarios == null ? List.of() : List.copyOf(weakScenarios);
        weakAbilities = weakAbilities == null ? List.of() : List.copyOf(weakAbilities);
    }
}
