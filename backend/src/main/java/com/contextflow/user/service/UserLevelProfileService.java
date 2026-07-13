package com.contextflow.user.service;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementSessionAnswerEntity;
import com.contextflow.placement.domain.PlacementSessionEntity;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserLevelProfileEntity;
import com.contextflow.user.dto.UserLevelProfileResponse;
import com.contextflow.user.repository.UserLevelProfileRepository;
import com.contextflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserLevelProfileService {

    private static final int WEAK_THRESHOLD_PERCENT = 60;

    private final UserRepository userRepository;
    private final UserLevelProfileRepository userLevelProfileRepository;
    private final ObjectMapper objectMapper;

    public UserLevelProfileService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public UserLevelProfileResponse getProfile(String username) {
        UserEntity user = activeUser(username);
        return userLevelProfileRepository.findByUserId(user.getId())
                .map(UserLevelProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User level profile not found."));
    }

    @Transactional
    public void upsertFromPlacementResult(
            UserEntity user,
            PlacementSessionEntity session,
            CefrLevel cefrLevel,
            List<PlacementSessionAnswerEntity> answerRows,
            Map<Long, PlacementItemEntity> itemMap
    ) {
        ProfileSnapshot snapshot = buildSnapshot(answerRows, itemMap);

        UserLevelProfileEntity profile = userLevelProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserLevelProfileEntity(
                        user.getId(),
                        cefrLevel,
                        snapshot.dimensionScoresJson(),
                        snapshot.weakScenariosJson(),
                        snapshot.weakAbilitiesJson(),
                        session.getId()
                ));

        profile.update(
                cefrLevel,
                snapshot.dimensionScoresJson(),
                snapshot.weakScenariosJson(),
                snapshot.weakAbilitiesJson(),
                session.getId()
        );
        profile.updateVocabularyMeasurement(
                snapshot.vocabularySizeEstimate(),
                snapshot.vocabularyBand(),
                snapshot.vocabularyMeasurementError()
        );
        userLevelProfileRepository.save(profile);
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private ProfileSnapshot buildSnapshot(
            List<PlacementSessionAnswerEntity> answerRows,
            Map<Long, PlacementItemEntity> itemMap
    ) {
        Map<String, ScoreCounter> abilityCounters = new LinkedHashMap<>();
        Map<String, ScoreCounter> scenarioCounters = new LinkedHashMap<>();

        for (PlacementSessionAnswerEntity answer : answerRows) {
            PlacementItemEntity item = itemMap.get(answer.getItemId());
            if (item == null || answer.getCorrect() == null) {
                continue;
            }
            boolean correct = Boolean.TRUE.equals(answer.getCorrect());
            abilityCounters.computeIfAbsent(item.getAbilityDimension(), ignored -> new ScoreCounter()).record(correct);
            scenarioCounters.computeIfAbsent(item.getScenarioTag(), ignored -> new ScoreCounter()).record(correct);
        }

        Map<String, Integer> dimensionScores = percentMap(abilityCounters);
        List<String> weakAbilities = weakKeys(abilityCounters);
        List<String> weakScenarios = weakKeys(scenarioCounters);

        int vocabularySizeEstimate = estimateVocabularySize(answerRows, itemMap);
        return new ProfileSnapshot(
                toJson(dimensionScores),
                toJson(weakScenarios),
                toJson(weakAbilities),
                vocabularySizeEstimate,
                vocabularyBand(vocabularySizeEstimate),
                measurementError((int) answerRows.stream().filter(PlacementSessionAnswerEntity::isAnswered).count())
        );
    }

    private int estimateVocabularySize(
            List<PlacementSessionAnswerEntity> answerRows,
            Map<Long, PlacementItemEntity> itemMap
    ) {
        Map<String, ScoreCounter> bandCounters = new LinkedHashMap<>();
        for (PlacementSessionAnswerEntity answer : answerRows) {
            PlacementItemEntity item = itemMap.get(answer.getItemId());
            if (item == null || answer.getCorrect() == null || item.getFrequencyBand() == null) {
                continue;
            }
            bandCounters.computeIfAbsent(item.getFrequencyBand(), ignored -> new ScoreCounter())
                    .record(Boolean.TRUE.equals(answer.getCorrect()));
        }
        if (bandCounters.size() < 2) {
            return nullSafeDifficultyEstimate(answerRows);
        }
        int estimate = 0;
        for (String band : List.of("TOP_1000", "TOP_2000", "TOP_3000", "TOP_5000", "TOP_8000", "TOP_12000", "TOP_16000", "TOP_20000")) {
            ScoreCounter counter = bandCounters.get(band);
            if (counter == null) {
                continue;
            }
            double masteryProbability = (counter.correct + 0.5d) / (counter.total + 1.0d);
            estimate += Math.round((float) (bandWidth(band) * masteryProbability));
        }
        return Math.max(500, Math.min(20000, estimate));
    }

    private int nullSafeDifficultyEstimate(List<PlacementSessionAnswerEntity> answerRows) {
        int answered = 0;
        int correct = 0;
        for (PlacementSessionAnswerEntity answer : answerRows) {
            if (answer.getCorrect() == null) {
                continue;
            }
            answered++;
            if (Boolean.TRUE.equals(answer.getCorrect())) {
                correct++;
            }
        }
        if (answered == 0) {
            return 1000;
        }
        int percent = Math.round((correct * 100.0f) / answered);
        if (percent >= 85) {
            return 8000;
        }
        if (percent >= 65) {
            return 5000;
        }
        if (percent >= 40) {
            return 2500;
        }
        return 1000;
    }

    private int bandWidth(String band) {
        return switch (band) {
            case "TOP_1000", "TOP_2000", "TOP_3000" -> 1000;
            case "TOP_5000" -> 2000;
            case "TOP_8000" -> 3000;
            case "TOP_12000", "TOP_16000", "TOP_20000" -> 4000;
            default -> 0;
        };
    }

    private String vocabularyBand(int vocabularySizeEstimate) {
        if (vocabularySizeEstimate >= 12000) {
            return "12000+";
        }
        if (vocabularySizeEstimate >= 8000) {
            return "8000-12000";
        }
        if (vocabularySizeEstimate >= 5000) {
            return "5000-8000";
        }
        if (vocabularySizeEstimate >= 3000) {
            return "3000-5000";
        }
        if (vocabularySizeEstimate >= 2000) {
            return "2000-3000";
        }
        if (vocabularySizeEstimate >= 1000) {
            return "1000-2000";
        }
        return "0-1000";
    }

    private int measurementError(int answeredCount) {
        return Math.max(600, 2600 - answeredCount * 140);
    }

    private Map<String, Integer> percentMap(Map<String, ScoreCounter> counters) {
        Map<String, Integer> result = new LinkedHashMap<>();
        counters.forEach((key, counter) -> result.put(key, counter.percent()));
        return result;
    }

    private List<String> weakKeys(Map<String, ScoreCounter> counters) {
        List<String> result = new ArrayList<>();
        counters.forEach((key, counter) -> {
            if (counter.percent() < WEAK_THRESHOLD_PERCENT) {
                result.add(key);
            }
        });
        return result;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build user level profile.");
        }
    }

    private record ProfileSnapshot(
            String dimensionScoresJson,
            String weakScenariosJson,
            String weakAbilitiesJson,
            int vocabularySizeEstimate,
            String vocabularyBand,
            int vocabularyMeasurementError
    ) {
    }

    private static final class ScoreCounter {
        private int total;
        private int correct;

        private void record(boolean isCorrect) {
            total++;
            if (isCorrect) {
                correct++;
            }
        }

        private int percent() {
            if (total == 0) {
                return 0;
            }
            return Math.round((correct * 100.0f) / total);
        }
    }
}
