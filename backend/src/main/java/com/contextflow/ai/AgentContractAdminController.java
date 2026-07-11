package com.contextflow.ai;

import com.contextflow.ai.agent.dto.AgentDialogueContractSampleResponse;
import com.contextflow.ai.agent.service.AgentDialogueContractService;
import com.contextflow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/agent-contract")
public class AgentContractAdminController {

    private final AgentDialogueContractService agentDialogueContractService;

    public AgentContractAdminController(AgentDialogueContractService agentDialogueContractService) {
        this.agentDialogueContractService = agentDialogueContractService;
    }

    @GetMapping("/dialogue/sample")
    public ApiResponse<AgentDialogueContractSampleResponse> dialogueSample() {
        return ApiResponse.ok(agentDialogueContractService.sample());
    }
}
