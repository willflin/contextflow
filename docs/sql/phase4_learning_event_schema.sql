-- Phase 4.3 学习单元与学习事件 / Learning units and learning events
-- 用途 / Purpose:
-- 1. learning_units 只保存语言单元本体。/ learning_units stores only the language unit identity.
-- 2. learning_unit_senses 保存具体词义或用法。/ learning_unit_senses stores concrete senses or usages.
-- 3. learning_unit_forms 保存词形，用于 went -> go 等匹配。/ learning_unit_forms stores surface forms such as went -> go.
-- 4. learning_events 仍然是一次语言单元出现事件，sense 可空。/ learning_events still records one unit occurrence; sense is optional.

-- Phase 4.9 matching note:
-- 表结构保留 WORD / PHRASE / SENTENCE_PATTERN；当前事件写入前只对 WORD 启用规范化 token 匹配，本次无建表变更。
-- The schema reserves WORD / PHRASE / SENTENCE_PATTERN; current event insertion only enables normalized token matching for WORD, and this phase has no DDL change.

CREATE TABLE learning_units (
    id BIGINT NOT NULL AUTO_INCREMENT,
    unit_type VARCHAR(32) NOT NULL,
    canonical_text VARCHAR(255) NOT NULL,
    normalized_text VARCHAR(255) NOT NULL,
    language_code VARCHAR(16) NOT NULL DEFAULT 'en',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_units_identity (language_code, unit_type, normalized_text),
    KEY idx_learning_units_type_status (unit_type, status),
    KEY idx_learning_units_normalized_text (normalized_text),
    CONSTRAINT ck_learning_units_type CHECK (unit_type IN ('WORD', 'PHRASE', 'SENTENCE_PATTERN')),
    CONSTRAINT ck_learning_units_status CHECK (status IN ('ACTIVE', 'OFFLINE', 'REVIEW'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_unit_senses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    learning_unit_id BIGINT NOT NULL,
    sense_key VARCHAR(255) NOT NULL,
    part_of_speech VARCHAR(32) NULL,
    definition_en VARCHAR(1000) NOT NULL,
    definition_zh VARCHAR(1000) NULL,
    difficulty_level VARCHAR(16) NULL,
    difficulty_confidence DECIMAL(5, 4) NULL,
    frequency_score DECIMAL(10, 4) NULL,
    frequency_band VARCHAR(32) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_unit_senses_key (learning_unit_id, sense_key),
    UNIQUE KEY uk_learning_unit_senses_unit_id (learning_unit_id, id),
    KEY idx_learning_unit_senses_unit_status (learning_unit_id, status),
    KEY idx_learning_unit_senses_level (difficulty_level),
    KEY idx_learning_unit_senses_pos (part_of_speech),
    CONSTRAINT fk_learning_unit_senses_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_unit_forms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    learning_unit_id BIGINT NOT NULL,
    form_text VARCHAR(255) NOT NULL,
    normalized_form VARCHAR(255) NOT NULL,
    form_type VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_unit_forms_identity (learning_unit_id, normalized_form, form_type),
    KEY idx_learning_unit_forms_normalized (normalized_form),
    KEY idx_learning_unit_forms_unit (learning_unit_id),
    CONSTRAINT fk_learning_unit_forms_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_data_sources (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_name VARCHAR(100) NOT NULL,
    source_version VARCHAR(100) NULL,
    source_url VARCHAR(500) NULL,
    license_name VARCHAR(100) NOT NULL,
    attribution VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_data_sources_identity (source_name, source_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_unit_sense_sources (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sense_id BIGINT NOT NULL,
    data_source_id BIGINT NOT NULL,
    attribute_type VARCHAR(32) NOT NULL,
    source_record_id VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_unit_sense_sources_identity (sense_id, data_source_id, attribute_type, source_record_id),
    KEY idx_learning_unit_sense_sources_sense (sense_id),
    KEY idx_learning_unit_sense_sources_source (data_source_id),
    CONSTRAINT fk_learning_unit_sense_sources_sense FOREIGN KEY (sense_id) REFERENCES learning_unit_senses (id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_unit_sense_sources_source FOREIGN KEY (data_source_id) REFERENCES learning_data_sources (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    learning_unit_id BIGINT NOT NULL,
    learning_unit_sense_id BIGINT NULL,
    event_type VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_id BIGINT NOT NULL,
    source_text TEXT NOT NULL,
    occurrence_text VARCHAR(500) NOT NULL,
    payload JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY fk_learning_events_unit (learning_unit_id),
    KEY fk_learning_events_sense (learning_unit_sense_id),
    KEY idx_learning_events_event_type_created (event_type, created_at),
    KEY idx_learning_events_source (source_type, source_id),
    KEY idx_learning_events_user_unit_created (user_id, learning_unit_id, created_at),
    KEY idx_learning_events_user_sense_created (user_id, learning_unit_sense_id, created_at),
    CONSTRAINT fk_learning_events_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_learning_events_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id),
    CONSTRAINT fk_learning_events_sense FOREIGN KEY (learning_unit_sense_id) REFERENCES learning_unit_senses (id),
    CONSTRAINT fk_learning_events_unit_sense_pair FOREIGN KEY (learning_unit_id, learning_unit_sense_id)
        REFERENCES learning_unit_senses (learning_unit_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_learning_unit_sense_stats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    learning_unit_sense_id BIGINT NOT NULL,
    exposure_count INT NOT NULL DEFAULT 0,
    attempt_count INT NOT NULL DEFAULT 0,
    correct_count INT NOT NULL DEFAULT 0,
    incorrect_count INT NOT NULL DEFAULT 0,
    correction_count INT NOT NULL DEFAULT 0,
    recommendation_count INT NOT NULL DEFAULT 0,
    mastery_score DECIMAL(5, 4) NOT NULL DEFAULT 0,
    mastery_level VARCHAR(32) NOT NULL DEFAULT 'UNSEEN',
    first_seen_at DATETIME(6) NULL,
    last_seen_at DATETIME(6) NULL,
    last_attempt_at DATETIME(6) NULL,
    next_review_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_learning_unit_sense_stats (user_id, learning_unit_sense_id),
    KEY idx_user_learning_unit_sense_stats_sense (learning_unit_sense_id),
    KEY idx_user_learning_unit_sense_stats_user_mastery (user_id, mastery_level),
    KEY idx_user_learning_unit_sense_stats_user_review (user_id, next_review_at),
    CONSTRAINT fk_user_learning_unit_sense_stats_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_learning_unit_sense_stats_sense FOREIGN KEY (learning_unit_sense_id)
        REFERENCES learning_unit_senses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DataGrip 检查语句 / DataGrip inspection queries

SELECT id, unit_type, canonical_text, normalized_text, language_code, status
FROM learning_units
ORDER BY id ASC;

SELECT lu.canonical_text, lus.sense_key, lus.part_of_speech, lus.definition_en, lus.definition_zh
FROM learning_unit_senses lus
JOIN learning_units lu ON lu.id = lus.learning_unit_id
ORDER BY lu.canonical_text, lus.id;

SELECT lu.canonical_text, luf.form_text, luf.normalized_form, luf.form_type
FROM learning_unit_forms luf
JOIN learning_units lu ON lu.id = luf.learning_unit_id
ORDER BY lu.canonical_text, luf.id;

SELECT le.id, u.username, lu.canonical_text, lus.sense_key, le.event_type, le.occurrence_text, le.created_at
FROM learning_events le
JOIN users u ON u.id = le.user_id
JOIN learning_units lu ON lu.id = le.learning_unit_id
LEFT JOIN learning_unit_senses lus ON lus.id = le.learning_unit_sense_id
ORDER BY le.id DESC;
