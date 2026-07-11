package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentDialogueContractSampleResponse(
        String contractVersion,
        AgentDialogueInput input,
        AgentDialogueOutput output,
        AgentContractValidationResult outputValidation,
        List<String> toolEndpoints
) {
    public AgentDialogueContractSampleResponse {
        toolEndpoints = toolEndpoints == null ? List.of() : List.copyOf(toolEndpoints);
    }
}
