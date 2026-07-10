import { useEffect, useState } from 'react';
import { AccessProbe, fetchAccessProbe } from './api/access';
import { CurrentUser, fetchCurrentUser, login, register } from './api/auth';
import { fetchHealth, HealthStatus } from './api/health';
import {
  fetchNextLearningPackage,
  LearningPackage,
  parseLearningPackageContent
} from './api/learning';
import {
  answerAdaptivePlacement,
  AdaptivePlacementSession,
  parsePlacementItemContent,
  PlacementSessionResult,
  PlacementTestItem,
  startAdaptivePlacement
} from './api/placement';
import { fetchUserLevelProfile, prettyJson, UserLevelProfile } from './api/userProfile';

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
  const [placementSession, setPlacementSession] = useState<AdaptivePlacementSession | null>(null);
  const [currentItem, setCurrentItem] = useState<PlacementTestItem | null>(null);
  const [selectedOptionIndex, setSelectedOptionIndex] = useState<number | null>(null);
  const [textAnswer, setTextAnswer] = useState('');
  const [placementBusy, setPlacementBusy] = useState(false);
  const [placementError, setPlacementError] = useState<string | null>(null);
  const [placementResult, setPlacementResult] = useState<PlacementSessionResult | null>(null);
  const [answerLog, setAnswerLog] = useState<string[]>([]);
  const [profile, setProfile] = useState<UserLevelProfile | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [learningPackage, setLearningPackage] = useState<LearningPackage | null>(null);
  const [learningBusy, setLearningBusy] = useState(false);
  const [learningError, setLearningError] = useState<string | null>(null);

  const isAdmin = currentUser?.role === 'ADMIN';
  const itemContent = currentItem ? parsePlacementItemContent(currentItem) : null;
  const options = itemContent?.options ?? [];

  useEffect(() => {
    void restoreCurrentUser();
  }, []);

  useEffect(() => {
    if (currentUser) {
      void loadProfile();
      if (currentUser.role === 'ADMIN') {
        void loadHealth();
      }
    }
  }, [currentUser]);

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

  async function restoreCurrentUser() {
    const token = tokenOrNull();

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
      resetPlacement();
      resetLearning();
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
    setProfile(null);
    setProfileError(null);
    resetPlacement();
    resetLearning();
  }

  async function checkProtectedEndpoint(path: '/api/learner/probe' | '/api/admin/probe') {
    const token = tokenOrNull();

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

  async function startPlacement() {
    const token = tokenOrNull();

    if (!token) {
      setPlacementError('Please log in first.');
      return;
    }

    setPlacementBusy(true);
    setPlacementError(null);
    setPlacementResult(null);
    setAnswerLog([]);
    setSelectedOptionIndex(null);
    setTextAnswer('');

    try {
      const session = await startAdaptivePlacement(token);
      setPlacementSession(session);
      setCurrentItem(session.currentItem);
    } catch (exception) {
      setPlacementError(exception instanceof Error ? exception.message : 'Failed to start placement test.');
    } finally {
      setPlacementBusy(false);
    }
  }

  async function submitCurrentAnswer() {
    const token = tokenOrNull();

    if (!token || !placementSession || !currentItem) {
      setPlacementError('No active placement item.');
      return;
    }

    if (options.length > 0 && selectedOptionIndex === null) {
      setPlacementError('Please choose an option.');
      return;
    }

    setPlacementBusy(true);
    setPlacementError(null);

    try {
      const response = await answerAdaptivePlacement(
        token,
        placementSession.sessionId,
        currentItem.id,
        selectedOptionIndex,
        textAnswer
      );

      setAnswerLog((previous) => [
        `Item ${response.itemId}: ${response.correct ? 'correct' : 'wrong'} - difficulty ${response.currentDifficultyScore}`,
        ...previous
      ]);
      setSelectedOptionIndex(null);
      setTextAnswer('');

      if (response.finished) {
        setCurrentItem(null);
        setPlacementResult(response.result);
        await loadProfile();
      } else {
        setCurrentItem(response.nextItem);
        setPlacementSession((previous) =>
          previous
            ? {
                ...previous,
                answeredCount: response.answeredCount,
                currentDifficultyScore: response.currentDifficultyScore,
                currentItem: response.nextItem ?? previous.currentItem
              }
            : previous
        );
      }
    } catch (exception) {
      setPlacementError(exception instanceof Error ? exception.message : 'Failed to submit answer.');
    } finally {
      setPlacementBusy(false);
    }
  }

  async function loadProfile() {
    const token = tokenOrNull();

    if (!token) {
      return;
    }

    try {
      const result = await fetchUserLevelProfile(token);
      setProfile(result);
      setProfileError(null);
    } catch (exception) {
      setProfile(null);
      setProfileError(exception instanceof Error ? exception.message : 'Profile is not generated yet.');
    }
  }

  function resetPlacement() {
    setPlacementSession(null);
    setCurrentItem(null);
    setSelectedOptionIndex(null);
    setTextAnswer('');
    setPlacementError(null);
    setPlacementResult(null);
    setAnswerLog([]);
  }

  async function loadNextLearningPackage() {
    const token = tokenOrNull();

    if (!token) {
      setLearningError('Please log in first.');
      return;
    }

    setLearningBusy(true);
    setLearningError(null);

    try {
      const result = await fetchNextLearningPackage(token);
      setLearningPackage(result);
    } catch (exception) {
      setLearningPackage(null);
      setLearningError(exception instanceof Error ? exception.message : 'Failed to load learning package.');
    } finally {
      setLearningBusy(false);
    }
  }

  function resetLearning() {
    setLearningPackage(null);
    setLearningError(null);
    setLearningBusy(false);
  }

  function tokenOrNull() {
    return window.localStorage.getItem('contextflow_token');
  }

  return (
    <main className="app-shell">
      <section className="hero">
        <p className="eyebrow">ContextFlow</p>
        <h1>Placement-first English learning</h1>
        <p className="summary">
          Start with a contextual placement test, then build a learner profile for later AI-generated study.
        </p>
      </section>

      {currentUser ? (
        <>
          <section className="account-bar">
            <div>
              <span className="label">Signed in as</span>
              <strong>{currentUser.displayName}</strong>
              <p>{currentUser.role}</p>
            </div>
            <button className="secondary-button" type="button" onClick={handleLogout}>
              Log out
            </button>
          </section>

          {isAdmin ? renderAdminConsole() : renderLearnerExperience()}
        </>
      ) : (
        renderAuthPanel()
      )}
    </main>
  );

  function renderAuthPanel() {
    return (
      <section className="login-panel">
        <div>
          <p className="eyebrow">Auth</p>
          <h2>{authMode === 'login' ? 'JWT login' : 'Learner registration'}</h2>
          <p className="hint">
            {authMode === 'login'
              ? 'Use learner / learner123 for the learner flow, or admin / admin123 for admin tools.'
              : 'New users are always registered as LEARNER.'}
          </p>
        </div>

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
      </section>
    );
  }

  function renderLearnerExperience() {
    return (
      <section className="learner-grid">
        <section className="tool-panel primary-flow">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Placement</p>
              <h2>Find your starting level</h2>
            </div>
            <button className="secondary-button" type="button" onClick={startPlacement} disabled={placementBusy}>
              Start test
            </button>
          </div>
          {renderPlacementQuestion(false)}
        </section>

        <section className="tool-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Profile</p>
              <h2>Your level</h2>
            </div>
            <button className="secondary-button" type="button" onClick={loadProfile}>
              Refresh
            </button>
          </div>
          {renderLearnerProfile()}
        </section>

        <section className="tool-panel learning-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Learning</p>
              <h2>Next scenario</h2>
            </div>
            <button
              className="secondary-button"
              type="button"
              onClick={loadNextLearningPackage}
              disabled={learningBusy || !profile}
            >
              Start learning
            </button>
          </div>
          {renderLearningPackage()}
        </section>
      </section>
    );
  }

  function renderAdminConsole() {
    return (
      <section className="admin-console">
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

        <section className="debug-grid">
          <section className="tool-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Admin Debug</p>
                <h2>Access probes</h2>
              </div>
            </div>
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
          </section>

          <section className="tool-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Admin Debug</p>
                <h2>Placement sandbox</h2>
              </div>
              <button className="secondary-button" type="button" onClick={startPlacement} disabled={placementBusy}>
                Start test
              </button>
            </div>
            {renderPlacementQuestion(true)}
          </section>

          <section className="tool-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Admin Debug</p>
                <h2>Raw profile</h2>
              </div>
              <button className="secondary-button" type="button" onClick={loadProfile}>
                Refresh profile
              </button>
            </div>
            {renderRawProfile()}
          </section>
        </section>
      </section>
    );
  }

  function renderPlacementQuestion(showDebug: boolean) {
    return (
      <>
        {placementSession && showDebug && (
          <div className="metrics-row">
            <div>
              <span className="label">Session</span>
              <strong>{placementSession.sessionId}</strong>
            </div>
            <div>
              <span className="label">Answered</span>
              <strong>{placementSession.answeredCount}</strong>
            </div>
            <div>
              <span className="label">Difficulty</span>
              <strong>{placementSession.currentDifficultyScore}</strong>
            </div>
          </div>
        )}

        {!currentItem && !placementResult && (
          <p className="hint">Start the placement test when you are ready.</p>
        )}

        {currentItem && itemContent && (
          <div className="question-panel">
            {showDebug && (
              <div className="question-meta">
                <span>{currentItem.itemType}</span>
                <span>{currentItem.cefrLevel}</span>
                <span>Difficulty {currentItem.difficultyScore}</span>
              </div>
            )}
            <h3>{itemContent.question ?? 'Question'}</h3>
            {options.length > 0 ? (
              <div className="option-list">
                {options.map((option, index) => (
                  <label className="option-item" key={option}>
                    <input
                      type="radio"
                      name="placement-option"
                      checked={selectedOptionIndex === index}
                      onChange={() => setSelectedOptionIndex(index)}
                    />
                    <span>{option}</span>
                  </label>
                ))}
              </div>
            ) : (
              <label className="text-answer">
                Answer
                <input value={textAnswer} onChange={(event) => setTextAnswer(event.target.value)} />
              </label>
            )}
            <button
              className="refresh-button compact-button"
              type="button"
              onClick={submitCurrentAnswer}
              disabled={placementBusy}
            >
              Submit answer
            </button>
          </div>
        )}

        {placementResult && (
          <div className="result-panel">
            <p className="success">Placement finished.</p>
            <div className="metrics-row">
              <div>
                <span className="label">Score</span>
                <strong>{placementResult.scorePercent}%</strong>
              </div>
              <div>
                <span className="label">Correct</span>
                <strong>
                  {placementResult.correctCount}/{placementResult.itemCount}
                </strong>
              </div>
              <div>
                <span className="label">Estimated level</span>
                <strong>{placementResult.estimatedLevel}</strong>
              </div>
            </div>
          </div>
        )}

        {placementError && <p className="error compact">Placement failed: {placementError}</p>}

        {showDebug && answerLog.length > 0 && (
          <ul className="answer-log">
            {answerLog.map((entry) => (
              <li key={entry}>{entry}</li>
            ))}
          </ul>
        )}
      </>
    );
  }

  function renderLearnerProfile() {
    if (!profile) {
      return <p className="hint">{profileError ?? 'Finish the placement test to generate your profile.'}</p>;
    }

    return (
      <div className="profile-content">
        <div className="level-badge">{profile.cefrLevel}</div>
        <p className="hint">Your learning path will use this level and your weak areas.</p>
      </div>
    );
  }

  function renderRawProfile() {
    if (!profile) {
      return <p className="hint">{profileError ?? 'No profile has been generated for this account.'}</p>;
    }

    return (
      <div className="profile-content">
        <div className="metrics-row">
          <div>
            <span className="label">CEFR</span>
            <strong>{profile.cefrLevel}</strong>
          </div>
          <div>
            <span className="label">Source session</span>
            <strong>{profile.lastPlacementSessionId ?? '-'}</strong>
          </div>
        </div>
        <label>
          Dimension scores
          <pre>{prettyJson(profile.dimensionScoresJson)}</pre>
        </label>
        <label>
          Weak scenarios
          <pre>{prettyJson(profile.weakScenariosJson)}</pre>
        </label>
        <label>
          Weak abilities
          <pre>{prettyJson(profile.weakAbilitiesJson)}</pre>
        </label>
      </div>
    );
  }

  function renderLearningPackage() {
    if (!profile) {
      return <p className="hint">Finish the placement test first. Your learning scenario will use your profile.</p>;
    }

    if (learningError) {
      return <p className="error compact">Learning package failed: {learningError}</p>;
    }

    if (!learningPackage) {
      return <p className="hint">Your next READY learning scenario will appear here.</p>;
    }

    const content = parseLearningPackageContent(learningPackage);

    return (
      <div className="learning-content">
        <div className="scenario-header">
          <div>
            <span className="label">Scenario</span>
            <strong>{learningPackage.scenarioName}</strong>
          </div>
          <span className="status-pill">{learningPackage.status}</span>
        </div>

        {content.scenario?.description && <p className="hint">{content.scenario.description}</p>}

        {content.goals && content.goals.length > 0 && (
          <section>
            <h3>Goals</h3>
            <ul>
              {content.goals.map((goal) => (
                <li key={goal}>{goal}</li>
              ))}
            </ul>
          </section>
        )}

        {content.roleplayAgent?.openingLine && (
          <section className="dialogue-preview">
            <span className="label">Roleplay opening</span>
            <p>{content.roleplayAgent.openingLine}</p>
          </section>
        )}

        {content.expressions && content.expressions.length > 0 && (
          <section>
            <h3>Useful expressions</h3>
            <div className="expression-list">
              {content.expressions.map((expression) => (
                <div className="expression-item" key={expression.phrase}>
                  <strong>{expression.phrase}</strong>
                  <p>{expression.meaning}</p>
                </div>
              ))}
            </div>
          </section>
        )}

        {content.practice && content.practice.length > 0 && (
          <section>
            <h3>Practice</h3>
            <ul>
              {content.practice.map((item) => (
                <li key={item.prompt}>{item.prompt}</li>
              ))}
            </ul>
          </section>
        )}
      </div>
    );
  }
}
