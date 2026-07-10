package com.contextflow.placement;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.placement.dto.AdaptivePlacementAnswerResponse;
import com.contextflow.placement.dto.AdaptivePlacementSessionResponse;
import com.contextflow.placement.dto.PlacementSessionResponse;
import com.contextflow.placement.dto.PlacementSessionResultResponse;
import com.contextflow.placement.dto.SubmitAdaptivePlacementAnswerRequest;
import com.contextflow.placement.dto.SubmitPlacementSessionRequest;
import com.contextflow.placement.service.PlacementSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/placement/session")
public class PlacementSessionController {

    private final PlacementSessionService placementSessionService;

    public PlacementSessionController(PlacementSessionService placementSessionService) {
        this.placementSessionService = placementSessionService;
    }

    @PostMapping("/start")
    public ApiResponse<PlacementSessionResponse> start(Authentication authentication) {
        return ApiResponse.ok(placementSessionService.startSession(currentUsername(authentication)));
    }

    @PostMapping("/adaptive/start")
    public ApiResponse<AdaptivePlacementSessionResponse> startAdaptive(Authentication authentication) {
        return ApiResponse.ok(placementSessionService.startAdaptiveSession(currentUsername(authentication)));
    }

    @PostMapping("/{sessionId}/answer")
    public ApiResponse<AdaptivePlacementAnswerResponse> answerAdaptive(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAdaptivePlacementAnswerRequest request,
            Authentication authentication
    ) {
        return ApiResponse.ok(placementSessionService.answerAdaptive(currentUsername(authentication), sessionId, request));
    }

    @PostMapping("/{sessionId}/submit")
    public ApiResponse<PlacementSessionResultResponse> submit(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitPlacementSessionRequest request,
            Authentication authentication
    ) {
        return ApiResponse.ok(placementSessionService.submitSession(currentUsername(authentication), sessionId, request));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
