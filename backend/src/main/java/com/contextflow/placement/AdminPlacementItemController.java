package com.contextflow.placement;

import com.contextflow.common.api.ApiResponse;
import com.contextflow.placement.dto.AdminPlacementItemResponse;
import com.contextflow.placement.service.PlacementItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/placement-items")
public class AdminPlacementItemController {

    private final PlacementItemService placementItemService;

    public AdminPlacementItemController(PlacementItemService placementItemService) {
        this.placementItemService = placementItemService;
    }

    @GetMapping
    public ApiResponse<List<AdminPlacementItemResponse>> search(
            @RequestParam(required = false) String abilityDimension,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ApiResponse.ok(placementItemService.searchAdminItems(abilityDimension, status, query, limit));
    }
}
