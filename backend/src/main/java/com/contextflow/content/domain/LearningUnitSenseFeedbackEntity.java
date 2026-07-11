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

import java.time.Instant;

@Entity
@Table(name = "learning_unit_sense_feedback")
public class LearningUnitSenseFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reported_by_user_id")
    private Long reportedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_unit_id")
    private LearningUnitEntity learningUnit;

    @Column(name = "surface_text", nullable = false, length = 255)
    private String surfaceText;

    @Column(name = "normalized_text", nullable = false, length = 255)
    private String normalizedText;

    @Column(name = "source_type", length = 32)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_text", columnDefinition = "text")
    private String sourceText;

    @Column(name = "suggested_definition_en", length = 1000)
    private String suggestedDefinitionEn;

    @Column(name = "suggested_definition_zh", length = 1000)
    private String suggestedDefinitionZh;

    @Column(name = "agent_reason", length = 1000)
    private String agentReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningUnitSenseFeedbackStatus status;

    @Column(columnDefinition = "json")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningUnitSenseFeedbackEntity() {
    }

    public LearningUnitSenseFeedbackEntity(
            Long reportedByUserId,
            LearningUnitEntity learningUnit,
            String surfaceText,
            String normalizedText,
            String sourceType,
            Long sourceId,
            String sourceText,
            String suggestedDefinitionEn,
            String suggestedDefinitionZh,
            String agentReason,
            String payload
    ) {
        this.reportedByUserId = reportedByUserId;
        this.learningUnit = learningUnit;
        this.surfaceText = surfaceText;
        this.normalizedText = normalizedText;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.sourceText = sourceText;
        this.suggestedDefinitionEn = suggestedDefinitionEn;
        this.suggestedDefinitionZh = suggestedDefinitionZh;
        this.agentReason = agentReason;
        this.status = LearningUnitSenseFeedbackStatus.PENDING;
        this.payload = payload;
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

    public Long getReportedByUserId() {
        return reportedByUserId;
    }

    public LearningUnitEntity getLearningUnit() {
        return learningUnit;
    }

    public String getSurfaceText() {
        return surfaceText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getSuggestedDefinitionEn() {
        return suggestedDefinitionEn;
    }

    public String getSuggestedDefinitionZh() {
        return suggestedDefinitionZh;
    }

    public String getAgentReason() {
        return agentReason;
    }

    public LearningUnitSenseFeedbackStatus getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
