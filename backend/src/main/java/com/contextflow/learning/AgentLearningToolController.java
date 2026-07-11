package com.contextflow.learning;

import com.contextflow.auth.dto.CurrentUserResponse;
import com.contextflow.common.api.ApiResponse;
import com.contextflow.content.dto.LearningUnitDetailResponse;
import com.contextflow.learning.dto.AgentLearningEventRequest;
import com.contextflow.learning.dto.AgentLearningEventResponse;
import com.contextflow.learning.dto.AgentSenseFeedbackRequest;
import com.contextflow.learning.dto.AgentSenseFeedbackResponse;
import com.contextflow.learning.service.AgentLearningToolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/learning/agent-tools")
public class AgentLearningToolController {

    private final AgentLearningToolService agentLearningToolService;

    public AgentLearningToolController(AgentLearningToolService agentLearningToolService) {
        this.agentLearningToolService = agentLearningToolService;
    }

    @GetMapping("/word-senses")
    public ApiResponse<LearningUnitDetailResponse> wordSenses(@RequestParam String text) {
        return ApiResponse.ok(agentLearningToolService.wordSenses(text));
    }

    @PostMapping("/events")
    public ApiResponse<AgentLearningEventResponse> recordEvent(
            @Valid @RequestBody AgentLearningEventRequest request,
            Authentication authentication
    ) {
        return ApiResponse.ok(agentLearningToolService.recordLearningEvent(currentUsername(authentication), request));
    }

    @PostMapping("/sense-feedback")
    public ApiResponse<AgentSenseFeedbackResponse> senseFeedback(
            @Valid @RequestBody AgentSenseFeedbackRequest request,
            Authentication authentication
    ) {
        return ApiResponse.ok(agentLearningToolService.submitSenseFeedback(currentUsername(authentication), request));
    }

    private String currentUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserResponse currentUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return currentUser.username();
    }
}
