package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.dto.AgentDialogueOutput;

import java.util.List;

public class AgentModelResponseException extends RuntimeException {

    private final String rawContent;
    private final AgentDialogueOutput output;
    private final List<String> validationErrors;

    public AgentModelResponseException(
            String message,
            String rawContent,
            AgentDialogueOutput output,
            List<String> validationErrors
    ) {
        super(message);
        this.rawContent = rawContent;
        this.output = output;
        this.validationErrors = validationErrors == null ? List.of() : List.copyOf(validationErrors);
    }

    public String rawContent() {
        return rawContent;
    }

    public AgentDialogueOutput output() {
        return output;
    }

    public List<String> validationErrors() {
        return validationErrors;
    }
}
