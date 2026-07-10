package com.contextflow.user.domain;

import com.contextflow.placement.domain.CefrLevel;
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
@Table(name = "user_level_profiles")
public class UserLevelProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level", nullable = false, length = 16)
    private CefrLevel cefrLevel;

    @Column(name = "dimension_scores", nullable = false, columnDefinition = "json")
    private String dimensionScoresJson;

    @Column(name = "weak_scenarios", nullable = false, columnDefinition = "json")
    private String weakScenariosJson;

    @Column(name = "weak_abilities", nullable = false, columnDefinition = "json")
    private String weakAbilitiesJson;

    @Column(name = "last_placement_session_id")
    private Long lastPlacementSessionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserLevelProfileEntity() {
    }

    public UserLevelProfileEntity(
            Long userId,
            CefrLevel cefrLevel,
            String dimensionScoresJson,
            String weakScenariosJson,
            String weakAbilitiesJson,
            Long lastPlacementSessionId
    ) {
        this.userId = userId;
        update(cefrLevel, dimensionScoresJson, weakScenariosJson, weakAbilitiesJson, lastPlacementSessionId);
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

    public void update(
            CefrLevel cefrLevel,
            String dimensionScoresJson,
            String weakScenariosJson,
            String weakAbilitiesJson,
            Long lastPlacementSessionId
    ) {
        this.cefrLevel = cefrLevel;
        this.dimensionScoresJson = dimensionScoresJson;
        this.weakScenariosJson = weakScenariosJson;
        this.weakAbilitiesJson = weakAbilitiesJson;
        this.lastPlacementSessionId = lastPlacementSessionId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public CefrLevel getCefrLevel() {
        return cefrLevel;
    }

    public String getDimensionScoresJson() {
        return dimensionScoresJson;
    }

    public String getWeakScenariosJson() {
        return weakScenariosJson;
    }

    public String getWeakAbilitiesJson() {
        return weakAbilitiesJson;
    }

    public Long getLastPlacementSessionId() {
        return lastPlacementSessionId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
