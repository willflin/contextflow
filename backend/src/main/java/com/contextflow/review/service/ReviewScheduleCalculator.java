package com.contextflow.review.service;

import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import com.contextflow.learning.domain.LearningEventType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ReviewScheduleCalculator {

    private static final double MIN_STABILITY = 0.1000;
    private static final double MAX_STABILITY = 5.0000;
    private static final double MIN_DIFFICULTY = 0.1000;
    private static final double MAX_DIFFICULTY = 0.9500;
    private static final int MIN_INTERVAL_HOURS = 6;
    private static final int MAX_INTERVAL_HOURS = 720;

    public ReviewScheduleResult calculate(
            UserLearningUnitSenseStatsEntity stats,
            LearningEventType eventType,
            Instant occurredAt
    ) {
        double difficulty = nextDifficulty(stats, eventType);
        double stability = nextStability(stats, eventType, difficulty);
        int intervalHours = nextIntervalHours(stats, eventType, stability, difficulty);
        Instant nextReviewAt = occurredAt.plus(intervalHours, ChronoUnit.HOURS);

        return new ReviewScheduleResult(
                decimal(stability),
                decimal(difficulty),
                occurredAt,
                intervalHours,
                nextReviewAt
        );
    }

    private double nextDifficulty(UserLearningUnitSenseStatsEntity stats, LearningEventType eventType) {
        double score = 0.50
                + stats.getCorrectionCount() * 0.06
                + stats.getIncorrectCount() * 0.08
                - stats.getAttemptCount() * 0.03
                - stats.getExposureCount() * 0.01
                - stats.getRecommendationCount() * 0.01
                - stats.getMasteryScore().doubleValue() * 0.20;

        if (eventType == LearningEventType.UNIT_CORRECTED) {
            score += 0.12;
        } else if (eventType == LearningEventType.UNIT_ATTEMPTED) {
            score -= 0.05;
        }

        return clamp(score, MIN_DIFFICULTY, MAX_DIFFICULTY);
    }

    private double nextStability(
            UserLearningUnitSenseStatsEntity stats,
            LearningEventType eventType,
            double difficulty
    ) {
        double currentStability = stats.getStabilityScore() == null
                ? 0.30
                : stats.getStabilityScore().doubleValue();
        double masteryBoost = stats.getMasteryScore().doubleValue() * 0.18;
        double delta = switch (eventType) {
            case UNIT_ATTEMPTED -> 0.18 + masteryBoost - difficulty * 0.05;
            case UNIT_EXPOSED -> 0.05 + masteryBoost * 0.30;
            case UNIT_RECOMMENDED -> 0.06 + masteryBoost * 0.40;
            case UNIT_CORRECTED -> -0.12 - difficulty * 0.08;
        };

        return clamp(currentStability + delta, MIN_STABILITY, MAX_STABILITY);
    }

    private int nextIntervalHours(
            UserLearningUnitSenseStatsEntity stats,
            LearningEventType eventType,
            double stability,
            double difficulty
    ) {
        double mastery = stats.getMasteryScore().doubleValue();
        double baseHours = switch (eventType) {
            case UNIT_CORRECTED -> 12.0;
            case UNIT_ATTEMPTED -> 24.0;
            case UNIT_EXPOSED, UNIT_RECOMMENDED -> 36.0;
        };

        double interval = baseHours
                * (1.0 + stability)
                * (0.75 + mastery)
                * (1.10 - difficulty);
        return (int) Math.round(clamp(interval, MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS));
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record ReviewScheduleResult(
            BigDecimal stabilityScore,
            BigDecimal difficultyScore,
            Instant lastReviewedAt,
            Integer reviewIntervalHours,
            Instant nextReviewAt
    ) {
    }
}
