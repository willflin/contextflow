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

    @Column(name = "first_seen_at")
    private Instant firstSeenAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserLearningUnitSenseStatsEntity() {
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
}
