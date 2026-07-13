ALTER TABLE placement_items
    DROP CHECK ck_placement_items_level;

ALTER TABLE placement_items
    ADD CONSTRAINT ck_placement_items_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2'));

ALTER TABLE placement_sessions
    DROP CHECK ck_placement_sessions_level;

ALTER TABLE placement_sessions
    ADD CONSTRAINT ck_placement_sessions_level CHECK (
        estimated_level IS NULL OR estimated_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')
    );

ALTER TABLE user_level_profiles
    DROP CHECK ck_user_level_profiles_level;

ALTER TABLE user_level_profiles
    ADD CONSTRAINT ck_user_level_profiles_level CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2'));

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
) VALUES
('CONTEXT_MEANING', 'B2', 78, 7800, 'TOP_8000', 1.150, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'In this sentence, what does mitigate mean? The new rule may mitigate the risk.',
   'options', JSON_ARRAY('reduce', 'create', 'hide completely', 'measure exactly'),
   'answerIndex', 0,
   'explanation', 'mitigate means to make something less severe.'
 )),
('SYNONYM_CHOICE', 'B2', 82, 8400, 'TOP_12000', 1.200, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Choose the closest meaning of coherent.',
   'options', JSON_ARRAY('logical and connected', 'angry and loud', 'newly bought', 'hard to pronounce'),
   'answerIndex', 0,
   'explanation', 'coherent means clear, logical, and connected.'
 )),
('ANTONYM_CHOICE', 'C1', 86, 9600, 'TOP_12000', 1.250, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Choose the opposite of scarce.',
   'options', JSON_ARRAY('abundant', 'fragile', 'remote', 'accurate'),
   'answerIndex', 0,
   'explanation', 'scarce means not enough; abundant means plentiful.'
 )),
('BEST_EXPRESSION_CHOICE', 'C1', 88, 10200, 'TOP_12000', 1.250, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Which sentence is the most natural formal English?',
   'options', JSON_ARRAY('The evidence is insufficient to support that conclusion.', 'The evidence is not enough support that conclusion.', 'The proof is small for conclusion.', 'The evidence cannot enough the conclusion.'),
   'answerIndex', 0,
   'explanation', 'insufficient to support is formal and natural.'
 )),
('CONTEXT_MEANING', 'C1', 91, 13200, 'TOP_16000', 1.300, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'In this sentence, what does ambiguous mean? The instruction was ambiguous, so people understood it differently.',
   'options', JSON_ARRAY('unclear or having more than one meaning', 'very short', 'legally required', 'emotionally painful'),
   'answerIndex', 0,
   'explanation', 'ambiguous means unclear or open to more than one interpretation.'
 )),
('SYNONYM_CHOICE', 'C1', 93, 14600, 'TOP_16000', 1.350, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Choose the closest meaning of scrutiny.',
   'options', JSON_ARRAY('careful examination', 'quick agreement', 'public celebration', 'minor delay'),
   'answerIndex', 0,
   'explanation', 'scrutiny means careful and detailed examination.'
 )),
('ANTONYM_CHOICE', 'C1', 95, 15800, 'TOP_16000', 1.350, 0.250, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Choose the opposite of plausible.',
   'options', JSON_ARRAY('implausible', 'flexible', 'visible', 'ordinary'),
   'answerIndex', 0,
   'explanation', 'plausible means seeming reasonable; implausible means not seeming reasonable.'
 )),
('CONTEXT_MEANING', 'C2', 97, 17200, 'TOP_20000', 1.450, 0.220, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'In this sentence, what does perfunctory mean? His apology sounded perfunctory rather than sincere.',
   'options', JSON_ARRAY('done with little care or interest', 'deeply emotional', 'legally binding', 'surprisingly generous'),
   'answerIndex', 0,
   'explanation', 'perfunctory means done routinely and without real care.'
 )),
('SYNONYM_CHOICE', 'C2', 98, 18400, 'TOP_20000', 1.500, 0.220, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Choose the closest meaning of equivocal.',
   'options', JSON_ARRAY('deliberately unclear', 'physically heavy', 'morally perfect', 'financially cheap'),
   'answerIndex', 0,
   'explanation', 'equivocal means ambiguous or deliberately unclear.'
 )),
('ANTONYM_CHOICE', 'C2', 99, 19200, 'TOP_20000', 1.500, 0.220, 'vocabulary_size', 'vocabulary_size', 'vocabulary_size', 'READY', 'LOCAL_EXACT',
 JSON_OBJECT(
   'question', 'Choose the opposite of meticulous.',
   'options', JSON_ARRAY('careless', 'precise', 'patient', 'neutral'),
   'answerIndex', 0,
   'explanation', 'meticulous means extremely careful; careless is the opposite.'
 ));
