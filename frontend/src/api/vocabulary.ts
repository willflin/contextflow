type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type VocabularyStatusFilter = 'ALL' | 'LEARNED' | 'UNLEARNED';

export type VocabularySenseProgress = {
  senseId: number;
  senseKey: string;
  partOfSpeech: string | null;
  definitionEn: string;
  definitionZh: string | null;
  difficultyLevel: string | null;
  frequencyBand: string | null;
  inLearningPlan: boolean;
  learned: boolean;
  masteryLevel: string | null;
  masteryScore: number | null;
  exposureCount: number | null;
  attemptCount: number | null;
  lastSeenAt: string | null;
  nextReviewAt: string | null;
};

export type VocabularyWord = {
  learningUnitId: number;
  canonicalText: string;
  normalizedText: string;
  learnedStatus: 'LEARNED' | 'PARTIAL' | 'UNLEARNED';
  plannedSenseCount: number;
  learnedSenseCount: number;
  totalSenseCount: number;
  senses: VocabularySenseProgress[];
};

export type LearningPlanAddWordResult = {
  learningUnitId: number;
  addedSenseCount: number;
  skippedOutOfLevelCount: number;
  message: string;
};

export type VocabularyList = {
  statusFilter: VocabularyStatusFilter;
  query: string | null;
  limit: number;
  totalMatchedWords: number;
  items: VocabularyWord[];
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchVocabulary(
  token: string,
  status: VocabularyStatusFilter,
  query: string,
  limit = 500
): Promise<VocabularyList> {
  const params = new URLSearchParams({
    status,
    limit: String(limit)
  });
  if (query.trim()) {
    params.set('query', query.trim());
  }

  const response = await fetch(`/api/learning/vocabulary?${params.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load vocabulary.'));
  }

  const result = (await response.json()) as ApiResponse<VocabularyList>;
  return result.data;
}

export async function addVocabularyWordToLearningPlan(
  token: string,
  learningUnitId: number
): Promise<LearningPlanAddWordResult> {
  const response = await fetch(`/api/learning/vocabulary/${learningUnitId}/plan`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to add word to learning plan.'));
  }

  const result = (await response.json()) as ApiResponse<LearningPlanAddWordResult>;
  return result.data;
}
