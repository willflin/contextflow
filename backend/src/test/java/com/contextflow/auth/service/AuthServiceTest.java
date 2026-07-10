package com.contextflow.auth.service;

import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.LoginRequest;
import com.contextflow.auth.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private final JwtProperties jwtProperties = new JwtProperties(
            "contextflow-test-secret-key-change-before-production-32bytes",
            3600
    );
    private final JwtTokenService jwtTokenService = new JwtTokenService(jwtProperties);
    private final AuthService authService = new AuthService(jwtProperties, jwtTokenService);

    @Test
    void loginShouldReturnLearnerJwt() {
        var response = authService.login(new LoginRequest("learner", "learner123"));

        assertThat(response.token()).contains(".");
        assertThat(response.user().role()).isEqualTo(UserRole.LEARNER);
    }

    @Test
    void meShouldReturnCurrentUserFromBearerToken() {
        var loginResponse = authService.login(new LoginRequest("admin", "admin123"));
        var response = authService.getCurrentUser("Bearer " + loginResponse.token());

        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void meShouldRejectMissingToken() {
        assertThatThrownBy(() -> authService.getCurrentUser(null))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("learner", "wrong")))
                .isInstanceOf(ResponseStatusException.class);
    }
}
