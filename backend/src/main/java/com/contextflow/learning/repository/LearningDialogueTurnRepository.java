package com.contextflow.learning.repository;

import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningDialogueTurnRepository extends JpaRepository<LearningDialogueTurnEntity, Long> {

    long countByLearningPackageId(Long learningPackageId);
}
