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
        return search(text, true);
    }

    @Transactional(readOnly = true)
    public LearningUnitDetailResponse searchForAdmin(String text) {
        return search(text, false);
    }

    private LearningUnitDetailResponse search(String text, boolean activeSensesOnly) {
        String normalized = normalize(text);
        LearningUnitEntity unit = learningUnitRepository
                .findFirstByLanguageCodeAndNormalizedTextOrderByIdAsc(LANGUAGE_CODE, normalized)
                .or(() -> learningUnitFormRepository.findByNormalizedFormWithUnit(normalized)
                        .stream()
                        .map(LearningUnitFormEntity::getLearningUnit)
                        .findFirst())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));

        return detail(unit, activeSensesOnly);
    }

    @Transactional(readOnly = true)
    public LearningUnitDetailResponse detail(Long id) {
        return detail(id, true);
    }

    @Transactional(readOnly = true)
    public LearningUnitDetailResponse detailForAdmin(Long id) {
        return detail(id, false);
    }

    private LearningUnitDetailResponse detail(Long id, boolean activeSensesOnly) {
        LearningUnitEntity unit = learningUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));
        return detail(unit, activeSensesOnly);
    }

    @Transactional(readOnly = true)
    public List<LearningUnitSenseResponse> senses(Long id) {
        return senses(id, true);
    }

    @Transactional(readOnly = true)
    public List<LearningUnitSenseResponse> sensesForAdmin(Long id) {
        return senses(id, false);
    }

    private List<LearningUnitSenseResponse> senses(Long id, boolean activeSensesOnly) {
        if (!learningUnitRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found.");
        }
        List<LearningUnitSenseEntity> senseRows = activeSensesOnly
                ? learningUnitSenseRepository.findByLearningUnitIdAndStatusOrderByIdAsc(id, LearningUnitStatus.ACTIVE)
                : learningUnitSenseRepository.findByLearningUnitIdOrderByIdAsc(id);
        return senseRows
                .stream()
                .map(this::senseResponse)
                .toList();
    }

    private LearningUnitDetailResponse detail(LearningUnitEntity unit, boolean activeSensesOnly) {
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
        List<LearningUnitSenseEntity> senseRows = activeSensesOnly
                ? learningUnitSenseRepository.findByLearningUnitIdAndStatusOrderByIdAsc(
                unit.getId(),
                LearningUnitStatus.ACTIVE
        )
                : learningUnitSenseRepository.findByLearningUnitIdOrderByIdAsc(unit.getId());
        List<LearningUnitSenseResponse> senses = senseRows
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
