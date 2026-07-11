ALTER TABLE user_learning_unit_sense_stats
    ADD COLUMN stability_score DECIMAL(8, 4) NOT NULL DEFAULT 0.3000 AFTER mastery_level,
    ADD COLUMN difficulty_score DECIMAL(8, 4) NOT NULL DEFAULT 0.5000 AFTER stability_score,
    ADD COLUMN last_reviewed_at DATETIME(6) NULL AFTER last_attempt_at,
    ADD COLUMN review_interval_hours INT NOT NULL DEFAULT 24 AFTER next_review_at,
    ADD KEY idx_user_learning_unit_sense_stats_refresh (last_priority_calculated_at, review_priority_score);
