-- Phase 4.1 学习包基础表 / Learning package foundation tables
-- 用途 / Purpose:
-- 1. scenario_templates 保存系统级场景模板。
--    scenario_templates stores system-level scenario templates.
-- 2. learning_packages 保存分配给用户的 READY 学习包。
--    learning_packages stores READY learning packages assigned to learners.
-- 3. 当前阶段先用 SEEDED_TEMPLATE 模拟未来 AI 预生成结果。
--    This phase uses SEEDED_TEMPLATE content as a stand-in for future AI pre-generation.

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

-- DataGrip 检查语句 / DataGrip inspection queries

SELECT
    id,
    code,
    name,
    difficulty_score,
    applicable_levels,
    status,
    created_at
FROM scenario_templates
ORDER BY difficulty_score ASC;

SELECT
    lp.id,
    u.username,
    st.code AS scenario_code,
    lp.status,
    lp.generation_source,
    lp.title,
    lp.assigned_at,
    lp.expires_at,
    lp.created_at
FROM learning_packages lp
JOIN users u ON u.id = lp.user_id
JOIN scenario_templates st ON st.id = lp.scenario_template_id
ORDER BY lp.id DESC;

SELECT
    id,
    JSON_PRETTY(content) AS content
FROM learning_packages
ORDER BY id DESC;
