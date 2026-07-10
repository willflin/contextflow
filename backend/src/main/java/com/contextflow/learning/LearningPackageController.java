package com.contextflow.learning;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.learning.dto.LearningPackageResponse;
import com.contextflow.learning.service.LearningPackageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/learning/packages")
public class LearningPackageController {

    private final LearningPackageService learningPackageService;

    public LearningPackageController(LearningPackageService learningPackageService) {
        this.learningPackageService = learningPackageService;
    }

    @GetMapping("/next")
    public ApiResponse<LearningPackageResponse> next(Authentication authentication) {
        return ApiResponse.ok(learningPackageService.getNextReadyPackage(currentUsername(authentication)));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
