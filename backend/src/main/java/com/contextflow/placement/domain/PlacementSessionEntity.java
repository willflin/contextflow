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

    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

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
        this.status = PlacementSessionStatus.STARTED;
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

    public Integer getItemCount() {
        return itemCount;
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
