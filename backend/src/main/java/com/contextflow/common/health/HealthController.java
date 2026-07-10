package com.contextflow.common.health;

import com.contextflow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<HealthStatus> health() {
        HealthStatus status = new HealthStatus(
                "UP",
                "contextflow-backend",
                "0.0.1-SNAPSHOT",
                Instant.now()
        );
        return ApiResponse.ok(status);
    }
}

