package com.contextflow.learning.service;

import com.contextflow.content.domain.DifficultyLevel;
import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import com.contextflow.content.repository.LearningUnitSenseRepository;
import com.contextflow.learning.domain.LearningPlanItemEntity;
import com.contextflow.learning.domain.LearningPlanItemSource;
import com.contextflow.learning.domain.LearningPlanItemStatus;
import com.contextflow.learning.dto.LearningPlanAddWordResponse;
import com.contextflow.learning.repository.LearningPlanItemRepository;
import com.contextflow.placement.domain.CefrLevel;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserLevelProfileEntity;
import com.contextflow.user.repository.UserLevelProfileRepository;
import com.contextflow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class LearningPlanService {

    private static final int MAX_LEVEL_GAP = 1;

    private final UserRepository userRepository;
    private final UserLevelProfileRepository userLevelProfileRepository;
    private final LearningUnitSenseRepository learningUnitSenseRepository;
    private final LearningPlanItemRepository learningPlanItemRepository;

    public LearningPlanService(
            UserRepository userRepository,
            UserLevelProfileRepository userLevelProfileRepository,
            LearningUnitSenseRepository learningUnitSenseRepository,
            LearningPlanItemRepository learningPlanItemRepository
    ) {
        this.userRepository = userRepository;
        this.userLevelProfileRepository = userLevelProfileRepository;
        this.learningUnitSenseRepository = learningUnitSenseRepository;
        this.learningPlanItemRepository = learningPlanItemRepository;
    }

    @Transactional
    public LearningPlanAddWordResponse addWord(String username, Long learningUnitId) {
        UserEntity user = activeUser(username);
        UserLevelProfileEntity profile = profile(user.getId());
        List<LearningUnitSenseEntity> senses = learningUnitSenseRepository.findActiveWordSensesByLearningUnitId(
                learningUnitId,
                LearningUnitType.WORD,
                LearningUnitStatus.ACTIVE,
                LearningUnitStatus.ACTIVE
        );
        if (senses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active word senses not found.");
        }

        int added = 0;
        int skipped = 0;
        for (LearningUnitSenseEntity sense : senses) {
            LearningPlanItemEntity item = learningPlanItemRepository
                    .findByUserIdAndLearningUnitSenseId(user.getId(), sense.getId())
                    .orElseGet(() -> new LearningPlanItemEntity(
                            user.getId(),
                            sense,
                            LearningPlanItemSource.USER_SELECTED,
                            LearningPlanItemStatus.ACTIVE,
                            null
                    ));
            if (withinLearnerLevel(profile.getCefrLevel(), sense.getDifficultyLevel())) {
                item.activate(LearningPlanItemSource.USER_SELECTED);
                added++;
            } else {
                item.skipOutOfLevel(outOfLevelReason(profile.getCefrLevel(), sense.getDifficultyLevel()));
                skipped++;
            }
            learningPlanItemRepository.save(item);
        }

        return new LearningPlanAddWordResponse(
                learningUnitId,
                added,
                skipped,
                added > 0
                        ? "已加入学习计划。"
                        : "该单词难度与当前用户等级相差过大，已从学习计划剔除。"
        );
    }

    @Transactional
    public List<LearningUnitSenseEntity> selectTargetSenses(UserEntity user, UserLevelProfileEntity profile, int limit) {
        pruneOutOfLevel(user.getId(), profile.getCefrLevel());
        List<LearningUnitSenseEntity> selected = new ArrayList<>();
        for (LearningPlanItemEntity item : learningPlanItemRepository.findByUserIdAndStatusWithSense(
                user.getId(),
                LearningPlanItemStatus.ACTIVE
        )) {
            LearningUnitSenseEntity sense = item.getLearningUnitSense();
            if (withinLearnerLevel(profile.getCefrLevel(), sense.getDifficultyLevel())) {
                selected.add(sense);
                if (selected.size() >= limit) {
                    return selected;
                }
            } else {
                item.skipOutOfLevel(outOfLevelReason(profile.getCefrLevel(), sense.getDifficultyLevel()));
            }
        }

        for (LearningUnitSenseEntity sense : learningUnitSenseRepository.findNewSenseCandidates(
                user.getId(),
                LearningUnitType.WORD,
                LearningUnitStatus.ACTIVE,
                LearningUnitStatus.ACTIVE
        )) {
            if (!withinLearnerLevel(profile.getCefrLevel(), sense.getDifficultyLevel())) {
                continue;
            }
            selected.add(sense);
            learningPlanItemRepository
                    .findByUserIdAndLearningUnitSenseId(user.getId(), sense.getId())
                    .orElseGet(() -> learningPlanItemRepository.save(new LearningPlanItemEntity(
                            user.getId(),
                            sense,
                            LearningPlanItemSource.AUTO_RECOMMENDED,
                            LearningPlanItemStatus.ACTIVE,
                            null
                    )));
            if (selected.size() >= limit) {
                return selected;
            }
        }
        return selected;
    }

    @Transactional(readOnly = true)
    public List<Long> activePlannedSenseIds(Long userId, List<Long> senseIds) {
        if (senseIds.isEmpty()) {
            return List.of();
        }
        return learningPlanItemRepository.findSenseIdsByUserIdAndSenseIdsAndStatus(
                userId,
                senseIds,
                LearningPlanItemStatus.ACTIVE
        );
    }

    private void pruneOutOfLevel(Long userId, CefrLevel cefrLevel) {
        for (LearningPlanItemEntity item : learningPlanItemRepository.findByUserIdAndStatusWithSense(
                userId,
                LearningPlanItemStatus.ACTIVE
        )) {
            LearningUnitSenseEntity sense = item.getLearningUnitSense();
            if (!withinLearnerLevel(cefrLevel, sense.getDifficultyLevel())) {
                item.skipOutOfLevel(outOfLevelReason(cefrLevel, sense.getDifficultyLevel()));
            }
        }
    }

    private boolean withinLearnerLevel(CefrLevel learnerLevel, DifficultyLevel difficultyLevel) {
        if (difficultyLevel == null) {
            return true;
        }
        return Math.abs(learnerLevel.ordinal() - difficultyLevel.ordinal()) <= MAX_LEVEL_GAP;
    }

    private String outOfLevelReason(CefrLevel learnerLevel, DifficultyLevel difficultyLevel) {
        if (difficultyLevel == null) {
            return null;
        }
        return "Learner level " + learnerLevel.name() + " is too far from word difficulty " + difficultyLevel.name() + ".";
    }

    private UserEntity activeUser(String username) {
        return userRepository.findByUsername(username)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private UserLevelProfileEntity profile(Long userId) {
        return userLevelProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Finish placement testing before building a learning plan."
                ));
    }
}
