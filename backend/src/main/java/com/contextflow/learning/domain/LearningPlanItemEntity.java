package com.contextflow.learning.domain;

import com.contextflow.content.domain.LearningUnitSenseEntity;
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

import java.time.Instant;

@Entity
@Table(name = "learning_plan_items")
public class LearningPlanItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_unit_sense_id", nullable = false)
    private LearningUnitSenseEntity learningUnitSense;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningPlanItemSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningPlanItemStatus status;

    @Column(name = "exclusion_reason", length = 255)
    private String exclusionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningPlanItemEntity() {
    }

    public LearningPlanItemEntity(
            Long userId,
            LearningUnitSenseEntity learningUnitSense,
            LearningPlanItemSource source,
            LearningPlanItemStatus status,
            String exclusionReason
    ) {
        this.userId = userId;
        this.learningUnitSense = learningUnitSense;
        this.source = source;
        this.status = status;
        this.exclusionReason = exclusionReason;
    }

    public void activate(LearningPlanItemSource source) {
        this.source = source;
        this.status = LearningPlanItemStatus.ACTIVE;
        this.exclusionReason = null;
    }

    public void skipOutOfLevel(String reason) {
        this.status = LearningPlanItemStatus.SKIPPED_OUT_OF_LEVEL;
        this.exclusionReason = reason;
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

    public LearningPlanItemSource getSource() {
        return source;
    }

    public LearningPlanItemStatus getStatus() {
        return status;
    }

    public String getExclusionReason() {
        return exclusionReason;
    }
}
