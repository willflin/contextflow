export type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type HealthStatus = {
  status: string;
  service: string;
  version: string;
  checkedAt: string;
};

export async function fetchHealth(): Promise<ApiResponse<HealthStatus>> {
  const response = await fetch('/api/health');

  if (!response.ok) {
    throw new Error(`Health request failed with status ${response.status}`);
  }

  return response.json();
}

