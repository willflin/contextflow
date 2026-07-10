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
        if (!seedDemoItems || placementItemRepository.count() > 0) {
            return;
        }

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
}
