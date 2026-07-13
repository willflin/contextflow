import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EcdictFormalTestImporter {
    private static final String SOURCE_NAME = "ECDICT_CLEAN_WORDS";
    private static final String SOURCE_VERSION_PREFIX = "word-frequency-v3-top";
    private static final int MAX_SENSES_PER_WORD = 8;
    private static final int MAX_DEFINITION_LENGTH = 1000;
    private static final Pattern DEFINITION_PREFIX = Pattern.compile("^([A-Za-z][A-Za-z ]{0,24}|[A-Za-z]{1,4})\\.\\s*(.*)$");

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Usage: java EcdictFormalTestImporter <jdbcUrl> <username> <password> <limit>"
            );
        }

        String jdbcUrl = args[0];
        String username = args[1];
        String password = args[2];
        int limit = Integer.parseInt(args[3]);
        if (limit < 1 || limit > 5000) {
            throw new IllegalArgumentException("limit must be between 1 and 5000");
        }

        Instant started = Instant.now();
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            connection.setAutoCommit(false);
            cleanupFormalLanguageData(connection);
            long sourceId = createDataSource(connection, limit);
            ImportCounters counters = importWords(connection, sourceId, limit);
            connection.commit();

            long seconds = Duration.between(started, Instant.now()).toSeconds();
            System.out.println("limit=" + limit);
            System.out.println("sourceId=" + sourceId);
            System.out.println("unitsImported=" + counters.unitsImported);
            System.out.println("sensesImported=" + counters.sensesImported);
            System.out.println("formsImported=" + counters.formsImported);
            System.out.println("sourcesImported=" + counters.sourcesImported);
            System.out.println("skippedRows=" + counters.skippedRows);
            System.out.println("seconds=" + seconds);
        }
    }

    private static void cleanupFormalLanguageData(Connection connection) throws SQLException {
        execute(connection, "DELETE FROM user_learning_unit_sense_stats");
        execute(connection, "DELETE FROM learning_events");
        execute(connection, "UPDATE learning_unit_sense_feedback SET learning_unit_id = NULL WHERE learning_unit_id IS NOT NULL");
        execute(connection, "DELETE FROM learning_unit_sense_scenario_tags");
        execute(connection, "DELETE FROM learning_unit_sense_sources");
        execute(connection, "DELETE FROM learning_unit_forms");
        execute(connection, "DELETE FROM learning_unit_senses");
        execute(connection, "DELETE FROM learning_units");
        execute(connection, "DELETE FROM learning_data_sources WHERE source_name IN ('MANUAL_SEED', 'ECDICT_CLEAN_WORDS')");
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static long createDataSource(Connection connection, int limit) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO learning_data_sources (
                    source_name,
                    source_version,
                    source_url,
                    license_name,
                    attribution
                ) VALUES (?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, SOURCE_NAME);
            statement.setString(2, SOURCE_VERSION_PREFIX + limit);
            statement.setString(3, "https://github.com/skywind3000/ECDICT");
            statement.setString(4, "MIT");
            statement.setString(5, "ECDICT cleaned word-frequency-v3 staging import for ContextFlow formal language-unit testing.");
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create ECDICT clean data source.");
                }
                return keys.getLong(1);
            }
        }
    }

    private static ImportCounters importWords(Connection connection, long sourceId, int limit) throws SQLException {
        ImportCounters counters = new ImportCounters();
        try (
                PreparedStatement select = connection.prepareStatement("""
                        SELECT
                            id,
                            raw_entry_id,
                            raw_import_batch_id,
                            source_row_number,
                            word,
                            normalized_word,
                            definition_raw,
                            translation_raw,
                            bnc_rank,
                            frq_rank,
                            effective_frequency_rank,
                            frequency_source,
                            exchange_raw
                        FROM ecdict_clean_word_entries
                        ORDER BY effective_frequency_rank ASC, normalized_word ASC
                        LIMIT ?
                        """)
        ) {
            select.setInt(1, limit);
            try (ResultSet rows = select.executeQuery()) {
                while (rows.next()) {
                    CleanWord word = CleanWord.from(rows);
                    List<SenseCandidate> senses = parseSenses(word);
                    if (senses.isEmpty()) {
                        counters.skippedRows++;
                        continue;
                    }
                    long unitId = insertUnit(connection, word);
                    counters.unitsImported++;
                    counters.formsImported += insertForms(connection, unitId, word);
                    for (int i = 0; i < senses.size(); i++) {
                        long senseId = insertSense(connection, unitId, word, senses.get(i), i + 1);
                        counters.sensesImported++;
                        counters.sourcesImported += insertSenseSources(connection, sourceId, senseId, word, i + 1);
                    }
                }
            }
        }
        return counters;
    }

    private static long insertUnit(Connection connection, CleanWord word) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO learning_units (
                    unit_type,
                    canonical_text,
                    normalized_text,
                    language_code,
                    status
                ) VALUES ('WORD', ?, ?, 'en', 'ACTIVE')
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, word.word);
            statement.setString(2, word.normalizedWord);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create learning unit for " + word.word);
                }
                return keys.getLong(1);
            }
        }
    }

    private static int insertForms(Connection connection, long unitId, CleanWord word) throws SQLException {
        Set<String> insertedKeys = new HashSet<>();
        int count = 0;
        count += insertForm(connection, unitId, word.word, "LEMMA", insertedKeys);
        if (word.exchangeRaw == null || word.exchangeRaw.isBlank()) {
            return count;
        }

        for (String token : word.exchangeRaw.split("/")) {
            int separator = token.indexOf(':');
            if (separator < 1 || separator == token.length() - 1) {
                continue;
            }
            String code = token.substring(0, separator);
            String form = token.substring(separator + 1);
            String formType = formType(code);
            if (formType == null || !form.matches("[A-Za-z]+")) {
                continue;
            }
            count += insertForm(connection, unitId, form, formType, insertedKeys);
        }
        return count;
    }

    private static int insertForm(
            Connection connection,
            long unitId,
            String form,
            String formType,
            Set<String> insertedKeys
    ) throws SQLException {
        String normalized = normalize(form);
        String key = normalized + ":" + formType;
        if (!insertedKeys.add(key)) {
            return 0;
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO learning_unit_forms (
                    learning_unit_id,
                    form_text,
                    normalized_form,
                    form_type
                ) VALUES (?, ?, ?, ?)
                """)) {
            statement.setLong(1, unitId);
            statement.setString(2, form);
            statement.setString(3, normalized);
            statement.setString(4, formType);
            statement.executeUpdate();
            return 1;
        }
    }

    private static String formType(String code) {
        return switch (code) {
            case "s" -> "PLURAL";
            case "3" -> "THIRD_PERSON_SINGULAR";
            case "p" -> "PAST_TENSE";
            case "d" -> "PAST_PARTICIPLE";
            case "i" -> "PRESENT_PARTICIPLE";
            case "r" -> "COMPARATIVE";
            case "t" -> "SUPERLATIVE";
            default -> null;
        };
    }

    private static long insertSense(
            Connection connection,
            long unitId,
            CleanWord word,
            SenseCandidate sense,
            int senseIndex
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO learning_unit_senses (
                    learning_unit_id,
                    sense_key,
                    part_of_speech,
                    definition_en,
                    definition_zh,
                    difficulty_level,
                    difficulty_confidence,
                    frequency_score,
                    frequency_band,
                    status
                ) VALUES (?, ?, ?, ?, ?, NULL, NULL, ?, ?, 'ACTIVE')
                """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, unitId);
            statement.setString(2, "ecdict-clean-v3:" + word.cleanId + ":" + senseIndex);
            setNullableString(statement, 3, sense.partOfSpeech);
            statement.setString(4, truncate(sense.definitionEn, MAX_DEFINITION_LENGTH));
            setNullableString(statement, 5, truncate(sense.definitionZh, MAX_DEFINITION_LENGTH));
            statement.setBigDecimal(6, frequencyScore(word.effectiveFrequencyRank));
            statement.setString(7, frequencyBand(word.effectiveFrequencyRank));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create sense for " + word.word);
                }
                return keys.getLong(1);
            }
        }
    }

    private static int insertSenseSources(
            Connection connection,
            long sourceId,
            long senseId,
            CleanWord word,
            int senseIndex
    ) throws SQLException {
        int count = 0;
        count += insertSource(connection, sourceId, senseId, "SENSE", "clean:" + word.cleanId + ":sense:" + senseIndex);
        count += insertSource(connection, sourceId, senseId, "DEFINITION", "clean:" + word.cleanId + ":definition:" + senseIndex);
        count += insertSource(connection, sourceId, senseId, "TRANSLATION", "clean:" + word.cleanId + ":translation:" + senseIndex);
        count += insertSource(connection, sourceId, senseId, "FREQUENCY", "clean:" + word.cleanId + ":frequency:" + word.frequencySource);
        return count;
    }

    private static int insertSource(
            Connection connection,
            long sourceId,
            long senseId,
            String attributeType,
            String sourceRecordId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO learning_unit_sense_sources (
                    sense_id,
                    data_source_id,
                    attribute_type,
                    source_record_id
                ) VALUES (?, ?, ?, ?)
                """)) {
            statement.setLong(1, senseId);
            statement.setLong(2, sourceId);
            statement.setString(3, attributeType);
            statement.setString(4, sourceRecordId);
            statement.executeUpdate();
            return 1;
        }
    }

    private static List<SenseCandidate> parseSenses(CleanWord word) {
        List<ParsedLine> definitions = parseDefinitionLines(word.definitionRaw);
        if (definitions.isEmpty()) {
            return List.of();
        }
        List<SenseCandidate> senses = new ArrayList<>();
        for (int i = 0; i < definitions.size() && senses.size() < MAX_SENSES_PER_WORD; i++) {
            ParsedLine definition = definitions.get(i);
            String definitionEn = trimToNull(definition.text);
            if (definitionEn == null) {
                continue;
            }
            String definitionZh = truncate(word.translationRaw, MAX_DEFINITION_LENGTH);
            senses.add(new SenseCandidate(
                    mapPartOfSpeech(definition.prefix),
                    definitionEn,
                    definitionZh
            ));
        }
        return senses;
    }

    private static List<ParsedLine> parseDefinitionLines(String raw) {
        List<ParsedLine> result = new ArrayList<>();
        for (String line : splitMeaningLines(raw)) {
            Matcher matcher = DEFINITION_PREFIX.matcher(line);
            if (matcher.matches()) {
                result.add(new ParsedLine(matcher.group(1), matcher.group(2)));
            } else if (!result.isEmpty()) {
                ParsedLine previous = result.remove(result.size() - 1);
                result.add(new ParsedLine(previous.prefix, previous.text + " " + line));
            } else {
                result.add(new ParsedLine(null, line));
            }
        }
        return result;
    }

    private static List<String> splitMeaningLines(String raw) {
        String text = trimToNull(raw);
        if (text == null) {
            return List.of();
        }
        text = text.replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            String trimmed = trimToNull(line);
            if (trimmed != null) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    private static String mapPartOfSpeech(String prefix) {
        String normalized = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT).trim();
        return switch (normalized) {
            case "n" -> "NOUN";
            case "v", "vt", "vi" -> "VERB";
            case "a", "adj" -> "ADJECTIVE";
            case "adv", "ad" -> "ADVERB";
            case "pron" -> "PRONOUN";
            case "prep" -> "PREPOSITION";
            case "conj" -> "CONJUNCTION";
            case "int", "interj" -> "INTERJECTION";
            case "num" -> "NUMERAL";
            case "aux" -> "AUXILIARY";
            case "art", "article", "definite article", "indefinite article" -> "DETERMINER";
            default -> null;
        };
    }

    private static BigDecimal frequencyScore(int rank) {
        BigDecimal numerator = BigDecimal.valueOf(Math.max(1, 50000 - rank + 1));
        return numerator.divide(BigDecimal.valueOf(50000), 4, RoundingMode.HALF_UP);
    }

    private static String frequencyBand(int rank) {
        if (rank <= 1000) {
            return "VERY_COMMON";
        }
        if (rank <= 5000) {
            return "COMMON";
        }
        if (rank <= 15000) {
            return "MEDIUM";
        }
        if (rank <= 30000) {
            return "UNCOMMON";
        }
        return "RARE";
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("'", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        String text = trimToNull(value);
        if (text == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, text);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record CleanWord(
            long cleanId,
            long rawEntryId,
            long rawImportBatchId,
            int sourceRowNumber,
            String word,
            String normalizedWord,
            String definitionRaw,
            String translationRaw,
            Integer bncRank,
            Integer frqRank,
            int effectiveFrequencyRank,
            String frequencySource,
            String exchangeRaw
    ) {
        static CleanWord from(ResultSet rows) throws SQLException {
            return new CleanWord(
                    rows.getLong("id"),
                    rows.getLong("raw_entry_id"),
                    rows.getLong("raw_import_batch_id"),
                    rows.getInt("source_row_number"),
                    rows.getString("word"),
                    rows.getString("normalized_word"),
                    rows.getString("definition_raw"),
                    rows.getString("translation_raw"),
                    (Integer) rows.getObject("bnc_rank"),
                    (Integer) rows.getObject("frq_rank"),
                    rows.getInt("effective_frequency_rank"),
                    rows.getString("frequency_source"),
                    rows.getString("exchange_raw")
            );
        }
    }

    private record ParsedLine(String prefix, String text) {
    }

    private record SenseCandidate(String partOfSpeech, String definitionEn, String definitionZh) {
    }

    private static class ImportCounters {
        int unitsImported;
        int sensesImported;
        int formsImported;
        int sourcesImported;
        int skippedRows;
    }
}
