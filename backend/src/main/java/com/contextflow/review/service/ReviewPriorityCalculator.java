package com.contextflow.review.service;

import com.contextflow.content.domain.FrequencyBand;
import com.contextflow.content.domain.MasteryLevel;
import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewPriorityCalculator {

    public ReviewPriorityResult calculate(UserLearningUnitSenseStatsEntity stats, Instant now) {
        List<String> reasons = new ArrayList<>();
        double rawPriority = dueScore(stats, now, reasons)
                + weaknessScore(stats, reasons)
                + forgettingRiskScore(stats, now, reasons)
                + scheduleDifficultyScore(stats, reasons)
                + outputRiskScore(stats, reasons)
                + correctionScore(stats, reasons)
                + exposureOnlyScore(stats, reasons)
                + recencyDecayScore(stats, now, reasons)
                - masteryPenalty(stats, reasons);

        double multiplier = frequencyMultiplier(stats, reasons);
        double score = Math.max(0.0, rawPriority * multiplier);
        BigDecimal normalizedScore = BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
        return new ReviewPriorityResult(normalizedScore, reasons);
    }

    private double dueScore(UserLearningUnitSenseStatsEntity stats, Instant now, List<String> reasons) {
        Instant nextReviewAt = stats.getNextReviewAt();
        if (nextReviewAt == null) {
            reasons.add("NO_REVIEW_TIME");
            return 10.0;
        }
        if (nextReviewAt.isAfter(now)) {
            return 0.0;
        }

        long overdueHours = Math.max(0, Duration.between(nextReviewAt, now).toHours());
        reasons.add("DUE_FOR_REVIEW");
        return Math.min(40.0 + overdueHours / 6.0, 80.0);
    }

    private double weaknessScore(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        MasteryLevel masteryLevel = stats.getMasteryLevel();
        double score = switch (masteryLevel) {
            case UNSEEN -> 30.0;
            case EXPOSED -> 15.0;
            case LEARNING -> 25.0;
            case FAMILIAR -> 8.0;
            case MASTERED -> 0.0;
        };
        if (score > 0.0) {
            reasons.add("WEAK_MASTERY");
        }
        return score;
    }

    private double outputRiskScore(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        if (stats.getAttemptCount() > 0) {
            reasons.add("HAS_LEARNER_OUTPUT");
            return 20.0;
        }
        return 0.0;
    }

    private double correctionScore(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        if (stats.getCorrectionCount() > 0) {
            reasons.add("HAS_CORRECTION");
        }
        return Math.min(stats.getCorrectionCount() * 8.0, 32.0);
    }

    private double exposureOnlyScore(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        if (stats.getExposureCount() > 0 && stats.getAttemptCount() == 0) {
            reasons.add("EXPOSURE_ONLY");
            return 15.0;
        }
        return 0.0;
    }

    private double recencyDecayScore(UserLearningUnitSenseStatsEntity stats, Instant now, List<String> reasons) {
        if (stats.getLastAttemptAt() != null) {
            long hours = Math.max(0, Duration.between(stats.getLastAttemptAt(), now).toHours());
            double score = Math.min((hours / 24.0) * 4.0, 24.0);
            if (score > 0.0) {
                reasons.add("ATTEMPT_DECAY");
            }
            return score;
        }
        if (stats.getLastSeenAt() != null) {
            long hours = Math.max(0, Duration.between(stats.getLastSeenAt(), now).toHours());
            double score = Math.min((hours / 24.0) * 2.0, 16.0);
            if (score > 0.0) {
                reasons.add("EXPOSURE_DECAY");
            }
            return score;
        }
        return 0.0;
    }

    private double forgettingRiskScore(
            UserLearningUnitSenseStatsEntity stats,
            Instant now,
            List<String> reasons
    ) {
        Instant anchor = stats.getLastReviewedAt() != null ? stats.getLastReviewedAt() : stats.getLastSeenAt();
        if (anchor == null) {
            return 0.0;
        }

        long elapsedHours = Math.max(0, Duration.between(anchor, now).toHours());
        double stabilityHours = Math.max(6.0, stats.getStabilityScore().doubleValue() * 72.0);
        double retention = Math.exp(-elapsedHours / stabilityHours);
        double score = Math.min((1.0 - retention) * 40.0, 40.0);
        if (score >= 8.0) {
            reasons.add("FORGETTING_RISK");
        }
        return score;
    }

    private double scheduleDifficultyScore(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        double score = stats.getDifficultyScore().doubleValue() * 12.0;
        if (score >= 6.0) {
            reasons.add("HIGH_SCHEDULE_DIFFICULTY");
        }
        return score;
    }

    private double masteryPenalty(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        double penalty = stats.getMasteryScore().doubleValue() * 30.0;
        if (penalty > 0.0) {
            reasons.add("MASTERY_PENALTY");
        }
        return penalty;
    }

    private double frequencyMultiplier(UserLearningUnitSenseStatsEntity stats, List<String> reasons) {
        FrequencyBand frequencyBand = stats.getLearningUnitSense().getFrequencyBand();
        if (frequencyBand == null) {
            return 0.85;
        }
        double multiplier = switch (frequencyBand) {
            case VERY_COMMON -> 0.25;
            case COMMON -> 0.45;
            case MEDIUM -> 0.75;
            case UNCOMMON -> 1.00;
            case RARE -> 1.15;
        };
        if (frequencyBand == FrequencyBand.VERY_COMMON || frequencyBand == FrequencyBand.COMMON) {
            reasons.add("HIGH_FREQUENCY_DAMPENED");
        }
        if (stats.getCorrectionCount() > 0 && multiplier < 0.75) {
            reasons.add("CORRECTION_OVERRIDE_FREQUENCY");
            return 0.75;
        }
        return multiplier;
    }

    public record ReviewPriorityResult(BigDecimal score, List<String> reasons) {
    }
}
