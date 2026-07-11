package com.contextflow.learning.service;

import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitSenseFeedbackEntity;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.dto.LearningUnitDetailResponse;
import com.contextflow.content.repository.LearningUnitRepository;
import com.contextflow.content.repository.LearningUnitSenseFeedbackRepository;
import com.contextflow.content.service.LearningUnitQueryService;
import com.contextflow.learning.domain.LearningEventDirection;
import com.contextflow.learning.domain.LearningEventEntity;
import com.contextflow.learning.domain.LearningEventType;
import com.contextflow.learning.dto.AgentLearningEventRequest;
import com.contextflow.learning.dto.AgentLearningEventResponse;
import com.contextflow.learning.dto.AgentSenseFeedbackRequest;
import com.contextflow.learning.dto.AgentSenseFeedbackResponse;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AgentLearningToolService {

    private final LearningUnitQueryService learningUnitQueryService;
    private final LearningUnitRepository learningUnitRepository;
    private final LearningUnitSenseFeedbackRepository feedbackRepository;
    private final LearningEventService learningEventService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AgentLearningToolService(
            LearningUnitQueryService learningUnitQueryService,
            LearningUnitRepository learningUnitRepository,
            LearningUnitSenseFeedbackRepository feedbackRepository,
            LearningEventService learningEventService,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.learningUnitQueryService = learningUnitQueryService;
        this.learningUnitRepository = learningUnitRepository;
        this.feedbackRepository = feedbackRepository;
        this.learningEventService = learningEventService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public LearningUnitDetailResponse wordSenses(String text) {
        LearningUnitDetailResponse response = learningUnitQueryService.search(text);
        if (!LearningUnitType.WORD.name().equals(response.unitType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agent word-senses tool only supports WORD units for now.");
        }
        return response;
    }

    @Transactional
    public AgentLearningEventResponse recordLearningEvent(String username, AgentLearningEventRequest request) {
        UserEntity user = activeUser(username);
        validateEventContract(request);
        LearningUnitEntity unit = learningUnitRepository.findById(request.learningUnitId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));
        if (unit.getUnitType() != LearningUnitType.WORD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agent event tool only supports WORD units for now.");
        }

        LearningEventEntity event = learningEventService.recordUnitOccurrence(
                user.getId(),
                request.learningUnitId(),
                request.learningUnitSenseId(),
                request.eventType(),
                request.eventDirection(),
                request.sourceType(),
                request.sourceId(),
                request.sourceText(),
                request.occurrenceText(),
                eventPayload(request)
        );

        return new AgentLearningEventResponse(
                true,
                event.getId(),
                event.getLearningUnitSenseId(),
                event.getEventDirection().name()
        );
    }

    @Transactional
    public AgentSenseFeedbackResponse submitSenseFeedback(String username, AgentSenseFeedbackRequest request) {
        UserEntity user = activeUser(username);
        LearningUnitEntity unit = null;
        if (request.learningUnitId() != null) {
            unit = learningUnitRepository.findById(request.learningUnitId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));
        }

        LearningUnitSenseFeedbackEntity feedback = feedbackRepository.save(new LearningUnitSenseFeedbackEntity(
                user.getId(),
                unit,
                request.surfaceText().trim(),
                normalize(request.surfaceText()),
                request.sourceType(),
                request.sourceId(),
                request.sourceText(),
                request.suggestedDefinitionEn(),
                request.suggestedDefinitionZh(),
                request.agentReason(),
                serializePayload(request.payload())
        ));

        return new AgentSenseFeedbackResponse(
                feedback.getId(),
                feedback.getStatus().name(),
                feedback.getCreatedAt()
        );
    }

    private void validateEventContract(AgentLearningEventRequest request) {
        if (request.eventDirection() == LearningEventDirection.LEARNER_OUTPUT
                && request.eventType() != LearningEventType.UNIT_ATTEMPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learner output events must use UNIT_ATTEMPTED.");
        }
        if (request.eventDirection() == LearningEventDirection.LEARNER_INPUT
                && request.eventType() == LearningEventType.UNIT_ATTEMPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learner input events cannot use UNIT_ATTEMPTED.");
        }
    }

    private Map<String, Object> eventPayload(AgentLearningEventRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("agentDecision", request.agentDecision());
        payload.put("agentReason", request.agentReason());
        payload.put("agentPayload", request.payload() == null ? Map.of() : request.payload());
        return payload;
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("'", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String serializePayload(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize feedback payload.");
        }
    }
}
