package com.contextflow.content.service;

import com.contextflow.content.domain.DifficultyLevel;
import com.contextflow.content.dto.VocabularyListResponse;
import com.contextflow.content.dto.VocabularySenseProgressResponse;
import com.contextflow.content.dto.VocabularyWordResponse;
import com.contextflow.learning.service.LearningPlanService;
import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserLevelProfileEntity;
import com.contextflow.user.repository.UserLevelProfileRepository;
import com.contextflow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VocabularyQueryService {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 500;
    private static final int LOW_LEVEL_GAP = 2;

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final UserLevelProfileRepository userLevelProfileRepository;
    private final LearningPlanService learningPlanService;

    public VocabularyQueryService(
            JdbcTemplate jdbcTemplate,
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            LearningPlanService learningPlanService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.learningPlanService = learningPlanService;
    }

    @Transactional(readOnly = true)
    public VocabularyListResponse list(
            String username,
            String status,
            String query,
            Integer requestedPage,
            Integer requestedPageSize,
            String sort
    ) {
        UserEntity user = userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
        String statusFilter = normalizeStatus(status);
        String sortMode = normalizeSort(sort);
        String normalizedQuery = normalizeQuery(query);
        int page = normalizePage(requestedPage);
        int pageSize = normalizePageSize(requestedPageSize);
        int offset = page * pageSize;
        String likeQuery = like(normalizedQuery);
        CefrLevel learnerLevel = userLevelProfileRepository.findByUserId(user.getId())
                .map(UserLevelProfileEntity::getCefrLevel)
                .orElse(CefrLevel.A1);

        List<Object> countParameters = baseParameters(user.getId(), statusFilter, likeQuery, learnerLevel);
        Integer totalMatchedWords = jdbcTemplate.queryForObject(
                countSql(statusFilter, normalizedQuery, learnerLevel),
                Integer.class,
                countParameters.toArray()
        );
        int total = totalMatchedWords == null ? 0 : totalMatchedWords;
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);

        List<Object> pageParameters = new ArrayList<>(countParameters);
        addSortParameters(pageParameters, sortMode, learnerLevel);
        pageParameters.add(pageSize);
        pageParameters.add(offset);
        List<Long> pageUnitIds = jdbcTemplate.query(
                pageSql(statusFilter, normalizedQuery, learnerLevel, sortMode),
                (rows, rowNumber) -> rows.getLong("id"),
                pageParameters.toArray()
        );

        Map<Long, WordBuilder> words = new LinkedHashMap<>();
        if (pageUnitIds.isEmpty()) {
            return new VocabularyListResponse(
                    statusFilter,
                    query == null ? null : query.trim(),
                    page,
                    pageSize,
                    total,
                    totalPages,
                    List.of()
            );
        }

        String placeholders = String.join(",", pageUnitIds.stream().map(ignored -> "?").toList());
        List<Object> detailParameters = new ArrayList<>();
        detailParameters.add(user.getId());
        detailParameters.addAll(pageUnitIds);
        jdbcTemplate.query("""
                SELECT
                    unit.id AS unit_id,
                    unit.canonical_text,
                    unit.normalized_text,
                    sense.id AS sense_id,
                    sense.sense_key,
                    sense.part_of_speech,
                    sense.definition_en,
                    sense.definition_zh,
                    sense.difficulty_level,
                    sense.frequency_band,
                    stats.mastery_level,
                    stats.mastery_score,
                    stats.exposure_count,
                    stats.attempt_count,
                    stats.last_seen_at,
                    stats.next_review_at
                FROM learning_units unit
                JOIN learning_unit_senses sense ON sense.learning_unit_id = unit.id
                LEFT JOIN user_learning_unit_sense_stats stats
                    ON stats.learning_unit_sense_id = sense.id AND stats.user_id = ?
                WHERE unit.id IN (%s)
                  AND sense.status = 'ACTIVE'
                ORDER BY unit.normalized_text ASC, sense.id ASC
                """.formatted(placeholders), rows -> {
            Long unitId = rows.getLong("unit_id");
            String canonicalText = rows.getString("canonical_text");
            String normalizedText = rows.getString("normalized_text");
            WordBuilder word = words.computeIfAbsent(unitId, ignored -> new WordBuilder(
                    unitId,
                    canonicalText,
                    normalizedText
            ));
            boolean learned = rows.getString("mastery_level") != null;
            Integer learnerLevelGap = learnerLevelGap(learnerLevel, rows.getString("difficulty_level"));
            boolean lowLevelCandidate = !learned && learnerLevelGap != null && learnerLevelGap >= LOW_LEVEL_GAP;
            word.senses.add(new VocabularySenseProgressResponse(
                    rows.getLong("sense_id"),
                    rows.getString("sense_key"),
                    rows.getString("part_of_speech"),
                    rows.getString("definition_en"),
                    rows.getString("definition_zh"),
                    rows.getString("difficulty_level"),
                    rows.getString("frequency_band"),
                    false,
                    lowLevelCandidate,
                    learnerLevelGap,
                    learned,
                    rows.getString("mastery_level"),
                    rows.getBigDecimal("mastery_score"),
                    (Integer) rows.getObject("exposure_count"),
                    (Integer) rows.getObject("attempt_count"),
                    instant(rows.getTimestamp("last_seen_at")),
                    instant(rows.getTimestamp("next_review_at"))
            ));
        }, detailParameters.toArray());

        List<Long> plannedSenseIds = learningPlanService.activePlannedSenseIds(
                user.getId(),
                words.values()
                        .stream()
                        .flatMap(word -> word.senses.stream())
                        .map(VocabularySenseProgressResponse::senseId)
                        .toList()
        );

        List<VocabularyWordResponse> items = words.values()
                .stream()
                .map(word -> word.toResponse(plannedSenseIds))
                .sorted((left, right) -> Integer.compare(
                        pageUnitIds.indexOf(left.learningUnitId()),
                        pageUnitIds.indexOf(right.learningUnitId())
                ))
                .toList();

        return new VocabularyListResponse(
                statusFilter,
                query == null ? null : query.trim(),
                page,
                pageSize,
                total,
                totalPages,
                items
        );
    }

    private String countSql(String statusFilter, String normalizedQuery, CefrLevel learnerLevel) {
        return "SELECT COUNT(*) FROM learning_units unit WHERE " + baseWhere(statusFilter, normalizedQuery, learnerLevel);
    }

    private String pageSql(String statusFilter, String normalizedQuery, CefrLevel learnerLevel, String sortMode) {
        return """
                SELECT unit.id
                FROM learning_units unit
                WHERE %s
                ORDER BY %s
                LIMIT ? OFFSET ?
                """.formatted(baseWhere(statusFilter, normalizedQuery, learnerLevel), orderBySql(sortMode));
    }

    private String orderBySql(String sortMode) {
        return switch (sortMode) {
            case "DIFFICULTY_ASC" -> """
                    COALESCE((
                        SELECT MIN(NULLIF(FIELD(sort_sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0))
                        FROM learning_unit_senses sort_sense
                        WHERE sort_sense.learning_unit_id = unit.id
                          AND sort_sense.status = 'ACTIVE'
                    ), 999) ASC,
                    unit.normalized_text ASC
                    """;
            case "DIFFICULTY_DESC" -> """
                    COALESCE((
                        SELECT MAX(NULLIF(FIELD(sort_sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0))
                        FROM learning_unit_senses sort_sense
                        WHERE sort_sense.learning_unit_id = unit.id
                          AND sort_sense.status = 'ACTIVE'
                    ), -1) DESC,
                    unit.normalized_text ASC
                    """;
            default -> """
                    COALESCE((
                        SELECT MIN(ABS(NULLIF(FIELD(sort_sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0) - ?))
                        FROM learning_unit_senses sort_sense
                        WHERE sort_sense.learning_unit_id = unit.id
                          AND sort_sense.status = 'ACTIVE'
                    ), 999) ASC,
                    unit.normalized_text ASC
                    """;
        };
    }

    private String baseWhere(String statusFilter, String normalizedQuery, CefrLevel learnerLevel) {
        StringBuilder sql = new StringBuilder("""
                unit.unit_type = 'WORD'
                AND unit.status = 'ACTIVE'
                AND EXISTS (
                    SELECT 1
                    FROM learning_unit_senses active_sense
                    WHERE active_sense.learning_unit_id = unit.id
                      AND active_sense.status = 'ACTIVE'
                )
                """);
        if (normalizedQuery != null) {
            sql.append("""
                    AND (
                        unit.normalized_text LIKE ?
                        OR EXISTS (
                            SELECT 1
                            FROM learning_unit_forms form
                            WHERE form.learning_unit_id = unit.id
                              AND form.normalized_form LIKE ?
                        )
                    )
                    """);
        }
        if ("LEARNED".equals(statusFilter)) {
            sql.append("""
                    AND EXISTS (
                        SELECT 1
                        FROM learning_unit_senses learned_sense
                        JOIN user_learning_unit_sense_stats stats
                          ON stats.learning_unit_sense_id = learned_sense.id
                         AND stats.user_id = ?
                        WHERE learned_sense.learning_unit_id = unit.id
                          AND learned_sense.status = 'ACTIVE'
                    )
                    """);
        } else if ("UNLEARNED".equals(statusFilter) || "LOW_LEVEL".equals(statusFilter)) {
            sql.append("""
                    AND NOT EXISTS (
                        SELECT 1
                        FROM learning_unit_senses learned_sense
                        JOIN user_learning_unit_sense_stats stats
                          ON stats.learning_unit_sense_id = learned_sense.id
                         AND stats.user_id = ?
                        WHERE learned_sense.learning_unit_id = unit.id
                          AND learned_sense.status = 'ACTIVE'
                    )
                    """);
        }
        if ("LOW_LEVEL".equals(statusFilter)) {
            List<String> lowLevels = lowLevelDifficultyNames(learnerLevel);
            if (lowLevels.isEmpty()) {
                sql.append(" AND 1 = 0 ");
            } else {
                sql.append("""
                        AND EXISTS (
                            SELECT 1
                            FROM learning_unit_senses low_sense
                            WHERE low_sense.learning_unit_id = unit.id
                              AND low_sense.status = 'ACTIVE'
                              AND low_sense.difficulty_level IN (%s)
                        )
                        """.formatted(String.join(",", lowLevels.stream().map(ignored -> "?").toList())));
            }
        }
        return sql.toString();
    }

    private List<Object> baseParameters(Long userId, String statusFilter, String likeQuery, CefrLevel learnerLevel) {
        List<Object> parameters = new ArrayList<>();
        if (likeQuery != null) {
            parameters.add(likeQuery);
            parameters.add(likeQuery);
        }
        if ("LEARNED".equals(statusFilter) || "UNLEARNED".equals(statusFilter) || "LOW_LEVEL".equals(statusFilter)) {
            parameters.add(userId);
        }
        if ("LOW_LEVEL".equals(statusFilter)) {
            parameters.addAll(lowLevelDifficultyNames(learnerLevel));
        }
        return parameters;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ALL";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!List.of("ALL", "LEARNED", "UNLEARNED", "LOW_LEVEL").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vocabulary status.");
        }
        return normalized;
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "AUTO";
        }
        String normalized = sort.trim().toUpperCase(Locale.ROOT);
        if (!List.of("AUTO", "DIFFICULTY_ASC", "DIFFICULTY_DESC").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vocabulary sort.");
        }
        return normalized;
    }

    private void addSortParameters(List<Object> parameters, String sortMode, CefrLevel learnerLevel) {
        if ("AUTO".equals(sortMode)) {
            parameters.add(learnerLevel.ordinal() + 1);
        }
    }

    private List<String> lowLevelDifficultyNames(CefrLevel learnerLevel) {
        return List.of(DifficultyLevel.values())
                .stream()
                .filter(level -> learnerLevel.ordinal() - level.ordinal() >= LOW_LEVEL_GAP)
                .map(DifficultyLevel::name)
                .toList();
    }

    private Integer learnerLevelGap(CefrLevel learnerLevel, String difficultyLevel) {
        if (difficultyLevel == null || difficultyLevel.isBlank()) {
            return null;
        }
        try {
            return learnerLevel.ordinal() - DifficultyLevel.valueOf(difficultyLevel).ordinal();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String normalized = query.toLowerCase(Locale.ROOT)
                .replace("'", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String like(String normalizedQuery) {
        return normalizedQuery == null ? null : "%" + normalizedQuery + "%";
    }

    private int normalizePage(Integer requestedPage) {
        if (requestedPage == null) {
            return 0;
        }
        return Math.max(requestedPage, 0);
    }

    private int normalizePageSize(Integer requestedPageSize) {
        if (requestedPageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(requestedPageSize, 1), MAX_PAGE_SIZE);
    }

    private Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static class WordBuilder {
        private final Long unitId;
        private final String canonicalText;
        private final String normalizedText;
        private final List<VocabularySenseProgressResponse> senses = new ArrayList<>();

        private WordBuilder(Long unitId, String canonicalText, String normalizedText) {
            this.unitId = unitId;
            this.canonicalText = canonicalText;
            this.normalizedText = normalizedText;
        }

        private VocabularyWordResponse toResponse(List<Long> plannedSenseIds) {
            int learnedSenseCount = (int) senses.stream().filter(VocabularySenseProgressResponse::learned).count();
            int plannedSenseCount = (int) senses.stream()
                    .filter(sense -> plannedSenseIds.contains(sense.senseId()))
                    .count();
            int lowLevelCandidateSenseCount = (int) senses.stream()
                    .filter(VocabularySenseProgressResponse::lowLevelCandidate)
                    .count();
            String learnedStatus;
            if (learnedSenseCount == 0) {
                learnedStatus = "UNLEARNED";
            } else if (learnedSenseCount == senses.size()) {
                learnedStatus = "LEARNED";
            } else {
                learnedStatus = "PARTIAL";
            }
            return new VocabularyWordResponse(
                    unitId,
                    canonicalText,
                    normalizedText,
                    learnedStatus,
                    plannedSenseCount,
                    lowLevelCandidateSenseCount,
                    learnedSenseCount,
                    senses.size(),
                    senses.stream()
                            .map(sense -> new VocabularySenseProgressResponse(
                                    sense.senseId(),
                                    sense.senseKey(),
                                    sense.partOfSpeech(),
                                    sense.definitionEn(),
                                    sense.definitionZh(),
                                    sense.difficultyLevel(),
                                    sense.frequencyBand(),
                                    plannedSenseIds.contains(sense.senseId()),
                                    sense.lowLevelCandidate(),
                                    sense.learnerLevelGap(),
                                    sense.learned(),
                                    sense.masteryLevel(),
                                    sense.masteryScore(),
                                    sense.exposureCount(),
                                    sense.attemptCount(),
                                    sense.lastSeenAt(),
                                    sense.nextReviewAt()
                            ))
                            .toList()
            );
        }
    }
}
