-- Phase 6.2 ECDICT 清洗单词表 / Phase 6.2 ECDICT cleaned word table
-- 目标：在不修改原始表的前提下，只保留普通单词形态且有有效频率的 ECDICT 词条。
-- Goal: keep ordinary word-shaped ECDICT entries with valid frequency without modifying raw tables.

CREATE TABLE IF NOT EXISTS ecdict_clean_word_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    raw_entry_id BIGINT NOT NULL,
    raw_import_batch_id BIGINT NOT NULL,
    source_row_number INT NULL,
    word VARCHAR(255) NOT NULL,
    normalized_word VARCHAR(255) GENERATED ALWAYS AS (LOWER(TRIM(word))) STORED,
    phonetic VARCHAR(255) NULL,
    definition_raw TEXT NULL,
    translation_raw TEXT NULL,
    pos_raw VARCHAR(255) NULL,
    collins_raw VARCHAR(32) NULL,
    oxford_raw VARCHAR(32) NULL,
    tag_raw VARCHAR(1000) NULL,
    bnc_rank INT NULL,
    frq_rank INT NULL,
    effective_frequency_rank INT GENERATED ALWAYS AS (COALESCE(frq_rank, bnc_rank)) STORED,
    frequency_source VARCHAR(16) GENERATED ALWAYS AS (
        CASE
            WHEN frq_rank IS NOT NULL THEN 'FRQ'
            WHEN bnc_rank IS NOT NULL THEN 'BNC'
            ELSE NULL
        END
    ) STORED,
    exchange_raw TEXT NULL,
    cleaning_rule_version VARCHAR(32) NOT NULL DEFAULT 'word-frequency-v3',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_ecdict_clean_word_entries_raw (raw_entry_id),
    UNIQUE KEY uk_ecdict_clean_word_entries_word (normalized_word),
    KEY idx_ecdict_clean_word_entries_batch (raw_import_batch_id),
    KEY idx_ecdict_clean_word_entries_effective_rank (effective_frequency_rank),
    KEY idx_ecdict_clean_word_entries_frequency_source (frequency_source),
    KEY idx_ecdict_clean_word_entries_frq_rank (frq_rank),
    KEY idx_ecdict_clean_word_entries_bnc_rank (bnc_rank),
    CONSTRAINT fk_ecdict_clean_word_entries_raw FOREIGN KEY (raw_entry_id)
        REFERENCES ecdict_import_entries (id) ON DELETE CASCADE,
    CONSTRAINT fk_ecdict_clean_word_entries_batch FOREIGN KEY (raw_import_batch_id)
        REFERENCES ecdict_import_batches (id) ON DELETE CASCADE,
    CONSTRAINT ck_ecdict_clean_word_entries_ranks CHECK (
        (bnc_rank IS NULL OR bnc_rank > 0) AND (frq_rank IS NULL OR frq_rank > 0)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
