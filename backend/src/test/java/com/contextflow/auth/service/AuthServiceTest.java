package com.contextflow.auth.service;

import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void loginShouldReturnLearnerToken() {
        var response = authService.login(new LoginRequest("learner", "learner123"));

        assertThat(response.token()).isEqualTo("mock-token-learner");
        assertThat(response.user().role()).isEqualTo(UserRole.LEARNER);
    }

    @Test
    void meShouldReturnCurrentUserFromBearerToken() {
        var response = authService.getCurrentUser("Bearer mock-token-admin");

        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("learner", "wrong")))
                .isInstanceOf(ResponseStatusException.class);
    }
}
