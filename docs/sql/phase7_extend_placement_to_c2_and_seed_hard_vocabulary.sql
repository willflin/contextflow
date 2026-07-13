-- Phase 7 Hard Vocabulary Placement Extension
-- Phase 7 高难度词汇测试扩展

ALTER TABLE placement_items
    DROP CHECK ck_placement_items_level;

ALTER TABLE placement_items
    ADD CONSTRAINT ck_placement_items_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2'));

ALTER TABLE placement_sessions
    DROP CHECK ck_placement_sessions_level;

ALTER TABLE placement_sessions
    ADD CONSTRAINT ck_placement_sessions_level CHECK (
        estimated_level IS NULL OR estimated_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')
    );

ALTER TABLE user_level_profiles
    DROP CHECK ck_user_level_profiles_level;

ALTER TABLE user_level_profiles
    ADD CONSTRAINT ck_user_level_profiles_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2'));

-- The Flyway migration V17 inserts C1-C2 vocabulary calibration items from TOP_8000 to TOP_20000.
-- Flyway V17 会插入 TOP_8000 到 TOP_20000 的 C1-C2 词汇量校准题。
