CREATE TABLE placement_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    item_count INT NOT NULL,
    correct_count INT NULL,
    score_percent DECIMAL(5, 2) NULL,
    estimated_level VARCHAR(16) NULL,
    started_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    submitted_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_placement_sessions_user_status (user_id, status),
    CONSTRAINT fk_placement_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_placement_sessions_status CHECK (status IN ('STARTED', 'SUBMITTED', 'EXPIRED')),
    CONSTRAINT ck_placement_sessions_level CHECK (estimated_level IS NULL OR estimated_level IN ('A1', 'A2', 'B1', 'B2', 'C1'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE placement_session_answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_order INT NOT NULL,
    selected_option_index INT NULL,
    is_correct BOOLEAN NULL,
    answered_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_placement_session_answers_session_item (session_id, item_id),
    KEY idx_placement_session_answers_session (session_id, item_order),
    CONSTRAINT fk_placement_session_answers_session FOREIGN KEY (session_id) REFERENCES placement_sessions (id),
    CONSTRAINT fk_placement_session_answers_item FOREIGN KEY (item_id) REFERENCES placement_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
