-- Phase 4.2 双 Agent 对话记录表 / Dual-agent dialogue turn table
-- 用途 / Purpose:
-- 1. 保存学习包中的每一轮用户发言、Roleplay 回复和 Mentor 反馈。
--    Store each learner message, Roleplay reply, and Mentor feedback inside a learning package.
-- 2. 当前阶段使用本地规则模拟双 Agent，后续替换为真实 AI 调用。
--    This phase uses local rules to simulate the dual-agent response; later phases will replace it with real AI calls.
-- 3. scoring_signal 后续会反哺 LearningEvent、UnitStats 和复习系统。
--    scoring_signal will later feed LearningEvent, UnitStats, and the review system.

CREATE TABLE learning_dialogue_turns (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    learning_package_id BIGINT NOT NULL,
    turn_index INT NOT NULL,
    user_message TEXT NOT NULL,
    roleplay_reply TEXT NOT NULL,
    mentor_feedback TEXT NOT NULL,
    corrections JSON NOT NULL,
    natural_expression TEXT NOT NULL,
    scoring_signal JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_dialogue_turn_package_index (learning_package_id, turn_index),
    KEY idx_dialogue_turns_user_created (user_id, created_at),
    KEY idx_dialogue_turns_package (learning_package_id),
    CONSTRAINT fk_dialogue_turns_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_dialogue_turns_package FOREIGN KEY (learning_package_id) REFERENCES learning_packages (id),
    CONSTRAINT ck_dialogue_turns_index CHECK (turn_index > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DataGrip 检查语句 / DataGrip inspection queries

SELECT
    ldt.id,
    u.username,
    lp.title AS learning_package,
    ldt.turn_index,
    ldt.user_message,
    ldt.roleplay_reply,
    ldt.mentor_feedback,
    JSON_PRETTY(ldt.corrections) AS corrections,
    ldt.natural_expression,
    JSON_PRETTY(ldt.scoring_signal) AS scoring_signal,
    ldt.created_at
FROM learning_dialogue_turns ldt
JOIN users u ON u.id = ldt.user_id
JOIN learning_packages lp ON lp.id = ldt.learning_package_id
ORDER BY ldt.id DESC;

SELECT
    learning_package_id,
    COUNT(*) AS turn_count,
    MAX(created_at) AS last_turn_at
FROM learning_dialogue_turns
GROUP BY learning_package_id
ORDER BY last_turn_at DESC;
