package com.contextflow.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "learning_dialogue_turns")
public class LearningDialogueTurnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "learning_package_id", nullable = false)
    private Long learningPackageId;

    @Column(name = "turn_index", nullable = false)
    private Integer turnIndex;

    @Column(name = "user_message", nullable = false, columnDefinition = "text")
    private String userMessage;

    @Column(name = "roleplay_reply", nullable = false, columnDefinition = "text")
    private String roleplayReply;

    @Column(name = "mentor_feedback", nullable = false, columnDefinition = "text")
    private String mentorFeedback;

    @Column(nullable = false, columnDefinition = "json")
    private String corrections;

    @Column(name = "natural_expression", nullable = false, columnDefinition = "text")
    private String naturalExpression;

    @Column(name = "scoring_signal", nullable = false, columnDefinition = "json")
    private String scoringSignal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LearningDialogueTurnEntity() {
    }

    public LearningDialogueTurnEntity(
            Long userId,
            Long learningPackageId,
            Integer turnIndex,
            String userMessage,
            String roleplayReply,
            String mentorFeedback,
            String corrections,
            String naturalExpression,
            String scoringSignal
    ) {
        this.userId = userId;
        this.learningPackageId = learningPackageId;
        this.turnIndex = turnIndex;
        this.userMessage = userMessage;
        this.roleplayReply = roleplayReply;
        this.mentorFeedback = mentorFeedback;
        this.corrections = corrections;
        this.naturalExpression = naturalExpression;
        this.scoringSignal = scoringSignal;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getLearningPackageId() {
        return learningPackageId;
    }

    public Integer getTurnIndex() {
        return turnIndex;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getRoleplayReply() {
        return roleplayReply;
    }

    public String getMentorFeedback() {
        return mentorFeedback;
    }

    public String getCorrections() {
        return corrections;
    }

    public String getNaturalExpression() {
        return naturalExpression;
    }

    public String getScoringSignal() {
        return scoringSignal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
