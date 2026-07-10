import { useEffect, useState } from 'react';
import { CurrentUser, fetchCurrentUser, login } from './api/auth';
import { fetchHealth, HealthStatus } from './api/health';

type LoadState = 'idle' | 'loading' | 'success' | 'error';

export default function App() {
  const [state, setState] = useState<LoadState>('idle');
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [username, setUsername] = useState('learner');
  const [password, setPassword] = useState('learner123');
  const [error, setError] = useState<string | null>(null);
  const [authError, setAuthError] = useState<string | null>(null);

  async function loadHealth() {
    setState('loading');
    setError(null);

    try {
      const result = await fetchHealth();
      setHealth(result.data);
      setState('success');
    } catch (exception) {
      setHealth(null);
      setError(exception instanceof Error ? exception.message : 'Unknown error');
      setState('error');
    }
  }

  useEffect(() => {
    void loadHealth();
    void restoreCurrentUser();
  }, []);

  async function restoreCurrentUser() {
    const token = window.localStorage.getItem('contextflow_token');

    if (!token) {
      return;
    }

    try {
      const user = await fetchCurrentUser(token);
      setCurrentUser(user);
    } catch {
      window.localStorage.removeItem('contextflow_token');
      setCurrentUser(null);
    }
  }

  async function handleLogin(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAuthError(null);

    try {
      const response = await login(username, password);
      window.localStorage.setItem('contextflow_token', response.token);
      setCurrentUser(response.user);
    } catch (exception) {
      setAuthError(exception instanceof Error ? exception.message : 'Login failed.');
      setCurrentUser(null);
    }
  }

  function handleLogout() {
    window.localStorage.removeItem('contextflow_token');
    setCurrentUser(null);
  }

  return (
    <main className="app-shell">
      <section className="hero">
        <p className="eyebrow">ContextFlow</p>
        <h1>AI contextual English learning</h1>
        <p className="summary">
          Phase 0 verifies the basic frontend and backend connection.
        </p>
      </section>

      <section className="status-panel">
        <div>
          <span className="label">Backend status</span>
          <strong>{state === 'success' ? health?.status : state}</strong>
        </div>
        <div>
          <span className="label">Service</span>
          <strong>{health?.service ?? '-'}</strong>
        </div>
        <div>
          <span className="label">Version</span>
          <strong>{health?.version ?? '-'}</strong>
        </div>
      </section>

      {error && <p className="error">Backend request failed: {error}</p>}

      <button className="refresh-button" type="button" onClick={loadHealth}>
        Refresh health check
      </button>

      <section className="login-panel">
        <div>
          <p className="eyebrow">Phase 1.1</p>
          <h2>Mock login</h2>
          <p className="hint">Try learner / learner123 or admin / admin123.</p>
        </div>

        {currentUser ? (
          <div className="current-user">
            <span className="label">Signed in as</span>
            <strong>{currentUser.displayName}</strong>
            <p>{currentUser.role}</p>
            <button className="secondary-button" type="button" onClick={handleLogout}>
              Log out
            </button>
          </div>
        ) : (
          <form className="login-form" onSubmit={handleLogin}>
            <label>
              Username
              <input value={username} onChange={(event) => setUsername(event.target.value)} />
            </label>
            <label>
              Password
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </label>
            {authError && <p className="error compact">Login failed: {authError}</p>}
            <button className="refresh-button compact-button" type="submit">
              Log in
            </button>
          </form>
        )}
      </section>
    </main>
  );
}
