ALTER TABLE learning_events
    ADD COLUMN event_direction VARCHAR(32) NOT NULL DEFAULT 'LEARNER_INPUT' AFTER event_type;

UPDATE learning_events
SET event_direction = 'LEARNER_OUTPUT'
WHERE event_type = 'UNIT_ATTEMPTED';

ALTER TABLE learning_events
    ADD KEY idx_learning_events_user_direction_created (user_id, event_direction, created_at),
    ADD CONSTRAINT ck_learning_events_direction CHECK (event_direction IN ('LEARNER_OUTPUT', 'LEARNER_INPUT'));

CREATE TABLE learning_unit_sense_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reported_by_user_id BIGINT NULL,
    learning_unit_id BIGINT NULL,
    surface_text VARCHAR(255) NOT NULL,
    normalized_text VARCHAR(255) NOT NULL,
    source_type VARCHAR(32) NULL,
    source_id BIGINT NULL,
    source_text TEXT NULL,
    suggested_definition_en VARCHAR(1000) NULL,
    suggested_definition_zh VARCHAR(1000) NULL,
    agent_reason VARCHAR(1000) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    payload JSON NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_learning_unit_sense_feedback_unit_status (learning_unit_id, status),
    KEY idx_learning_unit_sense_feedback_normalized_status (normalized_text, status),
    KEY idx_learning_unit_sense_feedback_user_created (reported_by_user_id, created_at),
    CONSTRAINT fk_learning_unit_sense_feedback_user FOREIGN KEY (reported_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_learning_unit_sense_feedback_unit FOREIGN KEY (learning_unit_id) REFERENCES learning_units (id),
    CONSTRAINT ck_learning_unit_sense_feedback_status CHECK (status IN ('PENDING', 'REVIEWED', 'RESOLVED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
