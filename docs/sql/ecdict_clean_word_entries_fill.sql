-- ECDICT 清洗单词表填充 / Fill ECDICT cleaned word table
-- 不修改 ecdict_import_entries 原始表。
-- Does not modify the raw ecdict_import_entries table.
-- word-frequency-v2 规则：只保留普通单词形态、必须有有效频率，并排除缩写和专名。
-- word-frequency-v2 rule: keep ordinary word-shaped entries with valid frequency, excluding abbreviations and proper names.

TRUNCATE TABLE ecdict_clean_word_entries;

INSERT INTO ecdict_clean_word_entries (
    raw_entry_id,
    raw_import_batch_id,
    source_row_number,
    word,
    phonetic,
    definition_raw,
    translation_raw,
    pos_raw,
    collins_raw,
    oxford_raw,
    tag_raw,
    bnc_rank,
    frq_rank,
    exchange_raw,
    cleaning_rule_version
)
SELECT
    id AS raw_entry_id,
    import_batch_id AS raw_import_batch_id,
    source_row_number,
    word,
    phonetic,
    definition_raw,
    translation_raw,
    pos_raw,
    collins_raw,
    oxford_raw,
    tag_raw,
    bnc_rank,
    frq_rank,
    exchange_raw,
    'word-frequency-v2'
FROM ecdict_import_entries
WHERE word REGEXP '^[A-Za-z]+$'
  AND (frq_rank IS NOT NULL OR bnc_rank IS NOT NULL)
  AND (word IN ('a', 'I') OR BINARY word = LOWER(word))
  AND NOT (
      LOWER(COALESCE(translation_raw, '')) LIKE 'abbr.%'
      OR LOWER(COALESCE(translation_raw, '')) LIKE 'abbr[%'
      OR LOWER(COALESCE(translation_raw, '')) LIKE 'abbr %'
      OR translation_raw LIKE '%缩略%'
      OR translation_raw LIKE '%（男子名%'
      OR translation_raw LIKE '%（女子名%'
      OR translation_raw LIKE '%（人名%'
      OR translation_raw LIKE '%（姓氏%'
      OR translation_raw LIKE '%城市名%'
  );

SELECT
    COUNT(*) AS clean_word_entries,
    COUNT(frq_rank) AS rows_with_frq_rank,
    COUNT(bnc_rank) AS rows_with_bnc_rank,
    COUNT(effective_frequency_rank) AS rows_with_any_frequency_rank,
    SUM(frequency_source = 'FRQ') AS rows_ranked_by_frq,
    SUM(frequency_source = 'BNC') AS rows_ranked_by_bnc
FROM ecdict_clean_word_entries;
