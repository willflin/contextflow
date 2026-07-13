ALTER TABLE placement_session_answers
    ADD COLUMN option_order_json JSON NULL AFTER difficulty_score;
