type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type AdminPlacementItem = {
  id: number;
  itemType: string;
  cefrLevel: string;
  difficultyScore: number;
  frequencyRank: number | null;
  frequencyBand: string | null;
  itemDiscrimination: number;
  guessingFactor: number;
  scenarioTag: string;
  targetSkill: string;
  abilityDimension: string;
  status: string;
  gradingType: string;
  contentJson: string;
  createdAt: string;
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function fetchAdminPlacementItems(
  token: string,
  params: {
    abilityDimension?: string;
    status?: string;
    query?: string;
    page?: number;
    size?: number;
  }
): Promise<PageResponse<AdminPlacementItem>> {
  const search = new URLSearchParams();
  if (params.abilityDimension) {
    search.set('abilityDimension', params.abilityDimension);
  }
  if (params.status) {
    search.set('status', params.status);
  }
  if (params.query?.trim()) {
    search.set('query', params.query.trim());
  }
  search.set('page', String(params.page ?? 0));
  search.set('size', String(params.size ?? 20));

  const response = await fetch(`/api/admin/placement-items?${search.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to load placement items.'));
  }

  const result = (await response.json()) as ApiResponse<PageResponse<AdminPlacementItem>>;
  return result.data;
}
