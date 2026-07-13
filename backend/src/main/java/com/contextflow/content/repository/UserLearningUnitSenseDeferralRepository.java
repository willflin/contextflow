package com.contextflow.content.repository;

import com.contextflow.content.domain.UserLearningUnitSenseDeferralEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserLearningUnitSenseDeferralRepository extends JpaRepository<UserLearningUnitSenseDeferralEntity, Long> {

    List<UserLearningUnitSenseDeferralEntity> findByUserIdAndDeferredUntilAfter(Long userId, Instant now);

    Optional<UserLearningUnitSenseDeferralEntity> findByUserIdAndLearningUnitSenseId(Long userId, Long learningUnitSenseId);
}
