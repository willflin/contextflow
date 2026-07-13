type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type AgentRuntimeDiagnostics = {
  springAiModelChat: string | null;
  deepSeekApiKeyConfigured: boolean;
  deepSeekChatApiKeyConfigured: boolean;
  deepSeekBaseUrl: string | null;
  deepSeekChatBaseUrl: string | null;
  deepSeekModel: string | null;
  deepSeekChatEnabled: string | null;
  deepSeekAutoConfigurationClassPresent: boolean;
  deepSeekApiClassPresent: boolean;
  chatModelBeanNames: string[];
  agentModelClientBeanNames: string[];
  activeProfiles: string[];
};

export type AgentRuntimeStatus = {
  provider: string;
  springAiClientAvailable: boolean;
  fallbackToLocalOnError: boolean;
  contractVersion: string;
  diagnostics: AgentRuntimeDiagnostics;
};

export type AgentDialogueOutput = {
  contractVersion: string;
  reply: string;
  feedback: string;
  corrections: unknown[];
  naturalExpression: string;
  unitMentions: unknown[];
  scoringSignal: Record<string, unknown>;
};

export type AgentRuntimeProbe = AgentRuntimeStatus & {
  modelAttempted: boolean;
  fallbackUsed: boolean;
  accepted: boolean;
  errors: string[];
  elapsedMs: number;
  errorMessage: string | null;
  output: AgentDialogueOutput | null;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchAgentRuntimeStatus(token: string): Promise<AgentRuntimeStatus> {
  const response = await fetch('/api/admin/agent-contract/runtime', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load Agent runtime.'));
  }

  const result = (await response.json()) as ApiResponse<AgentRuntimeStatus>;
  return result.data;
}

export async function runAgentRuntimeProbe(token: string): Promise<AgentRuntimeProbe> {
  const response = await fetch('/api/admin/agent-contract/dialogue/probe', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to probe Agent runtime.'));
  }

  const result = (await response.json()) as ApiResponse<AgentRuntimeProbe>;
  return result.data;
}
