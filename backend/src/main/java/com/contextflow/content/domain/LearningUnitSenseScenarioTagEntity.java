package com.contextflow.content.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "learning_unit_sense_scenario_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_learning_unit_sense_scenario_tags",
                columnNames = {"learning_unit_sense_id", "scenario_code"}
        ),
        indexes = @Index(
                name = "idx_learning_unit_sense_scenario_tags_scenario",
                columnList = "scenario_code,relevance_score"
        )
)
public class LearningUnitSenseScenarioTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_unit_sense_id", nullable = false)
    private LearningUnitSenseEntity learningUnitSense;

    @Column(name = "scenario_code", nullable = false, length = 120)
    private String scenarioCode;

    @Column(name = "relevance_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal relevanceScore;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LearningUnitSenseScenarioTagEntity() {
    }

    public LearningUnitSenseScenarioTagEntity(
            LearningUnitSenseEntity learningUnitSense,
            String scenarioCode,
            BigDecimal relevanceScore,
            String source
    ) {
        this.learningUnitSense = learningUnitSense;
        this.scenarioCode = scenarioCode;
        this.relevanceScore = relevanceScore;
        this.source = source;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public LearningUnitSenseEntity getLearningUnitSense() {
        return learningUnitSense;
    }

    public String getScenarioCode() {
        return scenarioCode;
    }

    public BigDecimal getRelevanceScore() {
        return relevanceScore;
    }

    public String getSource() {
        return source;
    }
}
