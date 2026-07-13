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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpringAiAgentModelClient implements AgentModelClient {

    private static final String SYSTEM_PROMPT = """
            You are ContextFlow's dual-agent English learning runtime.
            You must behave as two agents in one response:
            1. Roleplay Agent: continue the scenario in natural, level-appropriate English.
            2. Mentor Agent: give concise Chinese feedback with English examples when useful.

            Return only valid JSON matching the agent-dialogue.v1 AgentDialogueOutput contract.
            Do not wrap the response in markdown. Do not add explanations outside JSON.

            Required JSON shape:
            {
              "contractVersion": "agent-dialogue.v1",
              "reply": "...",
              "feedback": "...",
              "corrections": [{"original":"...","suggestion":"...","reason":"..."}],
              "naturalExpression": "...",
              "unitMentions": [],
              "scoringSignal": {"clarityScore":0,"naturalnessScore":0,"needsReview":false}
            }

            Event policy:
            - Use only learningUnitId and learningUnitSenseId values that appear in input.targetSenses or tool context.
            - Do not invent ids.
            - Record learner output only when the exact sense is known: eventType UNIT_ATTEMPTED, eventDirection LEARNER_OUTPUT, sourceField userMessage.
            - Record Roleplay/Mentor output exposed to the learner as LEARNER_INPUT, never UNIT_ATTEMPTED.
            - For Roleplay reply use eventType UNIT_EXPOSED and sourceField reply.
            - For Mentor correction suggestions use eventType UNIT_CORRECTED and sourceField correctionSuggestion.
            - For natural expression suggestions use eventType UNIT_RECOMMENDED and sourceField naturalExpression.
            - If the exact sense is unknown, omit the mention.
            - If user text is nonsense, unrecognizable, or completely wrong usage, do not create a recordable mention; explain it in feedback.
            """;

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final AgentDialogueContractService contractService;
    private final ObjectMapper objectMapper;

    public SpringAiAgentModelClient(
            ObjectProvider<ChatModel> chatModelProvider,
            AgentDialogueContractService contractService,
            ObjectMapper objectMapper
    ) {
        this.chatModelProvider = chatModelProvider;
        this.contractService = contractService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isAvailable() {
        return chatModelProvider.getIfAvailable() != null;
    }

    @Override
    public AgentDialogueOutput generateDialogue(AgentDialogueInput input) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            throw new IllegalStateException("Spring AI ChatModel is not available.");
        }
        String inputJson = writeJson(input);
        ChatResponse response = chatModel.call(new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userPrompt(inputJson))
        )));
        String content = response.getResult().getOutput().getText();
        AgentDialogueOutput output = readOutput(extractJson(content));
        AgentContractValidationResult validation = contractService.validateOutput(output);
        if (!validation.accepted()) {
            throw new IllegalStateException("Spring AI Agent output failed contract validation: " + validation.errors());
        }
        return output;
    }

    private String userPrompt(String inputJson) {
        return """
                Build the next dual-agent dialogue turn from this AgentDialogueInput JSON.
                Keep the Roleplay reply short enough for one conversational turn.
                Keep Mentor feedback concise and actionable.
                Prefer target senses when they naturally fit the scenario; do not force unrelated words.

                AgentDialogueInput:
                """ + inputJson;
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
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        throw new IllegalStateException("Spring AI Agent did not return a JSON object.");
    }
}
