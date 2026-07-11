package com.contextflow.learning.service;

import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitFormEntity;
import com.contextflow.content.domain.LearningUnitFormType;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.domain.PartOfSpeech;
import com.contextflow.content.repository.LearningUnitFormRepository;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import com.contextflow.learning.domain.LearningEventEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningEventType;
import com.contextflow.learning.repository.LearningEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearningEventServiceTest {

    private LearningUnitFormRepository formRepository;
    private LearningUnitSenseRepository senseRepository;
    private LearningEventRepository eventRepository;
    private LearningEventService service;

    @BeforeEach
    void setUp() {
        formRepository = mock(LearningUnitFormRepository.class);
        senseRepository = mock(LearningUnitSenseRepository.class);
        eventRepository = mock(LearningEventRepository.class);
        service = new LearningEventService(formRepository, senseRepository, eventRepository, new ObjectMapper());
    }

    @Test
    void dialogueEventsShouldCreateOneRowPerOccurrence() {
        LearningUnitEntity go = unit(1L, "go");
        LearningUnitFormEntity goForm = form(10L, go, "go");
        LearningDialogueTurnEntity turn = new LearningDialogueTurnEntity(
                5L,
                8L,
                1,
                "go go",
                "Please go now.",
                "Good.",
                "[]",
                "go",
                "{}"
        );
        setId(turn, 99L);

        when(formRepository.findAllWithUnitByUnitStatus(LearningUnitStatus.ACTIVE)).thenReturn(List.of(goForm));

        service.recordDialogueTurnEvents(turn, "general", List.of(), Map.of());

        verify(eventRepository).saveAll(argThat(events -> {
            int count = 0;
            for (LearningEventEntity ignored : events) {
                count++;
            }
            return count == 4;
        }));
    }

    @Test
    void wordMatchingShouldRespectWordBoundaries() {
        LearningUnitEntity good = unit(1L, "good");
        LearningUnitFormEntity goodForm = form(10L, good, "good");
        LearningDialogueTurnEntity turn = new LearningDialogueTurnEntity(
                5L,
                8L,
                1,
                "This is not goodbye.",
                "Goodbye.",
                "Goodbye.",
                "[]",
                "good",
                "{}"
        );
        setId(turn, 99L);

        when(formRepository.findAllWithUnitByUnitStatus(LearningUnitStatus.ACTIVE))
                .thenReturn(List.of(goodForm));

        service.recordDialogueTurnEvents(turn, "bank_account", List.of(), Map.of());

        verify(eventRepository).saveAll(argThat(events -> {
            int goodCount = 0;
            for (LearningEventEntity event : events) {
                if (event.getLearningUnitId().equals(1L)) {
                    goodCount++;
                }
            }
            return goodCount == 1;
        }));
    }

    @Test
    void dialogueEventsShouldIgnoreNonWordUnitsForNow() {
        LearningUnitEntity bankAccount = unit(2L, "bank account", LearningUnitType.PHRASE);
        LearningUnitFormEntity bankAccountForm = form(20L, bankAccount, "bank account");
        LearningDialogueTurnEntity turn = new LearningDialogueTurnEntity(
                5L,
                8L,
                1,
                "Could you help me open a bank account?",
                "Sure.",
                "Good.",
                "[]",
                "Could you help me open a bank account?",
                "{}"
        );
        setId(turn, 99L);

        when(formRepository.findAllWithUnitByUnitStatus(LearningUnitStatus.ACTIVE))
                .thenReturn(List.of(bankAccountForm));

        service.recordDialogueTurnEvents(turn, "bank_account", List.of(), Map.of());

        verify(eventRepository, never()).saveAll(any());
    }

    @Test
    void recordUnitOccurrenceShouldAllowNullSense() {
        when(eventRepository.save(any(LearningEventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.recordUnitOccurrence(
                1L,
                2L,
                null,
                LearningEventType.UNIT_EXPOSED,
                LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                3L,
                "go",
                "go",
                Map.of()
        );

        verify(eventRepository).save(any(LearningEventEntity.class));
    }

    @Test
    void recordUnitOccurrenceShouldRejectSenseFromAnotherUnit() {
        LearningUnitEntity anotherUnit = unit(10L, "bank");
        LearningUnitSenseEntity sense = new LearningUnitSenseEntity(
                anotherUnit,
                "financial-institution",
                PartOfSpeech.NOUN,
                "definition",
                "释义",
                null,
                null,
                null,
                null,
                LearningUnitStatus.ACTIVE
        );
        setId(sense, 20L);

        when(senseRepository.findByIdWithUnit(20L)).thenReturn(Optional.of(sense));

        assertThatThrownBy(() -> service.recordUnitOccurrence(
                1L,
                2L,
                20L,
                LearningEventType.UNIT_EXPOSED,
                LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                3L,
                "bank",
                "bank",
                Map.of()
        )).isInstanceOf(ResponseStatusException.class);
    }

    private LearningUnitEntity unit(Long id, String text) {
        return unit(id, text, LearningUnitType.WORD);
    }

    private LearningUnitEntity unit(Long id, String text, LearningUnitType unitType) {
        LearningUnitEntity unit = new LearningUnitEntity(
                unitType,
                text,
                text,
                "en",
                LearningUnitStatus.ACTIVE
        );
        setId(unit, id);
        return unit;
    }

    private LearningUnitFormEntity form(Long id, LearningUnitEntity unit, String formText) {
        LearningUnitFormEntity form = new LearningUnitFormEntity(unit, formText, formText, LearningUnitFormType.LEMMA);
        setId(form, id);
        return form;
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
