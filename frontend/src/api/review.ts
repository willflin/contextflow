type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type ReviewPlanItem = {
  pool: 'REVIEW' | 'NEW';
  learningUnitId: number;
  learningUnitSenseId: number;
  canonicalText: string;
  senseKey: string;
  partOfSpeech: string | null;
  definitionEn: string;
  definitionZh: string | null;
  difficultyLevel: string | null;
  frequencyBand: string | null;
  score: number;
  scenarioCode: string;
  reasons: string[];
};

export type ReviewScenarioGroup = {
  scenarioCode: string;
  targetSenseIds: number[];
  targets: ReviewPlanItem[];
};

export type ReviewPlan = {
  userLevel: string;
  limit: number;
  reviewTargetCount: number;
  newTargetCount: number;
  overdueReviewCount: number;
  priorityBasis: string;
  items: ReviewPlanItem[];
  scenarioGroups: ReviewScenarioGroup[];
  generatedAt: string;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchReviewPlan(token: string, limit = 12): Promise<ReviewPlan> {
  const response = await fetch(`/api/review/plan?limit=${limit}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load review plan.'));
  }

  const result = (await response.json()) as ApiResponse<ReviewPlan>;
  return result.data;
}
