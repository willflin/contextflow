type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type PlacementItemType =
  | 'SCENE_DIALOGUE_CHOICE'
  | 'CONTEXT_MEANING'
  | 'POLITENESS_JUDGMENT'
  | 'LISTENING_COMPREHENSION'
  | 'EXPRESSION_COMPLETION'
  | 'INTENT_UNDERSTANDING'
  | 'TRUE_FALSE'
  | 'SYNONYM_CHOICE'
  | 'ANTONYM_CHOICE'
  | 'CLOZE_TEXT';

export type PlacementGradingType = 'LOCAL_EXACT' | 'LOCAL_ACCEPTED_ANSWERS' | 'AI_JUDGE';

export type PlacementTestItem = {
  id: number;
  itemType: PlacementItemType;
  cefrLevel: string;
  difficultyScore: number;
  scenarioTag: string;
  targetSkill: string;
  abilityDimension: string;
  gradingType: PlacementGradingType;
  contentJson: string;
};

export type AdaptivePlacementSession = {
  sessionId: number;
  status: 'STARTED' | 'SUBMITTED' | 'EXPIRED';
  answeredCount: number;
  maxItemCount: number;
  currentDifficultyScore: number;
  currentItem: PlacementTestItem;
};

export type PlacementSessionResult = {
  sessionId: number;
  itemCount: number;
  answeredCount: number;
  correctCount: number;
  scorePercent: number;
  estimatedLevel: string;
};

export type AdaptivePlacementAnswer = {
  sessionId: number;
  itemId: number;
  correct: boolean;
  answeredCount: number;
  correctCount: number;
  currentDifficultyScore: number;
  finished: boolean;
  nextItem: PlacementTestItem | null;
  result: PlacementSessionResult | null;
};

export type PlacementItemContent = {
  question?: string;
  options?: string[];
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function startAdaptivePlacement(token: string): Promise<AdaptivePlacementSession> {
  const response = await fetch('/api/placement/session/adaptive/start', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to start placement test.'));
  }

  const result = (await response.json()) as ApiResponse<AdaptivePlacementSession>;
  return result.data;
}

export async function answerAdaptivePlacement(
  token: string,
  sessionId: number,
  itemId: number,
  selectedOptionIndex: number | null,
  textAnswer: string
): Promise<AdaptivePlacementAnswer> {
  const body =
    selectedOptionIndex === null
      ? { itemId, textAnswer }
      : { itemId, selectedOptionIndex, textAnswer: textAnswer || undefined };

  const response = await fetch(`/api/placement/session/${sessionId}/answer`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to submit placement answer.'));
  }

  const result = (await response.json()) as ApiResponse<AdaptivePlacementAnswer>;
  return result.data;
}

export function parsePlacementItemContent(item: PlacementTestItem): PlacementItemContent {
  try {
    return JSON.parse(item.contentJson) as PlacementItemContent;
  } catch {
    return { question: 'Invalid item content.', options: [] };
  }
}
