package com.contextflow.scenario.domain;

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
@Table(name = "scenario_templates")
public class ScenarioTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "difficulty_score", nullable = false)
    private Integer difficultyScore;

    @Column(name = "target_abilities", nullable = false, columnDefinition = "json")
    private String targetAbilitiesJson;

    @Column(name = "applicable_levels", nullable = false, columnDefinition = "json")
    private String applicableLevelsJson;

    @Column(name = "risk_tags", nullable = false, columnDefinition = "json")
    private String riskTagsJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScenarioTemplateStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ScenarioTemplateEntity() {
    }

    public ScenarioTemplateEntity(
            String code,
            String name,
            String description,
            Integer difficultyScore,
            String targetAbilitiesJson,
            String applicableLevelsJson,
            String riskTagsJson,
            ScenarioTemplateStatus status
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.difficultyScore = difficultyScore;
        this.targetAbilitiesJson = targetAbilitiesJson;
        this.applicableLevelsJson = applicableLevelsJson;
        this.riskTagsJson = riskTagsJson;
        this.status = status;
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDifficultyScore() {
        return difficultyScore;
    }

    public String getTargetAbilitiesJson() {
        return targetAbilitiesJson;
    }

    public String getApplicableLevelsJson() {
        return applicableLevelsJson;
    }

    public ScenarioTemplateStatus getStatus() {
        return status;
    }
}
