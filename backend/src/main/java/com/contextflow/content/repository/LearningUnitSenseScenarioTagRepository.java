package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningUnitSenseScenarioTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LearningUnitSenseScenarioTagRepository extends JpaRepository<LearningUnitSenseScenarioTagEntity, Long> {

    @Query("""
            select tag
            from LearningUnitSenseScenarioTagEntity tag
            join fetch tag.learningUnitSense sense
            where sense.id = :learningUnitSenseId
              and tag.scenarioCode = :scenarioCode
            """)
    Optional<LearningUnitSenseScenarioTagEntity> findByLearningUnitSenseIdAndScenarioCode(
            @Param("learningUnitSenseId") Long learningUnitSenseId,
            @Param("scenarioCode") String scenarioCode
    );

    @Query("""
            select tag
            from LearningUnitSenseScenarioTagEntity tag
            join fetch tag.learningUnitSense sense
            where sense.id in :learningUnitSenseIds
            order by tag.relevanceScore desc, tag.scenarioCode asc
            """)
    List<LearningUnitSenseScenarioTagEntity> findByLearningUnitSenseIdInOrderByRelevanceScoreDescScenarioCodeAsc(
            @Param("learningUnitSenseIds") Collection<Long> learningUnitSenseIds
    );
}
