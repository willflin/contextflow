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
@Table(name = "learning_unit_sense_sources")
public class LearningUnitSenseSourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sense_id", nullable = false)
    private LearningUnitSenseEntity sense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_source_id", nullable = false)
    private LearningDataSourceEntity dataSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false, length = 32)
    private SenseSourceAttributeType attributeType;

    @Column(name = "source_record_id", length = 255)
    private String sourceRecordId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LearningUnitSenseSourceEntity() {
    }

    public LearningUnitSenseSourceEntity(
            LearningUnitSenseEntity sense,
            LearningDataSourceEntity dataSource,
            SenseSourceAttributeType attributeType,
            String sourceRecordId
    ) {
        this.sense = sense;
        this.dataSource = dataSource;
        this.attributeType = attributeType;
        this.sourceRecordId = sourceRecordId;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public LearningDataSourceEntity getDataSource() {
        return dataSource;
    }

    public SenseSourceAttributeType getAttributeType() {
        return attributeType;
    }

    public String getSourceRecordId() {
        return sourceRecordId;
    }
}
