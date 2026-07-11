import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EcdictRawImporter {
    private static final int FIELD_COUNT = 13;
    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException(
                "Usage: java EcdictRawImporter <csvPath> <jdbcUrl> <username> <password> <fileName>"
            );
        }

        Path csvPath = Path.of(args[0]);
        String jdbcUrl = args[1];
        String username = args[2];
        String password = args[3];
        String fileName = args[4];
        String sha256 = sha256(csvPath);
        Instant started = Instant.now();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            connection.setAutoCommit(false);
            markAbandonedPendingBatches(connection);
            long batchId = createImportBatch(connection, fileName, sha256);

            int loadedRows = 0;
            int skippedRows = 0;
            try {
                ImportCounts counts = importEntries(connection, csvPath, batchId);
                loadedRows = counts.loadedRows();
                skippedRows = counts.skippedRows();
                completeBatch(connection, batchId, loadedRows, skippedRows);
                connection.commit();
                long seconds = Duration.between(started, Instant.now()).toSeconds();
                System.out.println("batchId=" + batchId);
                System.out.println("loadedRows=" + loadedRows);
                System.out.println("skippedRows=" + skippedRows);
                System.out.println("seconds=" + seconds);
            } catch (Exception ex) {
                failBatch(connection, batchId, loadedRows, skippedRows, ex.getMessage());
                connection.commit();
                throw ex;
            }
        }
    }

    private static void markAbandonedPendingBatches(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE ecdict_import_batches
            SET import_status = 'FAILED',
                error_message = 'Previous raw import did not complete.',
                completed_at = CURRENT_TIMESTAMP(6)
            WHERE source_name = 'ECDICT'
              AND import_status = 'PENDING'
              AND loaded_rows = 0
            """)) {
            statement.executeUpdate();
        }
    }

    private static long createImportBatch(Connection connection, String fileName, String sha256) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO ecdict_import_batches (
                source_name,
                source_version,
                source_url,
                license_name,
                file_name,
                file_sha256,
                import_status,
                started_at
            ) VALUES (
                'ECDICT',
                NULL,
                'https://github.com/skywind3000/ECDICT',
                'MIT',
                ?,
                ?,
                'PENDING',
                CURRENT_TIMESTAMP(6)
            )
            """, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fileName);
            statement.setString(2, sha256);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create ECDICT import batch.");
                }
                return keys.getLong(1);
            }
        }
    }

    private static ImportCounts importEntries(Connection connection, Path csvPath, long batchId)
        throws IOException, SQLException {
        String sql = """
            INSERT INTO ecdict_import_entries (
                import_batch_id,
                source_row_number,
                word,
                phonetic,
                definition_raw,
                translation_raw,
                pos_raw,
                collins_raw,
                oxford_raw,
                tag_raw,
                bnc_raw,
                frq_raw,
                bnc_rank,
                frq_rank,
                exchange_raw,
                detail_raw,
                audio_raw
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        int loadedRows = 0;
        int skippedRows = 0;
        int pendingBatchRows = 0;
        int sourceRowNumber = 0;

        try (
            BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            readRecord(reader);
            List<String> record;
            while ((record = readRecord(reader)) != null) {
                sourceRowNumber++;
                List<String> fields = normalizeFieldCount(record);
                String word = trimToNull(fields.get(0));
                if (word == null) {
                    skippedRows++;
                    continue;
                }

                statement.setLong(1, batchId);
                statement.setInt(2, sourceRowNumber);
                statement.setString(3, word);
                setNullableString(statement, 4, fields.get(1));
                setNullableString(statement, 5, fields.get(2));
                setNullableString(statement, 6, fields.get(3));
                setNullableString(statement, 7, fields.get(4));
                setNullableString(statement, 8, fields.get(5));
                setNullableString(statement, 9, fields.get(6));
                setNullableString(statement, 10, fields.get(7));
                setNullableString(statement, 11, fields.get(8));
                setNullableString(statement, 12, fields.get(9));
                setNullableInteger(statement, 13, positiveRank(fields.get(8)));
                setNullableInteger(statement, 14, positiveRank(fields.get(9)));
                setNullableString(statement, 15, fields.get(10));
                setNullableString(statement, 16, fields.get(11));
                setNullableString(statement, 17, fields.get(12));
                statement.addBatch();
                loadedRows++;
                pendingBatchRows++;

                if (pendingBatchRows >= BATCH_SIZE) {
                    statement.executeBatch();
                    pendingBatchRows = 0;
                }
            }

            if (pendingBatchRows > 0) {
                statement.executeBatch();
            }
        }

        return new ImportCounts(loadedRows, skippedRows);
    }

    private static List<String> readRecord(Reader reader) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean readAny = false;

        while (true) {
            int value = reader.read();
            if (value == -1) {
                if (!readAny && field.isEmpty() && fields.isEmpty()) {
                    return null;
                }
                fields.add(field.toString());
                return fields;
            }

            readAny = true;
            char ch = (char) value;
            if (ch == '"') {
                reader.mark(1);
                int next = reader.read();
                if (inQuotes && next == '"') {
                    field.append('"');
                } else {
                    inQuotes = !inQuotes;
                    if (next != -1) {
                        reader.reset();
                    }
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else if ((ch == '\n' || ch == '\r') && !inQuotes) {
                if (ch == '\r') {
                    reader.mark(1);
                    int next = reader.read();
                    if (next != '\n' && next != -1) {
                        reader.reset();
                    }
                }
                fields.add(field.toString());
                return fields;
            } else {
                field.append(ch);
            }
        }
    }

    private static List<String> normalizeFieldCount(List<String> record) {
        ArrayList<String> fields = new ArrayList<>(record);
        while (fields.size() < FIELD_COUNT) {
            fields.add("");
        }
        if (fields.size() > FIELD_COUNT) {
            StringBuilder last = new StringBuilder(fields.get(FIELD_COUNT - 1));
            for (int i = FIELD_COUNT; i < fields.size(); i++) {
                last.append(',').append(fields.get(i));
            }
            fields = new ArrayList<>(fields.subList(0, FIELD_COUNT));
            fields.set(FIELD_COUNT - 1, last.toString());
        }
        return fields;
    }

    private static void completeBatch(Connection connection, long batchId, int loadedRows, int skippedRows)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE ecdict_import_batches
            SET import_status = 'LOADED',
                total_rows = ?,
                loaded_rows = ?,
                skipped_rows = ?,
                completed_at = CURRENT_TIMESTAMP(6)
            WHERE id = ?
            """)) {
            statement.setInt(1, loadedRows + skippedRows);
            statement.setInt(2, loadedRows);
            statement.setInt(3, skippedRows);
            statement.setLong(4, batchId);
            statement.executeUpdate();
        }
    }

    private static void failBatch(Connection connection, long batchId, int loadedRows, int skippedRows, String error)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            UPDATE ecdict_import_batches
            SET import_status = 'FAILED',
                loaded_rows = ?,
                skipped_rows = ?,
                error_message = ?,
                completed_at = CURRENT_TIMESTAMP(6)
            WHERE id = ?
            """)) {
            statement.setInt(1, loadedRows);
            statement.setInt(2, skippedRows);
            statement.setString(3, truncate(error, 1000));
            statement.setLong(4, batchId);
            statement.executeUpdate();
        }
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return String.format("%064x", new BigInteger(1, digest.digest()));
    }

    private static Integer positiveRank(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null || !trimmed.matches("\\d+")) {
            return null;
        }
        int parsed = Integer.parseInt(trimmed);
        return parsed > 0 ? parsed : null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, trimmed);
        }
    }

    private static void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record ImportCounts(int loadedRows, int skippedRows) {
    }
}
