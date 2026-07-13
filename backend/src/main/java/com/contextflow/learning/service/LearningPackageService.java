package com.contextflow.learning.service;

import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageGenerationSource;
import com.contextflow.learning.domain.LearningPackageStatus;
import com.contextflow.learning.dto.LearningPackageResponse;
import com.contextflow.learning.repository.LearningPackageRepository;
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
    private final ObjectMapper objectMapper;

    public LearningPackageService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            ScenarioTemplateRepository scenarioTemplateRepository,
            LearningPackageRepository learningPackageRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.scenarioTemplateRepository = scenarioTemplateRepository;
        this.learningPackageRepository = learningPackageRepository;
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

        ScenarioTemplateEntity scenario = scenarioTemplateRepository.findById(packageEntity.getScenarioTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Scenario template not found."));

        return LearningPackageResponse.from(packageEntity, scenario.getName());
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
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
                    ? "Check in at a hotel and confirm your reservation details."
                    : "你需要办理酒店入住，并确认预订信息。";
            case "shopping_return" -> english
                    ? "Explain a problem with an item and ask for a return or exchange."
                    : "你需要向店员描述商品问题，并申请退货或换货。";
            case "bank_account" -> english
                    ? "Open a bank account and ask what documents are needed."
                    : "你需要去银行开一个账户，并询问需要哪些材料。";
            case "police_stop" -> english
                    ? "Stay calm, ask why you were stopped, and answer basic questions."
                    : "你需要冷静询问被拦下的原因，并回答基本问题。";
            default -> english
                    ? "Complete a realistic communication task using the target senses naturally."
                    : "你需要根据本轮目标词义完成一个真实沟通任务，并尽量自然地使用相关表达。";
        };
    }

    private String expectedLearnerAction(String scenarioCode, String cefrLevel) {
        boolean english = advancedLevel(cefrLevel);
        return switch (scenarioCode) {
            case "hotel_check_in" -> english
                    ? "Ask to check in, give the reservation name, and respond to room questions."
                    : "用英语提出入住请求，说明预订姓名，并回答房间相关问题。";
            case "shopping_return" -> english
                    ? "Describe the item issue and ask politely for a return or exchange."
                    : "用英语描述商品问题，并礼貌提出退货或换货请求。";
            case "bank_account" -> english
                    ? "Ask to open an account and clarify the documents or next steps."
                    : "用英语说明想开户，并询问所需材料或下一步。";
            case "police_stop" -> english
                    ? "Ask for the reason calmly and answer the officer's follow-up questions."
                    : "用英语冷静询问原因，并回答对方的后续问题。";
            default -> english
                    ? "Reply in English and move the task forward."
                    : "用英语回复，并推动任务继续。";
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
