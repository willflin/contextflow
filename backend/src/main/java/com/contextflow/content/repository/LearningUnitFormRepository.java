package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningUnitFormEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningUnitFormRepository extends JpaRepository<LearningUnitFormEntity, Long> {

    @Query("""
            select form
            from LearningUnitFormEntity form
            join fetch form.learningUnit unit
            where unit.status = :status
            order by unit.id asc, form.id asc
            """)
    List<LearningUnitFormEntity> findAllWithUnitByUnitStatus(@Param("status") LearningUnitStatus status);

    @Query("""
            select form
            from LearningUnitFormEntity form
            join fetch form.learningUnit unit
            where form.normalizedForm = :normalizedForm
            order by unit.id asc, form.id asc
            """)
    List<LearningUnitFormEntity> findByNormalizedFormWithUnit(@Param("normalizedForm") String normalizedForm);

    Optional<LearningUnitFormEntity> findByLearningUnitIdAndNormalizedFormAndFormType(
            Long learningUnitId,
            String normalizedForm,
            com.contextflow.content.domain.LearningUnitFormType formType
    );

    List<LearningUnitFormEntity> findByLearningUnitIdOrderByIdAsc(Long learningUnitId);
}
