package com.contextflow.placement.service;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.domain.PlacementSessionAnswerEntity;
import com.contextflow.placement.domain.PlacementSessionEntity;
import com.contextflow.placement.domain.PlacementSessionStatus;
import com.contextflow.placement.dto.PlacementAnswerSubmission;
import com.contextflow.placement.dto.PlacementItemResponse;
import com.contextflow.placement.dto.PlacementSessionResponse;
import com.contextflow.placement.dto.PlacementSessionResultResponse;
import com.contextflow.placement.dto.SubmitPlacementSessionRequest;
import com.contextflow.placement.repository.PlacementItemRepository;
import com.contextflow.placement.repository.PlacementSessionAnswerRepository;
import com.contextflow.placement.repository.PlacementSessionRepository;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlacementSessionService {

    private static final int SESSION_ITEM_COUNT = 5;

    private final UserRepository userRepository;
    private final PlacementItemRepository placementItemRepository;
    private final PlacementSessionRepository placementSessionRepository;
    private final PlacementSessionAnswerRepository placementSessionAnswerRepository;
    private final ObjectMapper objectMapper;

    public PlacementSessionService(
            UserRepository userRepository,
            PlacementItemRepository placementItemRepository,
            PlacementSessionRepository placementSessionRepository,
            PlacementSessionAnswerRepository placementSessionAnswerRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.placementItemRepository = placementItemRepository;
        this.placementSessionRepository = placementSessionRepository;
        this.placementSessionAnswerRepository = placementSessionAnswerRepository;
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
            answerRows.add(new PlacementSessionAnswerEntity(session.getId(), items.get(i).getId(), i + 1));
        }
        placementSessionAnswerRepository.saveAll(answerRows);

        return new PlacementSessionResponse(
                session.getId(),
                session.getStatus(),
                items.stream().map(PlacementItemResponse::from).toList()
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
