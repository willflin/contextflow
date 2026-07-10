package com.contextflow.auth.service;

import com.contextflow.auth.config.JwtProperties;
import com.contextflow.auth.domain.UserRole;
import com.contextflow.auth.dto.CurrentUserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtTokenService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String DISPLAY_NAME_CLAIM = "displayName";
    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(CurrentUserResponse user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenTtlSeconds());

        return Jwts.builder()
                .subject(user.username())
                .claim(DISPLAY_NAME_CLAIM, user.displayName())
                .claim(ROLE_CLAIM, user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Optional<CurrentUserResponse> parseUser(String authorizationHeader) {
        Optional<String> token = extractBearerToken(authorizationHeader);

        if (token.isEmpty()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token.get())
                    .getPayload();

            String username = claims.getSubject();
            String displayName = claims.get(DISPLAY_NAME_CLAIM, String.class);
            UserRole role = UserRole.valueOf(claims.get(ROLE_CLAIM, String.class));

            return Optional.of(new CurrentUserResponse(username, displayName, role));
        } catch (IllegalArgumentException | JwtException exception) {
            return Optional.empty();
        }
    }

    private Optional<String> extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_TYPE + " ")) {
            return Optional.empty();
        }

        return Optional.of(authorizationHeader.substring((TOKEN_TYPE + " ").length()));
    }
}

