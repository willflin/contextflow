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
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "learning_unit_forms")
public class LearningUnitFormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_unit_id", nullable = false)
    private LearningUnitEntity learningUnit;

    @Column(name = "form_text", nullable = false, length = 255)
    private String formText;

    @Column(name = "normalized_form", nullable = false, length = 255)
    private String normalizedForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 32)
    private LearningUnitFormType formType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LearningUnitFormEntity() {
    }

    public LearningUnitFormEntity(
            LearningUnitEntity learningUnit,
            String formText,
            String normalizedForm,
            LearningUnitFormType formType
    ) {
        this.learningUnit = learningUnit;
        this.formText = formText;
        this.normalizedForm = normalizedForm;
        this.formType = formType;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public LearningUnitEntity getLearningUnit() {
        return learningUnit;
    }

    public String getFormText() {
        return formText;
    }

    public String getNormalizedForm() {
        return normalizedForm;
    }

    public LearningUnitFormType getFormType() {
        return formType;
    }
}
