package com.contextflow.auth.service;

import com.contextflow.auth.config.JwtProperties;
import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.auth.dto.LoginRequest;
import com.contextflow.auth.dto.LoginResponse;
import com.contextflow.auth.dto.RegisterRequest;
import com.contextflow.user.domain.UserEntity;
import com.contextflow.user.domain.UserStatus;
import com.contextflow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtProperties jwtProperties;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(
            JwtProperties jwtProperties,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.jwtProperties = jwtProperties;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
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

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }

        UserEntity user = new UserEntity(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.displayName(),
                UserRole.LEARNER,
                UserStatus.ACTIVE
        );

        userRepository.save(user);

        CurrentUserResponse currentUser = new CurrentUserResponse(
                request.username(),
                request.displayName(),
                UserRole.LEARNER
        );

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

        return userRepository.findByUsername(tokenUser.username())
                .filter(UserEntity::isActive)
                .map(this::toCurrentUserResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token."));
    }

    private CurrentUserResponse toCurrentUserResponse(UserEntity user) {
        return new CurrentUserResponse(user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
