package com.contextflow.content.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "learning_data_sources")
public class LearningDataSourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @Column(name = "source_version", length = 100)
    private String sourceVersion;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "license_name", nullable = false, length = 100)
    private String licenseName;

    @Column(length = 1000)
    private String attribution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningDataSourceEntity() {
    }

    public LearningDataSourceEntity(
            String sourceName,
            String sourceVersion,
            String sourceUrl,
            String licenseName,
            String attribution
    ) {
        this.sourceName = sourceName;
        this.sourceVersion = sourceVersion;
        this.sourceUrl = sourceUrl;
        this.licenseName = licenseName;
        this.attribution = attribution;
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

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public String getAttribution() {
        return attribution;
    }
}
