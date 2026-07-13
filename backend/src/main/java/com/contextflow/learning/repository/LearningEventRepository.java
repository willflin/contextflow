package com.contextflow.learning.repository;

import com.contextflow.learning.domain.LearningEventEntity;
import com.contextflow.learning.domain.LearningEventSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LearningEventRepository extends JpaRepository<LearningEventEntity, Long> {

    List<LearningEventEntity> findBySourceTypeAndSourceIdOrderByIdAsc(
            LearningEventSourceType sourceType,
            Long sourceId
    );

    long countBySourceTypeAndSourceId(LearningEventSourceType sourceType, Long sourceId);

    long deleteBySourceTypeAndSourceId(LearningEventSourceType sourceType, Long sourceId);

    long deleteBySourceTypeAndSourceIdIn(LearningEventSourceType sourceType, Collection<Long> sourceIds);

    long countByLearningUnitId(Long learningUnitId);
}
