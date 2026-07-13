package com.contextflow.ai.agent.dto;

public record AgentLearningPackageContext(
        Long packageId,
        String status,
        String title,
        String scenarioCode,
        String scenarioName,
        String taskGoal,
        String taskInstructionLanguage,
        String expectedLearnerAction,
        String taskRegister,
        String registerGuidance,
        String taskProgress,
        String taskFacts,
        String taskConstraints,
        String roleplayPersona,
        String learnerRole,
        String openingLine,
        String contentJson
) {
}
