CREATE TABLE learning_plan_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    learning_unit_sense_id BIGINT NOT NULL,
    source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    exclusion_reason VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_learning_plan_items_user_sense (user_id, learning_unit_sense_id),
    KEY idx_learning_plan_items_user_status (user_id, status, id),
    KEY idx_learning_plan_items_sense (learning_unit_sense_id),
    CONSTRAINT fk_learning_plan_items_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_plan_items_sense FOREIGN KEY (learning_unit_sense_id)
        REFERENCES learning_unit_senses (id) ON DELETE CASCADE,
    CONSTRAINT ck_learning_plan_items_source CHECK (source IN ('USER_SELECTED', 'AUTO_RECOMMENDED')),
    CONSTRAINT ck_learning_plan_items_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'SKIPPED_OUT_OF_LEVEL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
