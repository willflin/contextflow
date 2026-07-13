type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type LearningUnitForm = {
  id: number;
  formText: string;
  normalizedForm: string;
  formType: string;
};

export type LearningUnitSense = {
  id: number;
  senseKey: string;
  partOfSpeech: string | null;
  definitionEn: string;
  definitionZh: string | null;
  difficultyLevel: string | null;
  difficultyConfidence: number | null;
  frequencyScore: number | null;
  frequencyBand: string | null;
  status: string;
};

export type LearningUnitDetail = {
  id: number;
  unitType: string;
  canonicalText: string;
  normalizedText: string;
  languageCode: string;
  status: string;
  forms: LearningUnitForm[];
  senses: LearningUnitSense[];
};

export type AdminWordPayload = {
  canonicalText: string;
  partOfSpeech: string;
  definitionEn: string;
  definitionZh: string;
  difficultyLevel: string;
  frequencyScore: number | null;
  frequencyBand: string;
  forms: Array<{
    formText: string;
    formType: string;
  }>;
};

export type AdminWordUpdatePayload = {
  canonicalText: string;
  status: string;
};

export type AdminSenseUpdatePayload = {
  partOfSpeech: string;
  definitionEn: string;
  definitionZh: string;
  difficultyLevel: string;
  difficultyConfidence: number | null;
  frequencyScore: number | null;
  frequencyBand: string;
  status: string;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function searchAdminWord(token: string, text: string): Promise<LearningUnitDetail> {
  const response = await fetch(`/api/admin/learning-units/search?text=${encodeURIComponent(text)}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Word not found.'));
  }

  const result = (await response.json()) as ApiResponse<LearningUnitDetail>;
  return result.data;
}

export async function createAdminWord(token: string, payload: AdminWordPayload): Promise<LearningUnitDetail> {
  const response = await fetch('/api/admin/learning-units/words', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to create word.'));
  }

  const result = (await response.json()) as ApiResponse<LearningUnitDetail>;
  return result.data;
}

export async function updateAdminWord(
  token: string,
  id: number,
  payload: AdminWordUpdatePayload
): Promise<LearningUnitDetail> {
  const response = await fetch(`/api/admin/learning-units/words/${id}`, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to update word.'));
  }

  const result = (await response.json()) as ApiResponse<LearningUnitDetail>;
  return result.data;
}

export async function updateAdminSense(
  token: string,
  senseId: number,
  payload: AdminSenseUpdatePayload
): Promise<LearningUnitDetail> {
  const response = await fetch(`/api/admin/learning-units/senses/${senseId}`, {
    method: 'PUT',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to update sense.'));
  }

  const result = (await response.json()) as ApiResponse<LearningUnitDetail>;
  return result.data;
}

export async function deleteAdminWord(token: string, id: number): Promise<void> {
  const response = await fetch(`/api/admin/learning-units/words/${id}`, {
    method: 'DELETE',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to delete word.'));
  }
}
