DROP TABLE IF EXISTS user_learning_unit_sense_stats;
DROP TABLE IF EXISTS learning_events;
DROP TABLE IF EXISTS learning_unit_sense_sources;
DROP TABLE IF EXISTS learning_unit_forms;
DROP TABLE IF EXISTS learning_unit_senses;
DROP TABLE IF EXISTS learning_data_sources;
DROP TABLE IF EXISTS learning_units;

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
    CONSTRAINT fk_learning_unit_senses_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id) ON DELETE CASCADE,
    CONSTRAINT ck_learning_unit_senses_pos CHECK (
        part_of_speech IS NULL OR part_of_speech IN (
            'NOUN',
            'VERB',
            'ADJECTIVE',
            'ADVERB',
            'PRONOUN',
            'DETERMINER',
            'PREPOSITION',
            'CONJUNCTION',
            'INTERJECTION',
            'NUMERAL',
            'AUXILIARY',
            'PARTICLE',
            'OTHER'
        )
    ),
    CONSTRAINT ck_learning_unit_senses_level CHECK (
        difficulty_level IS NULL OR difficulty_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')
    ),
    CONSTRAINT ck_learning_unit_senses_confidence CHECK (
        difficulty_confidence IS NULL OR difficulty_confidence BETWEEN 0 AND 1
    ),
    CONSTRAINT ck_learning_unit_senses_frequency_band CHECK (
        frequency_band IS NULL OR frequency_band IN ('VERY_COMMON', 'COMMON', 'MEDIUM', 'UNCOMMON', 'RARE')
    ),
    CONSTRAINT ck_learning_unit_senses_status CHECK (status IN ('ACTIVE', 'OFFLINE', 'REVIEW'))
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
    CONSTRAINT fk_learning_unit_forms_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id) ON DELETE CASCADE,
    CONSTRAINT ck_learning_unit_forms_type CHECK (
        form_type IN (
            'LEMMA',
            'PLURAL',
            'THIRD_PERSON_SINGULAR',
            'PAST_TENSE',
            'PAST_PARTICIPLE',
            'PRESENT_PARTICIPLE',
            'COMPARATIVE',
            'SUPERLATIVE',
            'VARIANT',
            'OTHER'
        )
    )
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
    CONSTRAINT fk_learning_unit_sense_sources_source FOREIGN KEY (data_source_id) REFERENCES learning_data_sources (id),
    CONSTRAINT ck_learning_unit_sense_sources_attribute CHECK (
        attribute_type IN ('SENSE', 'DEFINITION', 'TRANSLATION', 'DIFFICULTY', 'FREQUENCY')
    )
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
        REFERENCES learning_unit_senses (learning_unit_id, id),
    CONSTRAINT ck_learning_events_type CHECK (
        event_type IN ('UNIT_ATTEMPTED', 'UNIT_EXPOSED', 'UNIT_CORRECTED', 'UNIT_RECOMMENDED')
    ),
    CONSTRAINT ck_learning_events_source_type CHECK (source_type = 'LEARNING_DIALOGUE_TURN')
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
        REFERENCES learning_unit_senses (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_learning_unit_sense_stats_counts CHECK (
        exposure_count >= 0
        AND attempt_count >= 0
        AND correct_count >= 0
        AND incorrect_count >= 0
        AND correction_count >= 0
        AND recommendation_count >= 0
    ),
    CONSTRAINT ck_user_learning_unit_sense_stats_mastery_score CHECK (mastery_score BETWEEN 0 AND 1),
    CONSTRAINT ck_user_learning_unit_sense_stats_mastery_level CHECK (
        mastery_level IN ('UNSEEN', 'EXPOSED', 'LEARNING', 'FAMILIAR', 'MASTERED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
