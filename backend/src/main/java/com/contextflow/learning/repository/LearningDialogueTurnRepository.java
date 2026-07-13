package com.contextflow.learning.repository;

import com.contextflow.learning.domain.LearningDialogueTurnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningDialogueTurnRepository extends JpaRepository<LearningDialogueTurnEntity, Long> {

    long countByLearningPackageId(Long learningPackageId);

    List<LearningDialogueTurnEntity> findTop100ByOrderByIdDesc();

    List<LearningDialogueTurnEntity> findTop100ByLearningPackageIdOrderByTurnIndexDesc(Long learningPackageId);

    Page<LearningDialogueTurnEntity> findByLearningPackageId(Long learningPackageId, Pageable pageable);

    List<LearningDialogueTurnEntity> findTop6ByLearningPackageIdOrderByTurnIndexDesc(Long learningPackageId);

    List<LearningDialogueTurnEntity> findByLearningPackageIdOrderByIdAsc(Long learningPackageId);

    long deleteByLearningPackageId(Long learningPackageId);
}
