package com.contextflow.user;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.user.dto.UserLevelProfileResponse;
import com.contextflow.user.service.UserLevelProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserLevelProfileService userLevelProfileService;

    public UserProfileController(UserLevelProfileService userLevelProfileService) {
        this.userLevelProfileService = userLevelProfileService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserLevelProfileResponse> profile(Authentication authentication) {
        return ApiResponse.ok(userLevelProfileService.getProfile(currentUsername(authentication)));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
