package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentContractValidationResult(
        boolean accepted,
        List<String> errors
) {
    public AgentContractValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
