-- Phase 7 Placement Option Randomization
-- Phase 7 测试选项乱序

ALTER TABLE placement_session_answers
    ADD COLUMN option_order_json JSON NULL AFTER difficulty_score;
