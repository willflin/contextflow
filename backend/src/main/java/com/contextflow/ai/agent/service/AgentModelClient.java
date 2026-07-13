package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;

public interface AgentModelClient {

    default boolean isAvailable() {
        return true;
    }

    AgentDialogueOutput generateDialogue(AgentDialogueInput input);
}
