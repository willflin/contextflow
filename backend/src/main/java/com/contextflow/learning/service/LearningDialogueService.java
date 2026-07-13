package com.contextflow.learning.service;

import com.contextflow.ai.agent.dto.AgentCorrection;
import com.contextflow.ai.agent.dto.AgentConversationContext;
import com.contextflow.ai.agent.dto.AgentDialogueHistoryTurn;
import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.contextflow.ai.agent.dto.AgentLearnerProfileContext;
import com.contextflow.ai.agent.dto.AgentLearningPackageContext;
import com.contextflow.ai.agent.dto.AgentTargetSenseContext;
import com.contextflow.ai.agent.dto.AgentUnitMention;
import com.contextflow.ai.agent.dto.AgentUnitMentionDecision;
import com.contextflow.ai.agent.service.AgentDialogueContractService;
import com.contextflow.ai.agent.service.AgentRuntimeService;
import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageStatus;
import com.contextflow.learning.dto.CorrectionResponse;
import com.contextflow.learning.dto.LearningDialogueRequest;
import com.contextflow.learning.dto.LearningDialogueResponse;
import com.contextflow.learning.repository.LearningDialogueTurnRepository;
import com.contextflow.learning.repository.LearningPackageRepository;
import com.contextflow.review.dto.ReviewPlanItemResponse;
import com.contextflow.review.service.ReviewPlanService;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LearningDialogueService {

    private static final TypeReference<List<CorrectionResponse>> CORRECTION_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Object>> SCORING_SIGNAL_TYPE = new TypeReference<>() {
    };

    private final UserRepository userRepository;
    private final LearningPackageRepository learningPackageRepository;
    private final LearningDialogueTurnRepository learningDialogueTurnRepository;
    private final LearningEventService learningEventService;
    private final ReviewPlanService reviewPlanService;
    private final AgentDialogueContractService agentDialogueContractService;
    private final AgentRuntimeService agentRuntimeService;
    private final ObjectMapper objectMapper;

    public LearningDialogueService(
            UserRepository userRepository,
            LearningPackageRepository learningPackageRepository,
            LearningDialogueTurnRepository learningDialogueTurnRepository,
            LearningEventService learningEventService,
            ReviewPlanService reviewPlanService,
            AgentDialogueContractService agentDialogueContractService,
            AgentRuntimeService agentRuntimeService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.learningPackageRepository = learningPackageRepository;
        this.learningDialogueTurnRepository = learningDialogueTurnRepository;
        this.learningEventService = learningEventService;
        this.reviewPlanService = reviewPlanService;
        this.agentDialogueContractService = agentDialogueContractService;
        this.agentRuntimeService = agentRuntimeService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LearningDialogueResponse reply(String username, Long packageId, LearningDialogueRequest request) {
        UserEntity user = activeUser(username);
        LearningPackageEntity packageEntity = learningPackageRepository.findByIdAndUserId(packageId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning package not found."));

        if (packageEntity.getStatus() != LearningPackageStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Learning package is not ready.");
        }

        String userMessage = request.message().trim();
        int turnIndex = Math.toIntExact(learningDialogueTurnRepository.countByLearningPackageId(packageId) + 1);
        JsonNode packageContent = readPackageContent(packageEntity);
        String scenarioCode = packageContent.path("scenario").path("code").asText("general");
        MessageFeatures features = analyze(userMessage);

        List<CorrectionResponse> corrections = corrections(userMessage, scenarioCode, features);
        String roleplayReply = roleplayReply(scenarioCode, userMessage, turnIndex);
        String mentorFeedback = mentorFeedback(corrections, features);
        String naturalExpression = naturalExpression(scenarioCode);
        Map<String, Object> scoringSignal = scoringSignal(scenarioCode, features, corrections, turnIndex);
        AgentDialogueInput agentInput = agentInput(user, packageEntity, packageContent, turnIndex, userMessage);
        AgentDialogueOutput agentOutput = agentRuntimeService.generateDialogue(
                agentInput,
                () -> agentDialogueContractService.localOutput(
                        roleplayReply,
                        mentorFeedback,
                        corrections,
                        naturalExpression,
                        scoringSignal
                )
        );
        agentOutput = guardRoleplayOutput(agentOutput, packageEntity.getId(), packageContent, scenarioCode, userMessage, turnIndex);
        List<CorrectionResponse> outputCorrections = correctionResponses(agentOutput.corrections());

        LearningDialogueTurnEntity saved = learningDialogueTurnRepository.save(new LearningDialogueTurnEntity(
                user.getId(),
                packageId,
                turnIndex,
                userMessage,
                agentOutput.reply(),
                agentOutput.feedback(),
                serialize(outputCorrections),
                agentOutput.naturalExpression(),
                serialize(agentOutput.scoringSignal())
        ));
        boolean modelReturnedMentions = agentOutput.unitMentions() != null && !agentOutput.unitMentions().isEmpty();
        recordAgentUnitMentions(saved, agentOutput.unitMentions());
        if (!modelReturnedMentions) {
            learningEventService.recordDialogueTurnEvents(saved, scenarioCode, outputCorrections, agentOutput.scoringSignal());
        }

        return new LearningDialogueResponse(
                saved.getId(),
                saved.getLearningPackageId(),
                saved.getTurnIndex(),
                saved.getUserMessage(),
                saved.getRoleplayReply(),
                saved.getMentorFeedback(),
                deserializeCorrections(saved.getCorrections()),
                saved.getNaturalExpression(),
                deserializeScoringSignal(saved.getScoringSignal()),
                saved.getCreatedAt()
        );
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private JsonNode readPackageContent(LearningPackageEntity packageEntity) {
        try {
            return objectMapper.readTree(packageEntity.getContent());
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid learning package content.");
        }
    }

    private AgentDialogueInput agentInput(
            UserEntity user,
            LearningPackageEntity packageEntity,
            JsonNode packageContent,
            int turnIndex,
            String userMessage
    ) {
        JsonNode scenario = packageContent.path("scenario");
        JsonNode learningTask = packageContent.path("learningTask");
        JsonNode roleplayAgent = packageContent.path("roleplayAgent");
        JsonNode learnerProfile = packageContent.path("learnerProfile");
        return new AgentDialogueInput(
                AgentDialogueContractService.CONTRACT_VERSION,
                new AgentConversationContext(user.getId(), packageEntity.getId(), turnIndex, "zh-CN", Instant.now()),
                new AgentLearnerProfileContext(
                        learnerProfile.path("cefrLevel").asText("UNKNOWN"),
                        objectMap(learnerProfile.path("dimensionScores")),
                        stringList(learnerProfile.path("weakScenarios")),
                        stringList(learnerProfile.path("weakAbilities"))
                ),
                new AgentLearningPackageContext(
                        packageEntity.getId(),
                        packageEntity.getStatus().name(),
                        packageEntity.getTitle(),
                        scenario.path("code").asText("general"),
                        scenario.path("name").asText("General English"),
                        taskGoal(learningTask, scenario),
                        learningTask.path("instructionLanguage").asText("zh-CN"),
                        expectedLearnerAction(learningTask, scenario),
                        roleplayAgent.path("persona").asText(roleplayPersona(scenario.path("code").asText("general"))),
                        roleplayAgent.path("learnerRole").asText(learnerRole(scenario.path("code").asText("general"))),
                        roleplayAgent.path("openingLine").asText(openingLine(scenario.path("code").asText("general"))),
                        packageEntity.getContent()
                ),
                targetSenses(user.getUsername()),
                dialogueHistory(packageEntity.getId(), packageContent),
                userMessage,
                agentDialogueContractService.toolAccess()
        );
    }

    private String taskGoal(JsonNode learningTask, JsonNode scenario) {
        String goal = learningTask.path("goal").asText("");
        if (!goal.isBlank()) {
            return goal;
        }
        String generatedGoal = fallbackTaskGoal(scenario.path("code").asText("general"));
        if (!generatedGoal.isBlank()) {
            return generatedGoal;
        }
        String description = scenario.path("description").asText("");
        if (!description.isBlank()) {
            return description;
        }
        return "Complete this English learning task naturally.";
    }

    private String fallbackTaskGoal(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> "你需要办理酒店入住，并确认预订信息。";
            case "shopping_return" -> "你需要向店员描述商品问题，并申请退货或换货。";
            case "bank_account" -> "你需要去银行开一个账户，并询问需要哪些材料。";
            case "police_stop" -> "你需要冷静询问被拦下的原因，并回答基本问题。";
            default -> "";
        };
    }

    private String expectedLearnerAction(JsonNode learningTask, JsonNode scenario) {
        String action = learningTask.path("expectedLearnerAction").asText("");
        if (!action.isBlank()) {
            return action;
        }
        return switch (scenario.path("code").asText("general")) {
            case "hotel_check_in" -> "用英语提出入住请求，说明预订姓名，并回答房间相关问题。";
            case "shopping_return" -> "用英语描述商品问题，并礼貌提出退货或换货请求。";
            case "bank_account" -> "用英语说明想开户，并询问所需材料或下一步。";
            case "police_stop" -> "用英语冷静询问原因，并回答对方的后续问题。";
            default -> "用英语回复，并推动任务继续。";
        };
    }

    private String openingLine(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> "Good evening. Welcome to the hotel. Do you have a reservation?";
            case "shopping_return" -> "Hi, how can I help you with this item today?";
            case "bank_account" -> "Good morning. What kind of account would you like to open?";
            case "police_stop" -> "Hello. Do you know why I stopped you?";
            default -> "Hello. How can I help you today?";
        };
    }

    private String roleplayPersona(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> "Hotel front-desk staff. You help the learner check in or ask about a room.";
            case "shopping_return" -> "Store clerk. You help the learner return or exchange an item.";
            case "bank_account" -> "Bank service representative. You help the learner open an account.";
            case "police_stop" -> "Police officer. You ask calm, basic questions and explain the stop.";
            default -> "Task partner. You help the learner complete the communication task.";
        };
    }

    private String learnerRole(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> "Hotel guest or walk-in customer.";
            case "shopping_return" -> "Customer with an item problem.";
            case "bank_account" -> "Customer who wants banking service.";
            case "police_stop" -> "Person being stopped and questioned.";
            default -> "Learner trying to complete the task.";
        };
    }

    private AgentDialogueOutput guardRoleplayOutput(
            AgentDialogueOutput output,
            Long packageId,
            JsonNode packageContent,
            String scenarioCode,
            String userMessage,
            int turnIndex
    ) {
        if (output == null || output.reply() == null) {
            return output;
        }
        String openingLine = packageContent.path("roleplayAgent").path("openingLine").asText(openingLine(scenarioCode));
        boolean duplicate = sameMeaning(output.reply(), openingLine)
                || learningDialogueTurnRepository.findTop6ByLearningPackageIdOrderByTurnIndexDesc(packageId)
                .stream()
                .anyMatch(turn -> sameMeaning(output.reply(), turn.getRoleplayReply()));
        boolean roleBreak = roleplaySpeaksAsLearner(output.reply(), scenarioCode);
        if (!duplicate && !roleBreak) {
            return output;
        }
        return new AgentDialogueOutput(
                output.contractVersion(),
                repairedRoleplayReply(scenarioCode, userMessage, turnIndex),
                output.feedback(),
                output.corrections(),
                output.naturalExpression(),
                List.of(),
                output.scoringSignal()
        );
    }

    private boolean sameMeaning(String left, String right) {
        String normalizedLeft = normalizeDialogueText(left);
        String normalizedRight = normalizeDialogueText(right);
        return !normalizedLeft.isBlank()
                && !normalizedRight.isBlank()
                && (normalizedLeft.equals(normalizedRight)
                || normalizedLeft.contains(normalizedRight)
                || normalizedRight.contains(normalizedLeft));
    }

    private String normalizeDialogueText(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    private boolean roleplaySpeaksAsLearner(String reply, String scenarioCode) {
        String lower = reply == null ? "" : reply.toLowerCase(Locale.ROOT);
        if ("hotel_check_in".equals(scenarioCode)) {
            return lower.contains("i have a reservation")
                    || lower.contains("my reservation")
                    || lower.contains("under smith");
        }
        return lower.matches("^(yes|yeah|sure|of course)[,.! ]+i\\b.*");
    }

    private String repairedRoleplayReply(String scenarioCode, String userMessage, int turnIndex) {
        String trimmed = userMessage == null ? "" : userMessage.trim();
        boolean unclear = trimmed.isBlank() || trimmed.matches("\\?+");
        return switch (scenarioCode) {
            case "hotel_check_in" -> unclear
                    ? "Could you clarify your request? Are you checking in with a reservation, or would you like to book a room for tonight?"
                    : turnIndex <= 1
                    ? "Of course. Are you checking in with a reservation, or would you like to book a room for tonight?"
                    : "I can help with that. Could you tell me the name on the reservation?";
            case "shopping_return" -> unclear
                    ? "Could you tell me what is wrong with the item?"
                    : "I can help with that. Do you have the receipt with you?";
            case "bank_account" -> unclear
                    ? "Could you tell me what kind of account you would like to open?"
                    : "I can help with that. Do you have your ID and proof of address with you?";
            case "police_stop" -> unclear
                    ? "Could you please tell me what you want to ask?"
                    : "Please stay calm. Could you answer a few basic questions first?";
            default -> unclear
                    ? "Could you clarify what you mean?"
                    : "I understand. Could you tell me a little more?";
        };
    }

    private List<AgentTargetSenseContext> targetSenses(String username) {
        return reviewPlanService.plan(username, 12)
                .items()
                .stream()
                .map(this::targetSense)
                .toList();
    }

    private AgentTargetSenseContext targetSense(ReviewPlanItemResponse item) {
        return new AgentTargetSenseContext(
                item.pool(),
                item.learningUnitId(),
                item.learningUnitSenseId(),
                item.canonicalText(),
                item.senseKey(),
                item.partOfSpeech(),
                item.definitionEn(),
                item.definitionZh(),
                item.difficultyLevel(),
                item.frequencyBand(),
                null,
                item.score(),
                item.scenarioCode()
        );
    }

    private int recordAgentUnitMentions(
            LearningDialogueTurnEntity saved,
            List<AgentUnitMention> unitMentions
    ) {
        if (unitMentions == null || unitMentions.isEmpty()) {
            return 0;
        }

        int recorded = 0;
        for (AgentUnitMention mention : unitMentions) {
            if (!isRecordable(mention)) {
                continue;
            }
            try {
                learningEventService.recordUnitOccurrence(
                        saved.getUserId(),
                        mention.learningUnitId(),
                        mention.learningUnitSenseId(),
                        mention.eventType(),
                        mention.eventDirection(),
                        LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                        saved.getId(),
                        sourceText(saved, mention.sourceField()),
                        mention.occurrenceText(),
                        mentionPayload(saved, mention)
                );
                recorded++;
            } catch (RuntimeException ignored) {
                // Invalid model-provided ids should not block the learner dialogue.
            }
        }
        return recorded;
    }

    private boolean isRecordable(AgentUnitMention mention) {
        return mention != null
                && (mention.agentDecision() == AgentUnitMentionDecision.RECORD_EVENT
                || mention.agentDecision() == AgentUnitMentionDecision.RECORD_SPELLING_OR_FORM_ERROR)
                && mention.learningUnitId() != null
                && mention.learningUnitSenseId() != null
                && mention.eventType() != null
                && mention.eventDirection() != null
                && mention.occurrenceText() != null
                && !mention.occurrenceText().isBlank();
    }

    private String sourceText(LearningDialogueTurnEntity saved, String sourceField) {
        String normalized = sourceField == null ? "" : sourceField.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "usermessage", "user_message", "learner_message" -> saved.getUserMessage();
            case "reply", "roleplayreply", "roleplay_reply" -> saved.getRoleplayReply();
            case "feedback", "mentorfeedback", "mentor_feedback" -> saved.getMentorFeedback();
            case "naturalexpression", "natural_expression" -> saved.getNaturalExpression();
            case "correction", "corrections", "correctionsuggestion", "correction_suggestion" -> saved.getCorrections();
            default -> saved.getRoleplayReply();
        };
    }

    private Map<String, Object> mentionPayload(LearningDialogueTurnEntity saved, AgentUnitMention mention) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("packageId", saved.getLearningPackageId());
        payload.put("turnIndex", saved.getTurnIndex());
        payload.put("sourceField", mention.sourceField());
        payload.put("agentRole", mention.agentRole() == null ? null : mention.agentRole().name());
        payload.put("occurrenceIndex", mention.occurrenceIndex());
        payload.put("confidence", mention.confidence());
        payload.put("agentDecision", mention.agentDecision() == null ? null : mention.agentDecision().name());
        payload.put("agentReason", mention.agentReason());
        payload.put("canonicalText", mention.canonicalText());
        payload.put("senseKey", mention.senseKey());
        payload.put("agentPayload", mention.payload());
        return payload;
    }

    private List<AgentDialogueHistoryTurn> dialogueHistory(Long packageId, JsonNode packageContent) {
        List<AgentDialogueHistoryTurn> history = new ArrayList<>();
        String openingLine = packageContent.path("roleplayAgent").path("openingLine").asText("");
        if (!openingLine.isBlank()) {
            history.add(new AgentDialogueHistoryTurn(
                    0L,
                    0,
                    "",
                    openingLine,
                    "",
                    Instant.EPOCH
            ));
        }
        history.addAll(learningDialogueTurnRepository.findTop6ByLearningPackageIdOrderByTurnIndexDesc(packageId)
                .stream()
                .sorted(Comparator.comparing(LearningDialogueTurnEntity::getTurnIndex))
                .map(turn -> new AgentDialogueHistoryTurn(
                        turn.getId(),
                        turn.getTurnIndex(),
                        turn.getUserMessage(),
                        turn.getRoleplayReply(),
                        turn.getMentorFeedback(),
                        turn.getCreatedAt()
                ))
                .toList());
        return history;
    }

    private Map<String, Object> objectMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        return objectMapper.convertValue(node, new TypeReference<>() {
        });
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> {
            if (!item.asText("").isBlank()) {
                values.add(item.asText());
            }
        });
        return values;
    }

    private MessageFeatures analyze(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        boolean polite = lower.contains("please")
                || lower.contains("could")
                || lower.contains("would")
                || lower.contains("may i")
                || lower.contains("thank");
        boolean question = message.contains("?");
        int wordCount = message.split("\\s+").length;
        boolean likelyFragment = wordCount < 4 || !message.matches(".*[.!?]$");

        return new MessageFeatures(polite, question, wordCount, likelyFragment);
    }

    private List<CorrectionResponse> corrections(String message, String scenarioCode, MessageFeatures features) {
        List<CorrectionResponse> corrections = new ArrayList<>();
        String lower = message.toLowerCase(Locale.ROOT);

        if (lower.contains("i want account")) {
            corrections.add(new CorrectionResponse(
                    "I want account.",
                    "I'd like to open a bank account.",
                    "Use an article before a countable noun, and make the request softer."
            ));
        }

        if (lower.contains("give me")) {
            corrections.add(new CorrectionResponse(
                    "give me",
                    "Could I have ...?",
                    "Direct commands can sound rude in service situations."
            ));
        }

        if (features.likelyFragment()) {
            corrections.add(new CorrectionResponse(
                    message,
                    naturalExpression(scenarioCode),
                    "Try using a complete sentence for clearer roleplay communication."
            ));
        }

        if (!features.polite()) {
            corrections.add(new CorrectionResponse(
                    message,
                    naturalExpression(scenarioCode),
                    "A polite phrase like 'could', 'would', or 'please' sounds more natural here."
            ));
        }

        return corrections;
    }

    private String roleplayReply(String scenarioCode, String userMessage, int turnIndex) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> turnIndex == 1
                    ? "Sure. May I see your ID and the name on the reservation?"
                    : "Thanks. Would you prefer a quiet room or a room near the elevator?";
            case "shopping_return" -> userMessage.toLowerCase(Locale.ROOT).contains("return")
                    ? "Of course. Do you have the receipt with you?"
                    : "I can help with that. What seems to be the problem with the item?";
            case "bank_account" -> "Certainly. Do you have your ID and proof of address with you today?";
            case "police_stop" -> "Please stay calm. I stopped you because I need to ask a few questions.";
            default -> "I understand. Could you tell me a little more?";
        };
    }

    private String mentorFeedback(List<CorrectionResponse> corrections, MessageFeatures features) {
        if (corrections.isEmpty()) {
            return features.question()
                    ? "表达清楚，而且问题形式自然。继续保持礼貌和完整句。"
                    : "整体可理解。下一句可以尝试主动提问，让对话更像真实场景。";
        }

        return "这句话能表达大意，但还不够自然。优先改进礼貌程度、冠词和完整句。";
    }

    private String naturalExpression(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> "I have a reservation under my name. Could I check in, please?";
            case "shopping_return" -> "I'd like to return this item. Could you help me with that?";
            case "bank_account" -> "Could you help me open a bank account?";
            case "police_stop" -> "Could you explain why I was stopped?";
            default -> "Could you help me with this, please?";
        };
    }

    private Map<String, Object> scoringSignal(
            String scenarioCode,
            MessageFeatures features,
            List<CorrectionResponse> corrections,
            int turnIndex
    ) {
        int clarityScore = Math.min(100, Math.max(35, features.wordCount() * 10));
        int naturalnessScore = Math.max(35, 90 - corrections.size() * 15);

        return Map.of(
                "scenarioCode", scenarioCode,
                "turnIndex", turnIndex,
                "clarityScore", clarityScore,
                "naturalnessScore", naturalnessScore,
                "politenessDetected", features.polite(),
                "needsReview", !corrections.isEmpty(),
                "relatedAbilityTags", List.of("polite_request", "complete_sentence")
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize dialogue output.");
        }
    }

    private List<CorrectionResponse> deserializeCorrections(String corrections) {
        try {
            return objectMapper.readValue(corrections, CORRECTION_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private List<CorrectionResponse> correctionResponses(List<AgentCorrection> corrections) {
        if (corrections == null) {
            return List.of();
        }
        return corrections.stream()
                .map(correction -> new CorrectionResponse(
                        correction.original(),
                        correction.suggestion(),
                        correction.reason()
                ))
                .toList();
    }

    private Map<String, Object> deserializeScoringSignal(String scoringSignal) {
        try {
            return objectMapper.readValue(scoringSignal, SCORING_SIGNAL_TYPE);
        } catch (JsonProcessingException exception) {
            return Map.of();
        }
    }

    private record MessageFeatures(
            boolean polite,
            boolean question,
            int wordCount,
            boolean likelyFragment
    ) {
    }
}
