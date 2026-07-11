package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningUnitSenseSourceEntity;
import com.contextflow.content.domain.SenseSourceAttributeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningUnitSenseSourceRepository extends JpaRepository<LearningUnitSenseSourceEntity, Long> {

    @Query("""
            select sourceLink
            from LearningUnitSenseSourceEntity sourceLink
            join fetch sourceLink.dataSource dataSource
            where sourceLink.sense.id = :senseId
            order by sourceLink.id asc
            """)
    List<LearningUnitSenseSourceEntity> findBySenseIdWithDataSourceOrderByIdAsc(@Param("senseId") Long senseId);

    Optional<LearningUnitSenseSourceEntity> findBySenseIdAndDataSourceIdAndAttributeTypeAndSourceRecordId(
            Long senseId,
            Long dataSourceId,
            SenseSourceAttributeType attributeType,
            String sourceRecordId
    );
}
