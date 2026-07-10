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

    @Column(name = "scenario_tag", nullable = false, length = 64)
    private String scenarioTag;

    @Column(name = "target_skill", nullable = false, length = 64)
    private String targetSkill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlacementItemStatus status;

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
        this.itemType = itemType;
        this.cefrLevel = cefrLevel;
        this.scenarioTag = scenarioTag;
        this.targetSkill = targetSkill;
        this.status = status;
        this.contentJson = contentJson;
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

    public String getScenarioTag() {
        return scenarioTag;
    }

    public String getTargetSkill() {
        return targetSkill;
    }

    public PlacementItemStatus getStatus() {
        return status;
    }

    public String getContentJson() {
        return contentJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
