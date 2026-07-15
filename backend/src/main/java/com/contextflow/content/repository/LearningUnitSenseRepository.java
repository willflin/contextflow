package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningUnitSenseEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningUnitSenseRepository extends JpaRepository<LearningUnitSenseEntity, Long> {

    List<LearningUnitSenseEntity> findByLearningUnitIdAndStatusOrderByIdAsc(Long learningUnitId, LearningUnitStatus status);

    List<LearningUnitSenseEntity> findByLearningUnitIdOrderByIdAsc(Long learningUnitId);

    @Query("""
            select sense
            from LearningUnitSenseEntity sense
            join fetch sense.learningUnit unit
            where unit.id = :learningUnitId
              and unit.status = :unitStatus
              and unit.unitType = :unitType
              and sense.status = :senseStatus
            order by sense.id asc
            """)
    List<LearningUnitSenseEntity> findActiveWordSensesByLearningUnitId(
            @Param("learningUnitId") Long learningUnitId,
            @Param("unitType") LearningUnitType unitType,
            @Param("unitStatus") LearningUnitStatus unitStatus,
            @Param("senseStatus") LearningUnitStatus senseStatus
    );

    Optional<LearningUnitSenseEntity> findByLearningUnitIdAndSenseKey(Long learningUnitId, String senseKey);

    @Query("""
            select sense
            from LearningUnitSenseEntity sense
            join fetch sense.learningUnit unit
            where sense.id = :id
            """)
    Optional<LearningUnitSenseEntity> findByIdWithUnit(@Param("id") Long id);

    @Query("""
            select sense
            from LearningUnitSenseEntity sense
            join fetch sense.learningUnit unit
            where sense.id in :ids
            order by unit.normalizedText asc, sense.id asc
            """)
    List<LearningUnitSenseEntity> findByIdInWithUnit(@Param("ids") List<Long> ids);

    @Query("""
            select sense
            from LearningUnitSenseEntity sense
            join fetch sense.learningUnit unit
            where sense.status = :senseStatus
              and unit.status = :unitStatus
              and unit.unitType = :unitType
              and not exists (
                  select stats.id
                  from UserLearningUnitSenseStatsEntity stats
                  where stats.userId = :userId
                    and stats.learningUnitSense = sense
              )
            order by sense.id asc
            """)
    List<LearningUnitSenseEntity> findNewSenseCandidates(
            @Param("userId") Long userId,
            @Param("unitType") LearningUnitType unitType,
            @Param("unitStatus") LearningUnitStatus unitStatus,
            @Param("senseStatus") LearningUnitStatus senseStatus
    );
}
