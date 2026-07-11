package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningUnitEntity;
import com.contextflow.content.domain.LearningUnitStatus;
import com.contextflow.content.domain.LearningUnitType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningUnitRepository extends JpaRepository<LearningUnitEntity, Long> {

    List<LearningUnitEntity> findByStatusOrderByIdAsc(LearningUnitStatus status);

    Optional<LearningUnitEntity> findByLanguageCodeAndUnitTypeAndNormalizedText(
            String languageCode,
            LearningUnitType unitType,
            String normalizedText
    );

    Optional<LearningUnitEntity> findFirstByLanguageCodeAndNormalizedTextOrderByIdAsc(
            String languageCode,
            String normalizedText
    );
}
