package com.contextflow.placement.repository;

import com.contextflow.placement.domain.PlacementSessionAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementSessionAnswerRepository extends JpaRepository<PlacementSessionAnswerEntity, Long> {

    List<PlacementSessionAnswerEntity> findBySessionIdOrderByItemOrderAsc(Long sessionId);
}
