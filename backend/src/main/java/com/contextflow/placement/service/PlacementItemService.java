package com.contextflow.placement.service;

import com.contextflow.common.api.PageResponse;
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
    public PageResponse<AdminPlacementItemResponse> searchAdminItems(
            String abilityDimension,
            String status,
            String query,
            int page,
            int size
    ) {
        String normalizedAbility = normalize(abilityDimension);
        String normalizedStatus = normalize(status);
        String normalizedQuery = normalize(query);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));

        List<AdminPlacementItemResponse> filteredItems = placementItemRepository.findAll()
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
                .map(AdminPlacementItemResponse::from)
                .toList();
        int fromIndex = Math.min(safePage * safeSize, filteredItems.size());
        int toIndex = Math.min(fromIndex + safeSize, filteredItems.size());
        return PageResponse.of(filteredItems.subList(fromIndex, toIndex), safePage, safeSize, filteredItems.size());
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
