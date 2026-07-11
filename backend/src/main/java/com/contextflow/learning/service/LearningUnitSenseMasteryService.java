package com.contextflow.learning.service;

import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.UserLearningUnitSenseStatsRepository;
import com.contextflow.learning.domain.LearningEventEntity;
import com.contextflow.learning.domain.LearningEventType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LearningUnitSenseMasteryService {

    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final UserLearningUnitSenseStatsRepository statsRepository;

    public LearningUnitSenseMasteryService(
            LearningUnitSenseRepository learningUnitSenseRepository,
            UserLearningUnitSenseStatsRepository statsRepository
    ) {
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.statsRepository = statsRepository;
    }

    public void applyEvents(List<LearningEventEntity> events) {
        events.stream()
                .filter(event -> event.getLearningUnitSenseId() != null)
                .forEach(this::applyEvent);
    }

    public void applyEvent(LearningEventEntity event) {
        if (event.getLearningUnitSenseId() == null) {
            return;
        }

        LearningUnitSenseEntity sense = learningUnitSenseRepository.findById(event.getLearningUnitSenseId())
                .orElseThrow(() -> new IllegalStateException("Learning unit sense not found."));
        UserLearningUnitSenseStatsEntity stats = statsRepository
                .findByUserIdAndLearningUnitSenseId(event.getUserId(), event.getLearningUnitSenseId())
                .orElseGet(() -> new UserLearningUnitSenseStatsEntity(event.getUserId(), sense));

        Instant occurredAt = event.getCreatedAt() == null ? Instant.now() : event.getCreatedAt();
        applyEventType(stats, event.getEventType(), occurredAt);
        statsRepository.save(stats);
    }

    private void applyEventType(
            UserLearningUnitSenseStatsEntity stats,
            LearningEventType eventType,
            Instant occurredAt
    ) {
        switch (eventType) {
            case UNIT_ATTEMPTED -> stats.recordAttempt(occurredAt);
            case UNIT_EXPOSED -> stats.recordExposure(occurredAt);
            case UNIT_CORRECTED -> stats.recordCorrection(occurredAt);
            case UNIT_RECOMMENDED -> stats.recordRecommendation(occurredAt);
        }
    }
}
