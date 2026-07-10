export type UserRole = 'LEARNER' | 'ADMIN';

export type CurrentUser = {
  username: string;
  displayName: string;
  role: UserRole;
};

export type LoginResponse = {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  user: CurrentUser;
};

type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

async function readErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as ApiResponse<unknown>;
    return result.message || fallback;
  } catch {
    return fallback;
  }
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Invalid username or password.'));
  }

  const result = (await response.json()) as ApiResponse<LoginResponse>;
  return result.data;
}

export async function register(
  username: string,
  password: string,
  displayName: string
): Promise<LoginResponse> {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password, displayName })
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Registration failed.'));
  }

  const result = (await response.json()) as ApiResponse<LoginResponse>;
  return result.data;
}

export async function fetchCurrentUser(token: string): Promise<CurrentUser> {
  const response = await fetch('/api/auth/me', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Current user request failed.'));
  }

  const result = (await response.json()) as ApiResponse<CurrentUser>;
  return result.data;
}
