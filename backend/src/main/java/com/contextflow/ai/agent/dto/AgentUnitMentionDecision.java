package com.contextflow.ai.agent.dto;

public enum AgentUnitMentionDecision {
    RECORD_EVENT,
    RECORD_SPELLING_OR_FORM_ERROR,
    SUBMIT_MISSING_SENSE_FEEDBACK,
    SKIP_UNRECOGNIZABLE,
    SKIP_WRONG_USAGE
}
