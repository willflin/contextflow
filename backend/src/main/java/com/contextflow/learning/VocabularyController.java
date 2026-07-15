package com.contextflow.learning;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.content.dto.VocabularyListResponse;
import com.contextflow.content.service.VocabularyQueryService;
import com.contextflow.learning.dto.LearningPlanAddWordResponse;
import com.contextflow.learning.service.LearningPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/learning/vocabulary")
public class VocabularyController {

    private final VocabularyQueryService vocabularyQueryService;
    private final LearningPlanService learningPlanService;

    public VocabularyController(
            VocabularyQueryService vocabularyQueryService,
            LearningPlanService learningPlanService
    ) {
        this.vocabularyQueryService = vocabularyQueryService;
        this.learningPlanService = learningPlanService;
    }

    @GetMapping
    public ApiResponse<VocabularyListResponse> list(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        return ApiResponse.ok(vocabularyQueryService.list(currentUsername(authentication), status, query, limit));
    }

    @PostMapping("/{learningUnitId}/plan")
    public ApiResponse<LearningPlanAddWordResponse> addToLearningPlan(
            @PathVariable Long learningUnitId,
            Authentication authentication
    ) {
        return ApiResponse.ok(learningPlanService.addWord(currentUsername(authentication), learningUnitId));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
