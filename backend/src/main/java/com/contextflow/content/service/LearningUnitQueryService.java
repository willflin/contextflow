package com.contextflow.content.service;

import com.contextflow.content.domain.LearningDataSourceEntity;
import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitFormEntity;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitSenseSourceEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.dto.LearningDataSourceSummaryResponse;
import com.contextflow.content.dto.LearningUnitDetailResponse;
import com.contextflow.content.dto.LearningUnitFormResponse;
import com.contextflow.content.dto.LearningUnitSenseResponse;
import com.contextflow.content.repository.LearningUnitFormRepository;
import com.contextflow.content.repository.LearningUnitRepository;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.LearningUnitSenseSourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class LearningUnitQueryService {

    private static final String LANGUAGE_CODE = "en";

    private final LearningUnitRepository learningUnitRepository;
    private final LearningUnitFormRepository learningUnitFormRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final LearningUnitSenseSourceRepository learningUnitSenseSourceRepository;

    public LearningUnitQueryService(
            LearningUnitRepository learningUnitRepository,
            LearningUnitFormRepository learningUnitFormRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            LearningUnitSenseSourceRepository learningUnitSenseSourceRepository
    ) {
        this.learningUnitRepository = learningUnitRepository;
        this.learningUnitFormRepository = learningUnitFormRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.learningUnitSenseSourceRepository = learningUnitSenseSourceRepository;
    }

    @Transactional(readOnly = true)
    public LearningUnitDetailResponse search(String text) {
        String normalized = normalize(text);
        LearningUnitEntity unit = learningUnitRepository
                .findFirstByLanguageCodeAndNormalizedTextOrderByIdAsc(LANGUAGE_CODE, normalized)
                .or(() -> learningUnitFormRepository.findByNormalizedFormWithUnit(normalized)
                        .stream()
                        .map(LearningUnitFormEntity::getLearningUnit)
                        .findFirst())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));

        return detail(unit);
    }

    @Transactional(readOnly = true)
    public LearningUnitDetailResponse detail(Long id) {
        LearningUnitEntity unit = learningUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));
        return detail(unit);
    }

    @Transactional(readOnly = true)
    public List<LearningUnitSenseResponse> senses(Long id) {
        if (!learningUnitRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found.");
        }
        return learningUnitSenseRepository
                .findByLearningUnitIdAndStatusOrderByIdAsc(id, LearningUnitStatus.ACTIVE)
                .stream()
                .map(this::senseResponse)
                .toList();
    }

    private LearningUnitDetailResponse detail(LearningUnitEntity unit) {
        List<LearningUnitFormResponse> forms = learningUnitFormRepository
                .findByLearningUnitIdOrderByIdAsc(unit.getId())
                .stream()
                .map(form -> new LearningUnitFormResponse(
                        form.getId(),
                        form.getFormText(),
                        form.getNormalizedForm(),
                        form.getFormType().name()
                ))
                .toList();
        List<LearningUnitSenseResponse> senses = learningUnitSenseRepository
                .findByLearningUnitIdAndStatusOrderByIdAsc(unit.getId(), LearningUnitStatus.ACTIVE)
                .stream()
                .map(this::senseResponse)
                .toList();

        return new LearningUnitDetailResponse(
                unit.getId(),
                unit.getUnitType().name(),
                unit.getCanonicalText(),
                unit.getNormalizedText(),
                unit.getLanguageCode(),
                unit.getStatus().name(),
                forms,
                senses
        );
    }

    private LearningUnitSenseResponse senseResponse(LearningUnitSenseEntity sense) {
        List<LearningDataSourceSummaryResponse> sources = learningUnitSenseSourceRepository
                .findBySenseIdWithDataSourceOrderByIdAsc(sense.getId())
                .stream()
                .map(this::sourceResponse)
                .toList();

        return new LearningUnitSenseResponse(
                sense.getId(),
                sense.getSenseKey(),
                sense.getPartOfSpeech() == null ? null : sense.getPartOfSpeech().name(),
                sense.getDefinitionEn(),
                sense.getDefinitionZh(),
                sense.getDifficultyLevel() == null ? null : sense.getDifficultyLevel().name(),
                sense.getDifficultyConfidence(),
                sense.getFrequencyScore(),
                sense.getFrequencyBand() == null ? null : sense.getFrequencyBand().name(),
                sense.getStatus().name(),
                sources
        );
    }

    private LearningDataSourceSummaryResponse sourceResponse(LearningUnitSenseSourceEntity sourceLink) {
        LearningDataSourceEntity dataSource = sourceLink.getDataSource();
        return new LearningDataSourceSummaryResponse(
                dataSource.getSourceName(),
                dataSource.getSourceVersion(),
                dataSource.getSourceUrl(),
                dataSource.getLicenseName(),
                dataSource.getAttribution(),
                sourceLink.getAttributeType().name(),
                sourceLink.getSourceRecordId()
        );
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("'", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
