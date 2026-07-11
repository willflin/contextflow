package com.contextflow.review;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.review.dto.ReviewPlanResponse;
import com.contextflow.review.service.ReviewPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/review")
public class ReviewPlanController {

    private final ReviewPlanService reviewPlanService;

    public ReviewPlanController(ReviewPlanService reviewPlanService) {
        this.reviewPlanService = reviewPlanService;
    }

    @GetMapping("/plan")
    public ApiResponse<ReviewPlanResponse> plan(
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        return ApiResponse.ok(reviewPlanService.plan(currentUsername(authentication), limit));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
