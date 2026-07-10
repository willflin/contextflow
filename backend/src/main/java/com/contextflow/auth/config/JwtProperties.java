package com.contextflow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "contextflow.auth.jwt")
public record JwtProperties(
        String secret,
        long accessTokenTtlSeconds
) {
}

