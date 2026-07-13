package com.contextflow.placement.service;

import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.dto.AdminPlacementItemResponse;
import com.contextflow.placement.dto.PlacementItemResponse;
import com.contextflow.placement.repository.PlacementItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PlacementItemService {

    private static final int DEFAULT_SAMPLE_SIZE = 5;

    private final PlacementItemRepository placementItemRepository;

    public PlacementItemService(PlacementItemRepository placementItemRepository) {
        this.placementItemRepository = placementItemRepository;
    }

    @Transactional(readOnly = true)
    public List<PlacementItemResponse> getReadySample() {
        return placementItemRepository
                .findByStatusOrderByIdAsc(PlacementItemStatus.READY, PageRequest.of(0, DEFAULT_SAMPLE_SIZE))
                .stream()
                .map(PlacementItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPlacementItemResponse> searchAdminItems(
            String abilityDimension,
            String status,
            String query,
            int limit
    ) {
        String normalizedAbility = normalize(abilityDimension);
        String normalizedStatus = normalize(status);
        String normalizedQuery = normalize(query);
        int safeLimit = Math.max(1, Math.min(limit, 200));

        return placementItemRepository.findAll()
                .stream()
                .filter(item -> normalizedAbility == null || normalizedAbility.equals(normalize(item.getAbilityDimension())))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(normalize(item.getStatus().name())))
                .filter(item -> normalizedQuery == null
                        || normalize(item.getScenarioTag()).contains(normalizedQuery)
                        || normalize(item.getTargetSkill()).contains(normalizedQuery)
                        || normalize(item.getContentJson()).contains(normalizedQuery))
                .sorted(Comparator
                        .comparing(PlacementItemResponseSort::statusRank)
                        .thenComparing(PlacementItemResponseSort::difficultyScore)
                        .thenComparing(PlacementItemResponseSort::id))
                .limit(safeLimit)
                .map(AdminPlacementItemResponse::from)
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static final class PlacementItemResponseSort {
        private static int statusRank(com.contextflow.placement.domain.PlacementItemEntity item) {
            return PlacementItemStatus.READY.equals(item.getStatus()) ? 0 : 1;
        }

        private static Integer difficultyScore(com.contextflow.placement.domain.PlacementItemEntity item) {
            return item.getDifficultyScore();
        }

        private static Long id(com.contextflow.placement.domain.PlacementItemEntity item) {
            return item.getId();
        }
    }
}
