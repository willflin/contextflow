package com.contextflow.placement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "placement_session_answers")
public class PlacementSessionAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "grading_type", nullable = false, length = 32)
    private PlacementItemGradingType gradingType;

    @Column(name = "difficulty_score", nullable = false)
    private Integer difficultyScore;

    @Column(name = "selected_option_index")
    private Integer selectedOptionIndex;

    @Column(name = "raw_answer", columnDefinition = "text")
    private String rawAnswer;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "judge_payload", columnDefinition = "json")
    private String judgePayload;

    @Column(name = "answered_at")
    private Instant answeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlacementSessionAnswerEntity() {
    }

    public PlacementSessionAnswerEntity(Long sessionId, Long itemId, Integer itemOrder) {
        this(sessionId, itemId, itemOrder, PlacementItemGradingType.LOCAL_EXACT, 50);
    }

    public PlacementSessionAnswerEntity(
            Long sessionId,
            Long itemId,
            Integer itemOrder,
            PlacementItemGradingType gradingType,
            Integer difficultyScore
    ) {
        this.sessionId = sessionId;
        this.itemId = itemId;
        this.itemOrder = itemOrder;
        this.gradingType = gradingType;
        this.difficultyScore = difficultyScore;
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

    public void submit(Integer selectedOptionIndex, boolean correct) {
        this.selectedOptionIndex = selectedOptionIndex;
        this.correct = correct;
        this.answeredAt = Instant.now();
    }

    public void submit(Integer selectedOptionIndex, String rawAnswer, boolean correct, String judgePayload) {
        this.selectedOptionIndex = selectedOptionIndex;
        this.rawAnswer = rawAnswer;
        this.correct = correct;
        this.judgePayload = judgePayload;
        this.answeredAt = Instant.now();
    }

    public Long getItemId() {
        return itemId;
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public PlacementItemGradingType getGradingType() {
        return gradingType;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public boolean isAnswered() {
        return correct != null;
    }
}
