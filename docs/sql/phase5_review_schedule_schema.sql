-- Phase 5.3 遗忘曲线与复习调度 / Forgetting curve and review scheduling
-- 说明：本阶段把 review_priority_score 从请求时计算推进到事件驱动 + 定期刷新。
-- Note: this phase moves review_priority_score from request-time calculation to event-driven plus scheduled refresh.

ALTER TABLE user_learning_unit_sense_stats
    ADD COLUMN stability_score DECIMAL(8, 4) NOT NULL DEFAULT 0.3000 AFTER mastery_level,
    ADD COLUMN difficulty_score DECIMAL(8, 4) NOT NULL DEFAULT 0.5000 AFTER stability_score,
    ADD COLUMN last_reviewed_at DATETIME(6) NULL AFTER last_attempt_at,
    ADD COLUMN review_interval_hours INT NOT NULL DEFAULT 24 AFTER next_review_at,
    ADD KEY idx_user_learning_unit_sense_stats_refresh (last_priority_calculated_at, review_priority_score);

-- 字段含义 / Field meaning
-- stability_score: 词义记忆稳定度，越高间隔越长。 / Memory stability for the sense; higher means longer intervals.
-- difficulty_score: 调度难度，越高越容易提前复习。 / Scheduling difficulty; higher means earlier review pressure.
-- last_reviewed_at: 最近一次可用于复习调度的事件时间。 / Latest event time usable for review scheduling.
-- review_interval_hours: 当前建议复习间隔小时数。 / Current suggested review interval in hours.

-- 运行方式 / Runtime flow
-- 1. 有明确 learning_unit_sense_id 的事件写入 stats 后，立即更新 stability/difficulty/interval/next_review_at/review_priority_score。
-- 1. After an event with explicit learning_unit_sense_id updates stats, stability/difficulty/interval/next_review_at/review_priority_score are refreshed immediately.
-- 2. 后端定时任务周期性刷新所有已学词义的 review_priority_score。
-- 2. A backend scheduled job periodically refreshes review_priority_score for all learned senses.
-- 3. GET /api/review/plan 只读取持久化 review_priority_score 排序，不在请求时重算并写库。
-- 3. GET /api/review/plan reads persisted review_priority_score for ordering and does not recalculate/write during the request.
