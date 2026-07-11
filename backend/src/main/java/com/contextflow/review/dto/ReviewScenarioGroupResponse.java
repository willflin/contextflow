package com.contextflow.review.dto;

import java.util.List;

public record ReviewScenarioGroupResponse(
        String scenarioCode,
        List<Long> targetSenseIds,
        List<ReviewPlanItemResponse> targets
) {
}
