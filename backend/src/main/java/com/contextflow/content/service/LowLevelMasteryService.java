package com.contextflow.content.service;

import com.contextflow.content.domain.DifficultyLevel;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import com.contextflow.content.dto.LowLevelMasteryResponse;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.content.repository.UserLearningUnitSenseStatsRepository;
import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserLevelProfileEntity;
import com.contextflow.user.repository.UserLevelProfileRepository;
import com.contextflow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class LowLevelMasteryService {

    private static final int MIN_LEVEL_GAP = 2;
    private static final double PRIORITY_DECAY_FACTOR = 0.9;

    private final UserRepository userRepository;
    private final UserLevelProfileRepository userLevelProfileRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final UserLearningUnitSenseStatsRepository statsRepository;

    public LowLevelMasteryService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            UserLearningUnitSenseStatsRepository statsRepository
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.statsRepository = statsRepository;
    }

    @Transactional
    public LowLevelMasteryResponse markMastered(String username, List<Long> requestedSenseIds) {
        UserEntity user = activeUser(username);
        UserLevelProfileEntity profile = userLevelProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Finish placement testing before marking low-level vocabulary."
                ));
        Set<Long> senseIds = new LinkedHashSet<>(requestedSenseIds == null ? List.of() : requestedSenseIds);
        if (senseIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one sense.");
        }
        if (senseIds.size() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at most 100 senses at once.");
        }

        Instant now = Instant.now();
        int marked = 0;
        int skipped = 0;
        for (LearningUnitSenseEntity sense : learningUnitSenseRepository.findByIdInWithUnit(senseIds.stream().toList())) {
            int gap = learnerLevelGap(profile.getCefrLevel(), sense.getDifficultyLevel());
            if (gap < MIN_LEVEL_GAP) {
                skipped++;
                continue;
            }
            UserLearningUnitSenseStatsEntity stats = statsRepository
                    .findByUserIdAndLearningUnitSenseId(user.getId(), sense.getId())
                    .orElseGet(() -> new UserLearningUnitSenseStatsEntity(user.getId(), sense));
            int reviewIntervalHours = reviewIntervalHours(user.getId(), sense.getId(), gap);
            stats.markMasteredByUserLevelGap(
                    now,
                    now.plusSeconds(reviewIntervalHours * 3600L),
                    reviewIntervalHours,
                    reviewPriorityScore(gap)
            );
            statsRepository.save(stats);
            marked++;
        }

        return new LowLevelMasteryResponse(
                marked,
                skipped,
                "已按词义标记低于当前水平的候选词。"
        );
    }

    public int learnerLevelGap(CefrLevel learnerLevel, DifficultyLevel difficultyLevel) {
        if (difficultyLevel == null) {
            return Integer.MIN_VALUE;
        }
        return learnerLevel.ordinal() - difficultyLevel.ordinal();
    }

    private BigDecimal reviewPriorityScore(int gap) {
        double weight = Math.exp(-PRIORITY_DECAY_FACTOR * gap * gap);
        return BigDecimal.valueOf(100.0 * weight).setScale(4, RoundingMode.HALF_UP);
    }

    private int reviewIntervalHours(Long userId, Long senseId, int gap) {
        int baseDays = 7 + gap * gap * 14;
        int jitterDays = Math.floorMod(stableHash(userId, senseId), baseDays + 1);
        return (baseDays + jitterDays) * 24;
    }

    private int stableHash(Long userId, Long senseId) {
        long value = userId * 1_103_515_245L + senseId * 2_654_435_761L + 0x9E3779B97F4A7C15L;
        value ^= (value >>> 33);
        value *= 0xff51afd7ed558ccdL;
        value ^= (value >>> 33);
        return (int) value;
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }
}
