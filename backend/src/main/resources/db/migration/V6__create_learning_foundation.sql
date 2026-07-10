CREATE TABLE scenario_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    difficulty_score INT NOT NULL,
    target_abilities JSON NOT NULL,
    applicable_levels JSON NOT NULL,
    risk_tags JSON NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_scenario_templates_code (code),
    KEY idx_scenario_templates_status_difficulty (status, difficulty_score),
    CONSTRAINT ck_scenario_templates_status CHECK (status IN ('ACTIVE', 'OFFLINE')),
    CONSTRAINT ck_scenario_templates_difficulty CHECK (difficulty_score BETWEEN 1 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_packages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scenario_template_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    generation_source VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    content JSON NOT NULL,
    assigned_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    expires_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_learning_packages_user_status (user_id, status),
    KEY idx_learning_packages_scenario (scenario_template_id),
    CONSTRAINT fk_learning_packages_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_learning_packages_scenario FOREIGN KEY (scenario_template_id) REFERENCES scenario_templates (id),
    CONSTRAINT ck_learning_packages_status CHECK (
        status IN (
            'PENDING',
            'GENERATING_TEXT',
            'VALIDATING_TEXT',
            'GENERATING_AUDIO',
            'READY',
            'FAILED',
            'EXPIRED',
            'COMPLETED'
        )
    ),
    CONSTRAINT ck_learning_packages_source CHECK (generation_source IN ('SEEDED_TEMPLATE', 'AI_GENERATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
