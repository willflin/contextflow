package com.contextflow.learning.repository;

import com.contextflow.learning.domain.LearningPackageEntity;
import com.contextflow.learning.domain.LearningPackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningPackageRepository extends JpaRepository<LearningPackageEntity, Long> {

    Optional<LearningPackageEntity> findFirstByUserIdAndStatusOrderByIdAsc(Long userId, LearningPackageStatus status);

    List<LearningPackageEntity> findByUserIdOrderByIdAsc(Long userId);
}
