type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type UserLevelProfile = {
  cefrLevel: string;
  dimensionScoresJson: string;
  weakScenariosJson: string;
  weakAbilitiesJson: string;
  lastPlacementSessionId: number | null;
  updatedAt: string | null;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchUserLevelProfile(token: string): Promise<UserLevelProfile> {
  const response = await fetch('/api/user/profile', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load user profile.'));
  }

  const result = (await response.json()) as ApiResponse<UserLevelProfile>;
  return result.data;
}

export function prettyJson(json: string): string {
  try {
    return JSON.stringify(JSON.parse(json), null, 2);
  } catch {
    return json;
  }
}
