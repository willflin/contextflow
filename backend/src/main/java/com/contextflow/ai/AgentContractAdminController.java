package com.contextflow.ai;

import com.contextflow.ai.agent.dto.AgentDialogueContractSampleResponse;
import com.contextflow.ai.agent.dto.AgentRuntimeProbeResponse;
import com.contextflow.ai.agent.dto.AgentRuntimeStatusResponse;
import com.contextflow.ai.agent.service.AgentDialogueContractService;
import com.contextflow.ai.agent.service.AgentRuntimeService;
import com.contextflow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/agent-contract")
public class AgentContractAdminController {

    private final AgentDialogueContractService agentDialogueContractService;
    private final AgentRuntimeService agentRuntimeService;

    public AgentContractAdminController(
            AgentDialogueContractService agentDialogueContractService,
            AgentRuntimeService agentRuntimeService
    ) {
        this.agentDialogueContractService = agentDialogueContractService;
        this.agentRuntimeService = agentRuntimeService;
    }

    @GetMapping("/dialogue/sample")
    public ApiResponse<AgentDialogueContractSampleResponse> dialogueSample() {
        return ApiResponse.ok(agentDialogueContractService.sample());
    }

    @GetMapping("/runtime")
    public ApiResponse<AgentRuntimeStatusResponse> runtime() {
        return ApiResponse.ok(agentRuntimeService.status());
    }

    @PostMapping("/dialogue/probe")
    public ApiResponse<AgentRuntimeProbeResponse> dialogueProbe() {
        AgentDialogueContractSampleResponse sample = agentDialogueContractService.sample();
        return ApiResponse.ok(agentRuntimeService.probeDialogue(sample.input(), sample::output));
    }
}
