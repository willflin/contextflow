package com.contextflow.learning;

import com.contextflow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learner")
public class LearnerProbeController {

    @GetMapping("/probe")
    public ApiResponse<ProbeResponse> probe() {
        return ApiResponse.ok(new ProbeResponse("LEARNER", "Learner endpoint is accessible."));
    }

    private record ProbeResponse(String scope, String message) {
    }
}

