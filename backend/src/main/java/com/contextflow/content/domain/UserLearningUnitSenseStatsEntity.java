package com.contextflow.content.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "user_learning_unit_sense_stats")
public class UserLearningUnitSenseStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_unit_sense_id", nullable = false)
    private LearningUnitSenseEntity learningUnitSense;

    @Column(name = "exposure_count", nullable = false)
    private Integer exposureCount;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "incorrect_count", nullable = false)
    private Integer incorrectCount;

    @Column(name = "correction_count", nullable = false)
    private Integer correctionCount;

    @Column(name = "recommendation_count", nullable = false)
    private Integer recommendationCount;

    @Column(name = "mastery_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal masteryScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 32)
    private MasteryLevel masteryLevel;

    @Column(name = "stability_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal stabilityScore;

    @Column(name = "difficulty_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal difficultyScore;

    @Column(name = "first_seen_at")
    private Instant firstSeenAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "review_interval_hours", nullable = false)
    private Integer reviewIntervalHours;

    @Column(name = "review_priority_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal reviewPriorityScore;

    @Column(name = "last_priority_calculated_at")
    private Instant lastPriorityCalculatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserLearningUnitSenseStatsEntity() {
    }

    public UserLearningUnitSenseStatsEntity(Long userId, LearningUnitSenseEntity learningUnitSense) {
        this.userId = userId;
        this.learningUnitSense = learningUnitSense;
        this.exposureCount = 0;
        this.attemptCount = 0;
        this.correctCount = 0;
        this.incorrectCount = 0;
        this.correctionCount = 0;
        this.recommendationCount = 0;
        this.masteryScore = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.masteryLevel = MasteryLevel.UNSEEN;
        this.stabilityScore = new BigDecimal("0.3000");
        this.difficultyScore = new BigDecimal("0.5000");
        this.reviewIntervalHours = 24;
        this.reviewPriorityScore = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    }

    public void recordExposure(Instant occurredAt) {
        this.exposureCount++;
        markSeen(occurredAt);
        recalculateMastery();
    }

    public void recordAttempt(Instant occurredAt) {
        this.attemptCount++;
        this.lastAttemptAt = occurredAt;
        markSeen(occurredAt);
        recalculateMastery();
    }

    public void recordCorrection(Instant occurredAt) {
        this.correctionCount++;
        markSeen(occurredAt);
        recalculateMastery();
    }

    public void recordRecommendation(Instant occurredAt) {
        this.recommendationCount++;
        markSeen(occurredAt);
        recalculateMastery();
    }

    private void markSeen(Instant occurredAt) {
        if (this.firstSeenAt == null) {
            this.firstSeenAt = occurredAt;
        }
        this.lastSeenAt = occurredAt;
    }

    private void recalculateMastery() {
        double score = this.exposureCount * 0.05
                + this.attemptCount * 0.12
                + this.recommendationCount * 0.04
                - this.correctionCount * 0.04;
        score = Math.max(0.0, Math.min(1.0, score));
        this.masteryScore = BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);

        if (this.attemptCount == 0 && this.correctionCount == 0 && this.recommendationCount == 0) {
            this.masteryLevel = MasteryLevel.EXPOSED;
        } else if (score >= 0.85) {
            this.masteryLevel = MasteryLevel.MASTERED;
        } else if (score >= 0.55) {
            this.masteryLevel = MasteryLevel.FAMILIAR;
        } else if (score > 0.0 || this.correctionCount > 0) {
            this.masteryLevel = MasteryLevel.LEARNING;
        } else {
            this.masteryLevel = MasteryLevel.EXPOSED;
        }
    }

    public void updateReviewSchedule(
            BigDecimal stabilityScore,
            BigDecimal difficultyScore,
            Instant lastReviewedAt,
            Integer reviewIntervalHours,
            Instant nextReviewAt
    ) {
        this.stabilityScore = stabilityScore.setScale(4, RoundingMode.HALF_UP);
        this.difficultyScore = difficultyScore.setScale(4, RoundingMode.HALF_UP);
        this.lastReviewedAt = lastReviewedAt;
        this.reviewIntervalHours = reviewIntervalHours;
        this.nextReviewAt = nextReviewAt;
    }

    public void updateReviewPriorityScore(BigDecimal reviewPriorityScore, Instant calculatedAt) {
        this.reviewPriorityScore = reviewPriorityScore.setScale(4, RoundingMode.HALF_UP);
        this.lastPriorityCalculatedAt = calculatedAt;
    }

    public void markMasteredByUserLevelGap(
            Instant occurredAt,
            Instant nextReviewAt,
            int reviewIntervalHours,
            BigDecimal reviewPriorityScore
    ) {
        if (this.firstSeenAt == null) {
            this.firstSeenAt = occurredAt;
        }
        this.lastSeenAt = occurredAt;
        this.lastReviewedAt = occurredAt;
        this.nextReviewAt = nextReviewAt;
        this.reviewIntervalHours = reviewIntervalHours;
        this.masteryScore = BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
        this.masteryLevel = MasteryLevel.MASTERED;
        this.stabilityScore = BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
        this.difficultyScore = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        this.reviewPriorityScore = reviewPriorityScore.setScale(4, RoundingMode.HALF_UP);
        this.lastPriorityCalculatedAt = occurredAt;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LearningUnitSenseEntity getLearningUnitSense() {
        return learningUnitSense;
    }

    public Integer getExposureCount() {
        return exposureCount;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public Integer getCorrectionCount() {
        return correctionCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getIncorrectCount() {
        return incorrectCount;
    }

    public Integer getRecommendationCount() {
        return recommendationCount;
    }

    public BigDecimal getMasteryScore() {
        return masteryScore;
    }

    public MasteryLevel getMasteryLevel() {
        return masteryLevel;
    }

    public BigDecimal getStabilityScore() {
        return stabilityScore;
    }

    public BigDecimal getDifficultyScore() {
        return difficultyScore;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public Integer getReviewIntervalHours() {
        return reviewIntervalHours;
    }

    public BigDecimal getReviewPriorityScore() {
        return reviewPriorityScore;
    }

    public Instant getLastPriorityCalculatedAt() {
        return lastPriorityCalculatedAt;
    }
}
