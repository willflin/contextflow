package com.contextflow.content.service;

import com.contextflow.content.domain.DifficultyLevel;
import com.contextflow.content.domain.FrequencyBand;
import com.contextflow.content.domain.LearningDataSourceEntity;
import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitFormEntity;
import com.contextflow.content.domain.LearningUnitFormType;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitSenseSourceEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.domain.PartOfSpeech;
import com.contextflow.content.domain.SenseSourceAttributeType;
import com.contextflow.content.repository.LearningUnitFormRepository;
import com.contextflow.content.repository.LearningUnitRepository;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.LearningUnitSenseSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LearningUnitQueryServiceTest {

    private LearningUnitRepository unitRepository;
    private LearningUnitFormRepository formRepository;
    private LearningUnitSenseRepository senseRepository;
    private LearningUnitSenseSourceRepository sourceRepository;
    private LearningUnitQueryService service;

    @BeforeEach
    void setUp() {
        unitRepository = mock(LearningUnitRepository.class);
        formRepository = mock(LearningUnitFormRepository.class);
        senseRepository = mock(LearningUnitSenseRepository.class);
        sourceRepository = mock(LearningUnitSenseSourceRepository.class);
        service = new LearningUnitQueryService(unitRepository, formRepository, senseRepository, sourceRepository);
    }

    @Test
    void searchShouldResolveWentToGoByForm() {
        LearningUnitEntity go = unit(1L, "go");
        LearningUnitFormEntity went = form(10L, go, "went", LearningUnitFormType.PAST_TENSE);
        LearningUnitSenseEntity sense = sense(100L, go, "move-travel", PartOfSpeech.VERB);

        when(unitRepository.findFirstByLanguageCodeAndNormalizedTextOrderByIdAsc("en", "went"))
                .thenReturn(Optional.empty());
        when(formRepository.findByNormalizedFormWithUnit("went")).thenReturn(List.of(went));
        when(formRepository.findByLearningUnitIdOrderByIdAsc(1L)).thenReturn(List.of(went));
        when(senseRepository.findByLearningUnitIdAndStatusOrderByIdAsc(1L, LearningUnitStatus.ACTIVE))
                .thenReturn(List.of(sense));
        when(sourceRepository.findBySenseIdWithDataSourceOrderByIdAsc(100L)).thenReturn(List.of());

        var response = service.search("went");

        assertThat(response.canonicalText()).isEqualTo("go");
        assertThat(response.forms()).extracting("formText").contains("went");
    }

    @Test
    void bankShouldReturnMultipleSenses() {
        LearningUnitEntity bank = unit(2L, "bank");
        LearningUnitSenseEntity finance = sense(201L, bank, "financial-institution", PartOfSpeech.NOUN);
        LearningUnitSenseEntity river = sense(202L, bank, "river-side", PartOfSpeech.NOUN);

        when(unitRepository.existsById(2L)).thenReturn(true);
        when(senseRepository.findByLearningUnitIdAndStatusOrderByIdAsc(2L, LearningUnitStatus.ACTIVE))
                .thenReturn(List.of(finance, river));
        when(sourceRepository.findBySenseIdWithDataSourceOrderByIdAsc(201L)).thenReturn(List.of());
        when(sourceRepository.findBySenseIdWithDataSourceOrderByIdAsc(202L)).thenReturn(List.of());

        var response = service.senses(2L);

        assertThat(response).hasSize(2);
        assertThat(response).extracting("senseKey").containsExactly("financial-institution", "river-side");
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

    private LearningUnitFormEntity form(Long id, LearningUnitEntity unit, String formText, LearningUnitFormType formType) {
        LearningUnitFormEntity form = new LearningUnitFormEntity(unit, formText, formText, formType);
        setId(form, id);
        return form;
    }

    private LearningUnitSenseEntity sense(Long id, LearningUnitEntity unit, String senseKey, PartOfSpeech partOfSpeech) {
        LearningUnitSenseEntity sense = new LearningUnitSenseEntity(
                unit,
                senseKey,
                partOfSpeech,
                "definition",
                "释义",
                DifficultyLevel.A1,
                new BigDecimal("1.0000"),
                null,
                FrequencyBand.COMMON,
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
