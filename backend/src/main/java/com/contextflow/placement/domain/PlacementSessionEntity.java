package com.contextflow.placement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "placement_sessions")
public class PlacementSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlacementSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlacementSessionMode mode;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Column(name = "answered_count", nullable = false)
    private Integer answeredCount;

    @Column(name = "max_item_count", nullable = false)
    private Integer maxItemCount;

    @Column(name = "current_difficulty_score", nullable = false)
    private Integer currentDifficultyScore;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "score_percent")
    private BigDecimal scorePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_level", length = 16)
    private CefrLevel estimatedLevel;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlacementSessionEntity() {
    }

    public PlacementSessionEntity(Long userId, Integer itemCount) {
        this.userId = userId;
        this.itemCount = itemCount;
        this.answeredCount = 0;
        this.maxItemCount = itemCount;
        this.currentDifficultyScore = 50;
        this.mode = PlacementSessionMode.BATCH;
        this.status = PlacementSessionStatus.STARTED;
    }

    public static PlacementSessionEntity adaptive(Long userId, Integer maxItemCount, Integer currentDifficultyScore) {
        PlacementSessionEntity session = new PlacementSessionEntity();
        session.userId = userId;
        session.itemCount = maxItemCount;
        session.answeredCount = 0;
        session.maxItemCount = maxItemCount;
        session.currentDifficultyScore = currentDifficultyScore;
        session.mode = PlacementSessionMode.ADAPTIVE;
        session.status = PlacementSessionStatus.STARTED;
        return session;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.startedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void submit(Integer correctCount, BigDecimal scorePercent, CefrLevel estimatedLevel) {
        this.status = PlacementSessionStatus.SUBMITTED;
        this.correctCount = correctCount;
        this.answeredCount = this.itemCount;
        this.scorePercent = scorePercent;
        this.estimatedLevel = estimatedLevel;
        this.submittedAt = Instant.now();
    }

    public void updateAdaptiveProgress(Integer answeredCount, Integer correctCount, Integer currentDifficultyScore) {
        this.answeredCount = answeredCount;
        this.correctCount = correctCount;
        this.currentDifficultyScore = currentDifficultyScore;
    }

    public void finishAdaptive(Integer answeredCount, Integer correctCount, BigDecimal scorePercent, CefrLevel estimatedLevel) {
        this.status = PlacementSessionStatus.SUBMITTED;
        this.answeredCount = answeredCount;
        this.correctCount = correctCount;
        this.scorePercent = scorePercent;
        this.estimatedLevel = estimatedLevel;
        this.submittedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public PlacementSessionStatus getStatus() {
        return status;
    }

    public PlacementSessionMode getMode() {
        return mode;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public Integer getAnsweredCount() {
        return answeredCount;
    }

    public Integer getMaxItemCount() {
        return maxItemCount;
    }

    public Integer getCurrentDifficultyScore() {
        return currentDifficultyScore;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public BigDecimal getScorePercent() {
        return scorePercent;
    }

    public CefrLevel getEstimatedLevel() {
        return estimatedLevel;
    }
}
