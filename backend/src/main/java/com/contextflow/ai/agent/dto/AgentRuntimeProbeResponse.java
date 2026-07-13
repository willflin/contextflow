package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentRuntimeProbeResponse(
        String provider,
        boolean springAiClientAvailable,
        boolean fallbackToLocalOnError,
        String contractVersion,
        boolean modelAttempted,
        boolean fallbackUsed,
        boolean accepted,
        List<String> errors,
        Long elapsedMs,
        String errorMessage,
        String modelRawContent,
        AgentDialogueOutput modelOutput,
        List<String> modelValidationErrors,
        AgentDialogueOutput output,
        AgentRuntimeDiagnosticsResponse diagnostics
) {
    public AgentRuntimeProbeResponse {
        errors = errors == null ? List.of() : List.copyOf(errors);
        modelValidationErrors = modelValidationErrors == null ? List.of() : List.copyOf(modelValidationErrors);
    }
}
