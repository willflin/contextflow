import { useEffect, useState } from 'react';
import { AccessProbe, fetchAccessProbe } from './api/access';
import { CurrentUser, fetchCurrentUser, login, register } from './api/auth';
import { fetchHealth, HealthStatus } from './api/health';

type LoadState = 'idle' | 'loading' | 'success' | 'error';
type AuthMode = 'login' | 'register';

export default function App() {
  const [state, setState] = useState<LoadState>('idle');
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [username, setUsername] = useState('learner');
  const [password, setPassword] = useState('learner123');
  const [displayName, setDisplayName] = useState('New Learner');
  const [error, setError] = useState<string | null>(null);
  const [authError, setAuthError] = useState<string | null>(null);
  const [accessProbe, setAccessProbe] = useState<AccessProbe | null>(null);
  const [accessError, setAccessError] = useState<string | null>(null);

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
      setAccessProbe(null);
    }
  }

  async function handleLogin(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAuthError(null);

    try {
      const response =
        authMode === 'login'
          ? await login(username, password)
          : await register(username, password, displayName);
      window.localStorage.setItem('contextflow_token', response.token);
      setCurrentUser(response.user);
      setAccessProbe(null);
      setAccessError(null);
    } catch (exception) {
      setAuthError(exception instanceof Error ? exception.message : 'Login failed.');
      setCurrentUser(null);
    }
  }

  function handleLogout() {
    window.localStorage.removeItem('contextflow_token');
    setCurrentUser(null);
    setAccessProbe(null);
    setAccessError(null);
  }

  async function checkProtectedEndpoint(path: '/api/learner/probe' | '/api/admin/probe') {
    const token = window.localStorage.getItem('contextflow_token');

    if (!token) {
      setAccessError('Please log in first.');
      return;
    }

    try {
      const result = await fetchAccessProbe(path, token);
      setAccessProbe(result);
      setAccessError(null);
    } catch (exception) {
      setAccessProbe(null);
      setAccessError(exception instanceof Error ? exception.message : 'Access check failed.');
    }
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
          <p className="eyebrow">Phase 1.3</p>
          <h2>{authMode === 'login' ? 'JWT login' : 'Learner registration'}</h2>
          <p className="hint">
            {authMode === 'login'
              ? 'Try learner / learner123 or admin / admin123.'
              : 'New users are always registered as LEARNER.'}
          </p>
        </div>

        {currentUser ? (
          <div className="current-user">
            <span className="label">Signed in as</span>
            <strong>{currentUser.displayName}</strong>
            <p>{currentUser.role}</p>
            <button className="secondary-button" type="button" onClick={handleLogout}>
              Log out
            </button>
            <div className="access-actions">
              <button
                className="secondary-button"
                type="button"
                onClick={() => checkProtectedEndpoint('/api/learner/probe')}
              >
                Check learner API
              </button>
              <button
                className="secondary-button"
                type="button"
                onClick={() => checkProtectedEndpoint('/api/admin/probe')}
              >
                Check admin API
              </button>
            </div>
            {accessProbe && (
              <p className="success">
                {accessProbe.scope}: {accessProbe.message}
              </p>
            )}
            {accessError && <p className="error compact">Access denied: {accessError}</p>}
          </div>
        ) : (
          <form className="login-form" onSubmit={handleLogin}>
            <div className="auth-mode">
              <button
                className={authMode === 'login' ? 'mode-button active' : 'mode-button'}
                type="button"
                onClick={() => setAuthMode('login')}
              >
                Login
              </button>
              <button
                className={authMode === 'register' ? 'mode-button active' : 'mode-button'}
                type="button"
                onClick={() => setAuthMode('register')}
              >
                Register
              </button>
            </div>
            <label>
              Username
              <input value={username} onChange={(event) => setUsername(event.target.value)} />
            </label>
            {authMode === 'register' && (
              <label>
                Display name
                <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} />
              </label>
            )}
            <label>
              Password
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </label>
            {authError && <p className="error compact">Auth failed: {authError}</p>}
            <button className="refresh-button compact-button" type="submit">
              {authMode === 'login' ? 'Log in' : 'Create learner account'}
            </button>
          </form>
        )}
      </section>
    </main>
  );
}
