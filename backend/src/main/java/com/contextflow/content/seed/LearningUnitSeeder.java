package com.contextflow.content.seed;

import com.contextflow.content.domain.DifficultyLevel;
import com.contextflow.content.domain.FrequencyBand;
import com.contextflow.content.domain.LearningDataSourceEntity;
import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitFormEntity;
import com.contextflow.content.domain.LearningUnitFormType;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.domain.PartOfSpeech;
import com.contextflow.content.domain.SenseSourceAttributeType;
import com.contextflow.content.domain.LearningUnitSenseSourceEntity;
import com.contextflow.content.repository.LearningDataSourceRepository;
import com.contextflow.content.repository.LearningUnitFormRepository;
import com.contextflow.content.repository.LearningUnitRepository;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.LearningUnitSenseSourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class LearningUnitSeeder implements ApplicationRunner {

    private static final String LANGUAGE_CODE = "en";
    private static final String SOURCE_NAME = "MANUAL_SEED";

    private final boolean seedDemoUnits;
    private final LearningUnitRepository learningUnitRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final LearningUnitFormRepository learningUnitFormRepository;
    private final LearningDataSourceRepository learningDataSourceRepository;
    private final LearningUnitSenseSourceRepository learningUnitSenseSourceRepository;

    public LearningUnitSeeder(
            @Value("${contextflow.content.seed-demo-units:true}") boolean seedDemoUnits,
            LearningUnitRepository learningUnitRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            LearningUnitFormRepository learningUnitFormRepository,
            LearningDataSourceRepository learningDataSourceRepository,
            LearningUnitSenseSourceRepository learningUnitSenseSourceRepository
    ) {
        this.seedDemoUnits = seedDemoUnits;
        this.learningUnitRepository = learningUnitRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.learningUnitFormRepository = learningUnitFormRepository;
        this.learningDataSourceRepository = learningDataSourceRepository;
        this.learningUnitSenseSourceRepository = learningUnitSenseSourceRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDemoUnits) {
            return;
        }

        LearningDataSourceEntity source = learningDataSourceRepository
                .findBySourceNameAndSourceVersion(SOURCE_NAME, "v1")
                .orElseGet(() -> learningDataSourceRepository.save(new LearningDataSourceEntity(
                        SOURCE_NAME,
                        "v1",
                        null,
                        "INTERNAL_TEST_DATA",
                        "ContextFlow manual seed data for local development."
                )));

        LearningUnitEntity go = unit("go");
        form(go, "go", LearningUnitFormType.LEMMA);
        form(go, "goes", LearningUnitFormType.THIRD_PERSON_SINGULAR);
        form(go, "went", LearningUnitFormType.PAST_TENSE);
        form(go, "gone", LearningUnitFormType.PAST_PARTICIPLE);
        form(go, "going", LearningUnitFormType.PRESENT_PARTICIPLE);
        sense(source, go, "move-travel", PartOfSpeech.VERB, "to move or travel from one place to another",
                "去；移动；前往", DifficultyLevel.A1, FrequencyBand.VERY_COMMON);

        LearningUnitEntity bank = unit("bank");
        form(bank, "bank", LearningUnitFormType.LEMMA);
        form(bank, "banks", LearningUnitFormType.PLURAL);
        sense(source, bank, "financial-institution", PartOfSpeech.NOUN, "an organization that keeps and lends money",
                "银行", DifficultyLevel.A2, FrequencyBand.COMMON);
        sense(source, bank, "river-side", PartOfSpeech.NOUN, "the land along the side of a river",
                "河岸", DifficultyLevel.B1, FrequencyBand.MEDIUM);

        LearningUnitEntity book = unit("book");
        form(book, "book", LearningUnitFormType.LEMMA);
        form(book, "books", LearningUnitFormType.PLURAL);
        sense(source, book, "printed-work", PartOfSpeech.NOUN, "a written or printed work with pages",
                "书；书籍", DifficultyLevel.A1, FrequencyBand.VERY_COMMON);

        LearningUnitEntity good = unit("good");
        form(good, "good", LearningUnitFormType.LEMMA);
        form(good, "better", LearningUnitFormType.COMPARATIVE);
        form(good, "best", LearningUnitFormType.SUPERLATIVE);
        sense(source, good, "positive-quality", PartOfSpeech.ADJECTIVE, "of high quality or pleasant",
                "好的；优质的", DifficultyLevel.A1, FrequencyBand.VERY_COMMON);
    }

    private LearningUnitEntity unit(String canonicalText) {
        String normalized = normalize(canonicalText);
        return learningUnitRepository
                .findByLanguageCodeAndUnitTypeAndNormalizedText(LANGUAGE_CODE, LearningUnitType.WORD, normalized)
                .orElseGet(() -> learningUnitRepository.save(new LearningUnitEntity(
                        LearningUnitType.WORD,
                        canonicalText,
                        normalized,
                        LANGUAGE_CODE,
                        LearningUnitStatus.ACTIVE
                )));
    }

    private void form(LearningUnitEntity unit, String formText, LearningUnitFormType formType) {
        String normalized = normalize(formText);
        learningUnitFormRepository
                .findByLearningUnitIdAndNormalizedFormAndFormType(unit.getId(), normalized, formType)
                .orElseGet(() -> learningUnitFormRepository.save(new LearningUnitFormEntity(
                        unit,
                        formText,
                        normalized,
                        formType
                )));
    }

    private void sense(
            LearningDataSourceEntity source,
            LearningUnitEntity unit,
            String senseKey,
            PartOfSpeech partOfSpeech,
            String definitionEn,
            String definitionZh,
            DifficultyLevel difficultyLevel,
            FrequencyBand frequencyBand
    ) {
        LearningUnitSenseEntity sense = learningUnitSenseRepository
                .findByLearningUnitIdAndSenseKey(unit.getId(), senseKey)
                .orElseGet(() -> learningUnitSenseRepository.save(new LearningUnitSenseEntity(
                        unit,
                        senseKey,
                        partOfSpeech,
                        definitionEn,
                        definitionZh,
                        difficultyLevel,
                        new BigDecimal("1.0000"),
                        null,
                        frequencyBand,
                        LearningUnitStatus.ACTIVE
                )));

        learningUnitSenseSourceRepository
                .findBySenseIdAndDataSourceIdAndAttributeTypeAndSourceRecordId(
                        sense.getId(),
                        source.getId(),
                        SenseSourceAttributeType.SENSE,
                        senseKey
                )
                .orElseGet(() -> learningUnitSenseSourceRepository.save(new LearningUnitSenseSourceEntity(
                        sense,
                        source,
                        SenseSourceAttributeType.SENSE,
                        senseKey
                )));
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("'", " ")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
