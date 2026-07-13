package com.contextflow.ai.agent.dto;

public record AgentRuntimeStatusResponse(
        String provider,
        boolean springAiClientAvailable,
        boolean fallbackToLocalOnError,
        String contractVersion,
        AgentRuntimeDiagnosticsResponse diagnostics
) {
}
