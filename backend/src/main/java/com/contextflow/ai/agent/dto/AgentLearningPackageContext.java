package com.contextflow.ai.agent.dto;

public record AgentLearningPackageContext(
        Long packageId,
        String status,
        String title,
        String scenarioCode,
        String scenarioName,
        String contentJson
) {
}
