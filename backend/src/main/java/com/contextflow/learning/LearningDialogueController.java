package com.contextflow.learning;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.learning.dto.LearningDialogueRequest;
import com.contextflow.learning.dto.LearningDialogueResponse;
import com.contextflow.learning.service.LearningDialogueService;
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
@RequestMapping("/api/learning/packages")
public class LearningDialogueController {

    private final LearningDialogueService learningDialogueService;

    public LearningDialogueController(LearningDialogueService learningDialogueService) {
        this.learningDialogueService = learningDialogueService;
    }

    @PostMapping("/{packageId}/dialog")
    public ApiResponse<LearningDialogueResponse> dialog(
            @PathVariable Long packageId,
            @Valid @RequestBody LearningDialogueRequest request,
            Authentication authentication
    ) {
        return ApiResponse.ok(learningDialogueService.reply(currentUsername(authentication), packageId, request));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
