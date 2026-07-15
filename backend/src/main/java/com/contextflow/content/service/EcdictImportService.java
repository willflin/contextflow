package com.contextflow.content.service;

import com.contextflow.content.dto.EcdictImportResponse;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class EcdictImportService {

    private static final int BATCH_SIZE = 1000;
    private static final String SOURCE_VERSION = "local-csv";

    private final JdbcTemplate jdbcTemplate;

    public EcdictImportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public EcdictImportResponse importLocalCsv() {
        Path csvPath = resolveCsvPath();
        String sha256 = sha256(csvPath);
        ExistingBatch existingBatch = existingBatch(sha256);
        boolean reused = existingBatch != null;
        Long batchId = reused ? existingBatch.id() : createBatch(csvPath, sha256);

        int loadedRows = reused ? existingBatch.loadedRows() : loadRawRows(csvPath, batchId);
        normalizeCleanRows(batchId);
        synchronizeLearningUnits();
        synchronizeLearningUnitSenses();
        refreshLearningUnitDifficultySortKeys();
        synchronizeSourceLinks();

        int cleanRows = count("SELECT COUNT(*) FROM ecdict_clean_word_entries");
        int unitRows = count("SELECT COUNT(*) FROM learning_units WHERE unit_type = 'WORD'");
        int senseRows = count("""
                SELECT COUNT(*)
                FROM learning_unit_senses sense
                JOIN learning_units unit ON unit.id = sense.learning_unit_id
                WHERE unit.unit_type = 'WORD'
                """);
        markBatchNormalized(batchId, loadedRows);

        return new EcdictImportResponse(
                batchId,
                csvPath.getFileName().toString(),
                sha256,
                loadedRows,
                cleanRows,
                unitRows,
                senseRows,
                reused
        );
    }

    private Path resolveCsvPath() {
        List<Path> candidates = List.of(
                Path.of("data", "ecdict.csv"),
                Path.of("..", "data", "ecdict.csv"),
                Path.of("D:\\code\\english_tutor_project\\data\\ecdict.csv")
        );
        return candidates.stream()
                .map(Path::toAbsolutePath)
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Local ECDICT CSV not found. Expected data/ecdict.csv."
                ));
    }

    private String sha256(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream input = new DigestInputStream(Files.newInputStream(path), digest)) {
                input.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to hash ECDICT CSV.", exception);
        }
    }

    private ExistingBatch existingBatch(String sha256) {
        List<ExistingBatch> batches = jdbcTemplate.query("""
                SELECT id, loaded_rows
                FROM ecdict_import_batches
                WHERE file_sha256 = ?
                  AND import_status IN ('LOADED', 'NORMALIZED')
                ORDER BY id DESC
                LIMIT 1
                """, (rows, rowNumber) -> new ExistingBatch(
                rows.getLong("id"),
                rows.getInt("loaded_rows")
        ), sha256);
        return batches.isEmpty() ? null : batches.get(0);
    }

    private Long createBatch(Path csvPath, String sha256) {
        jdbcTemplate.update("""
                INSERT INTO ecdict_import_batches (
                    source_name, source_version, source_url, license_name, file_name,
                    file_sha256, import_status, started_at
                )
                VALUES ('ECDICT', ?, 'https://github.com/skywind3000/ECDICT', 'MIT', ?, ?, 'PENDING', ?)
                """, SOURCE_VERSION, csvPath.getFileName().toString(), sha256, Instant.now());
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private int loadRawRows(Path csvPath, Long batchId) {
        int loaded = 0;
        int sourceRowNumber = 0;
        List<EcdictRow> batch = new ArrayList<>(BATCH_SIZE);
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null || !header.startsWith("word,")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ECDICT CSV header.");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                sourceRowNumber++;
                EcdictRow row = parseRow(line, sourceRowNumber);
                if (row == null) {
                    continue;
                }
                batch.add(row);
                if (batch.size() >= BATCH_SIZE) {
                    loaded += insertRawBatch(batchId, batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                loaded += insertRawBatch(batchId, batch);
            }
        } catch (IOException exception) {
            markBatchFailed(batchId, exception.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read ECDICT CSV.", exception);
        }
        jdbcTemplate.update("""
                UPDATE ecdict_import_batches
                SET import_status = 'LOADED', total_rows = ?, loaded_rows = ?, completed_at = ?
                WHERE id = ?
                """, sourceRowNumber, loaded, Instant.now(), batchId);
        return loaded;
    }

    private EcdictRow parseRow(String line, int sourceRowNumber) {
        List<String> columns = parseCsvLine(line);
        if (columns.size() < 13) {
            return null;
        }
        String word = trimToNull(columns.get(0));
        if (word == null) {
            return null;
        }
        return new EcdictRow(
                sourceRowNumber,
                word,
                trimToNull(columns.get(1)),
                trimToNull(columns.get(2)),
                trimToNull(columns.get(3)),
                trimToNull(columns.get(4)),
                trimToNull(columns.get(5)),
                trimToNull(columns.get(6)),
                trimToNull(columns.get(7)),
                trimToNull(columns.get(8)),
                trimToNull(columns.get(9)),
                parsePositiveInt(columns.get(8)),
                parsePositiveInt(columns.get(9)),
                trimToNull(columns.get(10)),
                trimToNull(columns.get(11)),
                trimToNull(columns.get(12))
        );
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char ch = line.charAt(index);
            if (ch == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        columns.add(current.toString());
        return columns;
    }

    private int insertRawBatch(Long batchId, List<EcdictRow> rows) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO ecdict_import_entries (
                    import_batch_id, source_row_number, word, phonetic, definition_raw,
                    translation_raw, pos_raw, collins_raw, oxford_raw, tag_raw, bnc_raw,
                    frq_raw, bnc_rank, frq_rank, exchange_raw, detail_raw, audio_raw
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws SQLException {
                EcdictRow row = rows.get(index);
                statement.setLong(1, batchId);
                statement.setInt(2, row.sourceRowNumber());
                statement.setString(3, row.word());
                statement.setString(4, row.phonetic());
                statement.setString(5, row.definitionRaw());
                statement.setString(6, row.translationRaw());
                statement.setString(7, row.posRaw());
                statement.setString(8, row.collinsRaw());
                statement.setString(9, row.oxfordRaw());
                statement.setString(10, row.tagRaw());
                statement.setString(11, row.bncRaw());
                statement.setString(12, row.frqRaw());
                setInteger(statement, 13, row.bncRank());
                setInteger(statement, 14, row.frqRank());
                statement.setString(15, row.exchangeRaw());
                statement.setString(16, row.detailRaw());
                statement.setString(17, row.audioRaw());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
        return rows.size();
    }

    private void normalizeCleanRows(Long batchId) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO ecdict_clean_word_entries (
                    raw_entry_id, raw_import_batch_id, source_row_number, word, phonetic,
                    definition_raw, translation_raw, pos_raw, collins_raw, oxford_raw,
                    tag_raw, bnc_rank, frq_rank, exchange_raw, cleaning_rule_version
                )
                SELECT
                    id, import_batch_id, source_row_number, word, phonetic,
                    definition_raw, translation_raw, pos_raw, collins_raw, oxford_raw,
                    tag_raw, bnc_rank, frq_rank, exchange_raw, 'word-frequency-v3'
                FROM ecdict_import_entries
                WHERE import_batch_id = ?
                  AND word REGEXP '^[A-Za-z]+$'
                  AND (frq_rank IS NOT NULL OR bnc_rank IS NOT NULL)
                  AND (word IN ('a', 'I') OR BINARY word = LOWER(word))
                  AND NOT (
                      CHAR_LENGTH(LOWER(word)) > 1
                      AND REPLACE(LOWER(word), LEFT(LOWER(word), 1), '') = ''
                  )
                """, batchId);
    }

    private void synchronizeLearningUnits() {
        jdbcTemplate.update("""
                INSERT INTO learning_units (
                    unit_type, canonical_text, normalized_text, language_code, status
                )
                SELECT 'WORD', clean.word, clean.normalized_word, 'en', 'ACTIVE'
                FROM ecdict_clean_word_entries clean
                ON DUPLICATE KEY UPDATE status = 'ACTIVE'
                """);
        jdbcTemplate.update("""
                INSERT IGNORE INTO learning_unit_forms (
                    learning_unit_id, form_text, normalized_form, form_type
                )
                SELECT unit.id, clean.word, clean.normalized_word, 'LEMMA'
                FROM ecdict_clean_word_entries clean
                JOIN learning_units unit
                  ON unit.language_code = 'en'
                 AND unit.unit_type = 'WORD'
                 AND unit.normalized_text = clean.normalized_word
                """);
    }

    private void synchronizeLearningUnitSenses() {
        jdbcTemplate.update("""
                INSERT INTO learning_unit_senses (
                    learning_unit_id, sense_key, part_of_speech, definition_en, definition_zh,
                    difficulty_level, difficulty_confidence, frequency_score, frequency_band, status
                )
                SELECT
                    unit.id,
                    CONCAT('ecdict:', clean.id),
                    CASE
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%v%' THEN 'VERB'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%adj%' THEN 'ADJECTIVE'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%adv%' THEN 'ADVERB'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%pron%' THEN 'PRONOUN'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%prep%' THEN 'PREPOSITION'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%conj%' THEN 'CONJUNCTION'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%num%' THEN 'NUMERAL'
                        WHEN LOWER(COALESCE(clean.pos_raw, clean.translation_raw, '')) LIKE '%n%' THEN 'NOUN'
                        ELSE 'OTHER'
                    END,
                    LEFT(COALESCE(NULLIF(clean.definition_raw, ''), CONCAT('ECDICT entry for ', clean.word)), 1000),
                    LEFT(clean.translation_raw, 1000),
                    CASE
                        WHEN clean.effective_frequency_rank <= 1000 THEN 'A1'
                        WHEN clean.effective_frequency_rank <= 3000 THEN 'A2'
                        WHEN clean.effective_frequency_rank <= 6000 THEN 'B1'
                        WHEN clean.effective_frequency_rank <= 10000 THEN 'B2'
                        WHEN clean.effective_frequency_rank <= 18000 THEN 'C1'
                        ELSE 'C2'
                    END,
                    0.7000,
                    CASE
                        WHEN clean.effective_frequency_rank IS NULL THEN NULL
                        ELSE ROUND(1 / clean.effective_frequency_rank, 4)
                    END,
                    CASE
                        WHEN clean.effective_frequency_rank <= 1000 THEN 'VERY_COMMON'
                        WHEN clean.effective_frequency_rank <= 3000 THEN 'COMMON'
                        WHEN clean.effective_frequency_rank <= 10000 THEN 'MEDIUM'
                        WHEN clean.effective_frequency_rank <= 20000 THEN 'UNCOMMON'
                        ELSE 'RARE'
                    END,
                    'ACTIVE'
                FROM ecdict_clean_word_entries clean
                JOIN learning_units unit
                  ON unit.language_code = 'en'
                 AND unit.unit_type = 'WORD'
                 AND unit.normalized_text = clean.normalized_word
                ON DUPLICATE KEY UPDATE
                    status = 'ACTIVE',
                    difficulty_level = VALUES(difficulty_level),
                    frequency_band = VALUES(frequency_band)
                """);
    }

    private void refreshLearningUnitDifficultySortKeys() {
        jdbcTemplate.update("""
                UPDATE learning_units unit
                LEFT JOIN (
                    SELECT
                        sense.learning_unit_id,
                        MIN(NULLIF(FIELD(sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0)) AS min_rank,
                        MAX(NULLIF(FIELD(sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'), 0)) AS max_rank
                    FROM learning_unit_senses sense
                    WHERE sense.status = 'ACTIVE'
                    GROUP BY sense.learning_unit_id
                ) ranks ON ranks.learning_unit_id = unit.id
                SET
                    unit.difficulty_min_rank = ranks.min_rank,
                    unit.difficulty_max_rank = ranks.max_rank
                WHERE unit.unit_type = 'WORD'
                """);
    }

    private void synchronizeSourceLinks() {
        jdbcTemplate.update("""
                INSERT INTO learning_data_sources (
                    source_name, source_version, source_url, license_name, attribution
                )
                VALUES (
                    'ECDICT', ?, 'https://github.com/skywind3000/ECDICT', 'MIT',
                    'ECDICT local CSV imported into ContextFlow.'
                )
                ON DUPLICATE KEY UPDATE attribution = VALUES(attribution)
                """, SOURCE_VERSION);
        Long sourceId = jdbcTemplate.queryForObject("""
                SELECT id FROM learning_data_sources
                WHERE source_name = 'ECDICT' AND source_version = ?
                """, Long.class, SOURCE_VERSION);
        jdbcTemplate.update("""
                INSERT IGNORE INTO learning_unit_sense_sources (
                    sense_id, data_source_id, attribute_type, source_record_id
                )
                SELECT sense.id, ?, 'SENSE', CAST(clean.id AS CHAR)
                FROM ecdict_clean_word_entries clean
                JOIN learning_units unit
                  ON unit.language_code = 'en'
                 AND unit.unit_type = 'WORD'
                 AND unit.normalized_text = clean.normalized_word
                JOIN learning_unit_senses sense
                  ON sense.learning_unit_id = unit.id
                 AND sense.sense_key = CONCAT('ecdict:', clean.id)
                """, sourceId);
    }

    private void markBatchNormalized(Long batchId, int loadedRows) {
        jdbcTemplate.update("""
                UPDATE ecdict_import_batches
                SET import_status = 'NORMALIZED', loaded_rows = ?, completed_at = ?
                WHERE id = ?
                """, loadedRows, Instant.now(), batchId);
    }

    private void markBatchFailed(Long batchId, String message) {
        jdbcTemplate.update("""
                UPDATE ecdict_import_batches
                SET import_status = 'FAILED', error_message = ?
                WHERE id = ?
                """, message == null ? "Import failed." : message.substring(0, Math.min(message.length(), 1000)), batchId);
    }

    private int count(String sql) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private Integer parsePositiveInt(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void setInteger(PreparedStatement statement, int parameterIndex, Integer value) throws SQLException {
        if (value == null) {
            statement.setObject(parameterIndex, null);
        } else {
            statement.setInt(parameterIndex, value);
        }
    }

    private record ExistingBatch(Long id, int loadedRows) {
    }

    private record EcdictRow(
            int sourceRowNumber,
            String word,
            String phonetic,
            String definitionRaw,
            String translationRaw,
            String posRaw,
            String collinsRaw,
            String oxfordRaw,
            String tagRaw,
            String bncRaw,
            String frqRaw,
            Integer bncRank,
            Integer frqRank,
            String exchangeRaw,
            String detailRaw,
            String audioRaw
    ) {
    }
}
