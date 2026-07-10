CREATE TABLE placement_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_type VARCHAR(64) NOT NULL,
    cefr_level VARCHAR(16) NOT NULL,
    scenario_tag VARCHAR(64) NOT NULL,
    target_skill VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    content JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_placement_items_status_level (status, cefr_level),
    KEY idx_placement_items_type (item_type),
    CONSTRAINT ck_placement_items_type CHECK (
        item_type IN (
            'SCENE_DIALOGUE_CHOICE',
            'CONTEXT_MEANING',
            'POLITENESS_JUDGMENT',
            'LISTENING_COMPREHENSION',
            'EXPRESSION_COMPLETION',
            'INTENT_UNDERSTANDING'
        )
    ),
    CONSTRAINT ck_placement_items_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1')),
    CONSTRAINT ck_placement_items_status CHECK (
        status IN (
            'PENDING',
            'GENERATING_TEXT',
            'VALIDATING_TEXT',
            'GENERATING_AUDIO',
            'READY',
            'FAILED',
            'EXPIRED'
        )
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
