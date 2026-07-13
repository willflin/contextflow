-- Phase 7 Vocabulary Placement Measurement
-- Phase 7 词汇量水平测试结构

ALTER TABLE placement_items
    ADD COLUMN frequency_rank INT NULL AFTER difficulty_score,
    ADD COLUMN frequency_band VARCHAR(32) NULL AFTER frequency_rank,
    ADD COLUMN item_discrimination DECIMAL(6,3) NOT NULL DEFAULT 1.000 AFTER frequency_band,
    ADD COLUMN guessing_factor DECIMAL(6,3) NOT NULL DEFAULT 0.250 AFTER item_discrimination;

ALTER TABLE placement_items
    DROP CHECK ck_placement_items_type;

ALTER TABLE placement_items
    ADD CONSTRAINT ck_placement_items_type CHECK (
        item_type IN (
            'SCENE_DIALOGUE_CHOICE',
            'CONTEXT_MEANING',
            'POLITENESS_JUDGMENT',
            'LISTENING_COMPREHENSION',
            'EXPRESSION_COMPLETION',
            'INTENT_UNDERSTANDING',
            'TRUE_FALSE',
            'SYNONYM_CHOICE',
            'ANTONYM_CHOICE',
            'CLOZE_TEXT',
            'ZH_MEANING_CHOICE',
            'BEST_EXPRESSION_CHOICE'
        )
    ),
    ADD CONSTRAINT ck_placement_items_frequency_rank CHECK (frequency_rank IS NULL OR frequency_rank > 0),
    ADD CONSTRAINT ck_placement_items_irt CHECK (
        item_discrimination > 0
        AND guessing_factor >= 0
        AND guessing_factor < 1
    );

ALTER TABLE user_level_profiles
    ADD COLUMN vocabulary_size_estimate INT NULL AFTER cefr_level,
    ADD COLUMN vocabulary_band VARCHAR(32) NULL AFTER vocabulary_size_estimate,
    ADD COLUMN vocabulary_measurement_error INT NULL AFTER vocabulary_band;
