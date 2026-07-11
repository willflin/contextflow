package com.contextflow.content.repository;

import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLearningUnitSenseStatsRepository extends JpaRepository<UserLearningUnitSenseStatsEntity, Long> {

    Optional<UserLearningUnitSenseStatsEntity> findByUserIdAndLearningUnitSenseId(Long userId, Long learningUnitSenseId);
}
