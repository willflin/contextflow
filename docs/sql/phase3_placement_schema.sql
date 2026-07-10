-- Phase 3.1: Placement item pool
-- 阶段 3.1：水平测试题池
--
-- Purpose:
-- Store pre-generated placement test items. Users should only consume READY items.
--
-- 用途：
-- 保存预生成的水平测试题。用户只消费 READY 状态的题目。

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

-- DataGrip check:
-- DataGrip 检查：
SELECT id, item_type, cefr_level, scenario_tag, target_skill, status, created_at
FROM placement_items
ORDER BY id;
