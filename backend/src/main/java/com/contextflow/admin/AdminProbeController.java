package com.contextflow.admin;

import com.contextflow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminProbeController {

    @GetMapping("/probe")
    public ApiResponse<ProbeResponse> probe() {
        return ApiResponse.ok(new ProbeResponse("ADMIN", "Admin endpoint is accessible."));
    }

    private record ProbeResponse(String scope, String message) {
    }
}

