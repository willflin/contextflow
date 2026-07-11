package com.contextflow.content.repository;

import com.contextflow.content.domain.UserLearningUnitSenseStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserLearningUnitSenseStatsRepository extends JpaRepository<UserLearningUnitSenseStatsEntity, Long> {

    @Query("""
            select stats
            from UserLearningUnitSenseStatsEntity stats
            join fetch stats.learningUnitSense sense
            where stats.userId = :userId
              and sense.id = :learningUnitSenseId
            """)
    Optional<UserLearningUnitSenseStatsEntity> findByUserIdAndLearningUnitSenseId(
            @Param("userId") Long userId,
            @Param("learningUnitSenseId") Long learningUnitSenseId
    );
}
