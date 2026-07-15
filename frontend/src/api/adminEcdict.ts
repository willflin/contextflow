type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type EcdictImportResult = {
  batchId: number;
  fileName: string;
  fileSha256: string;
  loadedRows: number;
  cleanRows: number;
  learningUnitRows: number;
  learningSenseRows: number;
  reusedExistingBatch: boolean;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function importLocalEcdict(token: string): Promise<EcdictImportResult> {
  const response = await fetch('/api/admin/ecdict/import-local', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Failed to import ECDICT.'));
  }

  const result = (await response.json()) as ApiResponse<EcdictImportResult>;
  return result.data;
}
