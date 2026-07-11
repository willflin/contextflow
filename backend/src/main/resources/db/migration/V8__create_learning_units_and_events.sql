CREATE TABLE learning_units (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    unit_type VARCHAR(32) NOT NULL,
    text VARCHAR(255) NOT NULL,
    match_text VARCHAR(255) NOT NULL,
    meaning VARCHAR(500) NOT NULL,
    difficulty_level VARCHAR(16) NOT NULL,
    tags JSON NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_units_code (code),
    KEY idx_learning_units_status_type (status, unit_type),
    CONSTRAINT ck_learning_units_type CHECK (unit_type IN ('WORD', 'PHRASE', 'SENTENCE_PATTERN')),
    CONSTRAINT ck_learning_units_level CHECK (difficulty_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    CONSTRAINT ck_learning_units_status CHECK (status IN ('ACTIVE', 'OFFLINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    learning_unit_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_id BIGINT NOT NULL,
    source_text TEXT NOT NULL,
    occurrence_text VARCHAR(500) NOT NULL,
    payload JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_learning_events_user_unit_created (user_id, learning_unit_id, created_at),
    KEY idx_learning_events_source (source_type, source_id),
    KEY idx_learning_events_event_type_created (event_type, created_at),
    CONSTRAINT fk_learning_events_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_learning_events_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id),
    CONSTRAINT ck_learning_events_type CHECK (
        event_type IN ('UNIT_ATTEMPTED', 'UNIT_EXPOSED', 'UNIT_CORRECTED', 'UNIT_RECOMMENDED')
    ),
    CONSTRAINT ck_learning_events_source_type CHECK (source_type IN ('LEARNING_DIALOGUE_TURN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
