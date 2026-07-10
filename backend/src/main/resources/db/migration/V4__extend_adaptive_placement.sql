ALTER TABLE placement_items
    ADD COLUMN difficulty_score INT NOT NULL DEFAULT 50 AFTER cefr_level,
    ADD COLUMN ability_dimension VARCHAR(64) NOT NULL DEFAULT 'contextual_understanding' AFTER target_skill,
    ADD COLUMN grading_type VARCHAR(32) NOT NULL DEFAULT 'LOCAL_EXACT' AFTER status;

UPDATE placement_items SET difficulty_score = 35 WHERE scenario_tag = 'hotel_check_in';
UPDATE placement_items SET difficulty_score = 55 WHERE scenario_tag = 'shopping';
UPDATE placement_items SET difficulty_score = 60 WHERE scenario_tag = 'bank_account';
UPDATE placement_items SET difficulty_score = 75 WHERE scenario_tag = 'police_stop';

ALTER TABLE placement_items DROP CHECK ck_placement_items_type;

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
            'CLOZE_TEXT'
        )
    ),
    ADD CONSTRAINT ck_placement_items_grading_type CHECK (
        grading_type IN ('LOCAL_EXACT', 'LOCAL_ACCEPTED_ANSWERS', 'AI_JUDGE')
    ),
    ADD CONSTRAINT ck_placement_items_difficulty CHECK (difficulty_score BETWEEN 1 AND 100);

ALTER TABLE placement_sessions
    ADD COLUMN mode VARCHAR(32) NOT NULL DEFAULT 'BATCH' AFTER status,
    ADD COLUMN answered_count INT NOT NULL DEFAULT 0 AFTER item_count,
    ADD COLUMN max_item_count INT NOT NULL DEFAULT 10 AFTER answered_count,
    ADD COLUMN current_difficulty_score INT NOT NULL DEFAULT 50 AFTER max_item_count;

UPDATE placement_sessions
SET answered_count = COALESCE(correct_count, 0),
    max_item_count = item_count,
    current_difficulty_score = 50
WHERE mode = 'BATCH';

ALTER TABLE placement_sessions
    ADD CONSTRAINT ck_placement_sessions_mode CHECK (mode IN ('BATCH', 'ADAPTIVE')),
    ADD CONSTRAINT ck_placement_sessions_current_difficulty CHECK (current_difficulty_score BETWEEN 1 AND 100);

ALTER TABLE placement_session_answers
    ADD COLUMN grading_type VARCHAR(32) NOT NULL DEFAULT 'LOCAL_EXACT' AFTER item_order,
    ADD COLUMN difficulty_score INT NOT NULL DEFAULT 50 AFTER grading_type,
    ADD COLUMN raw_answer TEXT NULL AFTER selected_option_index,
    ADD COLUMN judge_payload JSON NULL AFTER is_correct;

ALTER TABLE placement_session_answers
    ADD CONSTRAINT ck_placement_session_answers_grading_type CHECK (
        grading_type IN ('LOCAL_EXACT', 'LOCAL_ACCEPTED_ANSWERS', 'AI_JUDGE')
    ),
    ADD CONSTRAINT ck_placement_session_answers_difficulty CHECK (difficulty_score BETWEEN 1 AND 100);
