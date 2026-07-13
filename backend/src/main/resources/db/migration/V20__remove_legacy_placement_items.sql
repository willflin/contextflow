CREATE TEMPORARY TABLE tmp_legacy_placement_item_ids (
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MEMORY;

INSERT INTO tmp_legacy_placement_item_ids (id)
SELECT id
FROM placement_items
WHERE JSON_EXTRACT(content, '$.explanation') IS NULL
   OR JSON_UNQUOTE(JSON_EXTRACT(content, '$.explanation')) NOT LIKE 'Frequency-rank vocabulary calibration item from ECDICT clean data.%';

CREATE TEMPORARY TABLE tmp_legacy_placement_session_ids (
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=MEMORY;

INSERT INTO tmp_legacy_placement_session_ids (id)
SELECT DISTINCT placement_session_answers.session_id
FROM placement_session_answers
JOIN tmp_legacy_placement_item_ids
    ON tmp_legacy_placement_item_ids.id = placement_session_answers.item_id;

UPDATE user_level_profiles
SET last_placement_session_id = NULL
WHERE last_placement_session_id IN (
    SELECT id FROM tmp_legacy_placement_session_ids
);

DELETE placement_session_answers
FROM placement_session_answers
JOIN tmp_legacy_placement_session_ids
    ON tmp_legacy_placement_session_ids.id = placement_session_answers.session_id;

DELETE placement_sessions
FROM placement_sessions
JOIN tmp_legacy_placement_session_ids
    ON tmp_legacy_placement_session_ids.id = placement_sessions.id;

DELETE placement_items
FROM placement_items
JOIN tmp_legacy_placement_item_ids
    ON tmp_legacy_placement_item_ids.id = placement_items.id;

DROP TEMPORARY TABLE tmp_legacy_placement_session_ids;
DROP TEMPORARY TABLE tmp_legacy_placement_item_ids;
