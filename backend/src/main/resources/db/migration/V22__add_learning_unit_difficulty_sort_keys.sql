SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE learning_units ADD COLUMN difficulty_min_rank TINYINT NULL AFTER normalized_text',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'learning_units'
      AND column_name = 'difficulty_min_rank'
);
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE learning_units ADD COLUMN difficulty_max_rank TINYINT NULL AFTER difficulty_min_rank',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'learning_units'
      AND column_name = 'difficulty_max_rank'
);
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE learning_units ADD KEY idx_learning_units_min_rank_text (unit_type, status, difficulty_min_rank, normalized_text, id)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'learning_units'
      AND index_name = 'idx_learning_units_min_rank_text'
);
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE learning_units ADD KEY idx_learning_units_max_rank_text (unit_type, status, difficulty_max_rank, normalized_text, id)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'learning_units'
      AND index_name = 'idx_learning_units_max_rank_text'
);
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql := (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE learning_units ADD KEY idx_learning_units_max_rank_desc_text (unit_type, status, difficulty_max_rank DESC, normalized_text, id)',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'learning_units'
      AND index_name = 'idx_learning_units_max_rank_desc_text'
);
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

UPDATE learning_units unit
JOIN (
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
WHERE unit.unit_type = 'WORD';
