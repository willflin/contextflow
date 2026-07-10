package com.contextflow.auth.service;

import com.contextflow.auth.config.JwtProperties;
import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.LoginRequest;
import com.contextflow.auth.dto.RegisterRequest;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserStatus;
import com.contextflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "contextflow-test-secret-key-change-before-production-32bytes",
                3600
        );
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userRepository = mock(UserRepository.class);

        when(userRepository.findByUsername("learner"))
                .thenReturn(Optional.of(new UserEntity(
                        "learner",
                        passwordEncoder.encode("learner123"),
                        "Demo Learner",
                        UserRole.LEARNER,
                        UserStatus.ACTIVE
                )));
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new UserEntity(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "Demo Admin",
                        UserRole.ADMIN,
                        UserStatus.ACTIVE
                )));

        JwtTokenService jwtTokenService = new JwtTokenService(jwtProperties);
        authService = new AuthService(jwtProperties, jwtTokenService, passwordEncoder, userRepository);
    }

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

    @Test
    void registerShouldCreateLearnerUserAndReturnJwt() {
        var response = authService.register(new RegisterRequest("newuser", "password123", "New User"));

        assertThat(response.token()).contains(".");
        assertThat(response.user().username()).isEqualTo("newuser");
        assertThat(response.user().role()).isEqualTo(UserRole.LEARNER);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void registerShouldRejectExistingUsername() {
        when(userRepository.existsByUsername("learner")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("learner", "password123", "Learner")))
                .isInstanceOf(ResponseStatusException.class);
    }
}
