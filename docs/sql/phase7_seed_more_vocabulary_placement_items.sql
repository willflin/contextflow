CREATE TEMPORARY TABLE tmp_vocabulary_placement_seed (
    band_row INT NOT NULL,
    word VARCHAR(255) NOT NULL,
    frequency_rank INT NOT NULL,
    frequency_band VARCHAR(32) NOT NULL,
    cefr_level VARCHAR(16) NOT NULL,
    difficulty_score INT NOT NULL,
    translation VARCHAR(120) NOT NULL,
    PRIMARY KEY (frequency_band, band_row)
) ENGINE=MEMORY;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_1000',
       'A1',
       18,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 1 AND 1000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_2000',
       'A2',
       35,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 1001 AND 2000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_3000',
       'B1',
       48,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 2001 AND 3000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_5000',
       'B1',
       62,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 3001 AND 5000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_8000',
       'B2',
       76,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 5001 AND 8000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_12000',
       'C1',
       86,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 8001 AND 12000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_16000',
       'C1',
       93,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 12001 AND 16000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

INSERT INTO tmp_vocabulary_placement_seed
SELECT ROW_NUMBER() OVER (ORDER BY candidate.frequency_rank, candidate.id),
       candidate.word,
       candidate.frequency_rank,
       'TOP_20000',
       'C2',
       98,
       candidate.translation
FROM (
    SELECT id, word, effective_frequency_rank AS frequency_rank, LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank BETWEEN 16001 AND 20000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
    ORDER BY effective_frequency_rank, id
    LIMIT 100
) candidate;

CREATE TEMPORARY TABLE tmp_vocabulary_placement_target ENGINE=MEMORY
AS SELECT * FROM tmp_vocabulary_placement_seed;

CREATE TEMPORARY TABLE tmp_vocabulary_placement_d1 ENGINE=MEMORY
AS SELECT * FROM tmp_vocabulary_placement_seed;

CREATE TEMPORARY TABLE tmp_vocabulary_placement_d2 ENGINE=MEMORY
AS SELECT * FROM tmp_vocabulary_placement_seed;

CREATE TEMPORARY TABLE tmp_vocabulary_placement_d3 ENGINE=MEMORY
AS SELECT * FROM tmp_vocabulary_placement_seed;

ALTER TABLE tmp_vocabulary_placement_target ADD PRIMARY KEY (frequency_band, band_row);
ALTER TABLE tmp_vocabulary_placement_d1 ADD PRIMARY KEY (frequency_band, band_row);
ALTER TABLE tmp_vocabulary_placement_d2 ADD PRIMARY KEY (frequency_band, band_row);
ALTER TABLE tmp_vocabulary_placement_d3 ADD PRIMARY KEY (frequency_band, band_row);

INSERT INTO placement_items (
    item_type,
    cefr_level,
    difficulty_score,
    frequency_rank,
    frequency_band,
    item_discrimination,
    guessing_factor,
    scenario_tag,
    target_skill,
    ability_dimension,
    status,
    grading_type,
    content
)
SELECT
    'ZH_MEANING_CHOICE',
    target.cefr_level,
    target.difficulty_score,
    target.frequency_rank,
    target.frequency_band,
    1.100,
    0.250,
    'vocabulary_size',
    'vocabulary_size',
    'vocabulary_size',
    'READY',
    'LOCAL_EXACT',
    JSON_OBJECT(
        'question', CONCAT('What is the best Chinese meaning of "', target.word, '"?'),
        'options', JSON_ARRAY(target.translation, d1.translation, d2.translation, d3.translation),
        'answerIndex', 0,
        'explanation', CONCAT('Frequency-rank vocabulary calibration item from ECDICT clean data. Rank: ', target.frequency_rank)
    )
FROM tmp_vocabulary_placement_target target
JOIN (
    SELECT frequency_band, COUNT(*) AS band_count
    FROM tmp_vocabulary_placement_seed
    GROUP BY frequency_band
) counts
    ON counts.frequency_band = target.frequency_band
JOIN tmp_vocabulary_placement_d1 d1
    ON d1.frequency_band = target.frequency_band
   AND d1.band_row = MOD(target.band_row, counts.band_count) + 1
JOIN tmp_vocabulary_placement_d2 d2
    ON d2.frequency_band = target.frequency_band
   AND d2.band_row = MOD(target.band_row + 1, counts.band_count) + 1
JOIN tmp_vocabulary_placement_d3 d3
    ON d3.frequency_band = target.frequency_band
   AND d3.band_row = MOD(target.band_row + 2, counts.band_count) + 1
WHERE counts.band_count >= 4
  AND target.translation NOT IN (d1.translation, d2.translation, d3.translation)
  AND d1.translation <> d2.translation
  AND d1.translation <> d3.translation
  AND d2.translation <> d3.translation;

DROP TEMPORARY TABLE tmp_vocabulary_placement_d3;
DROP TEMPORARY TABLE tmp_vocabulary_placement_d2;
DROP TEMPORARY TABLE tmp_vocabulary_placement_d1;
DROP TEMPORARY TABLE tmp_vocabulary_placement_target;
DROP TEMPORARY TABLE tmp_vocabulary_placement_seed;
