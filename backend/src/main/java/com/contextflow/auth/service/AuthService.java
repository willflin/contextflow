package com.contextflow.auth.service;

import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.auth.dto.LoginRequest;
import com.contextflow.auth.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final long MOCK_TOKEN_TTL_SECONDS = 3600;

    private final Map<String, MockUser> users = Map.of(
            "learner", new MockUser("learner", "learner123", "Demo Learner", UserRole.LEARNER, "mock-token-learner"),
            "admin", new MockUser("admin", "admin123", "Demo Admin", UserRole.ADMIN, "mock-token-admin")
    );

    public LoginResponse login(LoginRequest request) {
        MockUser user = users.get(request.username());

        if (user == null || !user.password().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        return new LoginResponse(
                user.token(),
                TOKEN_TYPE,
                MOCK_TOKEN_TTL_SECONDS,
                toCurrentUserResponse(user)
        );
    }

    public CurrentUserResponse getCurrentUser(String authorizationHeader) {
        return findCurrentUser(authorizationHeader)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    public Optional<CurrentUserResponse> findCurrentUser(String authorizationHeader) {
        Optional<String> token = extractBearerToken(authorizationHeader);

        return users.values().stream()
                .filter(user -> token.isPresent() && user.token().equals(token.get()))
                .findFirst()
                .map(this::toCurrentUserResponse);
    }

    private Optional<String> extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_TYPE + " ")) {
            return Optional.empty();
        }

        return Optional.of(authorizationHeader.substring((TOKEN_TYPE + " ").length()));
    }

    private CurrentUserResponse toCurrentUserResponse(MockUser user) {
        return new CurrentUserResponse(user.username(), user.displayName(), user.role());
    }

    private record MockUser(
            String username,
            String password,
            String displayName,
            UserRole role,
            String token
    ) {
    }
}
