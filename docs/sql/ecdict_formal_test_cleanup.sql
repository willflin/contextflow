-- ECDICT 正式表测试导入前清理 / Cleanup before ECDICT formal test import
-- 目标：清理旧 demo 语言单元和上一次测试导入，避免与本次小批量正式导入混在一起。
-- Goal: remove old demo language units and previous test imports before this small formal import.

DELETE FROM user_learning_unit_sense_stats;
DELETE FROM learning_events;
UPDATE learning_unit_sense_feedback
SET learning_unit_id = NULL
WHERE learning_unit_id IS NOT NULL;
DELETE FROM learning_unit_sense_scenario_tags;
DELETE FROM learning_unit_sense_sources;
DELETE FROM learning_unit_forms;
DELETE FROM learning_unit_senses;
DELETE FROM learning_units;
DELETE FROM learning_data_sources
WHERE source_name IN ('MANUAL_SEED', 'ECDICT_CLEAN_WORDS');
