ALTER TABLE user_learning_unit_sense_stats
    ADD COLUMN review_priority_score DECIMAL(8, 4) NOT NULL DEFAULT 0 AFTER next_review_at,
    ADD COLUMN last_priority_calculated_at DATETIME(6) NULL AFTER review_priority_score,
    ADD KEY idx_user_learning_unit_sense_stats_priority (user_id, review_priority_score),
    ADD KEY idx_user_learning_unit_sense_stats_next_priority (user_id, next_review_at, review_priority_score);

CREATE TABLE learning_unit_sense_scenario_tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    learning_unit_sense_id BIGINT NOT NULL,
    scenario_code VARCHAR(120) NOT NULL,
    relevance_score DECIMAL(5, 4) NOT NULL DEFAULT 1.0000,
    source VARCHAR(64) NOT NULL DEFAULT 'MANUAL',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_unit_sense_scenario_tags (learning_unit_sense_id, scenario_code),
    KEY idx_learning_unit_sense_scenario_tags_scenario (scenario_code, relevance_score),
    CONSTRAINT fk_learning_unit_sense_scenario_tags_sense FOREIGN KEY (learning_unit_sense_id)
        REFERENCES learning_unit_senses (id) ON DELETE CASCADE,
    CONSTRAINT ck_learning_unit_sense_scenario_tags_score CHECK (relevance_score BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
