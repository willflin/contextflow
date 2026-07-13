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

import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "placement_items")
public class PlacementItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 64)
    private PlacementItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level", nullable = false, length = 16)
    private CefrLevel cefrLevel;

    @Column(name = "difficulty_score", nullable = false)
    private Integer difficultyScore;

    @Column(name = "frequency_rank")
    private Integer frequencyRank;

    @Column(name = "frequency_band", length = 32)
    private String frequencyBand;

    @Column(name = "item_discrimination", nullable = false)
    private BigDecimal itemDiscrimination;

    @Column(name = "guessing_factor", nullable = false)
    private BigDecimal guessingFactor;

    @Column(name = "scenario_tag", nullable = false, length = 64)
    private String scenarioTag;

    @Column(name = "target_skill", nullable = false, length = 64)
    private String targetSkill;

    @Column(name = "ability_dimension", nullable = false, length = 64)
    private String abilityDimension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlacementItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_type", nullable = false, length = 32)
    private PlacementItemGradingType gradingType;

    @Column(name = "content", nullable = false, columnDefinition = "json")
    private String contentJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlacementItemEntity() {
    }

    public PlacementItemEntity(
            PlacementItemType itemType,
            CefrLevel cefrLevel,
            String scenarioTag,
            String targetSkill,
            PlacementItemStatus status,
            String contentJson
    ) {
        this(
                itemType,
                cefrLevel,
                defaultDifficulty(cefrLevel),
                scenarioTag,
                targetSkill,
                targetSkill,
                status,
                PlacementItemGradingType.LOCAL_EXACT,
                contentJson
        );
    }

    public PlacementItemEntity(
            PlacementItemType itemType,
            CefrLevel cefrLevel,
            Integer difficultyScore,
            String scenarioTag,
            String targetSkill,
            String abilityDimension,
            PlacementItemStatus status,
            PlacementItemGradingType gradingType,
            String contentJson
    ) {
        this.itemType = itemType;
        this.cefrLevel = cefrLevel;
        this.difficultyScore = difficultyScore;
        this.itemDiscrimination = BigDecimal.ONE;
        this.guessingFactor = BigDecimal.valueOf(0.25);
        this.scenarioTag = scenarioTag;
        this.targetSkill = targetSkill;
        this.abilityDimension = abilityDimension;
        this.status = status;
        this.gradingType = gradingType;
        this.contentJson = contentJson;
    }

    private static int defaultDifficulty(CefrLevel cefrLevel) {
        return switch (cefrLevel) {
            case A1 -> 20;
            case A2 -> 35;
            case B1 -> 55;
            case B2 -> 75;
            case C1 -> 90;
        };
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

    public PlacementItemType getItemType() {
        return itemType;
    }

    public CefrLevel getCefrLevel() {
        return cefrLevel;
    }

    public Integer getDifficultyScore() {
        return difficultyScore;
    }

    public Integer getFrequencyRank() {
        return frequencyRank;
    }

    public String getFrequencyBand() {
        return frequencyBand;
    }

    public BigDecimal getItemDiscrimination() {
        return itemDiscrimination;
    }

    public BigDecimal getGuessingFactor() {
        return guessingFactor;
    }

    public void configureVocabularyMeasurement(Integer frequencyRank, String frequencyBand, BigDecimal itemDiscrimination, BigDecimal guessingFactor) {
        this.frequencyRank = frequencyRank;
        this.frequencyBand = frequencyBand;
        this.itemDiscrimination = itemDiscrimination == null ? BigDecimal.ONE : itemDiscrimination;
        this.guessingFactor = guessingFactor == null ? BigDecimal.valueOf(0.25) : guessingFactor;
    }

    public String getScenarioTag() {
        return scenarioTag;
    }

    public String getTargetSkill() {
        return targetSkill;
    }

    public String getAbilityDimension() {
        return abilityDimension;
    }

    public PlacementItemStatus getStatus() {
        return status;
    }

    public PlacementItemGradingType getGradingType() {
        return gradingType;
    }

    public String getContentJson() {
        return contentJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
