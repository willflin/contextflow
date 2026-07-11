package com.contextflow.review.dto;

import java.time.Instant;
import java.util.List;

public record ReviewPlanResponse(
        String userLevel,
        int limit,
        int reviewTargetCount,
        int newTargetCount,
        int overdueReviewCount,
        String priorityBasis,
        List<ReviewPlanItemResponse> items,
        List<ReviewScenarioGroupResponse> scenarioGroups,
        Instant generatedAt
) {
}
