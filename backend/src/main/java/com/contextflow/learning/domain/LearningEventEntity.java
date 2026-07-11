package com.contextflow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "learning_events")
public class LearningEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "learning_unit_id", nullable = false)
    private Long learningUnitId;

    @Column(name = "learning_unit_sense_id")
    private Long learningUnitSenseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private LearningEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private LearningEventSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "source_text", nullable = false, columnDefinition = "text")
    private String sourceText;

    @Column(name = "occurrence_text", nullable = false, length = 500)
    private String occurrenceText;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LearningEventEntity() {
    }

    public LearningEventEntity(
            Long userId,
            Long learningUnitId,
            LearningEventType eventType,
            LearningEventSourceType sourceType,
            Long sourceId,
            String sourceText,
            String occurrenceText,
            String payload
    ) {
        this(userId, learningUnitId, null, eventType, sourceType, sourceId, sourceText, occurrenceText, payload);
    }

    public LearningEventEntity(
            Long userId,
            Long learningUnitId,
            Long learningUnitSenseId,
            LearningEventType eventType,
            LearningEventSourceType sourceType,
            Long sourceId,
            String sourceText,
            String occurrenceText,
            String payload
    ) {
        this.userId = userId;
        this.learningUnitId = learningUnitId;
        this.learningUnitSenseId = learningUnitSenseId;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.sourceText = sourceText;
        this.occurrenceText = occurrenceText;
        this.payload = payload;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getLearningUnitId() {
        return learningUnitId;
    }

    public Long getLearningUnitSenseId() {
        return learningUnitSenseId;
    }

    public LearningEventType getEventType() {
        return eventType;
    }

    public LearningEventSourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getOccurrenceText() {
        return occurrenceText;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
