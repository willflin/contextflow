package com.contextflow.learning.service;

import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageGenerationSource;
import com.contextflow.learning.domain.LearningPackageStatus;
import com.contextflow.learning.dto.LearningPackageResponse;
import com.contextflow.learning.repository.LearningDialogueTurnRepository;
import com.contextflow.learning.repository.LearningEventRepository;
import com.contextflow.learning.repository.LearningPackageRepository;
import com.contextflow.review.service.ReviewPlanService;
import com.contextflow.scenario.domain.ScenarioTemplateEntity;
import com.contextflow.scenario.domain.ScenarioTemplateStatus;
import com.contextflow.scenario.repository.ScenarioTemplateRepository;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserLevelProfileEntity;
import com.contextflow.user.repository.UserLevelProfileRepository;
import com.contextflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LearningPackageService {

    private final UserRepository userRepository;
    private final UserLevelProfileRepository userLevelProfileRepository;
    private final ScenarioTemplateRepository scenarioTemplateRepository;
    private final LearningPackageRepository learningPackageRepository;
    private final LearningDialogueTurnRepository learningDialogueTurnRepository;
    private final LearningEventRepository learningEventRepository;
    private final ReviewPlanService reviewPlanService;
    private final ObjectMapper objectMapper;

    public LearningPackageService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            ScenarioTemplateRepository scenarioTemplateRepository,
            LearningPackageRepository learningPackageRepository,
            LearningDialogueTurnRepository learningDialogueTurnRepository,
            LearningEventRepository learningEventRepository,
            ReviewPlanService reviewPlanService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.scenarioTemplateRepository = scenarioTemplateRepository;
        this.learningPackageRepository = learningPackageRepository;
        this.learningDialogueTurnRepository = learningDialogueTurnRepository;
        this.learningEventRepository = learningEventRepository;
        this.reviewPlanService = reviewPlanService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LearningPackageResponse getNextReadyPackage(String username) {
        UserEntity user = activeUser(username);
        UserLevelProfileEntity profile = userLevelProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Finish placement testing before starting learning."
                ));

        LearningPackageEntity packageEntity = learningPackageRepository
                .findFirstByUserIdAndStatusOrderByIdAsc(user.getId(), LearningPackageStatus.READY)
                .orElseGet(() -> createSeededPackage(user, profile));
        resetDialogueSession(packageEntity.getId());

        ScenarioTemplateEntity scenario = scenarioTemplateRepository.findById(packageEntity.getScenarioTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Scenario template not found."));

        return LearningPackageResponse.from(packageEntity, scenario.getName());
    }

    @Transactional
    public int skipPackage(String username, Long packageId) {
        UserEntity user = activeUser(username);
        LearningPackageEntity packageEntity = learningPackageRepository.findByIdAndUserId(packageId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning package not found."));
        if (packageEntity.getStatus() != LearningPackageStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only READY packages can be skipped.");
        }
        int deferredCount = reviewPlanService.deferCurrentPlan(username, 12);
        resetDialogueSession(packageEntity.getId());
        packageEntity.markExpired();
        learningPackageRepository.save(packageEntity);
        return deferredCount;
    }

    @Transactional
    public LearningPackageResponse completePackage(String username, Long packageId) {
        UserEntity user = activeUser(username);
        LearningPackageEntity packageEntity = learningPackageRepository.findByIdAndUserId(packageId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning package not found."));
        if (packageEntity.getStatus() != LearningPackageStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only READY packages can be completed.");
        }
        packageEntity.markCompleted();
        LearningPackageEntity saved = learningPackageRepository.save(packageEntity);
        ScenarioTemplateEntity scenario = scenarioTemplateRepository.findById(saved.getScenarioTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Scenario template not found."));
        return LearningPackageResponse.from(saved, scenario.getName());
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private void resetDialogueSession(Long packageId) {
        List<Long> turnIds = learningDialogueTurnRepository.findByLearningPackageIdOrderByIdAsc(packageId)
                .stream()
                .map(LearningDialogueTurnEntity::getId)
                .toList();
        if (turnIds.isEmpty()) {
            return;
        }
        learningEventRepository.deleteBySourceTypeAndSourceIdIn(LearningEventSourceType.LEARNING_DIALOGUE_TURN, turnIds);
        learningDialogueTurnRepository.deleteByLearningPackageId(packageId);
    }

    private LearningPackageEntity createSeededPackage(UserEntity user, UserLevelProfileEntity profile) {
        Set<Long> assignedScenarioIds = learningPackageRepository.findByUserIdOrderByIdAsc(user.getId())
                .stream()
                .map(LearningPackageEntity::getScenarioTemplateId)
                .collect(java.util.stream.Collectors.toSet());
        ScenarioTemplateEntity scenario = selectScenario(profile, assignedScenarioIds);
        String content = buildSeededContent(profile, scenario);

        return learningPackageRepository.save(new LearningPackageEntity(
                user.getId(),
                scenario.getId(),
                LearningPackageStatus.READY,
                LearningPackageGenerationSource.SEEDED_TEMPLATE,
                scenario.getName(),
                content,
                Instant.now(),
                Instant.now().plus(14, ChronoUnit.DAYS)
        ));
    }

    private ScenarioTemplateEntity selectScenario(UserLevelProfileEntity profile, Set<Long> excludedScenarioIds) {
        List<ScenarioTemplateEntity> candidates =
                scenarioTemplateRepository.findByStatusOrderByDifficultyScoreAsc(ScenarioTemplateStatus.ACTIVE);

        return candidates.stream()
                .filter(scenario -> supportsLevel(scenario, profile.getCefrLevel().name()))
                .filter(scenario -> !excludedScenarioIds.contains(scenario.getId()))
                .findFirst()
                .or(() -> candidates.stream()
                        .filter(scenario -> supportsLevel(scenario, profile.getCefrLevel().name()))
                        .findFirst())
                .or(() -> candidates.stream().findFirst())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No active scenario templates are available."));
    }

    private boolean supportsLevel(ScenarioTemplateEntity scenario, String cefrLevel) {
        try {
            JsonNode levels = objectMapper.readTree(scenario.getApplicableLevelsJson());
            if (!levels.isArray()) {
                return false;
            }
            for (JsonNode level : levels) {
                if (cefrLevel.equals(level.asText())) {
                    return true;
                }
            }
            return false;
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid scenario template levels.");
        }
    }

    private String buildSeededContent(UserLevelProfileEntity profile, ScenarioTemplateEntity scenario) {
        Map<String, Object> content = Map.of(
                "scenario", Map.of(
                        "code", scenario.getCode(),
                        "name", scenario.getName(),
                        "description", scenario.getDescription()
                ),
                "learningTask", Map.of(
                        "goal", taskGoal(scenario.getCode(), profile.getCefrLevel().name()),
                        "instructionLanguage", taskInstructionLanguage(profile.getCefrLevel().name()),
                        "expectedLearnerAction", expectedLearnerAction(scenario.getCode(), profile.getCefrLevel().name()),
                        "register", taskRegister(scenario.getCode()),
                        "registerGuidance", registerGuidance(scenario.getCode()),
                        "facts", taskFacts(scenario.getCode()),
                        "constraints", taskConstraints(scenario.getCode()),
                        "source", "scenario_template_seed"
                ),
                "learnerProfile", Map.of(
                        "cefrLevel", profile.getCefrLevel().name(),
                        "dimensionScores", parseJson(profile.getDimensionScoresJson()),
                        "weakScenarios", parseJson(profile.getWeakScenariosJson()),
                        "weakAbilities", parseJson(profile.getWeakAbilitiesJson())
                ),
                "goals", List.of(
                        "Understand the situation and respond naturally.",
                        "Use one polite request and one clarification question.",
                        "Notice expressions that match this scenario."
                ),
                "roleplayAgent", Map.of(
                        "role", scenario.getName() + " partner",
                        "persona", roleplayPersona(scenario.getCode()),
                        "learnerRole", learnerRole(scenario.getCode()),
                        "openingLine", openingLine(scenario.getCode())
                ),
                "mentorAgent", Map.of(
                        "focus", "Give short corrections and suggest more natural expressions.",
                        "language", "Chinese explanation with English examples"
                ),
                "expressions", expressions(scenario.getCode()),
                "practice", List.of(
                        Map.of(
                                "prompt", "Choose one expression and use it in your own sentence.",
                                "expectedAction", "Write one English sentence."
                        ),
                        Map.of(
                                "prompt", "Ask one polite follow-up question in this situation.",
                                "expectedAction", "Write one English question."
                        )
                )
        );

        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build learning package.");
        }
    }

    private Object parseJson(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException exception) {
            return json;
        }
    }

    private String taskInstructionLanguage(String cefrLevel) {
        return advancedLevel(cefrLevel) ? "en" : "zh-CN";
    }

    private String taskGoal(String scenarioCode, String cefrLevel) {
        boolean english = advancedLevel(cefrLevel);
        return switch (scenarioCode) {
            case "hotel_check_in" -> english
                    ? "You are Alex Chen. Check in at Harbor View Hotel tonight. You have a two-night reservation, prefer a quiet queen room, have your passport ready, and need to ask about breakfast time and Wi-Fi."
                    : "你是 Alex Chen，今晚到 Harbor View Hotel 办理入住。你已经预订两晚，想要安静的大床房，护照已准备好。请用英语完成入住，并询问早餐时间和 Wi-Fi。";
            case "shopping_return" -> english
                    ? "You bought wireless headphones yesterday. The left side has no sound. You have the receipt and paid by card. Ask for a refund or exchange."
                    : "你昨天买了一副无线耳机，左边没有声音。你带了收据，用银行卡付款。请用英语说明问题，并申请退款或换货。";
            case "bank_account" -> english
                    ? "You are Alex Chen. Open a savings account. You have your passport and proof of address, want a debit card, and need to ask about monthly fees and required documents."
                    : "你是 Alex Chen，要开一个储蓄账户。你带了护照和地址证明，想申请借记卡，并需要询问月费和所需材料。";
            case "police_stop" -> english
                    ? "You are walking to the subway at night and a police officer stops you. You have your ID, are not driving, and need to calmly ask why you were stopped and what to do next."
                    : "你晚上正走去地铁站，被警察拦下。你带了身份证件，没有开车。请冷静询问被拦下的原因，并询问下一步该怎么做。";
            default -> english
                    ? "Complete a realistic communication task using the target senses naturally."
                    : "你需要根据本轮目标词义完成一个真实沟通任务，并尽量自然地使用相关表达。";
        };
    }

    private String expectedLearnerAction(String scenarioCode, String cefrLevel) {
        boolean english = advancedLevel(cefrLevel);
        return switch (scenarioCode) {
            case "hotel_check_in" -> english
                    ? "Say you want to check in, give the name Alex Chen, mention the two-night reservation, request a quiet queen room, and ask about breakfast time and Wi-Fi."
                    : "用英语说明要入住，给出姓名 Alex Chen，说明已预订两晚，提出想要安静的大床房，并询问早餐时间和 Wi-Fi。";
            case "shopping_return" -> english
                    ? "Describe that the left side of the headphones has no sound, say you bought them yesterday, mention the receipt, and ask for a refund or exchange."
                    : "用英语说明耳机左边没有声音、昨天购买、带了收据，并礼貌申请退款或换货。";
            case "bank_account" -> english
                    ? "Ask to open a savings account, mention your passport and proof of address, ask about the debit card, monthly fees, and required documents."
                    : "用英语说明想开储蓄账户，提到护照和地址证明，询问借记卡、月费和所需材料。";
            case "police_stop" -> english
                    ? "Calmly ask why you were stopped, explain you are walking to the subway, show ID if asked, and ask what to do next."
                    : "用英语冷静询问原因，说明你正走去地铁站，如被要求则说明带了证件，并询问下一步该怎么做。";
            default -> english
                    ? "Reply in English and move the task forward."
                    : "用英语回复，并推动任务继续。";
        };
    }

    private String taskRegister(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in", "shopping_return" -> "daily_service";
            case "bank_account" -> "business_service";
            case "police_stop" -> "formal_sensitive";
            default -> "daily_conversation";
        };
    }

    private String registerGuidance(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> "Daily service conversation. Short, natural, polite phrases are acceptable; do not force long formal sentences.";
            case "shopping_return" -> "Daily service conversation. Clear, direct spoken English is acceptable; polite does not mean overly long.";
            case "bank_account" -> "Business service conversation. Use polite and clear wording, but keep sentences concise.";
            case "police_stop" -> "Formal and sensitive conversation. Stay calm, respectful, and clear; avoid slang or confrontational wording.";
            default -> "Daily conversation. Natural spoken English and simplified wording are acceptable when the meaning is clear.";
        };
    }

    private Map<String, String> taskFacts(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> Map.of(
                    "learnerName", "Alex Chen",
                    "hotelName", "Harbor View Hotel",
                    "arrival", "tonight",
                    "reservation", "two nights under Alex Chen",
                    "roomPreference", "quiet queen room",
                    "document", "passport ready",
                    "questionsToAsk", "breakfast time and Wi-Fi"
            );
            case "shopping_return" -> Map.of(
                    "item", "wireless headphones",
                    "purchaseTime", "yesterday",
                    "problem", "the left side has no sound",
                    "receipt", "available",
                    "payment", "paid by card",
                    "desiredOutcome", "refund or exchange"
            );
            case "bank_account" -> Map.of(
                    "learnerName", "Alex Chen",
                    "accountType", "savings account",
                    "documents", "passport and proof of address",
                    "requestedService", "debit card",
                    "questionsToAsk", "monthly fees and required documents"
            );
            case "police_stop" -> Map.of(
                    "situation", "walking to the subway at night",
                    "transport", "not driving",
                    "document", "ID is available",
                    "tone", "calm and polite",
                    "questionsToAsk", "why you were stopped and what to do next"
            );
            default -> Map.of(
                    "task", "complete the communication task",
                    "languageGoal", "use target senses naturally"
            );
        };
    }

    private List<String> taskConstraints(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> List.of(
                    "Do not ask the learner to invent a name, spelling, reservation code, address, or payment details.",
                    "Follow-up questions must stay within the given facts: Alex Chen, two-night reservation, quiet queen room, passport, breakfast, and Wi-Fi.",
                    "If information is needed, ask about room preference, check-in purpose, breakfast time, Wi-Fi, or services already present in the task facts."
            );
            case "shopping_return" -> List.of(
                    "Do not ask the learner to invent a brand, order number, address, or extra purchase details.",
                    "Follow-up questions must stay within the given facts: wireless headphones, left side no sound, bought yesterday, receipt, card payment, refund or exchange.",
                    "Use questions that elicit description, reason, refund, exchange, receipt, or payment language."
            );
            case "bank_account" -> List.of(
                    "Do not ask the learner to invent income, address, phone number, account number, or private details.",
                    "Follow-up questions must stay within the given facts: savings account, passport, proof of address, debit card, monthly fees, and documents.",
                    "Use questions that elicit account type, documents, fees, debit card, and next-step language."
            );
            case "police_stop" -> List.of(
                    "Do not ask the learner to invent illegal behavior, vehicle details, address, or personal history.",
                    "Follow-up questions must stay within the given facts: walking to the subway, not driving, ID available, calm tone, reason for stop, next steps.",
                    "Use questions that elicit clarification, reason, ID, destination, and next-step language."
            );
            default -> List.of(
                    "Do not ask the learner to invent missing personal facts.",
                    "Stay within the task facts and target senses."
            );
        };
    }

    private boolean advancedLevel(String cefrLevel) {
        return cefrLevel != null && (cefrLevel.startsWith("B2") || cefrLevel.startsWith("C"));
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

    private List<Map<String, String>> expressions(String scenarioCode) {
        return switch (scenarioCode) {
            case "hotel_check_in" -> List.of(
                    Map.of("phrase", "I have a reservation under ...", "meaning", "Use this to give your booking name."),
                    Map.of("phrase", "Could I check in, please?", "meaning", "A polite way to start check-in.")
            );
            case "shopping_return" -> List.of(
                    Map.of("phrase", "I'd like to return this item.", "meaning", "A direct but polite return request."),
                    Map.of("phrase", "Do you have this in another size?", "meaning", "Ask about size availability.")
            );
            case "bank_account" -> List.of(
                    Map.of("phrase", "Could you help me open an account?", "meaning", "A polite service request."),
                    Map.of("phrase", "What documents do I need?", "meaning", "Ask for required documents.")
            );
            case "police_stop" -> List.of(
                    Map.of("phrase", "Could you explain what happened?", "meaning", "Ask for clarification politely."),
                    Map.of("phrase", "I understand. What should I do next?", "meaning", "A calm response to instructions.")
            );
            default -> List.of(
                    Map.of("phrase", "Could you help me?", "meaning", "A general polite request.")
            );
        };
    }
}
