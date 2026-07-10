package com.contextflow.placement.service;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemGradingType;
import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.domain.PlacementSessionAnswerEntity;
import com.contextflow.placement.domain.PlacementSessionEntity;
import com.contextflow.placement.domain.PlacementSessionMode;
import com.contextflow.placement.domain.PlacementSessionStatus;
import com.contextflow.placement.dto.AdaptivePlacementAnswerResponse;
import com.contextflow.placement.dto.AdaptivePlacementSessionResponse;
import com.contextflow.placement.dto.PlacementAnswerSubmission;
import com.contextflow.placement.dto.PlacementItemResponse;
import com.contextflow.placement.dto.PlacementSessionResponse;
import com.contextflow.placement.dto.PlacementSessionResultResponse;
import com.contextflow.placement.dto.PlacementTestItemResponse;
import com.contextflow.placement.dto.SubmitAdaptivePlacementAnswerRequest;
import com.contextflow.placement.dto.SubmitPlacementSessionRequest;
import com.contextflow.placement.repository.PlacementItemRepository;
import com.contextflow.placement.repository.PlacementSessionAnswerRepository;
import com.contextflow.placement.repository.PlacementSessionRepository;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.repository.UserRepository;
import com.contextflow.user.service.UserLevelProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlacementSessionService {

    private static final int SESSION_ITEM_COUNT = 5;
    private static final int ADAPTIVE_MAX_ITEM_COUNT = 10;
    private static final int INITIAL_DIFFICULTY_SCORE = 50;
    private static final int DIFFICULTY_STEP = 12;

    private final UserRepository userRepository;
    private final PlacementItemRepository placementItemRepository;
    private final PlacementSessionRepository placementSessionRepository;
    private final PlacementSessionAnswerRepository placementSessionAnswerRepository;
    private final UserLevelProfileService userLevelProfileService;
    private final ObjectMapper objectMapper;

    public PlacementSessionService(
            UserRepository userRepository,
            PlacementItemRepository placementItemRepository,
            PlacementSessionRepository placementSessionRepository,
            PlacementSessionAnswerRepository placementSessionAnswerRepository,
            UserLevelProfileService userLevelProfileService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.placementItemRepository = placementItemRepository;
        this.placementSessionRepository = placementSessionRepository;
        this.placementSessionAnswerRepository = placementSessionAnswerRepository;
        this.userLevelProfileService = userLevelProfileService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PlacementSessionResponse startSession(String username) {
        UserEntity user = activeUser(username);
        List<PlacementItemEntity> items = placementItemRepository
                .findByStatusOrderByIdAsc(PlacementItemStatus.READY, PageRequest.of(0, SESSION_ITEM_COUNT));

        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No READY placement items are available.");
        }

        PlacementSessionEntity session = placementSessionRepository.save(new PlacementSessionEntity(user.getId(), items.size()));

        List<PlacementSessionAnswerEntity> answerRows = new java.util.ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            PlacementItemEntity item = items.get(i);
            answerRows.add(new PlacementSessionAnswerEntity(
                    session.getId(),
                    item.getId(),
                    i + 1,
                    item.getGradingType(),
                    item.getDifficultyScore()
            ));
        }
        placementSessionAnswerRepository.saveAll(answerRows);

        return new PlacementSessionResponse(
                session.getId(),
                session.getStatus(),
                items.stream().map(PlacementItemResponse::from).toList()
        );
    }

    @Transactional
    public AdaptivePlacementSessionResponse startAdaptiveSession(String username) {
        UserEntity user = activeUser(username);
        PlacementItemEntity firstItem = nextAdaptiveItem(INITIAL_DIFFICULTY_SCORE, Set.of())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No READY placement items are available."));

        PlacementSessionEntity session = placementSessionRepository.save(PlacementSessionEntity.adaptive(
                user.getId(),
                ADAPTIVE_MAX_ITEM_COUNT,
                INITIAL_DIFFICULTY_SCORE
        ));

        placementSessionAnswerRepository.save(new PlacementSessionAnswerEntity(
                session.getId(),
                firstItem.getId(),
                1,
                firstItem.getGradingType(),
                firstItem.getDifficultyScore()
        ));

        return new AdaptivePlacementSessionResponse(
                session.getId(),
                session.getStatus(),
                session.getAnsweredCount(),
                session.getMaxItemCount(),
                session.getCurrentDifficultyScore(),
                toTestItem(firstItem)
        );
    }

    @Transactional
    public AdaptivePlacementAnswerResponse answerAdaptive(
            String username,
            Long sessionId,
            SubmitAdaptivePlacementAnswerRequest request
    ) {
        UserEntity user = activeUser(username);
        PlacementSessionEntity session = placementSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement session not found."));

        if (!PlacementSessionMode.ADAPTIVE.equals(session.getMode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This endpoint only supports adaptive placement sessions.");
        }
        if (!PlacementSessionStatus.STARTED.equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Placement session has already finished.");
        }

        List<PlacementSessionAnswerEntity> answerRows =
                placementSessionAnswerRepository.findBySessionIdOrderByItemOrderAsc(sessionId);
        PlacementSessionAnswerEntity currentAnswer = answerRows.stream()
                .filter(answer -> answer.getItemId().equals(request.itemId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item does not belong to this session."));

        if (currentAnswer.isAnswered()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This item has already been answered.");
        }

        PlacementItemEntity currentItem = placementItemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement item not found."));
        boolean correct = grade(currentItem, currentAnswer.getGradingType(), request);
        currentAnswer.submit(request.selectedOptionIndex(), request.textAnswer(), correct, null);

        int answeredCount = (int) answerRows.stream().filter(PlacementSessionAnswerEntity::isAnswered).count();
        int correctCount = (int) answerRows.stream().filter(answer -> Boolean.TRUE.equals(answer.getCorrect())).count();
        int nextDifficulty = adjustDifficulty(session.getCurrentDifficultyScore(), correct);
        Set<Long> usedItemIds = answerRows.stream()
                .map(PlacementSessionAnswerEntity::getItemId)
                .collect(Collectors.toSet());

        Optional<PlacementItemEntity> nextItem = nextAdaptiveItem(nextDifficulty, usedItemIds);
        boolean finished = answeredCount >= session.getMaxItemCount() || nextItem.isEmpty();

        if (finished) {
            BigDecimal scorePercent = scorePercent(correctCount, answeredCount);
            CefrLevel estimatedLevel = estimateLevel(scorePercent);
            session.finishAdaptive(answeredCount, correctCount, scorePercent, estimatedLevel);
            userLevelProfileService.upsertFromPlacementResult(
                    user,
                    session,
                    estimatedLevel,
                    answerRows,
                    itemMap(answerRows)
            );
            return new AdaptivePlacementAnswerResponse(
                    session.getId(),
                    currentItem.getId(),
                    correct,
                    answeredCount,
                    correctCount,
                    nextDifficulty,
                    true,
                    null,
                    new PlacementSessionResultResponse(
                            session.getId(),
                            answeredCount,
                            answeredCount,
                            correctCount,
                            scorePercent,
                            estimatedLevel
                    )
            );
        }

        PlacementItemEntity selectedNextItem = nextItem.get();
        placementSessionAnswerRepository.save(new PlacementSessionAnswerEntity(
                session.getId(),
                selectedNextItem.getId(),
                answerRows.size() + 1,
                selectedNextItem.getGradingType(),
                selectedNextItem.getDifficultyScore()
        ));
        session.updateAdaptiveProgress(answeredCount, correctCount, nextDifficulty);

        return new AdaptivePlacementAnswerResponse(
                session.getId(),
                currentItem.getId(),
                correct,
                answeredCount,
                correctCount,
                nextDifficulty,
                false,
                toTestItem(selectedNextItem),
                null
        );
    }

    @Transactional
    public PlacementSessionResultResponse submitSession(
            String username,
            Long sessionId,
            SubmitPlacementSessionRequest request
    ) {
        UserEntity user = activeUser(username);
        PlacementSessionEntity session = placementSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement session not found."));

        if (!PlacementSessionStatus.STARTED.equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Placement session has already been submitted.");
        }

        List<PlacementSessionAnswerEntity> answerRows =
                placementSessionAnswerRepository.findBySessionIdOrderByItemOrderAsc(sessionId);
        Map<Long, Integer> submittedAnswers = submittedAnswers(request);
        Set<Long> assignedItemIds = answerRows.stream()
                .map(PlacementSessionAnswerEntity::getItemId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!assignedItemIds.equals(submittedAnswers.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submitted answers must match this session's items.");
        }

        Map<Long, PlacementItemEntity> itemMap = placementItemRepository.findAllById(assignedItemIds)
                .stream()
                .collect(Collectors.toMap(PlacementItemEntity::getId, Function.identity()));

        int correctCount = 0;
        for (PlacementSessionAnswerEntity answerRow : answerRows) {
            PlacementItemEntity item = itemMap.get(answerRow.getItemId());
            Integer selectedOptionIndex = submittedAnswers.get(answerRow.getItemId());
            boolean correct = selectedOptionIndex.equals(correctAnswerIndex(item));
            answerRow.submit(selectedOptionIndex, correct);
            if (correct) {
                correctCount++;
            }
        }

        BigDecimal scorePercent = scorePercent(correctCount, answerRows.size());
        CefrLevel estimatedLevel = estimateLevel(scorePercent);
        session.submit(correctCount, scorePercent, estimatedLevel);
        userLevelProfileService.upsertFromPlacementResult(user, session, estimatedLevel, answerRows, itemMap);

        return new PlacementSessionResultResponse(
                session.getId(),
                answerRows.size(),
                submittedAnswers.size(),
                correctCount,
                scorePercent,
                estimatedLevel
        );
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private Map<Long, Integer> submittedAnswers(SubmitPlacementSessionRequest request) {
        Map<Long, Integer> answers = new HashMap<>();
        for (PlacementAnswerSubmission answer : request.answers()) {
            if (answers.put(answer.itemId(), answer.selectedOptionIndex()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate item answer.");
            }
        }
        return answers;
    }

    private Integer correctAnswerIndex(PlacementItemEntity item) {
        try {
            return objectMapper.readTree(item.getContentJson()).path("answerIndex").asInt(-1);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid placement item content.");
        }
    }

    private boolean grade(
            PlacementItemEntity item,
            PlacementItemGradingType gradingType,
            SubmitAdaptivePlacementAnswerRequest request
    ) {
        return switch (gradingType) {
            case LOCAL_EXACT -> gradeLocalExact(item, request);
            case LOCAL_ACCEPTED_ANSWERS -> gradeAcceptedAnswers(item, request);
            case AI_JUDGE -> throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "AI judging is not wired yet.");
        };
    }

    private boolean gradeLocalExact(PlacementItemEntity item, SubmitAdaptivePlacementAnswerRequest request) {
        if (request.selectedOptionIndex() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "selectedOptionIndex is required for this item.");
        }
        return request.selectedOptionIndex().equals(correctAnswerIndex(item));
    }

    private boolean gradeAcceptedAnswers(PlacementItemEntity item, SubmitAdaptivePlacementAnswerRequest request) {
        if (request.selectedOptionIndex() != null && request.selectedOptionIndex().equals(correctAnswerIndex(item))) {
            return true;
        }
        String textAnswer = normalize(request.textAnswer());
        if (textAnswer.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "textAnswer is required for this item.");
        }

        try {
            JsonNode acceptedAnswers = objectMapper.readTree(item.getContentJson()).path("acceptedAnswers");
            if (!acceptedAnswers.isArray()) {
                return false;
            }
            for (JsonNode acceptedAnswer : acceptedAnswers) {
                if (textAnswer.equals(normalize(acceptedAnswer.asText()))) {
                    return true;
                }
            }
            return false;
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid placement item content.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Optional<PlacementItemEntity> nextAdaptiveItem(int targetDifficultyScore, Collection<Long> usedItemIds) {
        return placementItemRepository
                .findByStatusOrderByIdAsc(PlacementItemStatus.READY, PageRequest.of(0, 100))
                .stream()
                .filter(item -> !usedItemIds.contains(item.getId()))
                .min(Comparator
                        .comparingInt((PlacementItemEntity item) ->
                                Math.abs(item.getDifficultyScore() - targetDifficultyScore))
                        .thenComparing(PlacementItemEntity::getDifficultyScore)
                        .thenComparing(PlacementItemEntity::getId));
    }

    private Map<Long, PlacementItemEntity> itemMap(List<PlacementSessionAnswerEntity> answerRows) {
        Set<Long> itemIds = answerRows.stream()
                .map(PlacementSessionAnswerEntity::getItemId)
                .collect(Collectors.toSet());

        return placementItemRepository.findAllById(itemIds)
                .stream()
                .collect(Collectors.toMap(PlacementItemEntity::getId, Function.identity()));
    }

    private int adjustDifficulty(int currentDifficultyScore, boolean correct) {
        int next = correct
                ? currentDifficultyScore + DIFFICULTY_STEP
                : currentDifficultyScore - DIFFICULTY_STEP;
        return Math.max(1, Math.min(100, next));
    }

    private PlacementTestItemResponse toTestItem(PlacementItemEntity item) {
        try {
            JsonNode content = objectMapper.readTree(item.getContentJson());
            if (!(content instanceof ObjectNode publicContent)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid placement item content.");
            }
            publicContent.remove("answerIndex");
            publicContent.remove("acceptedAnswers");
            publicContent.remove("explanation");
            return PlacementTestItemResponse.from(item, objectMapper.writeValueAsString(publicContent));
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid placement item content.");
        }
    }

    private BigDecimal scorePercent(int correctCount, int itemCount) {
        return BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(itemCount), 2, RoundingMode.HALF_UP);
    }

    private CefrLevel estimateLevel(BigDecimal scorePercent) {
        int score = scorePercent.intValue();
        if (score >= 85) {
            return CefrLevel.B2;
        }
        if (score >= 65) {
            return CefrLevel.B1;
        }
        if (score >= 40) {
            return CefrLevel.A2;
        }
        return CefrLevel.A1;
    }
}
