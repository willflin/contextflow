package com.contextflow.auth;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.auth.dto.LoginRequest;
import com.contextflow.auth.dto.LoginResponse;
import com.contextflow.auth.dto.RegisterRequest;
import com.contextflow.auth.service.AuthService;
import com.contextflow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        return ApiResponse.ok(authService.getCurrentUser(authorizationHeader));
    }
}
