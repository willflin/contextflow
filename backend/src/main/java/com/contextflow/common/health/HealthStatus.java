package com.contextflow.common.health;

import java.time.Instant;

public record HealthStatus(
        String status,
        String service,
        String version,
        Instant checkedAt
) {
}

