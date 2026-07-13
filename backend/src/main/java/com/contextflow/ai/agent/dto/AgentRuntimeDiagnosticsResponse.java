package com.contextflow.ai.agent.dto;

import java.util.List;

public record AgentRuntimeDiagnosticsResponse(
        String springAiModelChat,
        boolean deepSeekApiKeyConfigured,
        boolean deepSeekChatApiKeyConfigured,
        String deepSeekBaseUrl,
        String deepSeekChatBaseUrl,
        String deepSeekModel,
        String deepSeekChatEnabled,
        boolean deepSeekAutoConfigurationClassPresent,
        boolean deepSeekApiClassPresent,
        List<String> chatModelBeanNames,
        List<String> agentModelClientBeanNames,
        List<String> activeProfiles
) {
    public AgentRuntimeDiagnosticsResponse {
        chatModelBeanNames = chatModelBeanNames == null ? List.of() : List.copyOf(chatModelBeanNames);
        agentModelClientBeanNames = agentModelClientBeanNames == null ? List.of() : List.copyOf(agentModelClientBeanNames);
        activeProfiles = activeProfiles == null ? List.of() : List.copyOf(activeProfiles);
    }
}
