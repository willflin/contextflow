package com.contextflow.review.service;

import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import com.contextflow.content.repository.UserLearningUnitSenseStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewPriorityRefreshService {

    private final UserLearningUnitSenseStatsRepository statsRepository;
    private final ReviewPriorityCalculator reviewPriorityCalculator;

    public ReviewPriorityRefreshService(
            UserLearningUnitSenseStatsRepository statsRepository,
            ReviewPriorityCalculator reviewPriorityCalculator
    ) {
        this.statsRepository = statsRepository;
        this.reviewPriorityCalculator = reviewPriorityCalculator;
    }

    @Transactional
    public int refreshAllPriorities() {
        Instant now = Instant.now();
        List<UserLearningUnitSenseStatsEntity> statsRows = statsRepository.findAllWithSenseAndUnit();
        for (UserLearningUnitSenseStatsEntity stats : statsRows) {
            stats.updateReviewPriorityScore(reviewPriorityCalculator.calculate(stats, now).score(), now);
        }
        statsRepository.saveAll(statsRows);
        return statsRows.size();
    }
}
