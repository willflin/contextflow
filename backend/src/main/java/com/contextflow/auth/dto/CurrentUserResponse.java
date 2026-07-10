package com.contextflow.auth.dto;

import com.contextflow.auth.domain.UserRole;

public record CurrentUserResponse(
        String username,
        String displayName,
        UserRole role
) {
}

