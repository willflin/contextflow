package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.dto.AgentContractValidationResult;
import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnBean(ChatModel.class)
public class SpringAiAgentModelClient implements AgentModelClient {

    private static final String SYSTEM_PROMPT = """
            You are ContextFlow's dual-agent runtime.
            Return only valid JSON matching the agent-dialogue.v1 AgentDialogueOutput contract.
            Do not wrap the response in markdown.
            Roleplay reply must be natural English for the scenario.
            Mentor feedback can use concise Chinese with English examples.
            unitMentions must include learningUnitId and learningUnitSenseId only when the exact sense is known.
            If the exact sense is unknown, omit that mention or mark it as a non-recordable decision.
            """;

    private final ChatModel chatModel;
    private final AgentDialogueContractService contractService;
    private final ObjectMapper objectMapper;

    public SpringAiAgentModelClient(
            ChatModel chatModel,
            AgentDialogueContractService contractService,
            ObjectMapper objectMapper
    ) {
        this.chatModel = chatModel;
        this.contractService = contractService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AgentDialogueOutput generateDialogue(AgentDialogueInput input) {
        String inputJson = writeJson(input);
        ChatResponse response = chatModel.call(new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(inputJson)
        )));
        String content = response.getResult().getOutput().getText();
        AgentDialogueOutput output = readOutput(extractJson(content));
        AgentContractValidationResult validation = contractService.validateOutput(output);
        if (!validation.accepted()) {
            throw new IllegalStateException("Spring AI Agent output failed contract validation: " + validation.errors());
        }
        return output;
    }

    private String writeJson(AgentDialogueInput input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize Agent input.", exception);
        }
    }

    private AgentDialogueOutput readOutput(String content) {
        try {
            return objectMapper.readValue(content, AgentDialogueOutput.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse Agent output JSON.", exception);
        }
    }

    private String extractJson(String content) {
        if (content == null) {
            throw new IllegalStateException("Spring AI Agent returned empty content.");
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }
}
