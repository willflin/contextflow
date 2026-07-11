-- Phase 5.2 复习优先级与学习计划 / Review priority and learning plan
-- 说明：review_priority_score 是复习候选唯一排序依据；新词义只在 NEW 池内使用 newSenseScore 临时排序。
-- Note: review_priority_score is the only priority basis for review candidates; newSenseScore only ranks NEW-pool candidates.

ALTER TABLE user_learning_unit_sense_stats
    ADD COLUMN review_priority_score DECIMAL(8, 4) NOT NULL DEFAULT 0 AFTER next_review_at,
    ADD COLUMN last_priority_calculated_at DATETIME(6) NULL AFTER review_priority_score,
    ADD KEY idx_user_learning_unit_sense_stats_priority (user_id, review_priority_score),
    ADD KEY idx_user_learning_unit_sense_stats_next_priority (user_id, next_review_at, review_priority_score);

CREATE TABLE learning_unit_sense_scenario_tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    learning_unit_sense_id BIGINT NOT NULL,
    scenario_code VARCHAR(120) NOT NULL,
    relevance_score DECIMAL(5, 4) NOT NULL DEFAULT 1.0000,
    source VARCHAR(64) NOT NULL DEFAULT 'MANUAL',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_unit_sense_scenario_tags (learning_unit_sense_id, scenario_code),
    KEY idx_learning_unit_sense_scenario_tags_scenario (scenario_code, relevance_score),
    CONSTRAINT fk_learning_unit_sense_scenario_tags_sense FOREIGN KEY (learning_unit_sense_id)
        REFERENCES learning_unit_senses (id) ON DELETE CASCADE,
    CONSTRAINT ck_learning_unit_sense_scenario_tags_score CHECK (relevance_score BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 计算公式 / Formula
-- rawPriority = dueScore + weaknessScore + outputRiskScore + correctionScore + exposureOnlyScore + recencyDecayScore - masteryPenalty
-- reviewPriorityScore = max(0, rawPriority * frequencyMultiplier)
-- 高频简单词仍记录事件和掌握度，但通过 frequencyMultiplier 降低进入复习队列的概率。
-- High-frequency simple words still record events and mastery, but frequencyMultiplier reduces review-queue pressure.

-- 新词义池 / New-sense pool
-- newSenseScore = frequencyUsefulnessScore + levelFitScore
-- 新词义会先选词义，再按 learning_unit_sense_scenario_tags 分配到一个或多个场景组。
-- New senses are selected first, then assigned to scenario groups through learning_unit_sense_scenario_tags.
