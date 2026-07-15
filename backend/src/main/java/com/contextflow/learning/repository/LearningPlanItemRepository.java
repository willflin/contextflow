package com.contextflow.learning.repository;

import com.contextflow.learning.domain.LearningPlanItemEntity;
import com.contextflow.learning.domain.LearningPlanItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LearningPlanItemRepository extends JpaRepository<LearningPlanItemEntity, Long> {

    Optional<LearningPlanItemEntity> findByUserIdAndLearningUnitSenseId(Long userId, Long learningUnitSenseId);

    @Query("""
            select item
            from LearningPlanItemEntity item
            join fetch item.learningUnitSense sense
            join fetch sense.learningUnit unit
            where item.userId = :userId
              and item.status = :status
            order by item.id asc
            """)
    List<LearningPlanItemEntity> findByUserIdAndStatusWithSense(
            @Param("userId") Long userId,
            @Param("status") LearningPlanItemStatus status
    );

    @Query("""
            select item.learningUnitSense.id
            from LearningPlanItemEntity item
            where item.userId = :userId
              and item.learningUnitSense.id in :senseIds
              and item.status = :status
            """)
    List<Long> findSenseIdsByUserIdAndSenseIdsAndStatus(
            @Param("userId") Long userId,
            @Param("senseIds") Collection<Long> senseIds,
            @Param("status") LearningPlanItemStatus status
    );
}
