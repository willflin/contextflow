package com.contextflow.auth.service;

import com.contextflow.auth.config.JwtProperties;
import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.auth.dto.LoginRequest;
import com.contextflow.auth.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtProperties jwtProperties;
    private final JwtTokenService jwtTokenService;

    private final Map<String, MockUser> users = Map.of(
            "learner", new MockUser("learner", "learner123", "Demo Learner", UserRole.LEARNER),
            "admin", new MockUser("admin", "admin123", "Demo Admin", UserRole.ADMIN)
    );

    public AuthService(JwtProperties jwtProperties, JwtTokenService jwtTokenService) {
        this.jwtProperties = jwtProperties;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        MockUser user = users.get(request.username());

        if (user == null || !user.password().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        CurrentUserResponse currentUser = toCurrentUserResponse(user);

        return new LoginResponse(
                jwtTokenService.createToken(currentUser),
                TOKEN_TYPE,
                jwtProperties.accessTokenTtlSeconds(),
                currentUser
        );
    }

    public CurrentUserResponse getCurrentUser(String authorizationHeader) {
        CurrentUserResponse tokenUser = jwtTokenService.parseUser(authorizationHeader)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));

        if (!users.containsKey(tokenUser.username())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }

        return tokenUser;
    }

    private CurrentUserResponse toCurrentUserResponse(MockUser user) {
        return new CurrentUserResponse(user.username(), user.displayName(), user.role());
    }

    private record MockUser(
            String username,
            String password,
            String displayName,
            UserRole role
    ) {
    }
}
