package com.contextflow.learning.service;

import com.contextflow.auth.domain.UserRole;
import com.contextflow.learning.domain.LearningEventSourceType;
import com.contextflow.learning.domain.LearningEventType;
import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageGenerationSource;
import com.contextflow.learning.domain.LearningPackageStatus;
import com.contextflow.learning.dto.LearningDialogueRequest;
import com.contextflow.learning.repository.LearningEventRepository;
import com.contextflow.learning.repository.LearningPackageRepository;
import com.contextflow.scenario.domain.ScenarioTemplateEntity;
import com.contextflow.scenario.domain.ScenarioTemplateStatus;
import com.contextflow.scenario.repository.ScenarioTemplateRepository;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserStatus;
import com.contextflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LearningDialogueEventIntegrationTest {

    @Autowired
    private LearningDialogueService learningDialogueService;

    @Autowired
    private LearningEventRepository learningEventRepository;

    @Autowired
    private LearningPackageRepository learningPackageRepository;

    @Autowired
    private ScenarioTemplateRepository scenarioTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void dialogueShouldPersistLearningEventsForLanguageUnitOccurrences() {
        UserEntity learner = userRepository.findByUsername("learner")
                .orElseGet(() -> userRepository.save(new UserEntity(
                        "learner",
                        "test-password-hash",
                        "Demo Learner",
                        UserRole.LEARNER,
                        UserStatus.ACTIVE
                )));
        ScenarioTemplateEntity scenarioTemplate = scenarioTemplateRepository
                .findByStatusOrderByDifficultyScoreAsc(ScenarioTemplateStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseGet(() -> scenarioTemplateRepository.save(new ScenarioTemplateEntity(
                        "bank_account_acceptance",
                        "Bank account acceptance",
                        "Acceptance scenario for dialogue learning events.",
                        30,
                        "[\"polite_request\"]",
                        "[\"A1\",\"A2\"]",
                        "[]",
                        ScenarioTemplateStatus.ACTIVE
                )));
        LearningPackageEntity learningPackage = learningPackageRepository.save(new LearningPackageEntity(
                learner.getId(),
                scenarioTemplate.getId(),
                LearningPackageStatus.READY,
                LearningPackageGenerationSource.SEEDED_TEMPLATE,
                "Acceptance bank package",
                """
                        {
                          "scenario": {
                            "code": "bank_account"
                          }
                        }
                        """,
                Instant.now(),
                null
        ));

        var response = learningDialogueService.reply(
                "learner",
                learningPackage.getId(),
                new LearningDialogueRequest("Could you help me open a bank account?")
        );

        var events = learningEventRepository.findBySourceTypeAndSourceIdOrderByIdAsc(
                LearningEventSourceType.LEARNING_DIALOGUE_TURN,
                response.turnId()
        );

        assertThat(events).hasSize(2);
        assertThat(events)
                .extracting("eventType")
                .containsExactlyInAnyOrder(LearningEventType.UNIT_ATTEMPTED, LearningEventType.UNIT_RECOMMENDED);
        assertThat(events)
                .extracting("occurrenceText")
                .containsOnly("bank");
        assertThat(events)
                .extracting("learningUnitSenseId")
                .containsOnlyNulls();
        assertThat(events)
                .allSatisfy(event -> assertThat(event.getPayload()).contains("\"scenarioCode\":\"bank_account\""));
    }
}
