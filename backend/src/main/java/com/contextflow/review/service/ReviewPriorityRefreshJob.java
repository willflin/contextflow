package com.contextflow.review.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "contextflow.review",
        name = "priority-refresh-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ReviewPriorityRefreshJob {

    private final ReviewPriorityRefreshService refreshService;

    public ReviewPriorityRefreshJob(ReviewPriorityRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @Scheduled(
            initialDelayString = "${contextflow.review.priority-refresh-initial-delay-ms:60000}",
            fixedDelayString = "${contextflow.review.priority-refresh-fixed-delay-ms:3600000}"
    )
    public void refreshPriorities() {
        refreshService.refreshAllPriorities();
    }
}
