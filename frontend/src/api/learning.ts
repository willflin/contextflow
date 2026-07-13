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
  learningTask?: {
    goal?: string;
    instructionLanguage?: string;
    expectedLearnerAction?: string;
    facts?: Record<string, string>;
    constraints?: string[];
    source?: string;
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
    persona?: string;
    learnerRole?: string;
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

export type DialogueCorrection = {
  original: string;
  suggestion: string;
  reason: string;
};

export type LearningDialogueTurn = {
  turnId: number;
  packageId: number;
  turnIndex: number;
  userMessage: string;
  roleplayReply: string;
  mentorFeedback: string;
  corrections: DialogueCorrection[];
  naturalExpression: string;
  scoringSignal: Record<string, unknown>;
  createdAt: string;
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

export async function sendLearningDialogueMessage(
  token: string,
  packageId: number,
  message: string
): Promise<LearningDialogueTurn> {
  const response = await fetch(`/api/learning/packages/${packageId}/dialog`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ message })
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to send dialogue message.'));
  }

  const result = (await response.json()) as ApiResponse<LearningDialogueTurn>;
  return result.data;
}

export function parseLearningPackageContent(learningPackage: LearningPackage): LearningPackageContent {
  try {
    return JSON.parse(learningPackage.contentJson) as LearningPackageContent;
  } catch {
    return {};
  }
}
