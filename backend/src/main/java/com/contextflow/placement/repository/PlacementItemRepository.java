package com.contextflow.placement.repository;

import com.contextflow.placement.domain.PlacementItemEntity;
import com.contextflow.placement.domain.PlacementItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementItemRepository extends JpaRepository<PlacementItemEntity, Long> {

    List<PlacementItemEntity> findByStatusOrderByIdAsc(PlacementItemStatus status, Pageable pageable);

    long countByAbilityDimension(String abilityDimension);
}
