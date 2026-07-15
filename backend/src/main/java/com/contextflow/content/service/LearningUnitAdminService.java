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
import com.contextflow.content.dto.AdminSenseUpdateRequest;
import com.contextflow.content.dto.AdminWordCreateRequest;
import com.contextflow.content.dto.AdminWordFormRequest;
import com.contextflow.content.dto.AdminWordUpdateRequest;
import com.contextflow.content.dto.LearningUnitDetailResponse;
import com.contextflow.content.repository.LearningDataSourceRepository;
import com.contextflow.content.repository.LearningUnitFormRepository;
import com.contextflow.content.repository.LearningUnitRepository;
import com.contextflow.content.repository.LearningUnitSenseSourceRepository;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.UserLearningUnitSenseStatsRepository;
import com.contextflow.learning.repository.LearningEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class LearningUnitAdminService {

    private static final String LANGUAGE_CODE = "en";
    private static final String MANUAL_SOURCE_NAME = "ADMIN_MANUAL";
    private static final String MANUAL_SOURCE_VERSION = "v1";

    private final LearningUnitRepository learningUnitRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final LearningUnitFormRepository learningUnitFormRepository;
    private final LearningDataSourceRepository learningDataSourceRepository;
    private final LearningUnitSenseSourceRepository learningUnitSenseSourceRepository;
    private final UserLearningUnitSenseStatsRepository statsRepository;
    private final LearningEventRepository learningEventRepository;
    private final LearningUnitQueryService learningUnitQueryService;
    private final JdbcTemplate jdbcTemplate;

    public LearningUnitAdminService(
            LearningUnitRepository learningUnitRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            LearningUnitFormRepository learningUnitFormRepository,
            LearningDataSourceRepository learningDataSourceRepository,
            LearningUnitSenseSourceRepository learningUnitSenseSourceRepository,
            UserLearningUnitSenseStatsRepository statsRepository,
            LearningEventRepository learningEventRepository,
            LearningUnitQueryService learningUnitQueryService,
            JdbcTemplate jdbcTemplate
    ) {
        this.learningUnitRepository = learningUnitRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.learningUnitFormRepository = learningUnitFormRepository;
        this.learningDataSourceRepository = learningDataSourceRepository;
        this.learningUnitSenseSourceRepository = learningUnitSenseSourceRepository;
        this.statsRepository = statsRepository;
        this.learningEventRepository = learningEventRepository;
        this.learningUnitQueryService = learningUnitQueryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public LearningUnitDetailResponse createWord(AdminWordCreateRequest request) {
        String canonicalText = cleanText(request.canonicalText(), "canonicalText is required.");
        String normalizedText = normalizeWord(canonicalText);
        validateSingleWord(normalizedText);
        learningUnitRepository.findByLanguageCodeAndUnitTypeAndNormalizedText(
                LANGUAGE_CODE,
                LearningUnitType.WORD,
                normalizedText
        ).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Word already exists.");
        });

        LearningUnitEntity unit = learningUnitRepository.save(new LearningUnitEntity(
                LearningUnitType.WORD,
                canonicalText,
                normalizedText,
                LANGUAGE_CODE,
                LearningUnitStatus.ACTIVE
        ));
        saveForm(unit, canonicalText, LearningUnitFormType.LEMMA);
        saveExtraForms(unit, request.forms());

        LearningUnitSenseEntity sense = learningUnitSenseRepository.save(new LearningUnitSenseEntity(
                unit,
                "admin-manual:" + normalizedText + ":1",
                parseEnum(request.partOfSpeech(), PartOfSpeech.class),
                cleanText(request.definitionEn(), "definitionEn is required."),
                trimToNull(request.definitionZh()),
                parseEnum(request.difficultyLevel(), DifficultyLevel.class),
                null,
                normalizeScore(request.frequencyScore()),
                parseEnum(request.frequencyBand(), FrequencyBand.class),
                LearningUnitStatus.ACTIVE
        ));
        linkManualSource(sense);
        learningUnitSenseRepository.flush();
        refreshLearningUnitDifficultySortKey(unit.getId());

        return learningUnitQueryService.detailForAdmin(unit.getId());
    }

    @Transactional
    public LearningUnitDetailResponse updateWord(Long id, AdminWordUpdateRequest request) {
        LearningUnitEntity unit = wordUnit(id);
        String canonicalText = trimToNull(request.canonicalText()) == null
                ? unit.getCanonicalText()
                : request.canonicalText().trim();
        String normalizedText = normalizeWord(canonicalText);
        validateSingleWord(normalizedText);
        LearningUnitStatus status = request.status() == null
                ? unit.getStatus()
                : parseRequiredEnum(request.status(), LearningUnitStatus.class);

        learningUnitRepository.findByLanguageCodeAndUnitTypeAndNormalizedText(
                        LANGUAGE_CODE,
                        LearningUnitType.WORD,
                        normalizedText
                )
                .filter(existing -> !existing.getId().equals(unit.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Word already exists.");
                });

        unit.updateWord(canonicalText, normalizedText, status);
        return learningUnitQueryService.detailForAdmin(unit.getId());
    }

    @Transactional
    public LearningUnitDetailResponse updateSense(Long senseId, AdminSenseUpdateRequest request) {
        LearningUnitSenseEntity sense = learningUnitSenseRepository.findByIdWithUnit(senseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit sense not found."));
        if (sense.getLearningUnit().getUnitType() != LearningUnitType.WORD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only WORD senses can be managed here.");
        }

        String definitionEn = trimToNull(request.definitionEn()) == null
                ? sense.getDefinitionEn()
                : request.definitionEn().trim();
        sense.updateAdminFields(
                request.partOfSpeech() == null ? sense.getPartOfSpeech() : parseEnum(request.partOfSpeech(), PartOfSpeech.class),
                definitionEn,
                request.definitionZh() == null ? sense.getDefinitionZh() : trimToNull(request.definitionZh()),
                request.difficultyLevel() == null ? sense.getDifficultyLevel() : parseEnum(request.difficultyLevel(), DifficultyLevel.class),
                request.difficultyConfidence() == null ? sense.getDifficultyConfidence() : normalizeScore(request.difficultyConfidence()),
                request.frequencyScore() == null ? sense.getFrequencyScore() : normalizeScore(request.frequencyScore()),
                request.frequencyBand() == null ? sense.getFrequencyBand() : parseEnum(request.frequencyBand(), FrequencyBand.class),
                request.status() == null ? sense.getStatus() : parseRequiredEnum(request.status(), LearningUnitStatus.class)
        );
        learningUnitSenseRepository.flush();
        refreshLearningUnitDifficultySortKey(sense.getLearningUnit().getId());

        return learningUnitQueryService.detailForAdmin(sense.getLearningUnit().getId());
    }

    @Transactional
    public void deleteWord(Long id) {
        LearningUnitEntity unit = wordUnit(id);
        if (learningEventRepository.countByLearningUnitId(id) > 0
                || statsRepository.countByLearningUnitSenseLearningUnitId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Word has learning events or mastery stats. Set it OFFLINE instead of deleting."
            );
        }
        jdbcTemplate.update(
                "UPDATE learning_unit_sense_feedback SET learning_unit_id = NULL WHERE learning_unit_id = ?",
                id
        );
        learningUnitRepository.delete(unit);
    }

    private LearningUnitEntity wordUnit(Long id) {
        LearningUnitEntity unit = learningUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning unit not found."));
        if (unit.getUnitType() != LearningUnitType.WORD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only WORD units can be managed here.");
        }
        return unit;
    }

    private void saveExtraForms(LearningUnitEntity unit, Iterable<AdminWordFormRequest> forms) {
        if (forms == null) {
            return;
        }
        Set<String> insertedKeys = new HashSet<>();
        insertedKeys.add(unit.getNormalizedText() + ":" + LearningUnitFormType.LEMMA.name());
        for (AdminWordFormRequest form : forms) {
            String formText = trimToNull(form.formText());
            if (formText == null) {
                continue;
            }
            LearningUnitFormType formType = form.formType() == null
                    ? LearningUnitFormType.VARIANT
                    : parseRequiredEnum(form.formType(), LearningUnitFormType.class);
            String normalizedForm = normalize(formText);
            String key = normalizedForm + ":" + formType.name();
            if (insertedKeys.add(key)) {
                saveForm(unit, formText, formType);
            }
        }
    }

    private void saveForm(LearningUnitEntity unit, String formText, LearningUnitFormType formType) {
        learningUnitFormRepository.save(new LearningUnitFormEntity(
                unit,
                formText.trim(),
                normalize(formText),
                formType
        ));
    }

    private void linkManualSource(LearningUnitSenseEntity sense) {
        LearningDataSourceEntity dataSource = learningDataSourceRepository
                .findBySourceNameAndSourceVersion(MANUAL_SOURCE_NAME, MANUAL_SOURCE_VERSION)
                .orElseGet(() -> learningDataSourceRepository.save(new LearningDataSourceEntity(
                        MANUAL_SOURCE_NAME,
                        MANUAL_SOURCE_VERSION,
                        null,
                        "MANUAL",
                        "Admin-created single-word entry in ContextFlow."
                )));
        learningUnitSenseSourceRepository.save(new LearningUnitSenseSourceEntity(
                sense,
                dataSource,
                SenseSourceAttributeType.SENSE,
                "admin:" + sense.getId()
        ));
    }

    private void refreshLearningUnitDifficultySortKey(Long learningUnitId) {
        jdbcTemplate.update("""
                UPDATE learning_units unit
                LEFT JOIN (
                    SELECT
                        sense.learning_unit_id,
                        MIN(NULLIF(FIELD(sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0)) AS min_rank,
                        MAX(NULLIF(FIELD(sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0)) AS max_rank
                    FROM learning_unit_senses sense
                    WHERE sense.learning_unit_id = ?
                      AND sense.status = 'ACTIVE'
                    GROUP BY sense.learning_unit_id
                ) ranks ON ranks.learning_unit_id = unit.id
                SET
                    unit.difficulty_min_rank = ranks.min_rank,
                    unit.difficulty_max_rank = ranks.max_rank
                WHERE unit.id = ?
                """, learningUnitId, learningUnitId);
    }

    private String normalizeWord(String text) {
        return normalize(text);
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("'", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private void validateSingleWord(String normalizedText) {
        if (!normalizedText.matches("[a-z]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one alphabetic English word is allowed.");
        }
    }

    private String cleanText(String text, String errorMessage) {
        String value = trimToNull(text);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must be between 0 and 1.");
        }
        return score;
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumType) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        return parseRequiredEnum(normalized, enumType);
    }

    private <T extends Enum<T>> T parseRequiredEnum(String value, Class<T> enumType) {
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + enumType.getSimpleName() + ".");
        }
    }
}
