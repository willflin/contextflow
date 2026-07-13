package com.contextflow.content.domain;

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
@Table(name = "learning_units")
public class LearningUnitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 32)
    private LearningUnitType unitType;

    @Column(name = "canonical_text", nullable = false, length = 255)
    private String canonicalText;

    @Column(name = "normalized_text", nullable = false, length = 255)
    private String normalizedText;

    @Column(name = "language_code", nullable = false, length = 16)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningUnitStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningUnitEntity() {
    }

    public LearningUnitEntity(
            LearningUnitType unitType,
            String canonicalText,
            String normalizedText,
            String languageCode,
            LearningUnitStatus status
    ) {
        this.unitType = unitType;
        this.canonicalText = canonicalText;
        this.normalizedText = normalizedText;
        this.languageCode = languageCode;
        this.status = status;
    }

    public void updateWord(String canonicalText, String normalizedText, LearningUnitStatus status) {
        this.canonicalText = canonicalText;
        this.normalizedText = normalizedText;
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

    public LearningUnitType getUnitType() {
        return unitType;
    }

    public String getCanonicalText() {
        return canonicalText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public LearningUnitStatus getStatus() {
        return status;
    }
}
