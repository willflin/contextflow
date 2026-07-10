package com.contextflow.auth.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        CurrentUserResponse user
) {
}

