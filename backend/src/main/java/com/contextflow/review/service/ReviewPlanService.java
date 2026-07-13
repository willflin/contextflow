package com.contextflow.review.service;

import com.contextflow.content.domain.DifficultyLevel;
import com.contextflow.content.domain.FrequencyBand;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitSenseScenarioTagEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.LearningUnitSenseScenarioTagRepository;
import com.contextflow.content.repository.UserLearningUnitSenseDeferralRepository;
import com.contextflow.content.repository.UserLearningUnitSenseStatsRepository;
import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.review.dto.ReviewPlanItemResponse;
import com.contextflow.review.dto.ReviewPlanResponse;
import com.contextflow.review.dto.ReviewScenarioGroupResponse;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.repository.UserLevelProfileRepository;
import com.contextflow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewPlanService {

    private static final int DEFAULT_LIMIT = 12;
    private static final int MAX_LIMIT = 50;
    private static final String REVIEW_POOL = "REVIEW";
    private static final String NEW_POOL = "NEW";
    private static final String FALLBACK_SCENARIO_CODE = "general";
    private static final BigDecimal SKIP_PENALTY = new BigDecimal("30.0000");

    private final UserRepository userRepository;
    private final UserLevelProfileRepository userLevelProfileRepository;
    private final UserLearningUnitSenseStatsRepository statsRepository;
    private final UserLearningUnitSenseDeferralRepository deferralRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final LearningUnitSenseScenarioTagRepository scenarioTagRepository;

    public ReviewPlanService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            UserLearningUnitSenseStatsRepository statsRepository,
            UserLearningUnitSenseDeferralRepository deferralRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            LearningUnitSenseScenarioTagRepository scenarioTagRepository
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.statsRepository = statsRepository;
        this.deferralRepository = deferralRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.scenarioTagRepository = scenarioTagRepository;
    }

    @Transactional
    public ReviewPlanResponse plan(String username, Integer requestedLimit) {
        UserEntity user = activeUser(username);
        Instant now = Instant.now();
        int limit = normalizeLimit(requestedLimit);
        CefrLevel userLevel = userLevelProfileRepository.findByUserId(user.getId())
                .map(profile -> profile.getCefrLevel())
                .orElse(CefrLevel.A1);
        Map<Long, BigDecimal> activeDeferrals = activeDeferrals(user.getId(), now);

        List<UserLearningUnitSenseStatsEntity> statsRows = statsRepository.findByUserIdWithSenseAndUnit(user.getId());
        List<Candidate> reviewCandidates = reviewCandidates(statsRows, activeDeferrals);
        int overdueReviewCount = (int) statsRows.stream()
                .filter(stats -> stats.getNextReviewAt() != null && !stats.getNextReviewAt().isAfter(now))
                .count();
        List<Candidate> newCandidates = learningUnitSenseRepository.findNewSenseCandidates(
                        user.getId(),
                        LearningUnitType.WORD,
                        LearningUnitStatus.ACTIVE,
                        LearningUnitStatus.ACTIVE
                )
                .stream()
                .map(sense -> scoreNewSense(sense, userLevel))
                .flatMap(Optional::stream)
                .map(candidate -> applyDeferral(candidate, activeDeferrals))
                .filter(candidate -> candidate.score().compareTo(BigDecimal.ZERO) > 0)
                .sorted(candidateComparator())
                .toList();

        List<Candidate> selectedCandidates = selectCandidates(
                reviewCandidates,
                newCandidates,
                limit,
                reviewRatio(statsRows.size(), overdueReviewCount)
        );
        Map<Long, String> scenarioCodes = scenarioCodes(selectedCandidates);
        List<ReviewPlanItemResponse> items = selectedCandidates.stream()
                .map(candidate -> toItem(candidate, scenarioCodes.getOrDefault(
                        candidate.sense().getId(),
                        FALLBACK_SCENARIO_CODE
                )))
                .toList();

        return new ReviewPlanResponse(
                userLevel.name(),
                limit,
                (int) items.stream().filter(item -> REVIEW_POOL.equals(item.pool())).count(),
                (int) items.stream().filter(item -> NEW_POOL.equals(item.pool())).count(),
                overdueReviewCount,
                "persisted reviewPriorityScore",
                items,
                scenarioGroups(items),
                now
        );
    }

    @Transactional
    public int deferCurrentPlan(String username, int hours) {
        UserEntity user = activeUser(username);
        ReviewPlanResponse currentPlan = plan(username, DEFAULT_LIMIT);
        Instant deferredUntil = Instant.now().plusSeconds(Math.max(1, hours) * 3600L);
        int deferredCount = 0;
        for (ReviewPlanItemResponse item : currentPlan.items()) {
            LearningUnitSenseEntity sense = learningUnitSenseRepository.findById(item.learningUnitSenseId()).orElse(null);
            if (sense == null) {
                continue;
            }
            deferralRepository.findByUserIdAndLearningUnitSenseId(user.getId(), sense.getId())
                    .ifPresentOrElse(
                            existing -> existing.update(SKIP_PENALTY, "TASK_SKIPPED", deferredUntil),
                            () -> deferralRepository.save(new com.contextflow.content.domain.UserLearningUnitSenseDeferralEntity(
                                    user.getId(),
                                    sense,
                                    SKIP_PENALTY,
                                    "TASK_SKIPPED",
                                    deferredUntil
                            ))
                    );
            statsRepository.findByUserIdAndLearningUnitSenseId(user.getId(), sense.getId())
                    .ifPresent(stats -> stats.updateReviewPriorityScore(
                            stats.getReviewPriorityScore().multiply(new BigDecimal("0.6500")),
                            Instant.now()
                    ));
            deferredCount++;
        }
        return deferredCount;
    }

    private List<Candidate> reviewCandidates(
            List<UserLearningUnitSenseStatsEntity> statsRows,
            Map<Long, BigDecimal> activeDeferrals
    ) {
        return statsRows.stream()
                .filter(stats -> stats.getReviewPriorityScore().compareTo(BigDecimal.ZERO) > 0)
                .map(stats -> new Candidate(
                        REVIEW_POOL,
                        stats.getLearningUnitSense(),
                        stats.getReviewPriorityScore(),
                        reviewReasons(stats)
                ))
                .map(candidate -> applyDeferral(candidate, activeDeferrals))
                .filter(candidate -> candidate.score().compareTo(BigDecimal.ZERO) > 0)
                .sorted(candidateComparator())
                .toList();
    }

    private Map<Long, BigDecimal> activeDeferrals(Long userId, Instant now) {
        Map<Long, BigDecimal> result = new HashMap<>();
        for (var deferral : deferralRepository.findByUserIdAndDeferredUntilAfter(userId, now)) {
            result.put(deferral.getLearningUnitSense().getId(), deferral.getPenaltyScore());
        }
        return result;
    }

    private Candidate applyDeferral(Candidate candidate, Map<Long, BigDecimal> activeDeferrals) {
        BigDecimal penalty = activeDeferrals.get(candidate.sense().getId());
        if (penalty == null) {
            return candidate;
        }
        List<String> reasons = new ArrayList<>(candidate.reasons());
        reasons.add("TASK_SKIPPED_DEFERRED");
        return new Candidate(
                candidate.pool(),
                candidate.sense(),
                candidate.score().subtract(penalty).max(BigDecimal.ZERO).setScale(4, RoundingMode.HALF_UP),
                reasons
        );
    }

    private List<String> reviewReasons(UserLearningUnitSenseStatsEntity stats) {
        List<String> reasons = new ArrayList<>();
        reasons.add("PERSISTED_REVIEW_PRIORITY");
        if (stats.getNextReviewAt() != null && !stats.getNextReviewAt().isAfter(Instant.now())) {
            reasons.add("DUE_FOR_REVIEW");
        }
        if (stats.getCorrectionCount() > 0) {
            reasons.add("HAS_CORRECTION");
        }
        if (stats.getAttemptCount() > 0) {
            reasons.add("HAS_LEARNER_OUTPUT");
        }
        if (stats.getDifficultyScore().doubleValue() >= 0.50) {
            reasons.add("HIGH_SCHEDULE_DIFFICULTY");
        }
        return reasons;
    }

    private Optional<Candidate> scoreNewSense(LearningUnitSenseEntity sense, CefrLevel userLevel) {
        List<String> reasons = new ArrayList<>();
        double frequencyScore = newFrequencyScore(sense.getFrequencyBand(), reasons);
        if (frequencyScore < 0) {
            return Optional.empty();
        }
        double levelFitScore = levelFitScore(sense.getDifficultyLevel(), userLevel, reasons);
        if (levelFitScore < 0) {
            return Optional.empty();
        }

        BigDecimal score = BigDecimal.valueOf(frequencyScore + levelFitScore)
                .setScale(4, RoundingMode.HALF_UP);
        return Optional.of(new Candidate(NEW_POOL, sense, score, reasons));
    }

    private double newFrequencyScore(FrequencyBand frequencyBand, List<String> reasons) {
        if (frequencyBand == null) {
            reasons.add("UNKNOWN_FREQUENCY");
            return 15.0;
        }
        return switch (frequencyBand) {
            case VERY_COMMON -> {
                reasons.add("NEW_VERY_COMMON");
                yield 40.0;
            }
            case COMMON -> {
                reasons.add("NEW_COMMON");
                yield 35.0;
            }
            case MEDIUM -> {
                reasons.add("NEW_MEDIUM_FREQUENCY");
                yield 25.0;
            }
            case UNCOMMON -> {
                reasons.add("NEW_UNCOMMON");
                yield 10.0;
            }
            case RARE -> -1.0;
        };
    }

    private double levelFitScore(DifficultyLevel difficultyLevel, CefrLevel userLevel, List<String> reasons) {
        if (difficultyLevel == null) {
            reasons.add("UNKNOWN_LEVEL");
            return 12.0;
        }

        int levelDiff = levelRank(difficultyLevel) - levelRank(userLevel);
        return switch (levelDiff) {
            case 0 -> {
                reasons.add("LEVEL_MATCH");
                yield 30.0;
            }
            case -1 -> {
                reasons.add("ONE_LEVEL_BELOW");
                yield 22.0;
            }
            case 1 -> {
                reasons.add("ONE_LEVEL_ABOVE");
                yield 18.0;
            }
            case -2 -> {
                reasons.add("TWO_LEVELS_BELOW");
                yield 8.0;
            }
            case 2 -> {
                reasons.add("TWO_LEVELS_ABOVE");
                yield 5.0;
            }
            default -> levelDiff >= 3 ? -1.0 : 0.0;
        };
    }

    private int levelRank(DifficultyLevel difficultyLevel) {
        return switch (difficultyLevel) {
            case A1 -> 1;
            case A2 -> 2;
            case B1 -> 3;
            case B2 -> 4;
            case C1 -> 5;
            case C2 -> 6;
        };
    }

    private int levelRank(CefrLevel cefrLevel) {
        return switch (cefrLevel) {
            case A1 -> 1;
            case A2 -> 2;
            case B1 -> 3;
            case B2 -> 4;
            case C1 -> 5;
            case C2 -> 6;
        };
    }

    private List<Candidate> selectCandidates(
            List<Candidate> reviewCandidates,
            List<Candidate> newCandidates,
            int limit,
            double reviewRatio
    ) {
        int reviewSlots = Math.min(reviewCandidates.size(), Math.round((float) (limit * reviewRatio)));
        int newSlots = Math.min(newCandidates.size(), limit - reviewSlots);

        int remaining = limit - reviewSlots - newSlots;
        if (remaining > 0) {
            int extraNew = Math.min(remaining, newCandidates.size() - newSlots);
            newSlots += extraNew;
            remaining -= extraNew;
        }
        if (remaining > 0) {
            int extraReview = Math.min(remaining, reviewCandidates.size() - reviewSlots);
            reviewSlots += extraReview;
        }

        List<Candidate> selected = new ArrayList<>(limit);
        selected.addAll(reviewCandidates.stream().limit(reviewSlots).toList());
        selected.addAll(newCandidates.stream().limit(newSlots).toList());
        return selected;
    }

    private double reviewRatio(int existingStatsCount, int overdueReviewCount) {
        if (existingStatsCount == 0) {
            return 0.40;
        }
        if (overdueReviewCount >= 20) {
            return 0.85;
        }
        if (overdueReviewCount <= 3) {
            return 0.50;
        }
        return 0.70;
    }

    private Comparator<Candidate> candidateComparator() {
        return Comparator.comparing(Candidate::score, Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.sense().getId());
    }

    private Map<Long, String> scenarioCodes(List<Candidate> candidates) {
        List<Long> senseIds = candidates.stream()
                .map(candidate -> candidate.sense().getId())
                .toList();
        Map<Long, String> result = new HashMap<>();
        if (senseIds.isEmpty()) {
            return result;
        }

        for (LearningUnitSenseScenarioTagEntity tag :
                scenarioTagRepository.findByLearningUnitSenseIdInOrderByRelevanceScoreDescScenarioCodeAsc(senseIds)) {
            result.putIfAbsent(tag.getLearningUnitSense().getId(), tag.getScenarioCode());
        }
        return result;
    }

    private ReviewPlanItemResponse toItem(Candidate candidate, String scenarioCode) {
        LearningUnitSenseEntity sense = candidate.sense();
        return new ReviewPlanItemResponse(
                candidate.pool(),
                sense.getLearningUnit().getId(),
                sense.getId(),
                sense.getLearningUnit().getCanonicalText(),
                sense.getSenseKey(),
                sense.getPartOfSpeech() == null ? null : sense.getPartOfSpeech().name(),
                sense.getDefinitionEn(),
                sense.getDefinitionZh(),
                sense.getDifficultyLevel() == null ? null : sense.getDifficultyLevel().name(),
                sense.getFrequencyBand() == null ? null : sense.getFrequencyBand().name(),
                candidate.score(),
                scenarioCode,
                candidate.reasons()
        );
    }

    private List<ReviewScenarioGroupResponse> scenarioGroups(List<ReviewPlanItemResponse> items) {
        Map<String, List<ReviewPlanItemResponse>> groupedItems = new LinkedHashMap<>();
        for (ReviewPlanItemResponse item : items) {
            groupedItems.computeIfAbsent(item.scenarioCode(), ignored -> new ArrayList<>()).add(item);
        }

        return groupedItems.entrySet()
                .stream()
                .map(entry -> new ReviewScenarioGroupResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(ReviewPlanItemResponse::learningUnitSenseId).toList(),
                        entry.getValue()
                ))
                .toList();
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.min(Math.max(requestedLimit, 1), MAX_LIMIT);
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private record Candidate(
            String pool,
            LearningUnitSenseEntity sense,
            BigDecimal score,
            List<String> reasons
    ) {
    }
}
