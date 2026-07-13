package com.contextflow.placement.seed;

import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemGradingType;
import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.domain.PlacementItemType;
import com.contextflow.placement.repository.PlacementItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PlacementItemSeeder implements ApplicationRunner {

    private final boolean seedDemoItems;
    private final PlacementItemRepository placementItemRepository;

    public PlacementItemSeeder(
            @Value("${contextflow.placement.seed-demo-items:true}") boolean seedDemoItems,
            PlacementItemRepository placementItemRepository
    ) {
        this.seedDemoItems = seedDemoItems;
        this.placementItemRepository = placementItemRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDemoItems) {
            return;
        }

        if (placementItemRepository.count() == 0) {
            placementItemRepository.saveAll(List.of(
                new PlacementItemEntity(
                        PlacementItemType.SCENE_DIALOGUE_CHOICE,
                        CefrLevel.A2,
                        35,
                        "hotel_check_in",
                        "contextual_vocabulary",
                        "contextual_understanding",
                        PlacementItemStatus.READY,
                        PlacementItemGradingType.LOCAL_EXACT,
                        """
                                {
                                  "question": "You are checking in at a hotel. The receptionist says: 'Could I see your ID, please?' What should you do?",
                                  "options": [
                                    "Show your passport or ID card.",
                                    "Ask for the Wi-Fi password immediately.",
                                    "Tell them you do not like the room.",
                                    "Leave the hotel."
                                  ],
                                  "answerIndex": 0,
                                  "explanation": "In this context, 'ID' means identification, such as a passport or ID card."
                                }
                                """
                ),
                new PlacementItemEntity(
                        PlacementItemType.CONTEXT_MEANING,
                        CefrLevel.B1,
                        55,
                        "shopping",
                        "word_meaning_in_context",
                        "vocabulary_meaning",
                        PlacementItemStatus.READY,
                        PlacementItemGradingType.LOCAL_EXACT,
                        """
                                {
                                  "question": "In a store, the clerk says: 'This jacket is on sale.' What does 'on sale' mean here?",
                                  "options": [
                                    "The jacket is cheaper than usual.",
                                    "The jacket is not available.",
                                    "The jacket is only for display.",
                                    "The jacket must be returned today."
                                  ],
                                  "answerIndex": 0,
                                  "explanation": "'On sale' usually means the price has been reduced."
                                }
                                """
                ),
                new PlacementItemEntity(
                        PlacementItemType.POLITENESS_JUDGMENT,
                        CefrLevel.B1,
                        60,
                        "bank_account",
                        "natural_expression",
                        "expression_naturalness",
                        PlacementItemStatus.READY,
                        PlacementItemGradingType.LOCAL_EXACT,
                        """
                                {
                                  "question": "You want to open a bank account. Which sentence sounds the most natural and polite?",
                                  "options": [
                                    "I want account. Open it.",
                                    "Could you help me open a bank account?",
                                    "Give me bank service now.",
                                    "You must make an account for me."
                                  ],
                                  "answerIndex": 1,
                                  "explanation": "'Could you help me...' is polite and natural in a bank service context."
                                }
                                """
                ),
                new PlacementItemEntity(
                        PlacementItemType.INTENT_UNDERSTANDING,
                        CefrLevel.B2,
                        75,
                        "police_stop",
                        "intent_understanding",
                        "pragmatic_understanding",
                        PlacementItemStatus.READY,
                        PlacementItemGradingType.LOCAL_EXACT,
                        """
                                {
                                  "question": "A police officer says: 'Do you know why I stopped you?' What is the officer asking?",
                                  "options": [
                                    "Whether you understand the reason for being stopped.",
                                    "Whether you know the officer personally.",
                                    "Whether you want to stop walking.",
                                    "Whether you can give directions."
                                  ],
                                  "answerIndex": 0,
                                  "explanation": "The officer is asking if you understand the reason for the stop."
                                }
                                """
                )
            ));
        }

        if (placementItemRepository.countByAbilityDimension("vocabulary_size") == 0) {
            placementItemRepository.saveAll(List.of(
                    vocabularyItem(PlacementItemType.ZH_MEANING_CHOICE, CefrLevel.A1, 18, 450, "TOP_1000",
                            "What does 'water' mean?",
                            List.of("水", "火", "空气", "纸"), 0),
                    vocabularyItem(PlacementItemType.ZH_MEANING_CHOICE, CefrLevel.A1, 22, 900, "TOP_1000",
                            "What does 'family' mean?",
                            List.of("家庭", "城市", "工作", "价格"), 0),
                    vocabularyItem(PlacementItemType.CONTEXT_MEANING, CefrLevel.A2, 35, 1450, "TOP_2000",
                            "In 'Please check the address', what does 'address' mean?",
                            List.of("地址", "年龄", "价格", "早餐"), 0),
                    vocabularyItem(PlacementItemType.ZH_MEANING_CHOICE, CefrLevel.A2, 40, 1900, "TOP_2000",
                            "What does 'arrive' mean?",
                            List.of("到达", "忘记", "比较", "借出"), 0),
                    vocabularyItem(PlacementItemType.CONTEXT_MEANING, CefrLevel.B1, 48, 2600, "TOP_3000",
                            "In 'The service is available today', what does 'available' mean?",
                            List.of("可获得的", "昂贵的", "安静的", "危险的"), 0),
                    vocabularyItem(PlacementItemType.SYNONYM_CHOICE, CefrLevel.B1, 55, 3100, "TOP_5000",
                            "Choose the closest meaning of 'request'.",
                            List.of("ask for", "throw away", "pay back", "look after"), 0),
                    vocabularyItem(PlacementItemType.ANTONYM_CHOICE, CefrLevel.B1, 62, 4300, "TOP_5000",
                            "Choose the opposite of 'increase'.",
                            List.of("decrease", "include", "improve", "describe"), 0),
                    vocabularyItem(PlacementItemType.BEST_EXPRESSION_CHOICE, CefrLevel.B2, 68, 5600, "TOP_8000",
                            "Which sentence is most natural?",
                            List.of("Could you confirm the details?", "Can you sure the details?", "Please truth the details.", "Could you detail confirm?"), 0),
                    vocabularyItem(PlacementItemType.CONTEXT_MEANING, CefrLevel.B2, 76, 7200, "TOP_8000",
                            "In 'The policy applies to all customers', what does 'applies to' mean?",
                            List.of("适用于", "申请成为", "涂在上面", "向上移动"), 0),
                    vocabularyItem(PlacementItemType.SYNONYM_CHOICE, CefrLevel.C1, 84, 9200, "TOP_12000",
                            "Choose the closest meaning of 'subtle'.",
                            List.of("not obvious", "very loud", "fully finished", "easy to count"), 0),
                    vocabularyItem(PlacementItemType.ANTONYM_CHOICE, CefrLevel.C1, 90, 10800, "TOP_12000",
                            "Choose the opposite of 'reluctant'.",
                            List.of("willing", "careful", "silent", "recent"), 0),
                    vocabularyItem(PlacementItemType.BEST_EXPRESSION_CHOICE, CefrLevel.C1, 94, 11800, "TOP_12000",
                            "Which expression best fits a formal clarification?",
                            List.of("Could you elaborate on that point?", "Can you big say it?", "Tell more thing.", "Make that point loudly."), 0)
            ));
        }
    }

    private PlacementItemEntity vocabularyItem(
            PlacementItemType itemType,
            CefrLevel cefrLevel,
            int difficultyScore,
            int frequencyRank,
            String frequencyBand,
            String question,
            List<String> options,
            int answerIndex
    ) {
        PlacementItemEntity item = new PlacementItemEntity(
                itemType,
                cefrLevel,
                difficultyScore,
                "vocabulary_size",
                "vocabulary_size",
                "vocabulary_size",
                PlacementItemStatus.READY,
                PlacementItemGradingType.LOCAL_EXACT,
                """
                        {
                          "question": "%s",
                          "options": %s,
                          "answerIndex": %d,
                          "explanation": "Vocabulary-size calibration item."
                        }
                        """.formatted(question, optionsJson(options), answerIndex)
        );
        item.configureVocabularyMeasurement(
                frequencyRank,
                frequencyBand,
                BigDecimal.ONE,
                BigDecimal.valueOf(0.25)
        );
        return item;
    }

    private String optionsJson(List<String> options) {
        return "[\"" + String.join("\",\"", options) + "\"]";
    }
}
