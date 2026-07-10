import { useEffect, useState } from 'react';
import { fetchHealth, HealthStatus } from './api/health';

type LoadState = 'idle' | 'loading' | 'success' | 'error';

export default function App() {
  const [state, setState] = useState<LoadState>('idle');
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [error, setError] = useState<string | null>(null);

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
  }, []);

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
    </main>
  );
}

