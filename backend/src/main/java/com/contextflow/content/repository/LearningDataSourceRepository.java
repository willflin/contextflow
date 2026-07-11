package com.contextflow.content.repository;

import com.contextflow.content.domain.LearningDataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningDataSourceRepository extends JpaRepository<LearningDataSourceEntity, Long> {

    Optional<LearningDataSourceEntity> findBySourceNameAndSourceVersion(String sourceName, String sourceVersion);
}
