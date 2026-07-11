-- ECDICT 原始 CSV 导入模板 / ECDICT raw CSV import template
-- 使用前请把 LOAD DATA LOCAL INFILE 后面的路径改为本机 ecdict.csv 路径。
-- Before use, change the path after LOAD DATA LOCAL INFILE to the local ecdict.csv path.
-- MySQL 需要开启 LOCAL INFILE；如果客户端禁用，需要在 DataGrip/连接参数中允许。
-- MySQL LOCAL INFILE must be enabled; allow it in DataGrip/client settings if disabled.

SET @file_name = 'ecdict.csv';

INSERT INTO ecdict_import_batches (
    source_name,
    source_version,
    source_url,
    license_name,
    file_name,
    import_status,
    started_at
) VALUES (
    'ECDICT',
    NULL,
    'https://github.com/skywind3000/ECDICT',
    'MIT',
    @file_name,
    'PENDING',
    CURRENT_TIMESTAMP(6)
);

SET @batch_id = LAST_INSERT_ID();

LOAD DATA LOCAL INFILE 'D:/code/english_tutor_project/data/ecdict.csv'
INTO TABLE ecdict_import_entries
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(@word, @phonetic, @definition, @translation, @pos, @collins, @oxford, @tag, @bnc, @frq, @exchange, @detail, @audio)
SET import_batch_id = @batch_id,
    word = TRIM(@word),
    phonetic = NULLIF(@phonetic, ''),
    definition_raw = NULLIF(@definition, ''),
    translation_raw = NULLIF(@translation, ''),
    pos_raw = NULLIF(@pos, ''),
    collins_raw = NULLIF(@collins, ''),
    oxford_raw = NULLIF(@oxford, ''),
    tag_raw = NULLIF(@tag, ''),
    bnc_raw = NULLIF(@bnc, ''),
    frq_raw = NULLIF(@frq, ''),
    bnc_rank = CASE WHEN @bnc REGEXP '^[0-9]+$' AND CAST(@bnc AS UNSIGNED) > 0 THEN CAST(@bnc AS UNSIGNED) ELSE NULL END,
    frq_rank = CASE WHEN @frq REGEXP '^[0-9]+$' AND CAST(@frq AS UNSIGNED) > 0 THEN CAST(@frq AS UNSIGNED) ELSE NULL END,
    exchange_raw = NULLIF(@exchange, ''),
    detail_raw = NULLIF(@detail, ''),
    audio_raw = NULLIF(@audio, '');

SET @first_entry_id = (
    SELECT MIN(id)
    FROM ecdict_import_entries
    WHERE import_batch_id = @batch_id
);

UPDATE ecdict_import_entries
SET source_row_number = id - @first_entry_id + 1
WHERE import_batch_id = @batch_id;

UPDATE ecdict_import_batches
SET import_status = 'LOADED',
    loaded_rows = (
        SELECT COUNT(*)
        FROM ecdict_import_entries
        WHERE import_batch_id = @batch_id
    ),
    total_rows = (
        SELECT COUNT(*)
        FROM ecdict_import_entries
        WHERE import_batch_id = @batch_id
    ),
    completed_at = CURRENT_TIMESTAMP(6)
WHERE id = @batch_id;

SELECT
    b.id AS batch_id,
    b.import_status,
    b.loaded_rows,
    MIN(e.frq_rank) AS best_frq_rank,
    MIN(e.bnc_rank) AS best_bnc_rank
FROM ecdict_import_batches b
LEFT JOIN ecdict_import_entries e ON e.import_batch_id = b.id
WHERE b.id = @batch_id
GROUP BY b.id, b.import_status, b.loaded_rows;
