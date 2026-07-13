package com.contextflow.content.service;

import com.contextflow.content.dto.VocabularyListResponse;
import com.contextflow.content.dto.VocabularySenseProgressResponse;
import com.contextflow.content.dto.VocabularyWordResponse;
import com.contextflow.user.domain.UserEntity;
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

    private static final int DEFAULT_LIMIT = 500;
    private static final int MAX_LIMIT = 1000;

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public VocabularyQueryService(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public VocabularyListResponse list(String username, String status, String query, Integer requestedLimit) {
        UserEntity user = userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
        String statusFilter = normalizeStatus(status);
        String normalizedQuery = normalizeQuery(query);
        int limit = normalizeLimit(requestedLimit);
        int rowLimit = Math.min(limit * 12, 6000);

        Map<Long, WordBuilder> words = new LinkedHashMap<>();
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
                WHERE unit.unit_type = 'WORD'
                  AND unit.status = 'ACTIVE'
                  AND sense.status = 'ACTIVE'
                  AND (
                      ? IS NULL
                      OR unit.normalized_text LIKE ?
                      OR EXISTS (
                          SELECT 1
                          FROM learning_unit_forms form
                          WHERE form.learning_unit_id = unit.id
                            AND form.normalized_form LIKE ?
                      )
                  )
                ORDER BY unit.normalized_text ASC, sense.id ASC
                LIMIT ?
                """, rows -> {
            Long unitId = rows.getLong("unit_id");
            String canonicalText = rows.getString("canonical_text");
            String normalizedText = rows.getString("normalized_text");
            WordBuilder word = words.computeIfAbsent(unitId, ignored -> new WordBuilder(
                    unitId,
                    canonicalText,
                    normalizedText
            ));
            boolean learned = rows.getString("mastery_level") != null;
            word.senses.add(new VocabularySenseProgressResponse(
                    rows.getLong("sense_id"),
                    rows.getString("sense_key"),
                    rows.getString("part_of_speech"),
                    rows.getString("definition_en"),
                    rows.getString("definition_zh"),
                    rows.getString("difficulty_level"),
                    rows.getString("frequency_band"),
                    learned,
                    rows.getString("mastery_level"),
                    rows.getBigDecimal("mastery_score"),
                    (Integer) rows.getObject("exposure_count"),
                    (Integer) rows.getObject("attempt_count"),
                    instant(rows.getTimestamp("last_seen_at")),
                    instant(rows.getTimestamp("next_review_at"))
            ));
        }, user.getId(), normalizedQuery, like(normalizedQuery), like(normalizedQuery), rowLimit);

        List<VocabularyWordResponse> matched = words.values()
                .stream()
                .map(WordBuilder::toResponse)
                .filter(word -> matchesStatus(word, statusFilter))
                .toList();

        return new VocabularyListResponse(
                statusFilter,
                query == null ? null : query.trim(),
                limit,
                matched.size(),
                matched.stream().limit(limit).toList()
        );
    }

    private boolean matchesStatus(VocabularyWordResponse word, String statusFilter) {
        return switch (statusFilter) {
            case "LEARNED" -> word.learnedSenseCount() > 0;
            case "UNLEARNED" -> word.learnedSenseCount() == 0;
            default -> true;
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ALL";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!List.of("ALL", "LEARNED", "UNLEARNED").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid vocabulary status.");
        }
        return normalized;
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

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.min(Math.max(requestedLimit, 1), MAX_LIMIT);
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

        private VocabularyWordResponse toResponse() {
            int learnedSenseCount = (int) senses.stream().filter(VocabularySenseProgressResponse::learned).count();
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
                    learnedSenseCount,
                    senses.size(),
                    senses
            );
        }
    }
}
