package com.contextflow.placement;

import com.contextflow.common.api.ApiResponse;
import com.contextflow.placement.dto.PlacementItemResponse;
import com.contextflow.placement.service.PlacementItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/placement/items")
public class PlacementItemController {

    private final PlacementItemService placementItemService;

    public PlacementItemController(PlacementItemService placementItemService) {
        this.placementItemService = placementItemService;
    }

    @GetMapping("/sample")
    public ApiResponse<List<PlacementItemResponse>> sample() {
        return ApiResponse.ok(placementItemService.getReadySample());
    }
}
