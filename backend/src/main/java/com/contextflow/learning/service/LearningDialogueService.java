package com.contextflow.learning.service;

import com.contextflow.ai.agent.dto.AgentDialogueOutput;
import com.contextflow.ai.agent.service.AgentDialogueContractService;
import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageStatus;
import com.contextflow.learning.dto.CorrectionResponse;
import com.contextflow.learning.dto.LearningDialogueRequest;
import com.contextflow.learning.dto.LearningDialogueResponse;
import com.contextflow.learning.repository.LearningDialogueTurnRepository;
import com.contextflow.learning.repository.LearningPackageRepository;
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

import java.util.ArrayList;
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
    private final AgentDialogueContractService agentDialogueContractService;
    private final ObjectMapper objectMapper;

    public LearningDialogueService(
            UserRepository userRepository,
            LearningPackageRepository learningPackageRepository,
            LearningDialogueTurnRepository learningDialogueTurnRepository,
            LearningEventService learningEventService,
            AgentDialogueContractService agentDialogueContractService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.learningPackageRepository = learningPackageRepository;
        this.learningDialogueTurnRepository = learningDialogueTurnRepository;
        this.learningEventService = learningEventService;
        this.agentDialogueContractService = agentDialogueContractService;
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
        AgentDialogueOutput agentOutput = agentDialogueContractService.localOutput(
                roleplayReply,
                mentorFeedback,
                corrections,
                naturalExpression,
                scoringSignal
        );

        LearningDialogueTurnEntity saved = learningDialogueTurnRepository.save(new LearningDialogueTurnEntity(
                user.getId(),
                packageId,
                turnIndex,
                userMessage,
                agentOutput.reply(),
                agentOutput.feedback(),
                serialize(corrections),
                agentOutput.naturalExpression(),
                serialize(agentOutput.scoringSignal())
        ));
        learningEventService.recordDialogueTurnEvents(saved, scenarioCode, corrections, agentOutput.scoringSignal());

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
