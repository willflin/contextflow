-- Phase 3.3: User level profile
-- 阶段 3.3：用户水平画像
--
-- Purpose:
-- Store the learner's placement result as structured profile data for later learning package generation.
--
-- 用途：
-- 把水平测试结果沉淀为结构化用户画像，供后续学习包生成使用。

CREATE TABLE user_level_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    cefr_level VARCHAR(16) NOT NULL,
    dimension_scores JSON NOT NULL,
    weak_scenarios JSON NOT NULL,
    weak_abilities JSON NOT NULL,
    last_placement_session_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_level_profiles_user (user_id),
    CONSTRAINT fk_user_level_profiles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_level_profiles_session FOREIGN KEY (last_placement_session_id) REFERENCES placement_sessions (id),
    CONSTRAINT ck_user_level_profiles_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DataGrip check:
-- DataGrip 检查：
SELECT user_id, cefr_level, dimension_scores, weak_scenarios, weak_abilities, last_placement_session_id, updated_at
FROM user_level_profiles
ORDER BY updated_at DESC;
