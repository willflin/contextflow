type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type AccessProbe = {
  scope: string;
  message: string;
};

export async function fetchAccessProbe(path: '/api/learner/probe' | '/api/admin/probe', token: string) {
  const response = await fetch(path, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}.`);
  }

  const result = (await response.json()) as ApiResponse<AccessProbe>;
  return result.data;
}
