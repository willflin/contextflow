
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ecdict_clean_word_entries`;
DROP TABLE IF EXISTS `ecdict_import_entries`;
DROP TABLE IF EXISTS `ecdict_import_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ecdict_import_batches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ECDICT',
  `source_version` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'https://github.com/skywind3000/ECDICT',
  `license_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MIT',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_sha256` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `import_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `total_rows` int NOT NULL DEFAULT '0',
  `loaded_rows` int NOT NULL DEFAULT '0',
  `skipped_rows` int NOT NULL DEFAULT '0',
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by_user_id` bigint DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_ecdict_import_batches_status` (`import_status`,`created_at`),
  KEY `idx_ecdict_import_batches_user_created` (`created_by_user_id`,`created_at`),
  CONSTRAINT `fk_ecdict_import_batches_user` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_ecdict_import_batches_counts` CHECK (((`total_rows` >= 0) and (`loaded_rows` >= 0) and (`skipped_rows` >= 0))),
  CONSTRAINT `ck_ecdict_import_batches_status` CHECK ((`import_status` in (_utf8mb4'PENDING',_utf8mb4'LOADED',_utf8mb4'NORMALIZED',_utf8mb4'FAILED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ecdict_import_entries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `import_batch_id` bigint NOT NULL,
  `source_row_number` int DEFAULT NULL,
  `word` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_word` varchar(255) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (lower(trim(`word`))) STORED,
  `phonetic` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `definition_raw` text COLLATE utf8mb4_unicode_ci,
  `translation_raw` text COLLATE utf8mb4_unicode_ci,
  `pos_raw` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `collins_raw` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `oxford_raw` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tag_raw` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bnc_raw` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `frq_raw` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bnc_rank` int DEFAULT NULL,
  `frq_rank` int DEFAULT NULL,
  `exchange_raw` text COLLATE utf8mb4_unicode_ci,
  `detail_raw` mediumtext COLLATE utf8mb4_unicode_ci,
  `audio_raw` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `normalization_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RAW',
  `normalize_error` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `normalized_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_ecdict_import_entries_batch` (`import_batch_id`,`id`),
  KEY `idx_ecdict_import_entries_normalized_word` (`normalized_word`),
  KEY `idx_ecdict_import_entries_frq_rank` (`frq_rank`),
  KEY `idx_ecdict_import_entries_bnc_rank` (`bnc_rank`),
  KEY `idx_ecdict_import_entries_normalization_status` (`normalization_status`,`updated_at`),
  CONSTRAINT `fk_ecdict_import_entries_batch` FOREIGN KEY (`import_batch_id`) REFERENCES `ecdict_import_batches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_ecdict_import_entries_normalization_status` CHECK ((`normalization_status` in (_utf8mb4'RAW',_utf8mb4'NORMALIZED',_utf8mb4'SKIPPED',_utf8mb4'FAILED'))),
  CONSTRAINT `ck_ecdict_import_entries_ranks` CHECK ((((`bnc_rank` is null) or (`bnc_rank` > 0)) and ((`frq_rank` is null) or (`frq_rank` > 0))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ecdict_clean_word_entries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `raw_entry_id` bigint NOT NULL,
  `raw_import_batch_id` bigint NOT NULL,
  `source_row_number` int DEFAULT NULL,
  `word` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_word` varchar(255) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS (lower(trim(`word`))) STORED,
  `phonetic` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `definition_raw` text COLLATE utf8mb4_unicode_ci,
  `translation_raw` text COLLATE utf8mb4_unicode_ci,
  `pos_raw` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `collins_raw` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `oxford_raw` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tag_raw` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bnc_rank` int DEFAULT NULL,
  `frq_rank` int DEFAULT NULL,
  `effective_frequency_rank` int GENERATED ALWAYS AS (coalesce(`frq_rank`,`bnc_rank`)) STORED,
  `frequency_source` varchar(16) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when (`frq_rank` is not null) then _utf8mb4'FRQ' when (`bnc_rank` is not null) then _utf8mb4'BNC' else NULL end)) STORED,
  `exchange_raw` text COLLATE utf8mb4_unicode_ci,
  `cleaning_rule_version` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'word-frequency-v3',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ecdict_clean_word_entries_raw` (`raw_entry_id`),
  UNIQUE KEY `uk_ecdict_clean_word_entries_word` (`normalized_word`),
  KEY `idx_ecdict_clean_word_entries_batch` (`raw_import_batch_id`),
  KEY `idx_ecdict_clean_word_entries_effective_rank` (`effective_frequency_rank`),
  KEY `idx_ecdict_clean_word_entries_frequency_source` (`frequency_source`),
  KEY `idx_ecdict_clean_word_entries_frq_rank` (`frq_rank`),
  KEY `idx_ecdict_clean_word_entries_bnc_rank` (`bnc_rank`),
  CONSTRAINT `fk_ecdict_clean_word_entries_batch` FOREIGN KEY (`raw_import_batch_id`) REFERENCES `ecdict_import_batches` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ecdict_clean_word_entries_raw` FOREIGN KEY (`raw_entry_id`) REFERENCES `ecdict_import_entries` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_ecdict_clean_word_entries_ranks` CHECK (((`bnc_rank` is null) or (`bnc_rank` > 0)) and ((`frq_rank` is null) or (`frq_rank` > 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_data_sources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_data_sources` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_version` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `license_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `attribution` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_data_sources_identity` (`source_name`,`source_version`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_dialogue_turns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_dialogue_turns` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `learning_package_id` bigint NOT NULL,
  `turn_index` int NOT NULL,
  `user_message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `roleplay_reply` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `mentor_feedback` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `corrections` json NOT NULL,
  `natural_expression` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `scoring_signal` json NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dialogue_turn_package_index` (`learning_package_id`,`turn_index`),
  KEY `idx_dialogue_turns_package` (`learning_package_id`),
  KEY `idx_dialogue_turns_user_created` (`user_id`,`created_at`),
  CONSTRAINT `fk_dialogue_turns_package` FOREIGN KEY (`learning_package_id`) REFERENCES `learning_packages` (`id`),
  CONSTRAINT `fk_dialogue_turns_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_dialogue_turns_index` CHECK ((`turn_index` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `learning_unit_id` bigint NOT NULL,
  `learning_unit_sense_id` bigint DEFAULT NULL,
  `event_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_direction` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LEARNER_INPUT',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_id` bigint NOT NULL,
  `source_text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `occurrence_text` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` json NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_learning_events_unit` (`learning_unit_id`),
  KEY `fk_learning_events_sense` (`learning_unit_sense_id`),
  KEY `idx_learning_events_event_type_created` (`event_type`,`created_at`),
  KEY `idx_learning_events_source` (`source_type`,`source_id`),
  KEY `idx_learning_events_user_direction_created` (`user_id`,`event_direction`,`created_at`),
  KEY `idx_learning_events_user_unit_created` (`user_id`,`learning_unit_id`,`created_at`),
  KEY `idx_learning_events_user_sense_created` (`user_id`,`learning_unit_sense_id`,`created_at`),
  KEY `fk_learning_events_unit_sense_pair` (`learning_unit_id`,`learning_unit_sense_id`),
  CONSTRAINT `fk_learning_events_sense` FOREIGN KEY (`learning_unit_sense_id`) REFERENCES `learning_unit_senses` (`id`),
  CONSTRAINT `fk_learning_events_unit` FOREIGN KEY (`learning_unit_id`) REFERENCES `learning_units` (`id`),
  CONSTRAINT `fk_learning_events_unit_sense_pair` FOREIGN KEY (`learning_unit_id`, `learning_unit_sense_id`) REFERENCES `learning_unit_senses` (`learning_unit_id`, `id`),
  CONSTRAINT `fk_learning_events_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_learning_events_direction` CHECK ((`event_direction` in (_utf8mb4'LEARNER_OUTPUT',_utf8mb4'LEARNER_INPUT'))),
  CONSTRAINT `ck_learning_events_source_type` CHECK ((`source_type` = _utf8mb4'LEARNING_DIALOGUE_TURN')),
  CONSTRAINT `ck_learning_events_type` CHECK ((`event_type` in (_utf8mb4'UNIT_ATTEMPTED',_utf8mb4'UNIT_EXPOSED',_utf8mb4'UNIT_CORRECTED',_utf8mb4'UNIT_RECOMMENDED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_packages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_packages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `scenario_template_id` bigint NOT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `generation_source` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` json NOT NULL,
  `assigned_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_learning_packages_scenario` (`scenario_template_id`),
  KEY `idx_learning_packages_user_status` (`user_id`,`status`),
  CONSTRAINT `fk_learning_packages_scenario` FOREIGN KEY (`scenario_template_id`) REFERENCES `scenario_templates` (`id`),
  CONSTRAINT `fk_learning_packages_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_learning_packages_source` CHECK ((`generation_source` in (_utf8mb4'SEEDED_TEMPLATE',_utf8mb4'AI_GENERATED'))),
  CONSTRAINT `ck_learning_packages_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'GENERATING_TEXT',_utf8mb4'VALIDATING_TEXT',_utf8mb4'GENERATING_AUDIO',_utf8mb4'READY',_utf8mb4'FAILED',_utf8mb4'EXPIRED',_utf8mb4'COMPLETED')))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_unit_forms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_unit_forms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `learning_unit_id` bigint NOT NULL,
  `form_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_form` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `form_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_unit_forms_identity` (`learning_unit_id`,`normalized_form`,`form_type`),
  KEY `idx_learning_unit_forms_normalized` (`normalized_form`),
  KEY `idx_learning_unit_forms_unit` (`learning_unit_id`),
  CONSTRAINT `fk_learning_unit_forms_unit` FOREIGN KEY (`learning_unit_id`) REFERENCES `learning_units` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_learning_unit_forms_type` CHECK ((`form_type` in (_utf8mb4'LEMMA',_utf8mb4'PLURAL',_utf8mb4'THIRD_PERSON_SINGULAR',_utf8mb4'PAST_TENSE',_utf8mb4'PAST_PARTICIPLE',_utf8mb4'PRESENT_PARTICIPLE',_utf8mb4'COMPARATIVE',_utf8mb4'SUPERLATIVE',_utf8mb4'VARIANT',_utf8mb4'OTHER')))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_unit_sense_sources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_unit_sense_sources` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sense_id` bigint NOT NULL,
  `data_source_id` bigint NOT NULL,
  `attribute_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_record_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_unit_sense_sources_identity` (`sense_id`,`data_source_id`,`attribute_type`,`source_record_id`),
  KEY `idx_learning_unit_sense_sources_sense` (`sense_id`),
  KEY `idx_learning_unit_sense_sources_source` (`data_source_id`),
  CONSTRAINT `fk_learning_unit_sense_sources_sense` FOREIGN KEY (`sense_id`) REFERENCES `learning_unit_senses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_learning_unit_sense_sources_source` FOREIGN KEY (`data_source_id`) REFERENCES `learning_data_sources` (`id`),
  CONSTRAINT `ck_learning_unit_sense_sources_attribute` CHECK ((`attribute_type` in (_utf8mb4'SENSE',_utf8mb4'DEFINITION',_utf8mb4'TRANSLATION',_utf8mb4'DIFFICULTY',_utf8mb4'FREQUENCY')))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_unit_sense_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_unit_sense_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reported_by_user_id` bigint DEFAULT NULL,
  `learning_unit_id` bigint DEFAULT NULL,
  `surface_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_id` bigint DEFAULT NULL,
  `source_text` text COLLATE utf8mb4_unicode_ci,
  `suggested_definition_en` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `suggested_definition_zh` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `agent_reason` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `payload` json DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_learning_unit_sense_feedback_unit_status` (`learning_unit_id`,`status`),
  KEY `idx_learning_unit_sense_feedback_normalized_status` (`normalized_text`,`status`),
  KEY `idx_learning_unit_sense_feedback_user_created` (`reported_by_user_id`,`created_at`),
  CONSTRAINT `fk_learning_unit_sense_feedback_unit` FOREIGN KEY (`learning_unit_id`) REFERENCES `learning_units` (`id`),
  CONSTRAINT `fk_learning_unit_sense_feedback_user` FOREIGN KEY (`reported_by_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_learning_unit_sense_feedback_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'REVIEWED',_utf8mb4'RESOLVED',_utf8mb4'REJECTED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_unit_sense_scenario_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_unit_sense_scenario_tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `learning_unit_sense_id` bigint NOT NULL,
  `scenario_code` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `relevance_score` decimal(5,4) NOT NULL DEFAULT '1.0000',
  `source` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MANUAL',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_unit_sense_scenario_tags` (`learning_unit_sense_id`,`scenario_code`),
  KEY `idx_learning_unit_sense_scenario_tags_scenario` (`scenario_code`,`relevance_score`),
  CONSTRAINT `fk_learning_unit_sense_scenario_tags_sense` FOREIGN KEY (`learning_unit_sense_id`) REFERENCES `learning_unit_senses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_learning_unit_sense_scenario_tags_score` CHECK ((`relevance_score` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_unit_senses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_unit_senses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `learning_unit_id` bigint NOT NULL,
  `sense_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `part_of_speech` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `definition_en` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `definition_zh` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `difficulty_level` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `difficulty_confidence` decimal(5,4) DEFAULT NULL,
  `frequency_score` decimal(10,4) DEFAULT NULL,
  `frequency_band` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_unit_senses_key` (`learning_unit_id`,`sense_key`),
  UNIQUE KEY `uk_learning_unit_senses_unit_id` (`learning_unit_id`,`id`),
  KEY `idx_learning_unit_senses_unit_status` (`learning_unit_id`,`status`),
  KEY `idx_learning_unit_senses_level` (`difficulty_level`),
  KEY `idx_learning_unit_senses_pos` (`part_of_speech`),
  CONSTRAINT `fk_learning_unit_senses_unit` FOREIGN KEY (`learning_unit_id`) REFERENCES `learning_units` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_learning_unit_senses_confidence` CHECK (((`difficulty_confidence` is null) or (`difficulty_confidence` between 0 and 1))),
  CONSTRAINT `ck_learning_unit_senses_frequency_band` CHECK (((`frequency_band` is null) or (`frequency_band` in (_utf8mb4'VERY_COMMON',_utf8mb4'COMMON',_utf8mb4'MEDIUM',_utf8mb4'UNCOMMON',_utf8mb4'RARE')))),
  CONSTRAINT `ck_learning_unit_senses_level` CHECK (((`difficulty_level` is null) or (`difficulty_level` in (_utf8mb4'A1',_utf8mb4'A2',_utf8mb4'B1',_utf8mb4'B2',_utf8mb4'C1',_utf8mb4'C2')))),
  CONSTRAINT `ck_learning_unit_senses_pos` CHECK (((`part_of_speech` is null) or (`part_of_speech` in (_utf8mb4'NOUN',_utf8mb4'VERB',_utf8mb4'ADJECTIVE',_utf8mb4'ADVERB',_utf8mb4'PRONOUN',_utf8mb4'DETERMINER',_utf8mb4'PREPOSITION',_utf8mb4'CONJUNCTION',_utf8mb4'INTERJECTION',_utf8mb4'NUMERAL',_utf8mb4'AUXILIARY',_utf8mb4'PARTICLE',_utf8mb4'OTHER')))),
  CONSTRAINT `ck_learning_unit_senses_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'OFFLINE',_utf8mb4'REVIEW')))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_units`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_units` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `unit_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `canonical_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `language_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'en',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_units_identity` (`language_code`,`unit_type`,`normalized_text`),
  KEY `idx_learning_units_type_status` (`unit_type`,`status`),
  KEY `idx_learning_units_normalized_text` (`normalized_text`),
  CONSTRAINT `ck_learning_units_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'OFFLINE',_utf8mb4'REVIEW'))),
  CONSTRAINT `ck_learning_units_type` CHECK ((`unit_type` in (_utf8mb4'WORD',_utf8mb4'PHRASE',_utf8mb4'SENTENCE_PATTERN')))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `placement_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `placement_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cefr_level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `difficulty_score` int NOT NULL DEFAULT '50',
  `scenario_tag` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_skill` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ability_dimension` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'contextual_understanding',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `grading_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL_EXACT',
  `content` json NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_placement_items_status_level` (`status`,`cefr_level`),
  KEY `idx_placement_items_type` (`item_type`),
  CONSTRAINT `ck_placement_items_difficulty` CHECK ((`difficulty_score` between 1 and 100)),
  CONSTRAINT `ck_placement_items_grading_type` CHECK ((`grading_type` in (_utf8mb4'LOCAL_EXACT',_utf8mb4'LOCAL_ACCEPTED_ANSWERS',_utf8mb4'AI_JUDGE'))),
  CONSTRAINT `ck_placement_items_level` CHECK ((`cefr_level` in (_utf8mb4'A1',_utf8mb4'A2',_utf8mb4'B1',_utf8mb4'B2',_utf8mb4'C1'))),
  CONSTRAINT `ck_placement_items_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'GENERATING_TEXT',_utf8mb4'VALIDATING_TEXT',_utf8mb4'GENERATING_AUDIO',_utf8mb4'READY',_utf8mb4'FAILED',_utf8mb4'EXPIRED'))),
  CONSTRAINT `ck_placement_items_type` CHECK ((`item_type` in (_utf8mb4'SCENE_DIALOGUE_CHOICE',_utf8mb4'CONTEXT_MEANING',_utf8mb4'POLITENESS_JUDGMENT',_utf8mb4'LISTENING_COMPREHENSION',_utf8mb4'EXPRESSION_COMPLETION',_utf8mb4'INTENT_UNDERSTANDING',_utf8mb4'TRUE_FALSE',_utf8mb4'SYNONYM_CHOICE',_utf8mb4'ANTONYM_CHOICE',_utf8mb4'CLOZE_TEXT')))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `placement_session_answers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `placement_session_answers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `item_order` int NOT NULL,
  `grading_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL_EXACT',
  `difficulty_score` int NOT NULL DEFAULT '50',
  `selected_option_index` int DEFAULT NULL,
  `raw_answer` text COLLATE utf8mb4_unicode_ci,
  `is_correct` tinyint(1) DEFAULT NULL,
  `judge_payload` json DEFAULT NULL,
  `answered_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_placement_session_answers_session_item` (`session_id`,`item_id`),
  KEY `fk_placement_session_answers_item` (`item_id`),
  KEY `idx_placement_session_answers_session` (`session_id`,`item_order`),
  CONSTRAINT `fk_placement_session_answers_item` FOREIGN KEY (`item_id`) REFERENCES `placement_items` (`id`),
  CONSTRAINT `fk_placement_session_answers_session` FOREIGN KEY (`session_id`) REFERENCES `placement_sessions` (`id`),
  CONSTRAINT `ck_placement_session_answers_difficulty` CHECK ((`difficulty_score` between 1 and 100)),
  CONSTRAINT `ck_placement_session_answers_grading_type` CHECK ((`grading_type` in (_utf8mb4'LOCAL_EXACT',_utf8mb4'LOCAL_ACCEPTED_ANSWERS',_utf8mb4'AI_JUDGE')))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `placement_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `placement_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BATCH',
  `item_count` int NOT NULL,
  `answered_count` int NOT NULL DEFAULT '0',
  `max_item_count` int NOT NULL DEFAULT '10',
  `current_difficulty_score` int NOT NULL DEFAULT '50',
  `correct_count` int DEFAULT NULL,
  `score_percent` decimal(5,2) DEFAULT NULL,
  `estimated_level` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `submitted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_placement_sessions_user_status` (`user_id`,`status`),
  CONSTRAINT `fk_placement_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_placement_sessions_current_difficulty` CHECK ((`current_difficulty_score` between 1 and 100)),
  CONSTRAINT `ck_placement_sessions_level` CHECK (((`estimated_level` is null) or (`estimated_level` in (_utf8mb4'A1',_utf8mb4'A2',_utf8mb4'B1',_utf8mb4'B2',_utf8mb4'C1')))),
  CONSTRAINT `ck_placement_sessions_mode` CHECK ((`mode` in (_utf8mb4'BATCH',_utf8mb4'ADAPTIVE'))),
  CONSTRAINT `ck_placement_sessions_status` CHECK ((`status` in (_utf8mb4'STARTED',_utf8mb4'SUBMITTED',_utf8mb4'EXPIRED')))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `scenario_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scenario_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `difficulty_score` int NOT NULL,
  `target_abilities` json NOT NULL,
  `applicable_levels` json NOT NULL,
  `risk_tags` json NOT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scenario_templates_code` (`code`),
  KEY `idx_scenario_templates_status_difficulty` (`status`,`difficulty_score`),
  CONSTRAINT `ck_scenario_templates_difficulty` CHECK ((`difficulty_score` between 1 and 100)),
  CONSTRAINT `ck_scenario_templates_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'OFFLINE')))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_learning_unit_sense_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_learning_unit_sense_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `learning_unit_sense_id` bigint NOT NULL,
  `exposure_count` int NOT NULL DEFAULT '0',
  `attempt_count` int NOT NULL DEFAULT '0',
  `correct_count` int NOT NULL DEFAULT '0',
  `incorrect_count` int NOT NULL DEFAULT '0',
  `correction_count` int NOT NULL DEFAULT '0',
  `recommendation_count` int NOT NULL DEFAULT '0',
  `mastery_score` decimal(5,4) NOT NULL DEFAULT '0.0000',
  `mastery_level` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNSEEN',
  `stability_score` decimal(8,4) NOT NULL DEFAULT '0.3000',
  `difficulty_score` decimal(8,4) NOT NULL DEFAULT '0.5000',
  `first_seen_at` datetime(6) DEFAULT NULL,
  `last_seen_at` datetime(6) DEFAULT NULL,
  `last_attempt_at` datetime(6) DEFAULT NULL,
  `last_reviewed_at` datetime(6) DEFAULT NULL,
  `next_review_at` datetime(6) DEFAULT NULL,
  `review_interval_hours` int NOT NULL DEFAULT '24',
  `review_priority_score` decimal(8,4) NOT NULL DEFAULT '0.0000',
  `last_priority_calculated_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_learning_unit_sense_stats` (`user_id`,`learning_unit_sense_id`),
  KEY `idx_user_learning_unit_sense_stats_sense` (`learning_unit_sense_id`),
  KEY `idx_user_learning_unit_sense_stats_priority` (`user_id`,`review_priority_score`),
  KEY `idx_user_learning_unit_sense_stats_next_priority` (`user_id`,`next_review_at`,`review_priority_score`),
  KEY `idx_user_learning_unit_sense_stats_refresh` (`last_priority_calculated_at`,`review_priority_score`),
  KEY `idx_user_learning_unit_sense_stats_user_mastery` (`user_id`,`mastery_level`),
  KEY `idx_user_learning_unit_sense_stats_user_review` (`user_id`,`next_review_at`),
  CONSTRAINT `fk_user_learning_unit_sense_stats_sense` FOREIGN KEY (`learning_unit_sense_id`) REFERENCES `learning_unit_senses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_learning_unit_sense_stats_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_user_learning_unit_sense_stats_counts` CHECK (((`exposure_count` >= 0) and (`attempt_count` >= 0) and (`correct_count` >= 0) and (`incorrect_count` >= 0) and (`correction_count` >= 0) and (`recommendation_count` >= 0))),
  CONSTRAINT `ck_user_learning_unit_sense_stats_mastery_level` CHECK ((`mastery_level` in (_utf8mb4'UNSEEN',_utf8mb4'EXPOSED',_utf8mb4'LEARNING',_utf8mb4'FAMILIAR',_utf8mb4'MASTERED'))),
  CONSTRAINT `ck_user_learning_unit_sense_stats_mastery_score` CHECK ((`mastery_score` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_level_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_level_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `cefr_level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dimension_scores` json NOT NULL,
  `weak_scenarios` json NOT NULL,
  `weak_abilities` json NOT NULL,
  `last_placement_session_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_level_profiles_user` (`user_id`),
  KEY `fk_user_level_profiles_session` (`last_placement_session_id`),
  CONSTRAINT `fk_user_level_profiles_session` FOREIGN KEY (`last_placement_session_id`) REFERENCES `placement_sessions` (`id`),
  CONSTRAINT `fk_user_level_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `ck_user_level_profiles_level` CHECK ((`cefr_level` in (_utf8mb4'A1',_utf8mb4'A2',_utf8mb4'B1',_utf8mb4'B2',_utf8mb4'C1')))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  CONSTRAINT `ck_users_role` CHECK ((`role` in (_utf8mb4'LEARNER',_utf8mb4'ADMIN'))),
  CONSTRAINT `ck_users_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'DISABLED')))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
