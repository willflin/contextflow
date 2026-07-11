package com.contextflow.learning.service;

import com.contextflow.content.domain.LearningUnitFormEntity;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.repository.LearningUnitFormRepository;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import com.contextflow.learning.domain.LearningEventEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningEventType;
import com.contextflow.learning.dto.CorrectionResponse;
import com.contextflow.learning.repository.LearningEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class LearningEventService {

    private final LearningUnitFormRepository learningUnitFormRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final LearningEventRepository learningEventRepository;
    private final ObjectMapper objectMapper;

    public LearningEventService(
            LearningUnitFormRepository learningUnitFormRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            LearningEventRepository learningEventRepository,
            ObjectMapper objectMapper
    ) {
        this.learningUnitFormRepository = learningUnitFormRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.learningEventRepository = learningEventRepository;
        this.objectMapper = objectMapper;
    }

    public LearningEventEntity recordUnitOccurrence(
            Long userId,
            Long learningUnitId,
            Long learningUnitSenseId,
            LearningEventType eventType,
            LearningEventSourceType sourceType,
            Long sourceId,
            String sourceText,
            String occurrenceText,
            Map<String, Object> payload
    ) {
        validateSenseBelongsToUnit(learningUnitId, learningUnitSenseId);

        return learningEventRepository.save(new LearningEventEntity(
                userId,
                learningUnitId,
                learningUnitSenseId,
                eventType,
                sourceType,
                sourceId,
                sourceText,
                occurrenceText,
                serializePayload(payload)
        ));
    }

    public void recordDialogueTurnEvents(
            LearningDialogueTurnEntity turn,
            String scenarioCode,
            List<CorrectionResponse> corrections,
            Map<String, Object> scoringSignal
    ) {
        List<LearningUnitFormEntity> activeForms =
                learningUnitFormRepository.findAllWithUnitByUnitStatus(LearningUnitStatus.ACTIVE);

        if (activeForms.isEmpty()) {
            return;
        }

        List<LearningEventEntity> events = new ArrayList<>();
        addEvents(events, activeForms, turn, LearningEventType.UNIT_ATTEMPTED, "userMessage",
                turn.getUserMessage(), scenarioCode, scoringSignal, null);
        addEvents(events, activeForms, turn, LearningEventType.UNIT_EXPOSED, "roleplayReply",
                turn.getRoleplayReply(), scenarioCode, scoringSignal, null);

        for (CorrectionResponse correction : corrections) {
            addEvents(events, activeForms, turn, LearningEventType.UNIT_CORRECTED, "correctionSuggestion",
                    correction.suggestion(), scenarioCode, scoringSignal, correction.reason());
        }

        addEvents(events, activeForms, turn, LearningEventType.UNIT_RECOMMENDED, "naturalExpression",
                turn.getNaturalExpression(), scenarioCode, scoringSignal, null);

        if (!events.isEmpty()) {
            learningEventRepository.saveAll(events);
        }
    }

    private void addEvents(
            List<LearningEventEntity> events,
            List<LearningUnitFormEntity> activeForms,
            LearningDialogueTurnEntity turn,
            LearningEventType eventType,
            String sourceField,
            String sourceText,
            String scenarioCode,
            Map<String, Object> scoringSignal,
            String correctionReason
    ) {
        Map<Long, List<OccurrenceMatch>> matchesByUnit = matchesByUnit(sourceText, activeForms);

        for (Map.Entry<Long, List<OccurrenceMatch>> entry : matchesByUnit.entrySet()) {
            List<OccurrenceMatch> matches = entry.getValue();
            matches.sort(Comparator.comparingInt(OccurrenceMatch::startTokenIndex)
                    .thenComparingInt(OccurrenceMatch::endTokenIndex)
                    .thenComparing(match -> match.form().getId()));

            for (int index = 0; index < matches.size(); index++) {
                OccurrenceMatch match = matches.get(index);
                events.add(new LearningEventEntity(
                        turn.getUserId(),
                        match.form().getLearningUnit().getId(),
                        eventType,
                        LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                        turn.getId(),
                        sourceText,
                        match.form().getFormText(),
                        payload(turn, scenarioCode, sourceField, index + 1, match, scoringSignal, correctionReason)
                ));
            }
        }
    }

    private Map<Long, List<OccurrenceMatch>> matchesByUnit(String sourceText, List<LearningUnitFormEntity> activeForms) {
        List<Token> sourceTokens = tokens(sourceText);
        Map<Long, List<OccurrenceMatch>> matchesByUnit = new TreeMap<>();

        activeForms.stream()
                .filter(form -> form.getLearningUnit().getUnitType() == LearningUnitType.WORD)
                .map(form -> new FormPattern(form, tokens(form.getNormalizedForm())))
                .filter(pattern -> !pattern.tokens().isEmpty())
                .sorted(Comparator.comparingInt((FormPattern pattern) -> pattern.tokens().size()).reversed()
                        .thenComparing(pattern -> pattern.form().getLearningUnit().getId())
                        .thenComparing(pattern -> pattern.form().getId()))
                .forEach(pattern -> addPatternMatches(sourceTokens, pattern, matchesByUnit));

        return matchesByUnit;
    }

    private void addPatternMatches(
            List<Token> sourceTokens,
            FormPattern pattern,
            Map<Long, List<OccurrenceMatch>> matchesByUnit
    ) {
        int size = pattern.tokens().size();
        for (int start = 0; start <= sourceTokens.size() - size; start++) {
            if (!matchesAt(sourceTokens, pattern.tokens(), start)) {
                continue;
            }

            int end = start + size;
            Long unitId = pattern.form().getLearningUnit().getId();
            List<OccurrenceMatch> unitMatches = matchesByUnit.computeIfAbsent(unitId, ignored -> new ArrayList<>());
            if (overlapsExisting(unitMatches, start, end)) {
                continue;
            }

            unitMatches.add(new OccurrenceMatch(pattern.form(), start, end));
        }
    }

    private boolean matchesAt(List<Token> sourceTokens, List<Token> matchTokens, int start) {
        for (int offset = 0; offset < matchTokens.size(); offset++) {
            if (!sourceTokens.get(start + offset).normalized().equals(matchTokens.get(offset).normalized())) {
                return false;
            }
        }
        return true;
    }

    private boolean overlapsExisting(List<OccurrenceMatch> matches, int start, int end) {
        return matches.stream().anyMatch(match -> start < match.endTokenIndex() && end > match.startTokenIndex());
    }

    private List<Token> tokens(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.toLowerCase(Locale.ROOT)
                .replace("’", "'")
                .replace("`", "'");
        List<Token> tokens = new ArrayList<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[a-z0-9]+").matcher(normalizedText);
        while (matcher.find()) {
            tokens.add(new Token(matcher.group(), matcher.start(), matcher.end()));
        }
        return tokens;
    }

    private String payload(
            LearningDialogueTurnEntity turn,
            String scenarioCode,
            String sourceField,
            int occurrenceIndex,
            OccurrenceMatch match,
            Map<String, Object> scoringSignal,
            String correctionReason
    ) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("packageId", turn.getLearningPackageId());
        payload.put("turnIndex", turn.getTurnIndex());
        payload.put("scenarioCode", scenarioCode);
        payload.put("sourceField", sourceField);
        payload.put("occurrenceIndex", occurrenceIndex);
        payload.put("occurrenceStartToken", match.startTokenIndex());
        payload.put("occurrenceEndToken", match.endTokenIndex());
        payload.put("scoringSignal", scoringSignal);
        if (correctionReason != null) {
            payload.put("correctionReason", correctionReason);
        }

        return serializePayload(payload);
    }

    private void validateSenseBelongsToUnit(Long learningUnitId, Long learningUnitSenseId) {
        if (learningUnitSenseId == null) {
            return;
        }

        LearningUnitSenseEntity sense = learningUnitSenseRepository.findByIdWithUnit(learningUnitSenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learning unit sense not found."));

        if (!sense.getLearningUnit().getId().equals(learningUnitId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learning unit sense does not belong to the learning unit.");
        }
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize learning event.");
        }
    }

    private record Token(String normalized, int startOffset, int endOffset) {
    }

    private record FormPattern(LearningUnitFormEntity form, List<Token> tokens) {
    }

    private record OccurrenceMatch(LearningUnitFormEntity form, int startTokenIndex, int endTokenIndex) {
    }
}
