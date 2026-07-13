type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type AgentRuntimeStatus = {
  provider: string;
  springAiClientAvailable: boolean;
  fallbackToLocalOnError: boolean;
  contractVersion: string;
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
