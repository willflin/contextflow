package com.contextflow.content.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_learning_unit_sense_deferrals")
public class UserLearningUnitSenseDeferralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_unit_sense_id", nullable = false)
    private LearningUnitSenseEntity learningUnitSense;

    @Column(name = "penalty_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal penaltyScore;

    @Column(nullable = false, length = 64)
    private String reason;

    @Column(name = "deferred_until", nullable = false)
    private Instant deferredUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserLearningUnitSenseDeferralEntity() {
    }

    public UserLearningUnitSenseDeferralEntity(
            Long userId,
            LearningUnitSenseEntity learningUnitSense,
            BigDecimal penaltyScore,
            String reason,
            Instant deferredUntil
    ) {
        this.userId = userId;
        this.learningUnitSense = learningUnitSense;
        update(penaltyScore, reason, deferredUntil);
    }

    public void update(BigDecimal penaltyScore, String reason, Instant deferredUntil) {
        this.penaltyScore = penaltyScore.setScale(4, RoundingMode.HALF_UP);
        this.reason = reason;
        this.deferredUntil = deferredUntil;
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

    public LearningUnitSenseEntity getLearningUnitSense() {
        return learningUnitSense;
    }

    public BigDecimal getPenaltyScore() {
        return penaltyScore;
    }
}
