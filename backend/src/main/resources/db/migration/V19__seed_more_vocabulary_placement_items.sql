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
WITH source_words AS (
    SELECT
        id,
        word,
        effective_frequency_rank AS frequency_rank,
        LEFT(TRIM(SUBSTRING_INDEX(translation_raw, '\n', 1)), 120) AS translation,
        CASE
            WHEN effective_frequency_rank <= 1000 THEN 'TOP_1000'
            WHEN effective_frequency_rank <= 2000 THEN 'TOP_2000'
            WHEN effective_frequency_rank <= 3000 THEN 'TOP_3000'
            WHEN effective_frequency_rank <= 5000 THEN 'TOP_5000'
            WHEN effective_frequency_rank <= 8000 THEN 'TOP_8000'
            WHEN effective_frequency_rank <= 12000 THEN 'TOP_12000'
            WHEN effective_frequency_rank <= 16000 THEN 'TOP_16000'
            ELSE 'TOP_20000'
        END AS frequency_band,
        CASE
            WHEN effective_frequency_rank <= 1000 THEN 'A1'
            WHEN effective_frequency_rank <= 2000 THEN 'A2'
            WHEN effective_frequency_rank <= 3000 THEN 'B1'
            WHEN effective_frequency_rank <= 5000 THEN 'B1'
            WHEN effective_frequency_rank <= 8000 THEN 'B2'
            WHEN effective_frequency_rank <= 12000 THEN 'C1'
            WHEN effective_frequency_rank <= 16000 THEN 'C1'
            ELSE 'C2'
        END AS cefr_level,
        CASE
            WHEN effective_frequency_rank <= 1000 THEN 18
            WHEN effective_frequency_rank <= 2000 THEN 35
            WHEN effective_frequency_rank <= 3000 THEN 48
            WHEN effective_frequency_rank <= 5000 THEN 62
            WHEN effective_frequency_rank <= 8000 THEN 76
            WHEN effective_frequency_rank <= 12000 THEN 86
            WHEN effective_frequency_rank <= 16000 THEN 93
            ELSE 98
        END AS difficulty_score
    FROM ecdict_clean_word_entries
    WHERE effective_frequency_rank IS NOT NULL
      AND effective_frequency_rank BETWEEN 1 AND 20000
      AND word REGEXP '^[A-Za-z][A-Za-z -]*$'
      AND translation_raw IS NOT NULL
      AND TRIM(translation_raw) <> ''
),
ranked_words AS (
    SELECT
        source_words.*,
        ROW_NUMBER() OVER (PARTITION BY frequency_band ORDER BY frequency_rank, id) AS band_row
    FROM source_words
),
selected_words AS (
    SELECT *
    FROM ranked_words
    WHERE band_row <= 100
),
distractors AS (
    SELECT
        selected_words.*,
        (
            SELECT LEFT(TRIM(SUBSTRING_INDEX(other.translation_raw, '\n', 1)), 120)
            FROM ecdict_clean_word_entries other
            WHERE other.id <> selected_words.id
              AND other.effective_frequency_rank IS NOT NULL
              AND other.translation_raw IS NOT NULL
              AND TRIM(other.translation_raw) <> ''
            ORDER BY ABS(other.effective_frequency_rank - selected_words.frequency_rank), other.id
            LIMIT 1 OFFSET 0
        ) AS distractor_1,
        (
            SELECT LEFT(TRIM(SUBSTRING_INDEX(other.translation_raw, '\n', 1)), 120)
            FROM ecdict_clean_word_entries other
            WHERE other.id <> selected_words.id
              AND other.effective_frequency_rank IS NOT NULL
              AND other.translation_raw IS NOT NULL
              AND TRIM(other.translation_raw) <> ''
            ORDER BY ABS(other.effective_frequency_rank - selected_words.frequency_rank), other.id
            LIMIT 1 OFFSET 1
        ) AS distractor_2,
        (
            SELECT LEFT(TRIM(SUBSTRING_INDEX(other.translation_raw, '\n', 1)), 120)
            FROM ecdict_clean_word_entries other
            WHERE other.id <> selected_words.id
              AND other.effective_frequency_rank IS NOT NULL
              AND other.translation_raw IS NOT NULL
              AND TRIM(other.translation_raw) <> ''
            ORDER BY ABS(other.effective_frequency_rank - selected_words.frequency_rank), other.id
            LIMIT 1 OFFSET 2
        ) AS distractor_3
    FROM selected_words
)
SELECT
    'ZH_MEANING_CHOICE',
    cefr_level,
    difficulty_score,
    frequency_rank,
    frequency_band,
    1.100,
    0.250,
    'vocabulary_size',
    'vocabulary_size',
    'vocabulary_size',
    'READY',
    'LOCAL_EXACT',
    JSON_OBJECT(
        'question', CONCAT('What is the best Chinese meaning of "', word, '"?'),
        'options', JSON_ARRAY(translation, distractor_1, distractor_2, distractor_3),
        'answerIndex', 0,
        'explanation', CONCAT('Frequency-rank vocabulary calibration item from ECDICT clean data. Rank: ', frequency_rank)
    )
FROM distractors
WHERE distractor_1 IS NOT NULL
  AND distractor_2 IS NOT NULL
  AND distractor_3 IS NOT NULL
  AND translation NOT IN (distractor_1, distractor_2, distractor_3);
