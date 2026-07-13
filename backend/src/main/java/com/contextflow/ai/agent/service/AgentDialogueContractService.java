package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.dto.AgentContractValidationResult;
import com.contextflow.ai.agent.dto.AgentConversationContext;
import com.contextflow.ai.agent.dto.AgentCorrection;
import com.contextflow.ai.agent.dto.AgentDialogueContractSampleResponse;
import com.contextflow.ai.agent.dto.AgentDialogueHistoryTurn;
import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.contextflow.ai.agent.dto.AgentLearnerProfileContext;
import com.contextflow.ai.agent.dto.AgentLearningPackageContext;
import com.contextflow.ai.agent.dto.AgentOutputSanitizationResult;
import com.contextflow.ai.agent.dto.AgentRole;
import com.contextflow.ai.agent.dto.AgentTargetSenseContext;
import com.contextflow.ai.agent.dto.AgentToolAccessContext;
import com.contextflow.ai.agent.dto.AgentUnitMention;
import com.contextflow.ai.agent.dto.AgentUnitMentionDecision;
import com.contextflow.learning.domain.LearningEventDirection;
import com.contextflow.learning.domain.LearningEventType;
import com.contextflow.learning.dto.CorrectionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AgentDialogueContractService {

    public static final String CONTRACT_VERSION = "agent-dialogue.v1";

    public AgentDialogueOutput localOutput(
            String reply,
            String feedback,
            List<CorrectionResponse> corrections,
            String naturalExpression,
            Map<String, Object> scoringSignal
    ) {
        AgentDialogueOutput output = new AgentDialogueOutput(
                CONTRACT_VERSION,
                reply,
                feedback,
                toAgentCorrections(corrections),
                naturalExpression,
                List.of(),
                scoringSignal
        );
        AgentContractValidationResult validation = validateOutput(output);
        if (!validation.accepted()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Local Agent output does not match the dialogue contract.");
        }
        return output;
    }

    public AgentContractValidationResult validateOutput(AgentDialogueOutput output) {
        List<String> errors = new ArrayList<>();
        if (output == null) {
            return new AgentContractValidationResult(false, List.of("output is required"));
        }
        if (!CONTRACT_VERSION.equals(output.contractVersion())) {
            errors.add("contractVersion must be " + CONTRACT_VERSION);
        }
        if (isBlank(output.reply())) {
            errors.add("reply is required");
        }
        if (isBlank(output.feedback())) {
            errors.add("feedback is required");
        }
        if (output.corrections() == null) {
            errors.add("corrections must be an array");
        }
        if (isBlank(output.naturalExpression())) {
            errors.add("naturalExpression is required");
        }
        if (output.unitMentions() == null) {
            errors.add("unitMentions must be an array");
        } else {
            validateUnitMentions(output, errors);
        }
        if (output.scoringSignal() == null) {
            errors.add("scoringSignal is required");
        }
        return new AgentContractValidationResult(errors.isEmpty(), errors);
    }

    public AgentOutputSanitizationResult sanitizeOutput(AgentDialogueOutput output) {
        if (output == null || output.unitMentions() == null || output.unitMentions().isEmpty()) {
            return new AgentOutputSanitizationResult(output, List.of());
        }

        List<AgentUnitMention> keptMentions = new ArrayList<>();
        List<String> droppedErrors = new ArrayList<>();
        for (int index = 0; index < output.unitMentions().size(); index++) {
            AgentUnitMention mention = output.unitMentions().get(index);
            List<String> mentionErrors = new ArrayList<>();
            validateUnitMention(output, mention, "unitMentions[" + index + "].", mentionErrors);
            if (mentionErrors.isEmpty()) {
                keptMentions.add(mention);
            } else {
                droppedErrors.addAll(mentionErrors);
            }
        }

        AgentDialogueOutput sanitizedOutput = new AgentDialogueOutput(
                output.contractVersion(),
                output.reply(),
                output.feedback(),
                output.corrections(),
                output.naturalExpression(),
                keptMentions,
                output.scoringSignal()
        );
        return new AgentOutputSanitizationResult(sanitizedOutput, droppedErrors);
    }

    public AgentDialogueContractSampleResponse sample() {
        AgentDialogueInput input = new AgentDialogueInput(
                CONTRACT_VERSION,
                new AgentConversationContext(1L, 10L, 2, "zh-CN", Instant.parse("2026-07-11T00:00:00Z")),
                new AgentLearnerProfileContext(
                        "A2",
                        Map.of("speaking", 58, "grammar", 52),
                        List.of("bank_account"),
                        List.of("polite_request", "complete_sentence")
                ),
                new AgentLearningPackageContext(
                        10L,
                        "READY",
                        "Bank account roleplay",
                        "bank_account",
                        "Open a bank account",
                        "你需要去银行开一个账户，并询问需要哪些材料。",
                        "zh-CN",
                        "用英语说明想开户，并询问所需材料或下一步。",
                        "Bank service representative. You help the learner open an account.",
                        "Customer who wants banking service.",
                        "Good morning. What kind of account would you like to open?",
                        "{\"scenario\":{\"code\":\"bank_account\"},\"learningTask\":{\"goal\":\"你需要去银行开一个账户，并询问需要哪些材料。\",\"instructionLanguage\":\"zh-CN\",\"expectedLearnerAction\":\"用英语说明想开户，并询问所需材料或下一步。\"},\"roleplayAgent\":{\"persona\":\"Bank service representative. You help the learner open an account.\",\"learnerRole\":\"Customer who wants banking service.\",\"openingLine\":\"Good morning. What kind of account would you like to open?\"}}"
                ),
                List.of(new AgentTargetSenseContext(
                        "review",
                        3L,
                        4L,
                        "bank",
                        "bank:financial-institution",
                        "NOUN",
                        "a financial institution",
                        "bank; financial institution",
                        "A1",
                        "HIGH",
                        new BigDecimal("0.92"),
                        new BigDecimal("64.50"),
                        "bank_account"
                )),
                List.of(new AgentDialogueHistoryTurn(
                        100L,
                        1,
                        "I want account.",
                        "Certainly. Do you have your ID?",
                        "Use an article before a countable noun.",
                        Instant.parse("2026-07-11T00:00:10Z")
                )),
                "Could you help me open a bank account?",
                toolAccess()
        );

        AgentDialogueOutput output = new AgentDialogueOutput(
                CONTRACT_VERSION,
                "Certainly. I can help you open a bank account. Do you have your ID and proof of address with you today?",
                "This is clear and polite.",
                List.of(),
                "Could you help me open a bank account?",
                List.of(new AgentUnitMention(
                        AgentRole.ROLEPLAY,
                        "reply",
                        3L,
                        4L,
                        "bank",
                        "bank:financial-institution",
                        LearningEventType.UNIT_EXPOSED,
                        LearningEventDirection.LEARNER_INPUT,
                        "bank",
                        1,
                        new BigDecimal("0.96"),
                        AgentUnitMentionDecision.RECORD_EVENT,
                        "The Roleplay Agent generated the financial-institution sense.",
                        Map.of("scenarioCode", "bank_account")
                )),
                Map.of(
                        "clarityScore", 88,
                        "naturalnessScore", 90,
                        "needsReview", false
                )
        );

        return new AgentDialogueContractSampleResponse(
                CONTRACT_VERSION,
                input,
                output,
                validateOutput(output),
                List.of(
                        "GET /api/learning/agent-tools/word-senses?text={word}",
                        "POST /api/learning/agent-tools/events",
                        "POST /api/learning/agent-tools/sense-feedback"
                )
        );
    }

    public AgentToolAccessContext toolAccess() {
        return new AgentToolAccessContext(
                "/api/learning/agent-tools/word-senses",
                "/api/learning/agent-tools/events",
                "/api/learning/agent-tools/sense-feedback",
                List.of(
                        "Record learner output only when the Agent can map the word to a specific learningUnitSenseId.",
                        "Record learner input for words intentionally generated by Roleplay or Mentor output.",
                        "Do not record nonsense, unrecognizable text, or completely wrong usage.",
                        "Submit sense feedback when the intended meaning is valid but absent from the database."
                )
        );
    }

    private List<AgentCorrection> toAgentCorrections(List<CorrectionResponse> corrections) {
        if (corrections == null) {
            return List.of();
        }
        return corrections.stream()
                .map(correction -> new AgentCorrection(
                        correction.original(),
                        correction.suggestion(),
                        correction.reason()
                ))
                .toList();
    }

    private void validateUnitMentions(AgentDialogueOutput output, List<String> errors) {
        List<AgentUnitMention> unitMentions = output.unitMentions();
        for (int index = 0; index < unitMentions.size(); index++) {
            AgentUnitMention mention = unitMentions.get(index);
            String prefix = "unitMentions[" + index + "].";
            validateUnitMention(output, mention, prefix, errors);
        }
    }

    private void validateUnitMention(
            AgentDialogueOutput output,
            AgentUnitMention mention,
            String prefix,
            List<String> errors
    ) {
        if (mention == null) {
            errors.add(prefix + "mention is required");
            return;
        }
        if (mention.agentRole() == null) {
            errors.add(prefix + "agentRole is required");
        }
        if (isBlank(mention.sourceField())) {
            errors.add(prefix + "sourceField is required");
        }
        if (mention.eventType() == null) {
            errors.add(prefix + "eventType is required");
        }
        if (mention.eventDirection() == null) {
            errors.add(prefix + "eventDirection is required");
        }
        if (isBlank(mention.occurrenceText())) {
            errors.add(prefix + "occurrenceText is required");
        }
        if (mention.occurrenceIndex() == null || mention.occurrenceIndex() < 1) {
            errors.add(prefix + "occurrenceIndex must be >= 1");
        }
        if (mention.agentDecision() == null) {
            errors.add(prefix + "agentDecision is required");
        }
        validateMentionDecision(mention, prefix, errors);
        validateConfidence(mention, prefix, errors);
        validateRecordableMention(mention, prefix, errors);
        validateEventDirection(mention, prefix, errors);
        validateSourceField(mention, prefix, errors);
        validateSourceOccurrence(output, mention, prefix, errors);
    }

    private void validateMentionDecision(AgentUnitMention mention, String prefix, List<String> errors) {
        if (mention.agentDecision() == null) {
            return;
        }
        if (mention.agentDecision() == AgentUnitMentionDecision.RECORD_EVENT
                || mention.agentDecision() == AgentUnitMentionDecision.RECORD_SPELLING_OR_FORM_ERROR) {
            return;
        }
        errors.add(prefix + "non-recordable decisions must be omitted from unitMentions");
    }

    private void validateConfidence(AgentUnitMention mention, String prefix, List<String> errors) {
        if (mention.confidence() == null) {
            errors.add(prefix + "confidence is required");
            return;
        }
        if (mention.confidence().compareTo(BigDecimal.ZERO) < 0
                || mention.confidence().compareTo(BigDecimal.ONE) > 0) {
            errors.add(prefix + "confidence must be between 0 and 1");
        }
        if ((mention.agentDecision() == AgentUnitMentionDecision.RECORD_EVENT
                || mention.agentDecision() == AgentUnitMentionDecision.RECORD_SPELLING_OR_FORM_ERROR)
                && mention.confidence().compareTo(new BigDecimal("0.50")) < 0) {
            errors.add(prefix + "recordable mention confidence must be >= 0.50");
        }
    }

    private void validateRecordableMention(AgentUnitMention mention, String prefix, List<String> errors) {
        if (mention.agentDecision() != AgentUnitMentionDecision.RECORD_EVENT
                && mention.agentDecision() != AgentUnitMentionDecision.RECORD_SPELLING_OR_FORM_ERROR) {
            return;
        }
        if (mention.learningUnitId() == null) {
            errors.add(prefix + "learningUnitId is required for recordable mentions");
        }
        if (mention.learningUnitSenseId() == null) {
            errors.add(prefix + "learningUnitSenseId is required for recordable mentions");
        }
    }

    private void validateEventDirection(AgentUnitMention mention, String prefix, List<String> errors) {
        if (mention.eventType() == null || mention.eventDirection() == null) {
            return;
        }
        if (mention.eventDirection() == LearningEventDirection.LEARNER_OUTPUT
                && mention.eventType() != LearningEventType.UNIT_ATTEMPTED) {
            errors.add(prefix + "learner output must use UNIT_ATTEMPTED");
        }
        if (mention.eventDirection() == LearningEventDirection.LEARNER_INPUT
                && mention.eventType() == LearningEventType.UNIT_ATTEMPTED) {
            errors.add(prefix + "learner input cannot use UNIT_ATTEMPTED");
        }
        String sourceField = normalizeSourceField(mention.sourceField());
        if (mention.eventDirection() == LearningEventDirection.LEARNER_OUTPUT
                && !"usermessage".equals(sourceField)
                && !"learnermessage".equals(sourceField)) {
            errors.add(prefix + "learner output must use sourceField userMessage");
        }
        if (mention.eventType() == LearningEventType.UNIT_EXPOSED && !"reply".equals(sourceField)) {
            errors.add(prefix + "UNIT_EXPOSED must use sourceField reply");
        }
        if (mention.eventType() == LearningEventType.UNIT_CORRECTED && !"correctionsuggestion".equals(sourceField)) {
            errors.add(prefix + "UNIT_CORRECTED must use sourceField correctionSuggestion");
        }
        if (mention.eventType() == LearningEventType.UNIT_RECOMMENDED && !"naturalexpression".equals(sourceField)) {
            errors.add(prefix + "UNIT_RECOMMENDED must use sourceField naturalExpression");
        }
    }

    private void validateSourceField(AgentUnitMention mention, String prefix, List<String> errors) {
        String sourceField = normalizeSourceField(mention.sourceField());
        if (sourceField.isBlank()) {
            return;
        }
        if (!List.of(
                "usermessage",
                "learnermessage",
                "reply",
                "feedback",
                "naturalexpression",
                "correctionsuggestion"
        ).contains(sourceField)) {
            errors.add(prefix + "sourceField is not supported");
        }
    }

    private void validateSourceOccurrence(
            AgentDialogueOutput output,
            AgentUnitMention mention,
            String prefix,
            List<String> errors
    ) {
        if (isBlank(mention.occurrenceText()) || isBlank(mention.sourceField())) {
            return;
        }
        String sourceField = normalizeSourceField(mention.sourceField());
        boolean found = switch (sourceField) {
            case "reply" -> containsIgnoreCase(output.reply(), mention.occurrenceText());
            case "feedback" -> containsIgnoreCase(output.feedback(), mention.occurrenceText());
            case "naturalexpression" -> containsIgnoreCase(output.naturalExpression(), mention.occurrenceText());
            case "correctionsuggestion" -> output.corrections() != null && output.corrections().stream()
                    .anyMatch(correction -> containsIgnoreCase(correction.suggestion(), mention.occurrenceText()));
            case "usermessage", "learnermessage" -> true;
            default -> false;
        };
        if (!found) {
            errors.add(prefix + "occurrenceText must appear in the referenced sourceField");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean containsIgnoreCase(String value, String fragment) {
        if (value == null || fragment == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT));
    }

    private String normalizeSourceField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace("_", "");
    }
}
