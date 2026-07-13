type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type AdminDialogueTurn = {
  id: number;
  userId: number;
  learningPackageId: number;
  turnIndex: number;
  userMessage: string;
  roleplayReply: string;
  mentorFeedback: string;
  corrections: string;
  naturalExpression: string;
  scoringSignal: string;
  learningEventCount: number;
  createdAt: string;
};

export type AdminDialogueTurnUpdatePayload = {
  userMessage: string;
  roleplayReply: string;
  mentorFeedback: string;
  corrections: string;
  naturalExpression: string;
  scoringSignal: string;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchAdminDialogues(token: string, packageId?: string): Promise<AdminDialogueTurn[]> {
  const query = packageId?.trim() ? `?packageId=${encodeURIComponent(packageId.trim())}` : '';
  const response = await fetch(`/api/admin/dialogues${query}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load dialogues.'));
  }

  const result = (await response.json()) as ApiResponse<AdminDialogueTurn[]>;
  return result.data;
}

export async function updateAdminDialogue(
  token: string,
  id: number,
  payload: AdminDialogueTurnUpdatePayload
): Promise<AdminDialogueTurn> {
  const response = await fetch(`/api/admin/dialogues/${id}`, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to update dialogue.'));
  }

  const result = (await response.json()) as ApiResponse<AdminDialogueTurn>;
  return result.data;
}

export async function deleteAdminDialogue(token: string, id: number): Promise<void> {
  const response = await fetch(`/api/admin/dialogues/${id}`, {
    method: 'DELETE',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to delete dialogue.'));
  }
}
