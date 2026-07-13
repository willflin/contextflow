package com.contextflow.learning.domain;

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

import java.time.Instant;

@Entity
@Table(name = "learning_packages")
public class LearningPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "scenario_template_id", nullable = false)
    private Long scenarioTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningPackageStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_source", nullable = false, length = 32)
    private LearningPackageGenerationSource generationSource;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "json")
    private String content;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningPackageEntity() {
    }

    public LearningPackageEntity(
            Long userId,
            Long scenarioTemplateId,
            LearningPackageStatus status,
            LearningPackageGenerationSource generationSource,
            String title,
            String content,
            Instant assignedAt,
            Instant expiresAt
    ) {
        this.userId = userId;
        this.scenarioTemplateId = scenarioTemplateId;
        this.status = status;
        this.generationSource = generationSource;
        this.title = title;
        this.content = content;
        this.assignedAt = assignedAt;
        this.expiresAt = expiresAt;
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

    public Long getScenarioTemplateId() {
        return scenarioTemplateId;
    }

    public LearningPackageStatus getStatus() {
        return status;
    }

    public void markCompleted() {
        this.status = LearningPackageStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markExpired() {
        this.status = LearningPackageStatus.EXPIRED;
        this.expiresAt = Instant.now();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
