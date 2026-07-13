package com.contextflow.learning.service;

import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.dto.AdminDialogueTurnResponse;
import com.contextflow.learning.dto.AdminDialogueTurnUpdateRequest;
import com.contextflow.learning.repository.LearningDialogueTurnRepository;
import com.contextflow.learning.repository.LearningEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminDialogueTurnService {

    private final LearningDialogueTurnRepository learningDialogueTurnRepository;
    private final LearningEventRepository learningEventRepository;
    private final ObjectMapper objectMapper;

    public AdminDialogueTurnService(
            LearningDialogueTurnRepository learningDialogueTurnRepository,
            LearningEventRepository learningEventRepository,
            ObjectMapper objectMapper
    ) {
        this.learningDialogueTurnRepository = learningDialogueTurnRepository;
        this.learningEventRepository = learningEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminDialogueTurnResponse> list(Long packageId) {
        List<LearningDialogueTurnEntity> turns = packageId == null
                ? learningDialogueTurnRepository.findTop100ByOrderByIdDesc()
                : learningDialogueTurnRepository.findTop100ByLearningPackageIdOrderByTurnIndexDesc(packageId);
        return turns.stream()
                .map(this::response)
                .toList();
    }

    @Transactional
    public AdminDialogueTurnResponse update(Long id, AdminDialogueTurnUpdateRequest request) {
        LearningDialogueTurnEntity turn = learningDialogueTurnRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dialogue turn not found."));
        validateJson(request.corrections(), "corrections");
        validateJson(request.scoringSignal(), "scoringSignal");

        turn.setUserMessage(request.userMessage().trim());
        turn.setRoleplayReply(request.roleplayReply().trim());
        turn.setMentorFeedback(request.mentorFeedback().trim());
        turn.setCorrections(request.corrections().trim());
        turn.setNaturalExpression(request.naturalExpression().trim());
        turn.setScoringSignal(request.scoringSignal().trim());
        return response(learningDialogueTurnRepository.save(turn));
    }

    @Transactional
    public void delete(Long id) {
        LearningDialogueTurnEntity turn = learningDialogueTurnRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dialogue turn not found."));
        learningEventRepository.deleteBySourceTypeAndSourceId(LearningEventSourceType.LEARNING_DIALOGUE_TURN, id);
        learningDialogueTurnRepository.delete(turn);
    }

    private AdminDialogueTurnResponse response(LearningDialogueTurnEntity turn) {
        long eventCount = learningEventRepository.countBySourceTypeAndSourceId(
                LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                turn.getId()
        );
        return AdminDialogueTurnResponse.from(turn, eventCount);
    }

    private void validateJson(String value, String field) {
        try {
            objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must be valid JSON.");
        }
    }
}
