-- Phase 6.2 ECDICT 原始导入表 / Phase 6.2 ECDICT raw import tables
-- 目标：先保存外部词典原始数据，再由后续规范化任务写入 learning_units / learning_unit_senses。
-- Goal: store external dictionary rows first, then normalize them into learning_units / learning_unit_senses later.

CREATE TABLE IF NOT EXISTS ecdict_import_batches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_name VARCHAR(100) NOT NULL DEFAULT 'ECDICT',
    source_version VARCHAR(100) NULL,
    source_url VARCHAR(500) NOT NULL DEFAULT 'https://github.com/skywind3000/ECDICT',
    license_name VARCHAR(100) NOT NULL DEFAULT 'MIT',
    file_name VARCHAR(255) NOT NULL,
    file_sha256 VARCHAR(64) NULL,
    import_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    total_rows INT NOT NULL DEFAULT 0,
    loaded_rows INT NOT NULL DEFAULT 0,
    skipped_rows INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000) NULL,
    created_by_user_id BIGINT NULL,
    started_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_ecdict_import_batches_status (import_status, created_at),
    KEY idx_ecdict_import_batches_user_created (created_by_user_id, created_at),
    CONSTRAINT fk_ecdict_import_batches_user FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_ecdict_import_batches_status CHECK (
        import_status IN ('PENDING', 'LOADED', 'NORMALIZED', 'FAILED')
    ),
    CONSTRAINT ck_ecdict_import_batches_counts CHECK (
        total_rows >= 0 AND loaded_rows >= 0 AND skipped_rows >= 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ecdict_import_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    import_batch_id BIGINT NOT NULL,
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
    bnc_raw VARCHAR(32) NULL,
    frq_raw VARCHAR(32) NULL,
    bnc_rank INT NULL,
    frq_rank INT NULL,
    exchange_raw TEXT NULL,
    detail_raw MEDIUMTEXT NULL,
    audio_raw VARCHAR(1000) NULL,
    normalization_status VARCHAR(32) NOT NULL DEFAULT 'RAW',
    normalize_error VARCHAR(1000) NULL,
    normalized_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_ecdict_import_entries_batch (import_batch_id, id),
    KEY idx_ecdict_import_entries_normalized_word (normalized_word),
    KEY idx_ecdict_import_entries_frq_rank (frq_rank),
    KEY idx_ecdict_import_entries_bnc_rank (bnc_rank),
    KEY idx_ecdict_import_entries_normalization_status (normalization_status, updated_at),
    CONSTRAINT fk_ecdict_import_entries_batch FOREIGN KEY (import_batch_id) REFERENCES ecdict_import_batches (id) ON DELETE CASCADE,
    CONSTRAINT ck_ecdict_import_entries_normalization_status CHECK (
        normalization_status IN ('RAW', 'NORMALIZED', 'SKIPPED', 'FAILED')
    ),
    CONSTRAINT ck_ecdict_import_entries_ranks CHECK (
        (bnc_rank IS NULL OR bnc_rank > 0) AND (frq_rank IS NULL OR frq_rank > 0)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
