package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.dto.AgentContractValidationResult;
import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.contextflow.ai.agent.dto.AgentOutputSanitizationResult;
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
            1. Roleplay Agent: continue the learning task in natural, level-appropriate English.
            2. Mentor Agent: give concise Chinese feedback with English examples when useful.

            Learning task policy:
            - learningPackage.taskGoal is the task objective the learner is trying to complete.
            - learningPackage.taskInstructionLanguage tells whether the task goal is shown in Chinese or English.
            - learningPackage.expectedLearnerAction describes what the learner should try to do next.
            - learningPackage.taskRegister describes the social/register context, such as daily service, business service, or formal sensitive.
            - learningPackage.registerGuidance tells whether colloquial, simplified, concise, formal, or business-like wording is appropriate.
            - learningPackage.taskFacts contains all fixed facts the learner may use.
            - learningPackage.taskConstraints contains hard boundaries for what the Roleplay Agent may ask.
            - learningPackage.roleplayPersona is the only role the Roleplay Agent may play.
            - learningPackage.learnerRole is the learner's role. Never speak as this role.
            - learningPackage.openingLine is already visible to the learner and is also included in dialogueHistory.
            - scenarioCode and scenarioName are only category or seed labels; do not treat them as the whole task.
            - If target senses do not fit the current task, continue the task naturally and omit unrelated unitMentions.

            Target-sense discipline:
            - The Roleplay Agent must stay tightly focused on the targetSenses and the learning task.
            - Each Roleplay reply should either naturally use one or two relevant target senses, or ask a question that helps the learner use target senses or task-related language next.
            - A small amount of free expansion is allowed only when it has learning value, such as introducing a useful higher-level expression or task-related word.
            - Do not waste turns on low-learning-value service filler: "Let me check", "Could you spell that?", asking for spelling, reservation codes, repeated ID checks, waiting, system lookup, or administrative details unless the target senses explicitly require them.
            - Do not ask for a name, spelling, reservation code, address, phone number, or document details just to simulate service procedure. Prefer language-learning prompts about intent, choices, descriptions, reasons, preferences, or clarification.
            - Never ask the learner to invent facts that are not in learningPackage.taskFacts.
            - Follow-up questions must stay within learningPackage.taskFacts and learningPackage.taskConstraints.
            - If the next realistic service step would require unknown facts, skip that step and ask a learning-focused question within the known facts instead.

            Immersion and role continuity rules:
            - The Roleplay Agent must never speak for the learner.
            - The Roleplay Agent must never invent learner facts such as reservation names, account types, documents, addresses, or purchase details unless the learner already stated them.
            - The Roleplay Agent must not repeat learningPackage.openingLine or any recent Roleplay reply in dialogueHistory.
            - If userMessage is only "?", unclear, or malformed, the Roleplay Agent should ask an in-role clarification question.
            - Mentor Agent may explain learner wording problems, but Roleplay Agent must stay in character.
            - Mentor feedback must follow taskRegister/registerGuidance. Do not always demand longer or more formal sentences. In daily spoken tasks, short natural answers are acceptable if clear.

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
              "scoringSignal": {"clarityScore":0,"naturalnessScore":0,"needsReview":false,"taskComplete":false,"completionReason":""}
            }

            unitMentions rules:
            - unitMentions is only for events that the backend may record.
            - Do not use unitMentions to explain skipped, unmatched, absent, or uncertain target senses.
            - If a target sense does not actually appear in userMessage, reply, correctionSuggestion, or naturalExpression, omit it.
            - If unitMentions is not empty, every object must include these exact fields:
              agentRole, sourceField, learningUnitId, learningUnitSenseId, canonicalText, senseKey,
              eventType, eventDirection, occurrenceText, occurrenceIndex, confidence, agentDecision, agentReason, payload.
            - Required enum values:
              agentRole: ROLEPLAY or MENTOR.
              eventType: UNIT_ATTEMPTED, UNIT_EXPOSED, UNIT_CORRECTED, or UNIT_RECOMMENDED.
              eventDirection: LEARNER_OUTPUT or LEARNER_INPUT.
              agentDecision: RECORD_EVENT or RECORD_SPELLING_OR_FORM_ERROR.
            - Do not return SUBMIT_MISSING_SENSE_FEEDBACK, SKIP_UNRECOGNIZABLE, or SKIP_WRONG_USAGE inside unitMentions.
            - If the learner input is nonsense, unrecognizable, completely wrong usage, or does not map to an exact sense, return unitMentions: [] and explain briefly in feedback.
            - occurrenceIndex must be a 1-based integer within the source field.
            - occurrenceText must be the exact surface text appearing in the referenced sourceField.
            - confidence must be a number from 0 to 1.
            - confidence must be >= 0.50 for every returned unitMention.
            - payload must be an object. Use {} if there is no payload.
            - If you are not certain enough to fill every required field, return unitMentions: [].
            - Never return incomplete unitMention objects.

            Valid unitMention example:
            {
              "agentRole": "ROLEPLAY",
              "sourceField": "reply",
              "learningUnitId": 3,
              "learningUnitSenseId": 4,
              "canonicalText": "bank",
              "senseKey": "bank:financial-institution",
              "eventType": "UNIT_EXPOSED",
              "eventDirection": "LEARNER_INPUT",
              "occurrenceText": "bank",
              "occurrenceIndex": 1,
              "confidence": 0.96,
              "agentDecision": "RECORD_EVENT",
              "agentReason": "The Roleplay Agent used the financial-institution sense.",
              "payload": {}
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
            - If user text is nonsense, unrecognizable, or completely wrong usage, do not create a unitMention; explain it in feedback.

            Task completion policy:
            - Set scoringSignal.taskComplete=true only when the learner has successfully achieved all required actions in learningPackage.expectedLearnerAction using the fixed facts in learningPackage.taskFacts.
            - Do not mark complete just because the learner sent one sentence; all task goals must be satisfied.
            - When taskComplete=true, Roleplay reply should naturally close the task in character, and Mentor feedback should briefly congratulate completion.
            - scoringSignal.completionReason must briefly state which objectives were completed.
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
        AgentDialogueOutput parsedOutput = readOutput(extractJson(content), content);
        AgentOutputSanitizationResult sanitization = contractService.sanitizeOutput(parsedOutput);
        AgentDialogueOutput output = sanitization.output();
        AgentContractValidationResult validation = contractService.validateOutput(output);
        if (!validation.accepted()) {
            List<String> errors = new java.util.ArrayList<>(validation.errors());
            errors.addAll(sanitization.droppedUnitMentionErrors());
            throw new AgentModelResponseException(
                    "Spring AI Agent output failed contract validation: " + errors,
                    content,
                    parsedOutput,
                    errors
            );
        }
        return output;
    }

    private String userPrompt(String inputJson) {
        return """
                Build the next dual-agent dialogue turn from this AgentDialogueInput JSON.
                Keep the Roleplay reply short enough for one conversational turn.
                Keep Mentor feedback concise and actionable.
                Use learningPackage.taskGoal as the task objective.
                Treat learningPackage.taskFacts as the full available task card.
                Use learningPackage.taskRegister and learningPackage.registerGuidance to choose the right tone.
                Mentor feedback must respect the task register; do not over-correct casual service dialogue into long formal sentences.
                Do not ask for facts outside learningPackage.taskFacts.
                Obey every item in learningPackage.taskConstraints.
                Prefer target senses when they naturally fit the task; do not force unrelated words.
                Never map an unrelated word to a target sense.
                Every Roleplay reply must have learning value: target-sense exposure, target-sense elicitation, or useful task-related expression.
                Avoid low-value operational filler such as checking systems, asking for spelling, reservation codes, or repeated document/name details.
                Respect learningPackage.roleplayPersona and learningPackage.learnerRole.
                Do not repeat learningPackage.openingLine or recent dialogueHistory replies.
                Do not answer on behalf of the learner.
                If userMessage is unclear, ask a short in-character clarification question.
                Before returning, verify that every unitMentions item has all required fields.
                Before returning, verify that occurrenceText literally appears in the referenced sourceField.
                If any required unitMention field would be missing, return unitMentions as an empty array.
                If no exact target sense is used in the turn, return unitMentions as an empty array.
                Set scoringSignal.taskComplete=true only if the learner has completed all task objectives.

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

    private AgentDialogueOutput readOutput(String content, String rawContent) {
        try {
            return objectMapper.readValue(content, AgentDialogueOutput.class);
        } catch (JsonProcessingException exception) {
            throw new AgentModelResponseException(
                    "Failed to parse Agent output JSON.",
                    rawContent,
                    null,
                    List.of(exception.getOriginalMessage())
            );
        }
    }

    private String extractJson(String content) {
        if (content == null) {
            throw new AgentModelResponseException(
                    "Spring AI Agent returned empty content.",
                    null,
                    null,
                    List.of("empty content")
            );
        }
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        throw new AgentModelResponseException(
                "Spring AI Agent did not return a JSON object.",
                content,
                null,
                List.of("JSON object not found")
        );
    }
}
