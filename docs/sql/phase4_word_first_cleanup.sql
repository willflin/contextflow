-- Phase 4.9 word-first cleanup / Phase 4.9 单词优先清理
-- Purpose / 用途:
-- Remove legacy PHRASE and SENTENCE_PATTERN language-unit data created before the word-first decision.
-- 清理 word-first 决策前遗留的 PHRASE 和 SENTENCE_PATTERN 语言单元数据。
--
-- Safety / 安全说明:
-- 1. This does not change table structure or Flyway history.
--    本脚本不修改表结构，也不修改 Flyway 历史。
-- 2. It deletes non-WORD learning units and their dependent rows.
--    本脚本会删除非 WORD 语言单元及其关联数据。
-- 3. Run the preview SELECT first. Use COMMIT only after checking the result.
--    请先执行预览 SELECT；确认后再 COMMIT。

USE contextflow;

-- Preview target units / 预览将被清理的语言单元
SELECT id, unit_type, canonical_text, normalized_text, language_code, status
FROM learning_units
WHERE unit_type IN ('PHRASE', 'SENTENCE_PATTERN')
ORDER BY unit_type, canonical_text, id;

-- Preview dependent events / 预览关联事件
SELECT le.id, le.learning_unit_id, lu.unit_type, lu.canonical_text, le.event_type, le.occurrence_text, le.created_at
FROM learning_events le
JOIN learning_units lu ON lu.id = le.learning_unit_id
WHERE lu.unit_type IN ('PHRASE', 'SENTENCE_PATTERN')
ORDER BY le.id;

START TRANSACTION;

CREATE TEMPORARY TABLE tmp_word_first_cleanup_units (
    id BIGINT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO tmp_word_first_cleanup_units (id)
SELECT id
FROM learning_units
WHERE unit_type IN ('PHRASE', 'SENTENCE_PATTERN');

-- learning_events does not cascade from learning_units, so delete it first.
-- learning_events 不会随 learning_units 级联删除，所以需要先删事件。
DELETE le
FROM learning_events le
JOIN tmp_word_first_cleanup_units target ON target.id = le.learning_unit_id;

-- Delete target units. Forms, senses, sense sources, and sense stats cascade through existing FKs.
-- 删除目标语言单元。词形、词义、词义来源、词义掌握度会按现有外键级联删除。
DELETE lu
FROM learning_units lu
JOIN tmp_word_first_cleanup_units target ON target.id = lu.id;

DROP TEMPORARY TABLE tmp_word_first_cleanup_units;

-- Post-check / 执行后检查
SELECT unit_type, COUNT(*) AS unit_count
FROM learning_units
GROUP BY unit_type
ORDER BY unit_type;

SELECT COUNT(*) AS non_word_event_count
FROM learning_events le
JOIN learning_units lu ON lu.id = le.learning_unit_id
WHERE lu.unit_type <> 'WORD';

-- If the post-check is correct, run COMMIT. Otherwise run ROLLBACK.
-- 如果检查结果正确，执行 COMMIT；否则执行 ROLLBACK。
-- COMMIT;
-- ROLLBACK;
