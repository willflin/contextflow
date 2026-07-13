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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "learning_unit_senses")
public class LearningUnitSenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_unit_id", nullable = false)
    private LearningUnitEntity learningUnit;

    @Column(name = "sense_key", nullable = false, length = 255)
    private String senseKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech", length = 32)
    private PartOfSpeech partOfSpeech;

    @Column(name = "definition_en", nullable = false, length = 1000)
    private String definitionEn;

    @Column(name = "definition_zh", length = 1000)
    private String definitionZh;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 16)
    private DifficultyLevel difficultyLevel;

    @Column(name = "difficulty_confidence", precision = 5, scale = 4)
    private BigDecimal difficultyConfidence;

    @Column(name = "frequency_score", precision = 10, scale = 4)
    private BigDecimal frequencyScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_band", length = 32)
    private FrequencyBand frequencyBand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningUnitStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningUnitSenseEntity() {
    }

    public LearningUnitSenseEntity(
            LearningUnitEntity learningUnit,
            String senseKey,
            PartOfSpeech partOfSpeech,
            String definitionEn,
            String definitionZh,
            DifficultyLevel difficultyLevel,
            BigDecimal difficultyConfidence,
            BigDecimal frequencyScore,
            FrequencyBand frequencyBand,
            LearningUnitStatus status
    ) {
        this.learningUnit = learningUnit;
        this.senseKey = senseKey;
        this.partOfSpeech = partOfSpeech;
        this.definitionEn = definitionEn;
        this.definitionZh = definitionZh;
        this.difficultyLevel = difficultyLevel;
        this.difficultyConfidence = difficultyConfidence;
        this.frequencyScore = frequencyScore;
        this.frequencyBand = frequencyBand;
        this.status = status;
    }

    public void updateAdminFields(
            PartOfSpeech partOfSpeech,
            String definitionEn,
            String definitionZh,
            DifficultyLevel difficultyLevel,
            BigDecimal difficultyConfidence,
            BigDecimal frequencyScore,
            FrequencyBand frequencyBand,
            LearningUnitStatus status
    ) {
        this.partOfSpeech = partOfSpeech;
        this.definitionEn = definitionEn;
        this.definitionZh = definitionZh;
        this.difficultyLevel = difficultyLevel;
        this.difficultyConfidence = difficultyConfidence;
        this.frequencyScore = frequencyScore;
        this.frequencyBand = frequencyBand;
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

    public LearningUnitEntity getLearningUnit() {
        return learningUnit;
    }

    public String getSenseKey() {
        return senseKey;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getDefinitionEn() {
        return definitionEn;
    }

    public String getDefinitionZh() {
        return definitionZh;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public BigDecimal getDifficultyConfidence() {
        return difficultyConfidence;
    }

    public BigDecimal getFrequencyScore() {
        return frequencyScore;
    }

    public FrequencyBand getFrequencyBand() {
        return frequencyBand;
    }

    public LearningUnitStatus getStatus() {
        return status;
    }
}
