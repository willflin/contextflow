package com.contextflow.placement.repository;

import com.contextflow.placement.domain.PlacementSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlacementSessionRepository extends JpaRepository<PlacementSessionEntity, Long> {

    Optional<PlacementSessionEntity> findByIdAndUserId(Long id, Long userId);
}
