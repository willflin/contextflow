package com.contextflow.scenario.seed;

import com.contextflow.scenario.domain.ScenarioTemplateEntity;
import com.contextflow.scenario.domain.ScenarioTemplateStatus;
import com.contextflow.scenario.repository.ScenarioTemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ScenarioTemplateSeeder implements ApplicationRunner {

    private final boolean seedDemoTemplates;
    private final ScenarioTemplateRepository scenarioTemplateRepository;

    public ScenarioTemplateSeeder(
            @Value("${contextflow.scenario.seed-demo-templates:true}") boolean seedDemoTemplates,
            ScenarioTemplateRepository scenarioTemplateRepository
    ) {
        this.seedDemoTemplates = seedDemoTemplates;
        this.scenarioTemplateRepository = scenarioTemplateRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDemoTemplates || scenarioTemplateRepository.count() > 0) {
            return;
        }

        scenarioTemplateRepository.saveAll(List.of(
                new ScenarioTemplateEntity(
                        "hotel_check_in",
                        "Hotel check-in",
                        "Handle a front-desk check-in conversation, ID request, room details, and basic service questions.",
                        35,
                        "[\"contextual_understanding\", \"polite_request\"]",
                        "[\"A1\", \"A2\", \"B1\"]",
                        "[]",
                        ScenarioTemplateStatus.ACTIVE
                ),
                new ScenarioTemplateEntity(
                        "shopping_return",
                        "Shopping and returns",
                        "Ask about price, size, discounts, and returning or exchanging an item.",
                        50,
                        "[\"vocabulary_meaning\", \"natural_expression\"]",
                        "[\"A2\", \"B1\", \"B2\"]",
                        "[]",
                        ScenarioTemplateStatus.ACTIVE
                ),
                new ScenarioTemplateEntity(
                        "bank_account",
                        "Opening a bank account",
                        "Explain your need, ask about documents, and respond to account-service questions.",
                        62,
                        "[\"expression_naturalness\", \"intent_understanding\"]",
                        "[\"B1\", \"B2\", \"C1\"]",
                        "[]",
                        ScenarioTemplateStatus.ACTIVE
                ),
                new ScenarioTemplateEntity(
                        "police_stop",
                        "Stopped by police",
                        "Understand a serious street interaction, answer clearly, and ask for clarification politely.",
                        75,
                        "[\"pragmatic_understanding\", \"clarification_request\"]",
                        "[\"B1\", \"B2\", \"C1\"]",
                        "[\"sensitive_scenario\"]",
                        ScenarioTemplateStatus.ACTIVE
                )
        ));
    }
}
