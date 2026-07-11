package com.contextflow.ai.agent.service;

import com.contextflow.ai.agent.dto.AgentDialogueInput;
import com.contextflow.ai.agent.dto.AgentDialogueOutput;

public interface AgentModelClient {

    AgentDialogueOutput generateDialogue(AgentDialogueInput input);
}
