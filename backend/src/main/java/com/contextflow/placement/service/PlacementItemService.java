package com.contextflow.placement.service;

import com.contextflow.placement.domain.PlacementItemStatus;
import com.contextflow.placement.dto.PlacementItemResponse;
import com.contextflow.placement.repository.PlacementItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
