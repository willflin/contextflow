package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.config.AgentProperties;
import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.contextflow.ai.agent.dto.AgentRuntimeStatusResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class AgentRuntimeService {

    private final AgentProperties agentProperties;
    private final ObjectProvider<AgentModelClient> agentModelClientProvider;

    public AgentRuntimeService(
            AgentProperties agentProperties,
            ObjectProvider<AgentModelClient> agentModelClientProvider
    ) {
        this.agentProperties = agentProperties;
        this.agentModelClientProvider = agentModelClientProvider;
    }

    public AgentDialogueOutput generateDialogue(
            AgentDialogueInput input,
            Supplier<AgentDialogueOutput> localFallback
    ) {
        if (agentProperties.getProvider() != AgentProperties.Provider.SPRING_AI) {
            return localFallback.get();
        }

        AgentModelClient client = agentModelClientProvider.getIfAvailable();
        if (client == null) {
            return fallbackOrThrow(localFallback, new IllegalStateException("Spring AI Agent client is not available."));
        }

        try {
            return client.generateDialogue(input);
        } catch (RuntimeException exception) {
            return fallbackOrThrow(localFallback, exception);
        }
    }

    public AgentRuntimeStatusResponse status() {
        return new AgentRuntimeStatusResponse(
                agentProperties.getProvider().name(),
                agentModelClientProvider.getIfAvailable() != null,
                agentProperties.isFallbackToLocalOnError(),
                AgentDialogueContractService.CONTRACT_VERSION
        );
    }

    private AgentDialogueOutput fallbackOrThrow(
            Supplier<AgentDialogueOutput> localFallback,
            RuntimeException exception
    ) {
        if (agentProperties.isFallbackToLocalOnError()) {
            return localFallback.get();
        }
        throw exception;
    }
}
