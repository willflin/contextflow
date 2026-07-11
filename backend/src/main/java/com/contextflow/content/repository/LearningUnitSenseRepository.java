package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningUnitSenseRepository extends JpaRepository<LearningUnitSenseEntity, Long> {

    List<LearningUnitSenseEntity> findByLearningUnitIdAndStatusOrderByIdAsc(Long learningUnitId, LearningUnitStatus status);

    Optional<LearningUnitSenseEntity> findByLearningUnitIdAndSenseKey(Long learningUnitId, String senseKey);

    @Query("""
            select sense
            from LearningUnitSenseEntity sense
            join fetch sense.learningUnit unit
            where sense.id = :id
            """)
    Optional<LearningUnitSenseEntity> findByIdWithUnit(@Param("id") Long id);
}
