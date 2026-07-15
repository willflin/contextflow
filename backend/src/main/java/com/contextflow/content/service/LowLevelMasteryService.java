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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
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
    private final JdbcTemplate jdbcTemplate;

    public LowLevelMasteryService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            UserLearningUnitSenseStatsRepository statsRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.statsRepository = statsRepository;
        this.jdbcTemplate = jdbcTemplate;
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
            int effectiveGap = Math.max(gap, MIN_LEVEL_GAP);
            UserLearningUnitSenseStatsEntity stats = statsRepository
                    .findByUserIdAndLearningUnitSenseId(user.getId(), sense.getId())
                    .orElseGet(() -> new UserLearningUnitSenseStatsEntity(user.getId(), sense));
            int reviewIntervalHours = reviewIntervalHours(user.getId(), sense.getId(), effectiveGap);
            stats.markMasteredByUserLevelGap(
                    now,
                    now.plusSeconds(reviewIntervalHours * 3600L),
                    reviewIntervalHours,
                    reviewPriorityScore(effectiveGap)
            );
            statsRepository.save(stats);
            marked++;
        }

        return new LowLevelMasteryResponse(
                marked,
                skipped,
                "已按词义标记为已掌握。"
        );
    }

    @Transactional
    public int markLowLevelUnseenSensesMasteredAfterInitialPlacement(UserEntity user, CefrLevel learnerLevel) {
        List<String> lowLevels = lowLevelDifficultyNames(learnerLevel);
        if (lowLevels.isEmpty()) {
            return 0;
        }

        Instant now = Instant.now();
        String placeholders = String.join(",", lowLevels.stream().map(ignored -> "?").toList());
        List<Object> parameters = new ArrayList<>();
        parameters.add(user.getId());
        parameters.add(Timestamp.from(now));
        parameters.add(Timestamp.from(now));
        parameters.add(Timestamp.from(now));
        parameters.add(user.getId());
        parameters.add(Timestamp.from(now));
        parameters.add(user.getId());
        parameters.add(Timestamp.from(now));
        parameters.add(Timestamp.from(now));
        parameters.add(Timestamp.from(now));
        parameters.add(learnerLevel.ordinal() + 1);
        parameters.addAll(lowLevels);

        return jdbcTemplate.update("""
                INSERT IGNORE INTO user_learning_unit_sense_stats (
                    user_id,
                    learning_unit_sense_id,
                    exposure_count,
                    attempt_count,
                    correct_count,
                    incorrect_count,
                    correction_count,
                    recommendation_count,
                    mastery_score,
                    mastery_level,
                    stability_score,
                    difficulty_score,
                    first_seen_at,
                    last_seen_at,
                    last_attempt_at,
                    last_reviewed_at,
                    next_review_at,
                    review_interval_hours,
                    review_priority_score,
                    last_priority_calculated_at,
                    created_at,
                    updated_at
                )
                SELECT
                    ?,
                    candidate.id,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    1.0000,
                    'MASTERED',
                    1.0000,
                    0.0000,
                    ?,
                    ?,
                    NULL,
                    ?,
                    TIMESTAMPADD(HOUR, (
                        (7 + candidate.level_gap * candidate.level_gap * 14)
                        + MOD(CRC32(CONCAT(?, ':', candidate.id)), (7 + candidate.level_gap * candidate.level_gap * 14) + 1)
                    ) * 24, ?),
                    (
                        (7 + candidate.level_gap * candidate.level_gap * 14)
                        + MOD(CRC32(CONCAT(?, ':', candidate.id)), (7 + candidate.level_gap * candidate.level_gap * 14) + 1)
                    ) * 24,
                    ROUND(100.0 * EXP(-0.9 * candidate.level_gap * candidate.level_gap), 4),
                    ?,
                    ?,
                    ?
                FROM (
                    SELECT
                        sense.id,
                        (? - FIELD(sense.difficulty_level, 'A1', 'A2', 'B1', 'B2', 'C1', 'C2')) AS level_gap
                    FROM learning_unit_senses sense
                    JOIN learning_units unit ON unit.id = sense.learning_unit_id
                    WHERE unit.unit_type = 'WORD'
                      AND unit.status = 'ACTIVE'
                      AND sense.status = 'ACTIVE'
                      AND sense.difficulty_level IN (%s)
                ) candidate
                """.formatted(placeholders), parameters.toArray());
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

    private List<String> lowLevelDifficultyNames(CefrLevel learnerLevel) {
        return List.of(DifficultyLevel.values())
                .stream()
                .filter(level -> learnerLevel.ordinal() - level.ordinal() >= MIN_LEVEL_GAP)
                .map(DifficultyLevel::name)
                .toList();
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }
}
