package com.contextflow.learning.service;

import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.domain.PartOfSpeech;
import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.UserLearningUnitSenseStatsRepository;
import com.contextflow.learning.domain.LearningEventDirection;
import com.contextflow.learning.domain.LearningEventEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningEventType;
import com.contextflow.review.service.ReviewPriorityCalculator;
import com.contextflow.review.service.ReviewScheduleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearningUnitSenseMasteryServiceTest {

    private LearningUnitSenseRepository senseRepository;
    private UserLearningUnitSenseStatsRepository statsRepository;
    private LearningUnitSenseMasteryService service;

    @BeforeEach
    void setUp() {
        senseRepository = mock(LearningUnitSenseRepository.class);
        statsRepository = mock(UserLearningUnitSenseStatsRepository.class);
        service = new LearningUnitSenseMasteryService(
                senseRepository,
                statsRepository,
                new ReviewPriorityCalculator(),
                new ReviewScheduleCalculator()
        );
    }

    @Test
    void attemptedEventShouldUpdateSenseStats() {
        LearningUnitEntity go = unit(1L, "go");
        LearningUnitSenseEntity sense = sense(100L, go);
        LearningEventEntity event = new LearningEventEntity(
                5L,
                1L,
                100L,
                LearningEventType.UNIT_ATTEMPTED,
                LearningEventDirection.LEARNER_OUTPUT,
                LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                9L,
                "go",
                "go",
                "{}"
        );

        when(senseRepository.findById(100L)).thenReturn(Optional.of(sense));
        when(statsRepository.findByUserIdAndLearningUnitSenseId(5L, 100L)).thenReturn(Optional.empty());

        service.applyEvent(event);

        ArgumentCaptor<UserLearningUnitSenseStatsEntity> captor =
                ArgumentCaptor.forClass(UserLearningUnitSenseStatsEntity.class);
        verify(statsRepository).save(captor.capture());
        assertThat(captor.getValue().getAttemptCount()).isEqualTo(1);
        assertThat(captor.getValue().getLearningUnitSense().getId()).isEqualTo(100L);
        assertThat(captor.getValue().getReviewIntervalHours()).isPositive();
        assertThat(captor.getValue().getNextReviewAt()).isNotNull();
        assertThat(captor.getValue().getReviewPriorityScore()).isNotNull();
    }

    @Test
    void eventWithoutSenseShouldNotUpdateStats() {
        LearningEventEntity event = new LearningEventEntity(
                5L,
                1L,
                null,
                LearningEventType.UNIT_ATTEMPTED,
                LearningEventDirection.LEARNER_OUTPUT,
                LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                9L,
                "bank",
                "bank",
                "{}"
        );

        service.applyEvent(event);

        verify(statsRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private LearningUnitEntity unit(Long id, String text) {
        LearningUnitEntity unit = new LearningUnitEntity(
                LearningUnitType.WORD,
                text,
                text,
                "en",
                LearningUnitStatus.ACTIVE
        );
        setId(unit, id);
        return unit;
    }

    private LearningUnitSenseEntity sense(Long id, LearningUnitEntity unit) {
        LearningUnitSenseEntity sense = new LearningUnitSenseEntity(
                unit,
                "move-travel",
                PartOfSpeech.VERB,
                "definition",
                "释义",
                null,
                null,
                null,
                null,
                LearningUnitStatus.ACTIVE
        );
        setId(sense, id);
        return sense;
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
