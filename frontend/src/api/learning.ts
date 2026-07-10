type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type LearningPackage = {
  id: number;
  status: 'PENDING' | 'GENERATING_TEXT' | 'VALIDATING_TEXT' | 'GENERATING_AUDIO' | 'READY' | 'FAILED' | 'EXPIRED' | 'COMPLETED';
  title: string;
  scenarioName: string;
  contentJson: string;
  assignedAt: string | null;
};

export type LearningPackageContent = {
  scenario?: {
    code?: string;
    name?: string;
    description?: string;
  };
  learnerProfile?: {
    cefrLevel?: string;
    dimensionScores?: Record<string, number>;
    weakScenarios?: string[];
    weakAbilities?: string[];
  };
  goals?: string[];
  roleplayAgent?: {
    role?: string;
    openingLine?: string;
  };
  mentorAgent?: {
    focus?: string;
    language?: string;
  };
  expressions?: Array<{
    phrase?: string;
    meaning?: string;
  }>;
  practice?: Array<{
    prompt?: string;
    expectedAction?: string;
  }>;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchNextLearningPackage(token: string): Promise<LearningPackage> {
  const response = await fetch('/api/learning/packages/next', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load learning package.'));
  }

  const result = (await response.json()) as ApiResponse<LearningPackage>;
  return result.data;
}

export function parseLearningPackageContent(learningPackage: LearningPackage): LearningPackageContent {
  try {
    return JSON.parse(learningPackage.contentJson) as LearningPackageContent;
  } catch {
    return {};
  }
}
