package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.config.AgentProperties;
import com.contextflow.ai.agent.dto.AgentContractValidationResult;
import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.contextflow.ai.agent.dto.AgentRuntimeDiagnosticsResponse;
import com.contextflow.ai.agent.dto.AgentRuntimeProbeResponse;
import com.contextflow.ai.agent.dto.AgentRuntimeStatusResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Service
public class AgentRuntimeService {

    private final AgentProperties agentProperties;
    private final ObjectProvider<AgentModelClient> agentModelClientProvider;
    private final AgentDialogueContractService contractService;
    private final Environment environment;
    private final ApplicationContext applicationContext;

    public AgentRuntimeService(
            AgentProperties agentProperties,
            ObjectProvider<AgentModelClient> agentModelClientProvider,
            AgentDialogueContractService contractService,
            Environment environment,
            ApplicationContext applicationContext
    ) {
        this.agentProperties = agentProperties;
        this.agentModelClientProvider = agentModelClientProvider;
        this.contractService = contractService;
        this.environment = environment;
        this.applicationContext = applicationContext;
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

    public AgentRuntimeProbeResponse probeDialogue(
            AgentDialogueInput input,
            Supplier<AgentDialogueOutput> localFallback
    ) {
        long startedAt = System.nanoTime();
        AgentModelClient client = agentModelClientProvider.getIfAvailable();
        boolean clientAvailable = isClientAvailable(client);

        if (agentProperties.getProvider() != AgentProperties.Provider.SPRING_AI) {
            return probeFallback(
                    localFallback,
                    startedAt,
                    false,
                    false,
                    clientAvailable,
                    null
            );
        }

        if (!clientAvailable) {
            RuntimeException exception = new IllegalStateException(client == null
                    ? "Spring AI Agent client is not available."
                    : "Spring AI ChatModel is not available.");
            if (agentProperties.isFallbackToLocalOnError()) {
                return probeFallback(localFallback, startedAt, false, true, false, exception);
            }
            return probeFailure(startedAt, false, false, false, exception);
        }

        try {
            AgentDialogueOutput output = client.generateDialogue(input);
            AgentContractValidationResult validation = contractService.validateOutput(output);
            return probeResponse(
                    startedAt,
                    true,
                    false,
                    clientAvailable,
                    validation.accepted(),
                    validation.errors(),
                    null,
                    output
            );
        } catch (RuntimeException exception) {
            if (agentProperties.isFallbackToLocalOnError()) {
                return probeFallback(localFallback, startedAt, true, true, clientAvailable, exception);
            }
            return probeFailure(startedAt, true, false, clientAvailable, exception);
        }
    }

    public AgentRuntimeStatusResponse status() {
        AgentModelClient client = agentModelClientProvider.getIfAvailable();
        return new AgentRuntimeStatusResponse(
                agentProperties.getProvider().name(),
                isClientAvailable(client),
                agentProperties.isFallbackToLocalOnError(),
                AgentDialogueContractService.CONTRACT_VERSION,
                diagnostics()
        );
    }

    public AgentRuntimeDiagnosticsResponse diagnostics() {
        return new AgentRuntimeDiagnosticsResponse(
                environment.getProperty("spring.ai.model.chat"),
                hasText(environment.getProperty("spring.ai.deepseek.api-key")),
                hasText(environment.getProperty("spring.ai.deepseek.chat.api-key")),
                environment.getProperty("spring.ai.deepseek.base-url"),
                environment.getProperty("spring.ai.deepseek.chat.base-url"),
                environment.getProperty("spring.ai.deepseek.chat.options.model"),
                environment.getProperty("spring.ai.deepseek.chat.enabled"),
                isPresent("org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration"),
                isPresent("org.springframework.ai.deepseek.api.DeepSeekApi"),
                List.of(applicationContext.getBeanNamesForType(ChatModel.class)),
                List.of(applicationContext.getBeanNamesForType(AgentModelClient.class)),
                Arrays.asList(environment.getActiveProfiles())
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

    private AgentRuntimeProbeResponse probeFallback(
            Supplier<AgentDialogueOutput> localFallback,
            long startedAt,
            boolean modelAttempted,
            boolean fallbackUsed,
            boolean clientAvailable,
            RuntimeException exception
    ) {
        AgentDialogueOutput output = localFallback.get();
        AgentContractValidationResult validation = contractService.validateOutput(output);
        return probeResponse(
                startedAt,
                modelAttempted,
                fallbackUsed,
                clientAvailable,
                validation.accepted(),
                validation.errors(),
                compactError(exception),
                output
        );
    }

    private AgentRuntimeProbeResponse probeFailure(
            long startedAt,
            boolean modelAttempted,
            boolean fallbackUsed,
            boolean clientAvailable,
            RuntimeException exception
    ) {
        return probeResponse(
                startedAt,
                modelAttempted,
                fallbackUsed,
                clientAvailable,
                false,
                List.of(compactError(exception)),
                compactError(exception),
                null
        );
    }

    private AgentRuntimeProbeResponse probeResponse(
            long startedAt,
            boolean modelAttempted,
            boolean fallbackUsed,
            boolean clientAvailable,
            boolean accepted,
            List<String> errors,
            String errorMessage,
            AgentDialogueOutput output
    ) {
        return new AgentRuntimeProbeResponse(
                agentProperties.getProvider().name(),
                clientAvailable,
                agentProperties.isFallbackToLocalOnError(),
                AgentDialogueContractService.CONTRACT_VERSION,
                modelAttempted,
                fallbackUsed,
                accepted,
                errors,
                elapsedMs(startedAt),
                errorMessage,
                output,
                diagnostics()
        );
    }

    private Long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String compactError(RuntimeException exception) {
        if (exception == null) {
            return null;
        }
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private boolean isPresent(String className) {
        return ClassUtils.isPresent(className, getClass().getClassLoader());
    }

    private boolean isClientAvailable(AgentModelClient client) {
        return client != null && client.isAvailable();
    }
}
