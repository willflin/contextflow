import { ReactNode, useEffect, useRef, useState } from 'react';
import { AccessProbe, fetchAccessProbe } from './api/access';
import { AgentRuntimeProbe, AgentRuntimeStatus, fetchAgentRuntimeStatus, runAgentRuntimeProbe } from './api/agentRuntime';
import {
  AdminSenseUpdatePayload,
  AdminWordPayload,
  AdminWordUpdatePayload,
  createAdminWord,
  deleteAdminWord,
  LearningUnitDetail,
  searchAdminWord,
  updateAdminSense,
  updateAdminWord
} from './api/adminWords';
import {
  AdminDialogueTurn,
  AdminDialogueTurnUpdatePayload,
  deleteAdminDialogue,
  fetchAdminDialogues,
  updateAdminDialogue
} from './api/adminDialogues';
import { EcdictImportResult, importLocalEcdict } from './api/adminEcdict';
import { AdminPlacementItem, fetchAdminPlacementItems } from './api/adminPlacementItems';
import { CurrentUser, fetchCurrentUser, login, register } from './api/auth';
import { fetchHealth, HealthStatus } from './api/health';
import {
  fetchNextLearningPackage,
  LearningDialogueTurn,
  LearningPackage,
  completeLearningPackage,
  parseLearningPackageContent,
  sendLearningDialogueMessage,
  skipLearningPackage
} from './api/learning';
import {
  answerAdaptivePlacement,
  AdaptivePlacementSession,
  parsePlacementItemContent,
  PlacementSessionResult,
  PlacementTestItem,
  startAdaptivePlacement
} from './api/placement';
import { fetchReviewPlan, ReviewPlan } from './api/review';
import { fetchUserLevelProfile, UserLevelProfile } from './api/userProfile';
import {
  addVocabularyWordToLearningPlan,
  fetchVocabulary,
  markLowLevelSensesMastered,
  VocabularyList,
  VocabularySortMode,
  VocabularyStatusFilter,
  VocabularyWord
} from './api/vocabulary';

type LoadState = 'idle' | 'loading' | 'success' | 'error';
type AuthMode = 'login' | 'register';
type LearnerView = 'home' | 'vocabulary';
type AdminView = 'home' | 'runtime' | 'words' | 'dialogues' | 'placementItems';
type ThemeMode = 'light' | 'dark';
type LowLevelMasteryDraft = {
  word: VocabularyWord;
  selectedSenseIds: number[];
};
const ADMIN_PAGE_SIZE = 20;
type MentorHint = {
  id: number;
  level: 'HINT' | 'PRECISE';
  text: string;
};
type TaskChecklistItem = {
  key: string;
  label: string;
  done: boolean;
};

const emptyAdminWordForm: AdminWordPayload = {
  canonicalText: '',
  partOfSpeech: 'NOUN',
  definitionEn: '',
  definitionZh: '',
  difficultyLevel: '',
  frequencyScore: null,
  frequencyBand: '',
  forms: []
};

const emptyAdminSenseForm: AdminSenseUpdatePayload = {
  partOfSpeech: '',
  definitionEn: '',
  definitionZh: '',
  difficultyLevel: '',
  difficultyConfidence: null,
  frequencyScore: null,
  frequencyBand: '',
  status: 'ACTIVE'
};

const emptyAdminDialogueForm: AdminDialogueTurnUpdatePayload = {
  userMessage: '',
  roleplayReply: '',
  mentorFeedback: '',
  corrections: '[]',
  naturalExpression: '',
  scoringSignal: '{}'
};

export default function App() {
  const [state, setState] = useState<LoadState>('idle');
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [learnerView, setLearnerView] = useState<LearnerView>('home');
  const [adminView, setAdminView] = useState<AdminView>('home');
  const [themeMode, setThemeMode] = useState<ThemeMode>(() => {
    const storedTheme = window.localStorage.getItem('contextflow_theme');
    if (storedTheme === 'light' || storedTheme === 'dark') {
      return storedTheme;
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });
  const [username, setUsername] = useState('learner');
  const [password, setPassword] = useState('learner123');
  const [displayName, setDisplayName] = useState('New Learner');
  const [error, setError] = useState<string | null>(null);
  const [authError, setAuthError] = useState<string | null>(null);
  const [accessProbe, setAccessProbe] = useState<AccessProbe | null>(null);
  const [accessError, setAccessError] = useState<string | null>(null);
  const [agentRuntime, setAgentRuntime] = useState<AgentRuntimeStatus | null>(null);
  const [agentRuntimeBusy, setAgentRuntimeBusy] = useState(false);
  const [agentRuntimeError, setAgentRuntimeError] = useState<string | null>(null);
  const [agentRuntimeProbe, setAgentRuntimeProbe] = useState<AgentRuntimeProbe | null>(null);
  const [agentRuntimeProbeBusy, setAgentRuntimeProbeBusy] = useState(false);
  const [agentRuntimeProbeError, setAgentRuntimeProbeError] = useState<string | null>(null);
  const [ecdictImportBusy, setEcdictImportBusy] = useState(false);
  const [ecdictImportResult, setEcdictImportResult] = useState<EcdictImportResult | null>(null);
  const [ecdictImportError, setEcdictImportError] = useState<string | null>(null);
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
  const [reviewPlan, setReviewPlan] = useState<ReviewPlan | null>(null);
  const [reviewBusy, setReviewBusy] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);
  const [vocabulary, setVocabulary] = useState<VocabularyList | null>(null);
  const [vocabularyQuery, setVocabularyQuery] = useState('');
  const [vocabularyStatus, setVocabularyStatus] = useState<VocabularyStatusFilter>('ALL');
  const [vocabularySort, setVocabularySort] = useState<VocabularySortMode>('AUTO');
  const [vocabularyPage, setVocabularyPage] = useState(0);
  const [vocabularyPageSize, setVocabularyPageSize] = useState(100);
  const [vocabularyBusy, setVocabularyBusy] = useState(false);
  const [vocabularyError, setVocabularyError] = useState<string | null>(null);
  const [learningPlanBusyWordId, setLearningPlanBusyWordId] = useState<number | null>(null);
  const [learningPlanMessage, setLearningPlanMessage] = useState<string | null>(null);
  const [lowLevelMasteryDraft, setLowLevelMasteryDraft] = useState<LowLevelMasteryDraft | null>(null);
  const [lowLevelMasteryBusy, setLowLevelMasteryBusy] = useState(false);
  const [adminWordQuery, setAdminWordQuery] = useState('');
  const [adminWordDetail, setAdminWordDetail] = useState<LearningUnitDetail | null>(null);
  const [adminWordForm, setAdminWordForm] = useState<AdminWordPayload>(emptyAdminWordForm);
  const [adminWordUpdateForm, setAdminWordUpdateForm] = useState<AdminWordUpdatePayload>({
    canonicalText: '',
    status: 'ACTIVE'
  });
  const [adminSenseForm, setAdminSenseForm] = useState<AdminSenseUpdatePayload>(emptyAdminSenseForm);
  const [adminWordBusy, setAdminWordBusy] = useState(false);
  const [adminWordError, setAdminWordError] = useState<string | null>(null);
  const [adminDialoguePackageId, setAdminDialoguePackageId] = useState('');
  const [adminDialogues, setAdminDialogues] = useState<AdminDialogueTurn[]>([]);
  const [adminDialoguePage, setAdminDialoguePage] = useState(0);
  const [adminDialogueTotalPages, setAdminDialogueTotalPages] = useState(0);
  const [adminDialogueTotalItems, setAdminDialogueTotalItems] = useState(0);
  const [adminDialogueSelected, setAdminDialogueSelected] = useState<AdminDialogueTurn | null>(null);
  const [adminDialogueForm, setAdminDialogueForm] = useState<AdminDialogueTurnUpdatePayload>(emptyAdminDialogueForm);
  const [adminDialogueBusy, setAdminDialogueBusy] = useState(false);
  const [adminDialogueError, setAdminDialogueError] = useState<string | null>(null);
  const [adminPlacementItems, setAdminPlacementItems] = useState<AdminPlacementItem[]>([]);
  const [adminPlacementPage, setAdminPlacementPage] = useState(0);
  const [adminPlacementTotalPages, setAdminPlacementTotalPages] = useState(0);
  const [adminPlacementTotalItems, setAdminPlacementTotalItems] = useState(0);
  const [adminPlacementQuery, setAdminPlacementQuery] = useState('');
  const [adminPlacementAbility, setAdminPlacementAbility] = useState('vocabulary_size');
  const [adminPlacementStatus, setAdminPlacementStatus] = useState('READY');
  const [adminPlacementMinDifficulty, setAdminPlacementMinDifficulty] = useState('');
  const [adminPlacementMaxDifficulty, setAdminPlacementMaxDifficulty] = useState('');
  const [adminPlacementBusy, setAdminPlacementBusy] = useState(false);
  const [adminPlacementError, setAdminPlacementError] = useState<string | null>(null);
  const [learningPackage, setLearningPackage] = useState<LearningPackage | null>(null);
  const [learningBusy, setLearningBusy] = useState(false);
  const [skipBusy, setSkipBusy] = useState(false);
  const [completeBusy, setCompleteBusy] = useState(false);
  const [learningError, setLearningError] = useState<string | null>(null);
  const [dialogueTurns, setDialogueTurns] = useState<LearningDialogueTurn[]>([]);
  const [dialogueMessage, setDialogueMessage] = useState('');
  const [dialogueBusy, setDialogueBusy] = useState(false);
  const [dialogueError, setDialogueError] = useState<string | null>(null);
  const [mentorHints, setMentorHints] = useState<MentorHint[]>([]);
  const [mentorHintCount, setMentorHintCount] = useState(0);
  const [mentorHintBusy, setMentorHintBusy] = useState(false);
  const [preciseHintPromptVisible, setPreciseHintPromptVisible] = useState(false);
  const [preciseHintUnlocked, setPreciseHintUnlocked] = useState(false);
  const [completionPromptVisible, setCompletionPromptVisible] = useState(false);
  const [completionReason, setCompletionReason] = useState('');
  const mentorHintTimerRef = useRef<number | null>(null);

  const isAdmin = currentUser?.role === 'ADMIN';
  const itemContent = currentItem ? parsePlacementItemContent(currentItem) : null;
  const options = itemContent?.options ?? [];

  useEffect(() => {
    void restoreCurrentUser();
  }, []);

  useEffect(() => () => clearMentorHintTimer(), []);

  useEffect(() => {
    document.documentElement.dataset.theme = themeMode;
    window.localStorage.setItem('contextflow_theme', themeMode);
  }, [themeMode]);

  useEffect(() => {
    if (currentUser) {
      if (currentUser.role === 'ADMIN') {
        void loadHealth();
        void loadAgentRuntime();
      } else {
        void loadProfile();
      }
    }
  }, [currentUser]);

  useEffect(() => {
    if (!learningPackage || dialogueBusy || dialogueMessage.trim()) {
      return;
    }
    const timeoutId = window.setTimeout(() => {
      setPreciseHintUnlocked(true);
      setPreciseHintPromptVisible(true);
    }, 180000);
    return () => window.clearTimeout(timeoutId);
  }, [learningPackage?.id, dialogueTurns.length, dialogueBusy, dialogueMessage]);

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
      resetReviewPlan();
      resetVocabulary();
      resetAdminWords();
      resetAdminDialogues();
      resetAdminPlacementItems();
      resetAgentRuntime();
      setLearnerView('home');
      setAdminView('home');
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
    resetVocabulary();
    resetAdminWords();
    resetAdminDialogues();
    resetAdminPlacementItems();
    resetPlacement();
    resetLearning();
    resetReviewPlan();
    resetAgentRuntime();
    setLearnerView('home');
    setAdminView('home');
  }

  async function loadAgentRuntime() {
    const token = tokenOrNull();

    if (!token) {
      setAgentRuntimeError('请先登录。');
      return;
    }

    setAgentRuntimeBusy(true);
    setAgentRuntimeError(null);

    try {
      const result = await fetchAgentRuntimeStatus(token);
      setAgentRuntime(result);
    } catch (exception) {
      setAgentRuntime(null);
      setAgentRuntimeError(exception instanceof Error ? exception.message : 'Agent 运行时加载失败。');
    } finally {
      setAgentRuntimeBusy(false);
    }
  }

  async function probeAgentRuntime() {
    const token = tokenOrNull();

    if (!token) {
      setAgentRuntimeProbeError('请先登录。');
      return;
    }

    setAgentRuntimeProbeBusy(true);
    setAgentRuntimeProbeError(null);

    try {
      const result = await runAgentRuntimeProbe(token);
      setAgentRuntimeProbe(result);
      setAgentRuntime({
        provider: result.provider,
        springAiClientAvailable: result.springAiClientAvailable,
        fallbackToLocalOnError: result.fallbackToLocalOnError,
        contractVersion: result.contractVersion,
        diagnostics: result.diagnostics
      });
    } catch (exception) {
      setAgentRuntimeProbe(null);
      setAgentRuntimeProbeError(exception instanceof Error ? exception.message : 'Agent 探测失败。');
    } finally {
      setAgentRuntimeProbeBusy(false);
    }
  }

  function resetAgentRuntime() {
    setAgentRuntime(null);
    setAgentRuntimeBusy(false);
    setAgentRuntimeError(null);
    setAgentRuntimeProbe(null);
    setAgentRuntimeProbeBusy(false);
    setAgentRuntimeProbeError(null);
    setEcdictImportBusy(false);
    setEcdictImportResult(null);
    setEcdictImportError(null);
  }

  async function runEcdictImport() {
    const token = tokenOrNull();

    if (!token) {
      setEcdictImportError('请先登录。');
      return;
    }

    setEcdictImportBusy(true);
    setEcdictImportError(null);

    try {
      const result = await importLocalEcdict(token);
      setEcdictImportResult(result);
    } catch (exception) {
      setEcdictImportResult(null);
      setEcdictImportError(exception instanceof Error ? exception.message : 'ECDICT 导入失败。');
    } finally {
      setEcdictImportBusy(false);
    }
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

  async function loadReviewPlan() {
    const token = tokenOrNull();

    if (!token) {
      setReviewError('Please log in first.');
      return;
    }

    setReviewBusy(true);
    setReviewError(null);

    try {
      const result = await fetchReviewPlan(token);
      setReviewPlan(result);
    } catch (exception) {
      setReviewPlan(null);
      setReviewError(exception instanceof Error ? exception.message : 'Failed to load review plan.');
    } finally {
      setReviewBusy(false);
    }
  }

  function resetReviewPlan() {
    setReviewPlan(null);
    setReviewBusy(false);
    setReviewError(null);
  }

  async function loadVocabulary(
    page = vocabularyPage,
    pageSize = vocabularyPageSize,
    status = vocabularyStatus,
    sort = vocabularySort
  ) {
    const token = tokenOrNull();

    if (!token) {
      setVocabularyError('请先登录。');
      return;
    }

    setVocabularyBusy(true);
    setVocabularyError(null);

    try {
      const result = await fetchVocabulary(token, status, vocabularyQuery, page, pageSize, sort);
      setVocabularyPage(result.page);
      setVocabularyPageSize(result.pageSize);
      setVocabulary(result);
    } catch (exception) {
      setVocabulary(null);
      setVocabularyError(exception instanceof Error ? exception.message : '词库加载失败。');
    } finally {
      setVocabularyBusy(false);
    }
  }

  function resetVocabulary() {
    setVocabulary(null);
    setVocabularyQuery('');
    setVocabularyStatus('ALL');
    setVocabularySort('AUTO');
    setVocabularyPage(0);
    setVocabularyPageSize(100);
    setVocabularyBusy(false);
    setVocabularyError(null);
    setLearningPlanBusyWordId(null);
    setLearningPlanMessage(null);
    setLowLevelMasteryDraft(null);
    setLowLevelMasteryBusy(false);
  }

  async function addWordToLearningPlan(learningUnitId: number) {
    const token = tokenOrNull();

    if (!token) {
      setVocabularyError('请先登录。');
      return;
    }

    setLearningPlanBusyWordId(learningUnitId);
    setLearningPlanMessage(null);
    setVocabularyError(null);

    try {
      const result = await addVocabularyWordToLearningPlan(token, learningUnitId);
      setLearningPlanMessage(
        `${result.message} 加入 ${result.addedSenseCount} 个词义，剔除 ${result.skippedOutOfLevelCount} 个词义。`
      );
      await loadVocabulary(vocabularyPage);
    } catch (exception) {
      setVocabularyError(exception instanceof Error ? exception.message : '加入学习计划失败。');
    } finally {
      setLearningPlanBusyWordId(null);
    }
  }

  function openLowLevelMastery(word: VocabularyWord) {
    setLowLevelMasteryDraft({
      word,
      selectedSenseIds: word.senses
        .filter((sense) => sense.lowLevelCandidate)
        .map((sense) => sense.senseId)
    });
    setLearningPlanMessage(null);
    setVocabularyError(null);
  }

  function toggleLowLevelSense(senseId: number) {
    setLowLevelMasteryDraft((previous) => {
      if (!previous) {
        return previous;
      }
      const selected = new Set(previous.selectedSenseIds);
      if (selected.has(senseId)) {
        selected.delete(senseId);
      } else {
        selected.add(senseId);
      }
      return {
        ...previous,
        selectedSenseIds: Array.from(selected)
      };
    });
  }

  function selectAllLowLevelSenses() {
    setLowLevelMasteryDraft((previous) => previous
      ? {
          ...previous,
          selectedSenseIds: previous.word.senses
            .map((sense) => sense.senseId)
        }
      : previous);
  }

  async function markSelectedLowLevelSensesMastered() {
    const token = tokenOrNull();

    if (!token) {
      setVocabularyError('请先登录。');
      return;
    }
    if (!lowLevelMasteryDraft || lowLevelMasteryDraft.selectedSenseIds.length === 0) {
      setVocabularyError('请选择至少一个候选词义。');
      return;
    }

    setLowLevelMasteryBusy(true);
    setVocabularyError(null);

    try {
      const result = await markLowLevelSensesMastered(token, lowLevelMasteryDraft.selectedSenseIds);
      setLearningPlanMessage(`${result.message} 已标记 ${result.markedSenseCount} 个词义，跳过 ${result.skippedSenseCount} 个。`);
      setLowLevelMasteryDraft(null);
      await loadVocabulary(vocabularyPage);
    } catch (exception) {
      setVocabularyError(exception instanceof Error ? exception.message : '标记掌握失败。');
    } finally {
      setLowLevelMasteryBusy(false);
    }
  }

  async function openVocabularyPage() {
    setLearnerView('vocabulary');
    if (!vocabulary && !vocabularyBusy) {
      await loadVocabulary();
    }
  }

  async function applyVocabularyStatusFilter(status: VocabularyStatusFilter) {
    const token = tokenOrNull();
    setVocabularyStatus(status);
    setVocabularyPage(0);
    if (!token) {
      return;
    }
    setVocabularyBusy(true);
    setVocabularyError(null);
    try {
      const result = await fetchVocabulary(token, status, vocabularyQuery, 0, vocabularyPageSize, vocabularySort);
      setVocabulary(result);
    } catch (exception) {
      setVocabulary(null);
      setVocabularyError(exception instanceof Error ? exception.message : '词库加载失败。');
    } finally {
      setVocabularyBusy(false);
    }
  }

  async function changeVocabularyPage(page: number) {
    const nextPage = Math.max(page, 0);
    setVocabularyPage(nextPage);
    await loadVocabulary(nextPage);
  }

  async function changeVocabularyPageSize(pageSize: number) {
    setVocabularyPageSize(pageSize);
    setVocabularyPage(0);
    await loadVocabulary(0, pageSize);
  }

  async function changeVocabularySort(sort: VocabularySortMode) {
    setVocabularySort(sort);
    setVocabularyPage(0);
    await loadVocabulary(0, vocabularyPageSize, vocabularyStatus, sort);
  }

  function formatVocabularyStatus(status: VocabularyStatusFilter) {
    switch (status) {
      case 'LEARNED':
        return '已学';
      case 'UNLEARNED':
        return '未学';
      case 'LOW_LEVEL':
        return '远低于当前水平';
      default:
        return '全部';
    }
  }

  async function searchAdminWordByQuery(event?: React.FormEvent<HTMLFormElement>) {
    event?.preventDefault();
    const token = tokenOrNull();
    const query = adminWordQuery.trim();

    if (!token) {
      setAdminWordError('请先登录。');
      return;
    }
    if (!query) {
      setAdminWordError('请输入要查询的单词。');
      return;
    }

    setAdminWordBusy(true);
    setAdminWordError(null);

    try {
      const result = await searchAdminWord(token, query);
      setAdminWordDetail(result);
      fillAdminEditForms(result);
    } catch (exception) {
      setAdminWordDetail(null);
      setAdminWordError(exception instanceof Error ? exception.message : '单词查询失败。');
    } finally {
      setAdminWordBusy(false);
    }
  }

  async function createAdminWordFromForm(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const token = tokenOrNull();

    if (!token) {
      setAdminWordError('请先登录。');
      return;
    }

    setAdminWordBusy(true);
    setAdminWordError(null);

    try {
      const result = await createAdminWord(token, adminWordForm);
      setAdminWordDetail(result);
      fillAdminEditForms(result);
      setAdminWordForm(emptyAdminWordForm);
    } catch (exception) {
      setAdminWordError(exception instanceof Error ? exception.message : '单词新增失败。');
    } finally {
      setAdminWordBusy(false);
    }
  }

  async function updateAdminWordFromForm(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const token = tokenOrNull();

    if (!token || !adminWordDetail) {
      setAdminWordError('请先查询一个单词。');
      return;
    }

    setAdminWordBusy(true);
    setAdminWordError(null);

    try {
      const result = await updateAdminWord(token, adminWordDetail.id, adminWordUpdateForm);
      setAdminWordDetail(result);
      fillAdminEditForms(result);
    } catch (exception) {
      setAdminWordError(exception instanceof Error ? exception.message : '单词保存失败。');
    } finally {
      setAdminWordBusy(false);
    }
  }

  async function updateAdminSenseFromForm(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const token = tokenOrNull();
    const sense = adminWordDetail?.senses[0];

    if (!token || !adminWordDetail || !sense) {
      setAdminWordError('请先查询一个包含词义的单词。');
      return;
    }

    setAdminWordBusy(true);
    setAdminWordError(null);

    try {
      const result = await updateAdminSense(token, sense.id, adminSenseForm);
      setAdminWordDetail(result);
      fillAdminEditForms(result);
    } catch (exception) {
      setAdminWordError(exception instanceof Error ? exception.message : '词义保存失败。');
    } finally {
      setAdminWordBusy(false);
    }
  }

  async function deleteAdminWordFromDetail() {
    const token = tokenOrNull();

    if (!token || !adminWordDetail) {
      setAdminWordError('请先查询一个单词。');
      return;
    }

    const confirmed = window.confirm('确认删除这个单词？已有学习记录的单词会被后端拒绝删除。');
    if (!confirmed) {
      return;
    }

    setAdminWordBusy(true);
    setAdminWordError(null);

    try {
      await deleteAdminWord(token, adminWordDetail.id);
      setAdminWordDetail(null);
      setAdminWordQuery('');
      setAdminWordUpdateForm({ canonicalText: '', status: 'ACTIVE' });
      setAdminSenseForm(emptyAdminSenseForm);
    } catch (exception) {
      setAdminWordError(exception instanceof Error ? exception.message : '单词删除失败。');
    } finally {
      setAdminWordBusy(false);
    }
  }

  function fillAdminEditForms(detail: LearningUnitDetail) {
    setAdminWordUpdateForm({
      canonicalText: detail.canonicalText,
      status: detail.status
    });
    const firstSense = detail.senses[0];
    setAdminSenseForm(
      firstSense
        ? {
            partOfSpeech: firstSense.partOfSpeech ?? '',
            definitionEn: firstSense.definitionEn,
            definitionZh: firstSense.definitionZh ?? '',
            difficultyLevel: firstSense.difficultyLevel ?? '',
            difficultyConfidence: firstSense.difficultyConfidence,
            frequencyScore: firstSense.frequencyScore,
            frequencyBand: firstSense.frequencyBand ?? '',
            status: firstSense.status
          }
        : emptyAdminSenseForm
    );
  }

  function resetAdminWords() {
    setAdminWordQuery('');
    setAdminWordDetail(null);
    setAdminWordForm(emptyAdminWordForm);
    setAdminWordUpdateForm({ canonicalText: '', status: 'ACTIVE' });
    setAdminSenseForm(emptyAdminSenseForm);
    setAdminWordBusy(false);
    setAdminWordError(null);
  }

  async function loadAdminDialogues(page = adminDialoguePage) {
    const token = tokenOrNull();

    if (!token) {
      setAdminDialogueError('请先登录。');
      return;
    }

    setAdminDialogueBusy(true);
    setAdminDialogueError(null);

    try {
      const result = await fetchAdminDialogues(token, adminDialoguePackageId, page, ADMIN_PAGE_SIZE);
      setAdminDialogues(result.items);
      setAdminDialoguePage(result.page);
      setAdminDialogueTotalPages(result.totalPages);
      setAdminDialogueTotalItems(result.totalItems);
      if (adminDialogueSelected && !result.items.some((item) => item.id === adminDialogueSelected.id)) {
        clearAdminDialogueSelection();
      }
    } catch (exception) {
      setAdminDialogues([]);
      setAdminDialogueTotalPages(0);
      setAdminDialogueTotalItems(0);
      setAdminDialogueError(exception instanceof Error ? exception.message : '对话记录加载失败。');
    } finally {
      setAdminDialogueBusy(false);
    }
  }

  function selectAdminDialogue(turn: AdminDialogueTurn) {
    setAdminDialogueSelected(turn);
    setAdminDialogueForm({
      userMessage: turn.userMessage,
      roleplayReply: turn.roleplayReply,
      mentorFeedback: turn.mentorFeedback,
      corrections: turn.corrections,
      naturalExpression: turn.naturalExpression,
      scoringSignal: turn.scoringSignal
    });
    setAdminDialogueError(null);
  }

  function clearAdminDialogueSelection() {
    setAdminDialogueSelected(null);
    setAdminDialogueForm(emptyAdminDialogueForm);
  }

  async function saveAdminDialogue(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const token = tokenOrNull();

    if (!token || !adminDialogueSelected) {
      setAdminDialogueError('请先选择一条对话。');
      return;
    }

    setAdminDialogueBusy(true);
    setAdminDialogueError(null);

    try {
      const updated = await updateAdminDialogue(token, adminDialogueSelected.id, adminDialogueForm);
      setAdminDialogueSelected(updated);
      setAdminDialogues((previous) => previous.map((item) => (item.id === updated.id ? updated : item)));
    } catch (exception) {
      setAdminDialogueError(exception instanceof Error ? exception.message : '对话保存失败。');
    } finally {
      setAdminDialogueBusy(false);
    }
  }

  async function deleteSelectedAdminDialogue() {
    const token = tokenOrNull();

    if (!token || !adminDialogueSelected) {
      setAdminDialogueError('请先选择一条对话。');
      return;
    }

    setAdminDialogueBusy(true);
    setAdminDialogueError(null);

    try {
      await deleteAdminDialogue(token, adminDialogueSelected.id);
      setAdminDialogues((previous) => previous.filter((item) => item.id !== adminDialogueSelected.id));
      clearAdminDialogueSelection();
    } catch (exception) {
      setAdminDialogueError(exception instanceof Error ? exception.message : '对话删除失败。');
    } finally {
      setAdminDialogueBusy(false);
    }
  }

  function resetAdminDialogues() {
    setAdminDialoguePackageId('');
    setAdminDialogues([]);
    setAdminDialoguePage(0);
    setAdminDialogueTotalPages(0);
    setAdminDialogueTotalItems(0);
    clearAdminDialogueSelection();
    setAdminDialogueBusy(false);
    setAdminDialogueError(null);
  }

  async function loadAdminPlacementItems(page = adminPlacementPage) {
    const token = tokenOrNull();

    if (!token) {
      setAdminPlacementError('请先登录。');
      return;
    }

    setAdminPlacementBusy(true);
    setAdminPlacementError(null);

    try {
      const result = await fetchAdminPlacementItems(token, {
        abilityDimension: adminPlacementAbility || undefined,
        status: adminPlacementStatus || undefined,
        query: adminPlacementQuery,
        minDifficulty: adminPlacementMinDifficulty,
        maxDifficulty: adminPlacementMaxDifficulty,
        page,
        size: ADMIN_PAGE_SIZE
      });
      setAdminPlacementItems(result.items);
      setAdminPlacementPage(result.page);
      setAdminPlacementTotalPages(result.totalPages);
      setAdminPlacementTotalItems(result.totalItems);
    } catch (exception) {
      setAdminPlacementItems([]);
      setAdminPlacementTotalPages(0);
      setAdminPlacementTotalItems(0);
      setAdminPlacementError(exception instanceof Error ? exception.message : '题库加载失败。');
    } finally {
      setAdminPlacementBusy(false);
    }
  }

  function resetAdminPlacementItems() {
    setAdminPlacementItems([]);
    setAdminPlacementPage(0);
    setAdminPlacementTotalPages(0);
    setAdminPlacementTotalItems(0);
    setAdminPlacementQuery('');
    setAdminPlacementAbility('vocabulary_size');
    setAdminPlacementStatus('READY');
    setAdminPlacementMinDifficulty('');
    setAdminPlacementMaxDifficulty('');
    setAdminPlacementBusy(false);
    setAdminPlacementError(null);
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
      setDialogueTurns([]);
      setDialogueMessage('');
      setDialogueError(null);
      setCompletionPromptVisible(false);
      setCompletionReason('');
      resetMentorHints();
      await loadReviewPlan();
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
    setSkipBusy(false);
    setCompleteBusy(false);
    setDialogueTurns([]);
    setDialogueMessage('');
    setDialogueError(null);
    setDialogueBusy(false);
    setCompletionPromptVisible(false);
    setCompletionReason('');
    resetMentorHints();
  }

  async function submitDialogueMessage(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const token = tokenOrNull();
    const trimmedMessage = dialogueMessage.trim();

    if (!token || !learningPackage) {
      setDialogueError('请先开始学习并加载任务。');
      return;
    }

    if (learningPackage.status === 'COMPLETED') {
      setDialogueError('本轮学习已完成，请开始下一轮任务。');
      return;
    }

    if (!trimmedMessage) {
      setDialogueError('请输入英文回复。');
      return;
    }

    setDialogueBusy(true);
    setDialogueError(null);

    try {
      const response = await sendLearningDialogueMessage(token, learningPackage.id, trimmedMessage);
      setDialogueTurns((previous) => [...previous, response]);
      setDialogueMessage('');
      resetMentorHints();
      if (isTaskComplete(response.scoringSignal)) {
        setLearningPackage((previous) => (previous ? { ...previous, status: 'COMPLETED' } : previous));
        setCompletionReason(readCompletionReason(response.scoringSignal));
        setCompletionPromptVisible(true);
      }
    } catch (exception) {
      setDialogueError(exception instanceof Error ? exception.message : 'Dialogue failed.');
    } finally {
      setDialogueBusy(false);
    }
  }

  async function skipCurrentLearningPackage() {
    const token = tokenOrNull();
    if (!token || !learningPackage) {
      setLearningError('请先开始学习并加载任务。');
      return;
    }
    setSkipBusy(true);
    setLearningError(null);
    try {
      await skipLearningPackage(token, learningPackage.id);
      await loadNextLearningPackage();
    } catch (exception) {
      setLearningError(exception instanceof Error ? exception.message : '跳过当前任务失败。');
    } finally {
      setSkipBusy(false);
    }
  }

  async function completeCurrentLearningPackage() {
    const token = tokenOrNull();
    if (!token || !learningPackage) {
      setLearningError('请先开始学习并加载任务。');
      return;
    }
    if (learningPackage.status === 'COMPLETED') {
      setCompletionPromptVisible(false);
      setCompletionReason('');
      await loadNextLearningPackage();
      return;
    }
    setCompleteBusy(true);
    setLearningError(null);
    try {
      const completedPackage = await completeLearningPackage(token, learningPackage.id);
      setLearningPackage(completedPackage);
      setCompletionReason('你已手动结束当前对话，可以进入下一轮学习。');
      setCompletionPromptVisible(true);
    } catch (exception) {
      setLearningError(exception instanceof Error ? exception.message : '结束当前对话失败。');
    } finally {
      setCompleteBusy(false);
    }
  }

  function submitDialogueOnEnter(event: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key !== 'Enter' || event.shiftKey || event.nativeEvent.isComposing) {
      return;
    }
    event.preventDefault();
    event.currentTarget.form?.requestSubmit();
  }

  function resetMentorHints() {
    clearMentorHintTimer();
    setMentorHints([]);
    setMentorHintCount(0);
    setMentorHintBusy(false);
    setPreciseHintPromptVisible(false);
    setPreciseHintUnlocked(false);
  }

  function clearMentorHintTimer() {
    if (mentorHintTimerRef.current !== null) {
      window.clearTimeout(mentorHintTimerRef.current);
      mentorHintTimerRef.current = null;
    }
  }

  function enqueueMentorHint(hint: MentorHint) {
    clearMentorHintTimer();
    setMentorHintBusy(true);
    mentorHintTimerRef.current = window.setTimeout(() => {
      setMentorHints((previous) => [...previous, hint]);
      setMentorHintBusy(false);
      mentorHintTimerRef.current = null;
    }, 250);
  }

  function isTaskComplete(scoringSignal: Record<string, unknown>) {
    return scoringSignal.taskComplete === true || scoringSignal.taskComplete === 'true';
  }

  function readCompletionReason(scoringSignal: Record<string, unknown>) {
    return typeof scoringSignal.completionReason === 'string' ? scoringSignal.completionReason : '';
  }

  async function startNextRoundAfterCompletion() {
    setCompletionPromptVisible(false);
    setCompletionReason('');
    await loadNextLearningPackage();
  }

  function requestMentorHint() {
    if (!learningPackage) {
      return;
    }
    if (mentorHintCount >= 3) {
      setPreciseHintUnlocked(true);
      setPreciseHintPromptVisible(true);
      return;
    }
    const nextCount = mentorHintCount + 1;
    setMentorHintCount(nextCount);
    enqueueMentorHint({
      id: Date.now(),
      level: 'HINT',
      text: buildMentorHint(nextCount)
    });
  }

  function addPreciseHint() {
    setPreciseHintUnlocked(true);
    setPreciseHintPromptVisible(false);
    enqueueMentorHint({
      id: Date.now(),
      level: 'PRECISE',
      text: buildPreciseHint()
    });
  }

  function continueMentorHintAfterPrompt() {
    setPreciseHintPromptVisible(false);
    const nextCount = mentorHintCount + 1;
    setMentorHintCount(nextCount);
    enqueueMentorHint({
      id: Date.now(),
      level: 'HINT',
      text: buildMentorHint(nextCount)
    });
  }

  function buildMentorHint(step: number) {
    const content = learningPackage ? parseLearningPackageContent(learningPackage) : {};
    const facts = content.learningTask?.facts ?? fallbackTaskFacts(content.scenario?.code) ?? {};
    const registerNote = taskRegisterNote(content);
    const currentQuestion = currentRoleplayQuestion(content);
    const targets = reviewPlan?.items ?? [];
    const newTargets = targets.filter((item) => item.pool === 'NEW');
    const reviewedTargets = targets.filter((item) => item.pool === 'REVIEW');
    const factSummary = Object.entries(facts)
      .slice(0, 4)
      .map(([key, value]) => `${formatTaskFactLabel(key)}=${value}`)
      .join('；');

    if (step === 1) {
      const wordNote =
        newTargets.length > 0
          ? `这里可能有新词：${newTargets.slice(0, 2).map((item) => `${item.canonicalText}（${item.definitionZh ?? item.definitionEn}）`).join('、')}。`
          : reviewedTargets.length > 0
          ? `要用到的重点词你已经学过，先回忆和 ${reviewedTargets.slice(0, 2).map((item) => item.canonicalText).join('、')} 相关的表达。`
          : '先从任务卡里的已给信息开始，不需要编造新信息。';
      return `先别急着写完整答案。当前对方在问：${currentQuestion}。${registerNote} 看任务卡：${factSummary}。${wordNote}`;
    }

    if (step === 2) {
      return `先判断当前问题要你做什么：回答事实、提出请求，还是询问信息。${registerNote} 不一定要写长句，先用一个清楚的短句接住当前问题。`;
    }

    if (step === 3) {
      return '可以先搭一个空框架，但不要直接套完整答案：Yes, ... / I need ... / Could you tell me ... ? 你只需要把当前问题相关的任务卡信息放进去。';
    }

    return `继续普通提示：只回答当前问题即可。${registerNote} 选择任务卡中的一个相关事实，先写短句，不要一次覆盖整个任务背景。`;
  }

  function buildPreciseHint() {
    const content = learningPackage ? parseLearningPackageContent(learningPackage) : {};
    const scenarioCode = content.scenario?.code;
    const currentQuestion = currentRoleplayQuestion(content);
    const question = currentQuestion.toLowerCase();
    const targets = reviewPlan?.items.slice(0, 4).map((item) => item.canonicalText).join('、');
    const targetNote = targets ? `可顺带考虑重点词：${targets}。` : '优先使用当前问题相关的信息。';
    return `${taskRegisterNote(content)} 当前问题是：“${currentQuestion}” ${targetNote} ${preciseHintForCurrentQuestion(scenarioCode, question)}`;
  }

  function currentRoleplayQuestion(content: ReturnType<typeof parseLearningPackageContent>) {
    const lastTurn = dialogueTurns[dialogueTurns.length - 1];
    return lastTurn?.roleplayReply || content.roleplayAgent?.openingLine || '请根据当前任务继续回应。';
  }

  function preciseHintForCurrentQuestion(scenarioCode: string | undefined, question: string) {
    if (scenarioCode === 'hotel_check_in') {
      if (question.includes('reservation')) {
        return '可以答：Yes, I have a two-night reservation under Alex Chen.';
      }
      if (question.includes('room') || question.includes('prefer')) {
        return '可以答：Could I have a quiet queen room, please?';
      }
      if (question.includes('breakfast')) {
        return '可以问：What time is breakfast?';
      }
      if (question.includes('wi-fi') || question.includes('wifi')) {
        return '可以问：Could you tell me the Wi-Fi information?';
      }
      return '可以答：I would like to check in. My name is Alex Chen.';
    }
    if (scenarioCode === 'shopping_return') {
      if (question.includes('problem') || question.includes('wrong')) {
        return '可以答：The left side has no sound.';
      }
      if (question.includes('receipt')) {
        return '可以答：Yes, I have the receipt.';
      }
      if (question.includes('refund') || question.includes('exchange')) {
        return '可以答：I would like a refund or an exchange.';
      }
      return '可以答：I bought these wireless headphones yesterday, but the left side has no sound.';
    }
    if (scenarioCode === 'bank_account') {
      if (question.includes('kind') || question.includes('type')) {
        return '可以答：I would like to open a savings account.';
      }
      if (question.includes('id') || question.includes('document') || question.includes('proof')) {
        return '可以答：I have my passport and proof of address.';
      }
      if (question.includes('debit')) {
        return '可以问：Can I apply for a debit card?';
      }
      if (question.includes('fee')) {
        return '可以问：Are there any monthly fees?';
      }
      return '可以答：I would like to open a savings account.';
    }
    if (scenarioCode === 'police_stop') {
      if (question.includes('why')) {
        return '可以问：Could you explain why I was stopped?';
      }
      if (question.includes('where') || question.includes('going')) {
        return '可以答：I am walking to the subway.';
      }
      if (question.includes('id')) {
        return '可以答：Yes, I have my ID with me.';
      }
      return '可以问：What should I do next?';
    }
    return '可以先用一句短答回应当前问题：Yes, ... / I need ... / Could you tell me ... ?';
  }

  function tokenOrNull() {
    return window.localStorage.getItem('contextflow_token');
  }

  function toggleTheme() {
    setThemeMode((previous) => (previous === 'light' ? 'dark' : 'light'));
  }

  return (
    <main className="app-shell" data-theme={themeMode}>
      <section className="hero">
        <div>
          <p className="eyebrow">ContextFlow</p>
          <h1>语境化英语学习</h1>
          <p className="summary">
            先完成水平测试，再基于用户画像进入对话学习、词义掌握度和复习闭环。
          </p>
        </div>
        <button className="theme-toggle" type="button" onClick={toggleTheme} aria-pressed={themeMode === 'dark'}>
          {themeMode === 'dark' ? '白天模式' : '夜间模式'}
        </button>
      </section>

      {currentUser ? (
        <>
          <section className="account-bar">
            <div>
              <span className="label">当前用户</span>
              <strong>{currentUser.displayName}</strong>
              <p>{currentUser.role}</p>
            </div>
            <button className="secondary-button" type="button" onClick={handleLogout}>
              退出
            </button>
          </section>

          {isAdmin ? renderAdminConsoleV2() : renderLearnerExperience()}
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
          <h2>{authMode === 'login' ? '登录' : '注册学习者'}</h2>
          <p className="hint">
            {authMode === 'login'
              ? '测试账号：learner / learner123，admin / admin123。'
              : '新注册用户固定为 LEARNER。'}
          </p>
        </div>

        <form className="login-form" onSubmit={handleLogin}>
          <div className="auth-mode">
            <button
              className={authMode === 'login' ? 'mode-button active' : 'mode-button'}
              type="button"
              onClick={() => setAuthMode('login')}
            >
              登录
            </button>
            <button
              className={authMode === 'register' ? 'mode-button active' : 'mode-button'}
              type="button"
              onClick={() => setAuthMode('register')}
            >
              注册
            </button>
          </div>
          <label>
            用户名
            <input value={username} onChange={(event) => setUsername(event.target.value)} />
          </label>
          {authMode === 'register' && (
            <label>
              显示名
              <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} />
            </label>
          )}
          <label>
            密码
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          {authError && <p className="error compact">登录失败：{authError}</p>}
          <button className="refresh-button compact-button" type="submit">
            {authMode === 'login' ? '登录' : '创建学习者账号'}
          </button>
        </form>
      </section>
    );
  }

  function renderLearnerExperience() {
    if (learnerView === 'vocabulary') {
      return renderVocabularyPage();
    }

    return (
      <section className="learner-grid">
        <section className="tool-panel primary-flow">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Placement</p>
              <h2>找到起始水平</h2>
            </div>
            <button className="secondary-button" type="button" onClick={startPlacement} disabled={placementBusy}>
              开始测试
            </button>
          </div>
          {renderPlacementQuestion(false)}
        </section>

        <section className="tool-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Profile</p>
              <h2>你的水平</h2>
            </div>
            <button className="secondary-button" type="button" onClick={loadProfile}>
              刷新
            </button>
          </div>
          {renderLearnerProfile()}
        </section>

        <section className="tool-panel learning-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">词库</p>
              <h2>已学与未学单词</h2>
            </div>
          </div>
          <div className="secondary-entry">
            <p className="hint">查看当前测试词库、已学状态，并搜索指定单词。</p>
            <button className="primary-button" type="button" onClick={openVocabularyPage} disabled={vocabularyBusy}>
              进入词库
            </button>
          </div>
        </section>

        <section className="tool-panel learning-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Learning</p>
              <h2>场景会话</h2>
            </div>
            <button
              className="secondary-button"
              type="button"
              onClick={loadNextLearningPackage}
              disabled={learningBusy || !profile}
            >
              开始学习
            </button>
            <button
              className="secondary-button"
              type="button"
              onClick={skipCurrentLearningPackage}
              disabled={learningBusy || skipBusy || completeBusy || !learningPackage || learningPackage.status !== 'READY'}
            >
              跳过当前任务
            </button>
            <button
              className="secondary-button"
              type="button"
              onClick={completeCurrentLearningPackage}
              disabled={learningBusy || skipBusy || completeBusy || !learningPackage || !['READY', 'COMPLETED'].includes(learningPackage.status)}
            >
              {learningPackage?.status === 'COMPLETED' ? '开始新任务' : '结束当前对话'}
            </button>
          </div>
          {renderLearningPackage()}
        </section>
      </section>
    );
  }

  function renderVocabularyPage() {
    return (
      <section className="learner-secondary-page">
        <section className="tool-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">词库</p>
              <h2>已学与未学单词</h2>
            </div>
            <div className="button-row">
              <button className="secondary-button" type="button" onClick={() => setLearnerView('home')}>
                返回学习首页
              </button>
              <button className="secondary-button" type="button" onClick={() => void loadVocabulary()} disabled={vocabularyBusy}>
                刷新
              </button>
            </div>
          </div>
          {renderVocabularyPanel()}
        </section>
      </section>
    );
  }

  function renderAdminConsoleV2() {
    if (adminView === 'runtime') {
      return renderAdminSecondaryPage('系统运行时', renderAdminRuntimePage());
    }
    if (adminView === 'words') {
      return renderAdminSecondaryPage('单词管理', renderAdminWordPanel());
    }
    if (adminView === 'dialogues') {
      return renderAdminSecondaryPage('对话记录', renderAdminDialoguePanel(), () => loadAdminDialogues(), adminDialogueBusy);
    }
    if (adminView === 'placementItems') {
      return renderAdminSecondaryPage('题库管理', renderAdminPlacementItemPanel(), () => loadAdminPlacementItems(), adminPlacementBusy);
    }

    return (
      <section className="admin-console">
        <section className="admin-entry-grid">
          <button className="admin-entry-card" type="button" onClick={() => setAdminView('runtime')}>
            <span className="eyebrow">Runtime</span>
            <strong>系统运行时</strong>
            <small>健康检查、权限探测、Spring AI 状态。</small>
          </button>
          <button className="admin-entry-card" type="button" onClick={() => setAdminView('words')}>
            <span className="eyebrow">Words</span>
            <strong>单词管理</strong>
            <small>查询、新增单个单词、编辑词义。</small>
          </button>
          <button
            className="admin-entry-card"
            type="button"
            onClick={() => {
              setAdminView('dialogues');
              void loadAdminDialogues(0);
            }}
          >
            <span className="eyebrow">Dialogues</span>
            <strong>对话记录</strong>
            <small>查询、编辑、删除学习对话记录。</small>
          </button>
          <button
            className="admin-entry-card"
            type="button"
            onClick={() => {
              setAdminView('placementItems');
              void loadAdminPlacementItems(0);
            }}
          >
            <span className="eyebrow">Placement</span>
            <strong>题库管理</strong>
            <small>查看水平测试题、难度、频率层级。</small>
          </button>
        </section>
      </section>
    );
  }

  function renderAdminSecondaryPage(
    title: string,
    content: ReactNode,
    refresh?: () => void | Promise<void>,
    busy?: boolean
  ) {
    return (
      <section className="admin-console">
        <section className="tool-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">管理员</p>
              <h2>{title}</h2>
            </div>
            <div className="button-row">
              <button className="secondary-button" type="button" onClick={() => setAdminView('home')}>
                返回管理首页
              </button>
              {refresh && (
                <button className="secondary-button" type="button" onClick={() => void refresh()} disabled={busy}>
                  刷新
                </button>
              )}
            </div>
          </div>
          {content}
        </section>
      </section>
    );
  }

  function renderAdminRuntimePage() {
    return (
      <div className="admin-runtime-content">
        <section className="status-panel">
          <div>
            <span className="label">后端状态</span>
            <strong>{state === 'success' ? health?.status : state}</strong>
          </div>
          <div>
            <span className="label">服务</span>
            <strong>{health?.service ?? '-'}</strong>
          </div>
          <div>
            <span className="label">版本</span>
            <strong>{health?.version ?? '-'}</strong>
          </div>
        </section>
        {error && <p className="error">后端请求失败：{error}</p>}
        <div className="button-row">
          <button className="secondary-button" type="button" onClick={loadHealth}>
            刷新健康检查
          </button>
          <button className="secondary-button" type="button" onClick={() => checkProtectedEndpoint('/api/learner/probe')}>
            检查 learner API
          </button>
          <button className="secondary-button" type="button" onClick={() => checkProtectedEndpoint('/api/admin/probe')}>
            检查 admin API
          </button>
          <button className="secondary-button" type="button" onClick={loadAgentRuntime} disabled={agentRuntimeBusy}>
            刷新 Agent
          </button>
          <button className="primary-button" type="button" onClick={probeAgentRuntime} disabled={agentRuntimeProbeBusy}>
            探测模型
          </button>
        </div>
        {accessProbe && (
          <p className="success">
            {accessProbe.scope}: {accessProbe.message}
          </p>
        )}
        {accessError && <p className="error compact">访问失败：{accessError}</p>}
        {renderAgentRuntimePanel()}

        <section className="tool-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">ECDICT</p>
              <h2>完整词表导入</h2>
            </div>
            <button className="secondary-button" type="button" onClick={runEcdictImport} disabled={ecdictImportBusy}>
              {ecdictImportBusy ? '导入中' : '导入本地 CSV'}
            </button>
          </div>
          <p className="hint">读取项目根目录的 data/ecdict.csv，导入 raw/clean 表并同步到系统学习词表。</p>
          {ecdictImportError && <p className="error compact">ECDICT 导入失败：{ecdictImportError}</p>}
          {ecdictImportResult && (
            <div className="metrics-row compact-metrics">
              <div>
                <span className="label">Batch</span>
                <strong>#{ecdictImportResult.batchId}</strong>
              </div>
              <div>
                <span className="label">原始行</span>
                <strong>{ecdictImportResult.loadedRows}</strong>
              </div>
              <div>
                <span className="label">清洗词</span>
                <strong>{ecdictImportResult.cleanRows}</strong>
              </div>
              <div>
                <span className="label">系统词义</span>
                <strong>{ecdictImportResult.learningSenseRows}</strong>
              </div>
            </div>
          )}
        </section>
      </div>
    );
  }

  function renderAdminConsole() {
    return (
      <section className="admin-console">
        <section className="status-panel">
          <div>
            <span className="label">后端状态</span>
            <strong>{state === 'success' ? health?.status : state}</strong>
          </div>
          <div>
            <span className="label">服务</span>
            <strong>{health?.service ?? '-'}</strong>
          </div>
          <div>
            <span className="label">版本</span>
            <strong>{health?.version ?? '-'}</strong>
          </div>
        </section>

        {error && <p className="error">后端请求失败：{error}</p>}

        <button className="refresh-button" type="button" onClick={loadHealth}>
          刷新健康检查
        </button>

        <section className="debug-grid">
          <section className="tool-panel agent-runtime-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">管理员</p>
                <h2>权限检查</h2>
              </div>
            </div>
            <div className="access-actions">
              <button
                className="secondary-button"
                type="button"
                onClick={() => checkProtectedEndpoint('/api/learner/probe')}
              >
                检查 learner API
              </button>
              <button
                className="secondary-button"
                type="button"
                onClick={() => checkProtectedEndpoint('/api/admin/probe')}
              >
                检查 admin API
              </button>
            </div>
            {accessProbe && (
              <p className="success">
                {accessProbe.scope}: {accessProbe.message}
              </p>
            )}
            {accessError && <p className="error compact">访问失败：{accessError}</p>}
          </section>

          <section className="tool-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Agent</p>
                <h2>运行时</h2>
              </div>
              <div className="button-row">
                <button className="secondary-button" type="button" onClick={loadAgentRuntime} disabled={agentRuntimeBusy}>
                  刷新
                </button>
                <button
                  className="primary-button"
                  type="button"
                  onClick={probeAgentRuntime}
                  disabled={agentRuntimeProbeBusy}
                >
                  探测模型
                </button>
              </div>
            </div>
            {renderAgentRuntimePanel()}
          </section>

          <section className="tool-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">数据管理</p>
                <h2>单词管理</h2>
              </div>
            </div>
            {renderAdminWordPanel()}
          </section>

          <section className="tool-panel agent-runtime-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">数据管理</p>
                <h2>对话记录</h2>
              </div>
                <button className="secondary-button" type="button" onClick={() => loadAdminDialogues(0)} disabled={adminDialogueBusy}>
                刷新
              </button>
            </div>
            {renderAdminDialoguePanel()}
          </section>
        </section>
      </section>
    );
  }

  function renderPlacementQuestion(showDebug: boolean) {
    const placementProgressCurrent = placementSession ? Math.min(placementSession.answeredCount + 1, placementSession.maxItemCount) : 0;
    const placementProgressTotal = placementSession?.maxItemCount ?? 0;
    const placementProgressPercent = placementProgressTotal > 0
      ? Math.round((placementProgressCurrent / placementProgressTotal) * 100)
      : 0;
    return (
      <>
        {placementSession && showDebug && (
          <div className="metrics-row">
            <div>
              <span className="label">会话</span>
              <strong>{placementSession.sessionId}</strong>
            </div>
            <div>
              <span className="label">已答</span>
              <strong>{placementSession.answeredCount}</strong>
            </div>
            <div>
              <span className="label">难度</span>
              <strong>{placementSession.currentDifficultyScore}</strong>
            </div>
          </div>
        )}

        {!currentItem && !placementResult && (
          <p className="hint">准备好后开始水平测试。</p>
        )}

        {currentItem && itemContent && (
          <div className="question-panel">
            {placementSession && (
              <div className="placement-progress" aria-label="测试进度">
                <div className="placement-progress-header">
                  <strong>
                    {placementProgressCurrent}/{placementProgressTotal}
                  </strong>
                  <span>还剩 {Math.max(placementProgressTotal - placementProgressCurrent, 0)} 题</span>
                </div>
                <div className="placement-progress-track">
                  <span style={{ width: `${placementProgressPercent}%` }} />
                </div>
              </div>
            )}
            {showDebug && (
              <div className="question-meta">
                <span>{currentItem.itemType}</span>
                <span>{currentItem.cefrLevel}</span>
                <span>Difficulty {currentItem.difficultyScore}</span>
              </div>
            )}
            <h3>{itemContent.question ?? '题目'}</h3>
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
                <label className="option-item unknown-option">
                  <input
                    type="radio"
                    name="placement-option"
                    checked={selectedOptionIndex === options.length}
                    onChange={() => setSelectedOptionIndex(options.length)}
                  />
                  <span>I DON'T KNOW</span>
                </label>
              </div>
            ) : (
              <label className="text-answer">
                答案
                <input value={textAnswer} onChange={(event) => setTextAnswer(event.target.value)} />
              </label>
            )}
            <p className="hint compact">请尽量诚实作答；不认识的词请选择 I DON'T KNOW，不要靠猜测。</p>
            <button
              className="refresh-button compact-button"
              type="button"
              onClick={submitCurrentAnswer}
              disabled={placementBusy}
            >
              提交答案
            </button>
          </div>
        )}

        {placementResult && (
          <div className="result-panel">
            <p className="success">水平测试完成。</p>
            <div className="metrics-row">
              <div>
                <span className="label">得分</span>
                <strong>{placementResult.scorePercent}%</strong>
              </div>
              <div>
                <span className="label">正确</span>
                <strong>
                  {placementResult.correctCount}/{placementResult.itemCount}
                </strong>
              </div>
              <div>
                <span className="label">估计水平</span>
                <strong>{placementResult.estimatedLevel}</strong>
              </div>
              <div>
                <span className="label">估计词汇量</span>
                <strong>{placementResult.vocabularySizeEstimate} 词</strong>
              </div>
              <div>
                <span className="label">频率层</span>
                <strong>{placementResult.vocabularyBand}</strong>
              </div>
            </div>
          </div>
        )}

        {placementError && <p className="error compact">测试失败：{placementError}</p>}

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
      return <p className="hint">{profileError ?? '完成水平测试后会生成你的画像。'}</p>;
    }

    return (
      <div className="profile-content">
        <div className="profile-level-layout">
          <div className={`level-badge level-${profile.cefrLevel.toLowerCase()}`}>{profile.cefrLevel}</div>
          <div className="level-ladder" aria-label="CEFR 水平阶梯">
            {cefrLadder().map((level) => (
              <div className={`level-step ${level.key === profile.cefrLevel ? 'active' : ''}`} key={level.key}>
                <span className="level-step-color" style={{ background: level.color }} />
                <div>
                  <strong>{level.key}</strong>
                  <small>{level.label}</small>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="profile-metrics">
          <div>
            <span className="label">估计词汇量</span>
            <strong>{profile.vocabularySizeEstimate ? `${profile.vocabularySizeEstimate} 词` : '待测'}</strong>
          </div>
          <div>
            <span className="label">频率层</span>
            <strong>{profile.vocabularyBand ?? '待测'}</strong>
          </div>
          <div>
            <span className="label">误差范围</span>
            <strong>{profile.vocabularyMeasurementError ? `±${profile.vocabularyMeasurementError}` : '待校准'}</strong>
          </div>
        </div>
        <p className="hint">后续学习会使用你的水平、词汇量和薄弱项。</p>
      </div>
    );
  }

  function cefrLadder() {
    return [
      { key: 'C2', label: '精通', color: '#c89ce0' },
      { key: 'C1', label: '熟练', color: '#b894d4' },
      { key: 'B2', label: '独立高级', color: '#ca919c' },
      { key: 'B1', label: '独立初级', color: '#7ca5d2' },
      { key: 'A2', label: '基础', color: '#78bdbb' },
      { key: 'A1', label: '入门', color: '#92c79b' }
    ];
  }

  function renderAgentRuntimePanel() {
    if (agentRuntimeError) {
      return <p className="error compact">Agent 运行时加载失败：{agentRuntimeError}</p>;
    }

    if (!agentRuntime) {
      return <p className="hint">刷新后查看当前 Agent provider 和 Spring AI 状态。</p>;
    }

    return (
      <div className="stacked-panel">
        <div className="metrics-row">
          <div>
            <span className="label">Provider</span>
            <strong>{agentRuntime.provider}</strong>
          </div>
          <div>
            <span className="label">Spring AI</span>
            <strong>{agentRuntime.springAiClientAvailable ? '可用' : '不可用'}</strong>
          </div>
          <div>
            <span className="label">契约</span>
            <strong>{agentRuntime.contractVersion}</strong>
          </div>
        </div>

        <div className="agent-diagnostics">
          <span className="label">Spring AI 诊断</span>
          <pre>{JSON.stringify(agentRuntime.diagnostics, null, 2)}</pre>
        </div>

        {agentRuntimeProbeBusy && <p className="hint">正在探测 Agent 模型...</p>}
        {agentRuntimeProbeError && <p className="error compact">Agent 探测失败：{agentRuntimeProbeError}</p>}

        {agentRuntimeProbe && (
          <div className="probe-result">
            <div className="metrics-row">
              <div>
                <span className="label">模型调用</span>
                <strong>{agentRuntimeProbe.modelAttempted ? '已尝试' : '未尝试'}</strong>
              </div>
              <div>
                <span className="label">Fallback</span>
                <strong>{agentRuntimeProbe.fallbackUsed ? '已使用' : '未使用'}</strong>
              </div>
              <div>
                <span className="label">契约校验</span>
                <strong>{agentRuntimeProbe.accepted ? '通过' : '未通过'}</strong>
              </div>
              <div>
                <span className="label">耗时</span>
                <strong>{agentRuntimeProbe.elapsedMs}ms</strong>
              </div>
            </div>

            {agentRuntimeProbe.errorMessage && (
              <p className="error compact">模型错误：{agentRuntimeProbe.errorMessage}</p>
            )}
            {agentRuntimeProbe.errors.length > 0 && (
              <pre>{JSON.stringify(agentRuntimeProbe.errors, null, 2)}</pre>
            )}
            {agentRuntimeProbe.modelValidationErrors.length > 0 && (
              <label>
                模型契约错误
                <pre>{JSON.stringify(agentRuntimeProbe.modelValidationErrors, null, 2)}</pre>
              </label>
            )}
            {agentRuntimeProbe.modelOutput && (
              <label>
                模型实际输出
                <pre>{JSON.stringify(agentRuntimeProbe.modelOutput, null, 2)}</pre>
              </label>
            )}
            {agentRuntimeProbe.modelRawContent && !agentRuntimeProbe.modelOutput && (
              <label>
                模型原始输出
                <pre>{agentRuntimeProbe.modelRawContent}</pre>
              </label>
            )}
            {agentRuntimeProbe.output && (
              <div className="agent-probe-output">
                <label>
                  最终 Roleplay 回复
                  <pre>{agentRuntimeProbe.output.reply}</pre>
                </label>
                <label>
                  最终 Mentor 反馈
                  <pre>{agentRuntimeProbe.output.feedback}</pre>
                </label>
                <label>
                  unitMentions
                  <pre>{JSON.stringify(agentRuntimeProbe.output.unitMentions, null, 2)}</pre>
                </label>
              </div>
            )}
          </div>
        )}
        {!agentRuntimeProbe && !agentRuntimeProbeBusy && !agentRuntimeProbeError && (
          <p className="hint">启用 DeepSeek 配置后点击“探测模型”，确认是否真实调用并返回合规 JSON。</p>
        )}
        </div>
    );
  }

  function renderVocabularyPanel() {
    return (
      <div className="vocabulary-content">
        <form
          className="admin-filter-form"
          onSubmit={(event) => {
            event.preventDefault();
            setVocabularyPage(0);
            void loadVocabulary(0);
          }}
        >
          <select
            value={vocabularyStatus}
            onChange={(event) => setVocabularyStatus(event.target.value as VocabularyStatusFilter)}
          >
            <option value="ALL">全部</option>
            <option value="LEARNED">已学</option>
            <option value="UNLEARNED">未学</option>
            <option value="LOW_LEVEL">远低于当前水平</option>
          </select>
          <select
            value={vocabularySort}
            onChange={(event) => void changeVocabularySort(event.target.value as VocabularySortMode)}
            disabled={vocabularyBusy}
            aria-label="词库排序"
          >
            <option value="AUTO">自动排序</option>
            <option value="DIFFICULTY_ASC">难度从低到高</option>
            <option value="DIFFICULTY_DESC">难度从高到低</option>
          </select>
          <input
            value={vocabularyQuery}
            onChange={(event) => setVocabularyQuery(event.target.value)}
            placeholder="搜索单词，例如 go"
          />
          <select
            value={vocabularyPageSize}
            onChange={(event) => void changeVocabularyPageSize(Number(event.target.value))}
            disabled={vocabularyBusy}
            aria-label="每页显示数量"
          >
            <option value={50}>每页 50</option>
            <option value={100}>每页 100</option>
            <option value={200}>每页 200</option>
            <option value={500}>每页 500</option>
          </select>
          <button className="secondary-button" type="submit" disabled={vocabularyBusy}>
            查询
          </button>
        </form>
        <div className="quick-filter-row" aria-label="词库快捷筛选">
          {(['ALL', 'LEARNED', 'UNLEARNED', 'LOW_LEVEL'] as VocabularyStatusFilter[]).map((status) => (
            <button
              className={`filter-chip ${vocabularyStatus === status ? 'active' : ''}`}
              type="button"
              key={status}
              onClick={() => void applyVocabularyStatusFilter(status)}
              disabled={vocabularyBusy}
            >
              {formatVocabularyStatus(status)}
            </button>
          ))}
        </div>

        <p className="hint">
          词库已支持完整导入；当前页面按搜索、状态和分页浏览，不再只展示固定 500 个单词。
        </p>

        {vocabularyError && <p className="error compact">词库加载失败：{vocabularyError}</p>}
        {learningPlanMessage && <p className="success">{learningPlanMessage}</p>}

        {!vocabulary && !vocabularyError && <p className="hint">登录后会加载当前测试词库。</p>}

        {vocabulary && (
          <div className="vocabulary-window">
            <p className="hint">
              匹配 {vocabulary.totalMatchedWords} 个单词，当前第 {vocabulary.totalPages === 0 ? 0 : vocabulary.page + 1}/{Math.max(vocabulary.totalPages, 1)} 页，显示 {vocabulary.items.length} 个。
            </p>
            <div className="pagination-row">
              <span>
                每页 {vocabulary.pageSize} 个
              </span>
              <div className="button-row">
                <button
                  className="secondary-button compact-button"
                  type="button"
                  onClick={() => void changeVocabularyPage(vocabulary.page - 1)}
                  disabled={vocabularyBusy || vocabulary.page <= 0}
                >
                  上一页
                </button>
                <button
                  className="secondary-button compact-button"
                  type="button"
                  onClick={() => void changeVocabularyPage(vocabulary.page + 1)}
                  disabled={vocabularyBusy || vocabulary.page + 1 >= vocabulary.totalPages}
                >
                  下一页
                </button>
              </div>
            </div>
            <div className="vocabulary-list">
              {vocabulary.items.map((word) => (
                <article className="vocabulary-card" key={word.learningUnitId}>
                  <div className="word-row">
                    <strong>{word.canonicalText}</strong>
                    <div className="word-actions">
                      {word.plannedSenseCount > 0 && (
                        <span className="target-type learned">计划中 {word.plannedSenseCount}</span>
                      )}
                      <span className={`target-type ${word.learnedStatus.toLowerCase()}`}>
                        {word.learnedStatus === 'UNLEARNED' ? '未学' : word.learnedStatus === 'PARTIAL' ? '部分已学' : '已学'}
                      </span>
                      {vocabularyStatus === 'LOW_LEVEL' && word.lowLevelCandidateSenseCount > 0 && (
                        <button
                          className="primary-button compact-button"
                          type="button"
                          onClick={() => openLowLevelMastery(word)}
                        >
                          标记掌握
                        </button>
                      )}
                      <button
                        className="secondary-button compact-button"
                        type="button"
                        onClick={() => void addWordToLearningPlan(word.learningUnitId)}
                        disabled={learningPlanBusyWordId === word.learningUnitId}
                      >
                        {learningPlanBusyWordId === word.learningUnitId ? '加入中' : '加入计划'}
                      </button>
                    </div>
                  </div>
                  <small>
                    {word.learnedSenseCount}/{word.totalSenseCount} 个词义已学习
                  </small>
                  <div className="sense-list">
                    {word.senses.map((sense) => (
                      <div className="sense-line" key={sense.senseId}>
                        <span>{sense.partOfSpeech ?? 'OTHER'}</span>
                        <p>
                          {sense.definitionZh ?? sense.definitionEn}
                          {sense.inLearningPlan && <small>已在学习计划</small>}
                          {sense.lowLevelCandidate && <small>低于当前水平 {sense.learnerLevelGap} 档</small>}
                        </p>
                      </div>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          </div>
        )}
        {renderLowLevelMasteryDialog()}
      </div>
    );
  }

  function renderLowLevelMasteryDialog() {
    if (!lowLevelMasteryDraft) {
      return null;
    }

    const selected = new Set(lowLevelMasteryDraft.selectedSenseIds);
    return (
      <div className="hint-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="low-level-mastery-title">
        <div className="hint-modal wide-modal">
          <div className="panel-heading compact-heading">
            <div>
              <p className="eyebrow">低难词义确认</p>
              <h3 id="low-level-mastery-title">{lowLevelMasteryDraft.word.canonicalText}</h3>
            </div>
            <span className="status-pill">候选 {lowLevelMasteryDraft.word.lowLevelCandidateSenseCount}</span>
          </div>
          <p>
            候选词义会默认勾选；你也可以手动勾选其他词义一起标记为已掌握。
          </p>
          <div className="sense-list mastery-sense-list">
            {lowLevelMasteryDraft.word.senses.map((sense) => (
              <label className={`mastery-sense-item ${sense.lowLevelCandidate ? 'candidate' : 'optional'}`} key={sense.senseId}>
                <input
                  type="checkbox"
                  checked={selected.has(sense.senseId)}
                  disabled={lowLevelMasteryBusy}
                  onChange={() => toggleLowLevelSense(sense.senseId)}
                />
                <span>{sense.partOfSpeech ?? 'OTHER'}</span>
                <p>
                  {sense.definitionZh ?? sense.definitionEn}
                  <small>
                    {sense.difficultyLevel ?? '未分级'}
                    {sense.lowLevelCandidate ? ` · 低于当前水平 ${sense.learnerLevelGap} 档` : ' · 可手动选择'}
                  </small>
                </p>
              </label>
            ))}
          </div>
          <div className="hint-modal-actions">
            <button className="secondary-button compact-button" type="button" onClick={() => setLowLevelMasteryDraft(null)} disabled={lowLevelMasteryBusy}>
              取消
            </button>
            <button className="secondary-button compact-button" type="button" onClick={selectAllLowLevelSenses} disabled={lowLevelMasteryBusy}>
              全选
            </button>
            <button
              className="primary-button compact-button"
              type="button"
              onClick={() => void markSelectedLowLevelSensesMastered()}
              disabled={lowLevelMasteryBusy || lowLevelMasteryDraft.selectedSenseIds.length === 0}
            >
              {lowLevelMasteryBusy ? '标记中' : '确认标记掌握'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  function renderAdminPagination(
    page: number,
    totalPages: number,
    totalItems: number,
    busy: boolean,
    onPageChange: (page: number) => void
  ) {
    if (totalItems === 0) {
      return null;
    }
    return (
      <div className="pagination-row">
        <span>
          第 {page + 1}/{Math.max(totalPages, 1)} 页，共 {totalItems} 条
        </span>
        <div className="button-row">
          <button
            className="secondary-button compact-button"
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={busy || page <= 0}
          >
            上一页
          </button>
          <button
            className="secondary-button compact-button"
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={busy || page + 1 >= totalPages}
          >
            下一页
          </button>
        </div>
      </div>
    );
  }

  function renderAdminPlacementItemPanel() {
    return (
      <div className="admin-placement-content">
        <form
          className="admin-filter-form"
          onSubmit={(event) => {
            event.preventDefault();
            void loadAdminPlacementItems(0);
          }}
        >
          <select value={adminPlacementAbility} onChange={(event) => setAdminPlacementAbility(event.target.value)}>
            <option value="vocabulary_size">词汇量</option>
            <option value="">全部能力维度</option>
          </select>
          <select value={adminPlacementStatus} onChange={(event) => setAdminPlacementStatus(event.target.value)}>
            <option value="READY">READY</option>
            <option value="">全部状态</option>
            <option value="PENDING">PENDING</option>
            <option value="GENERATING_TEXT">GENERATING_TEXT</option>
            <option value="VALIDATING_TEXT">VALIDATING_TEXT</option>
            <option value="GENERATING_AUDIO">GENERATING_AUDIO</option>
            <option value="FAILED">FAILED</option>
            <option value="EXPIRED">EXPIRED</option>
          </select>
          <input
            value={adminPlacementQuery}
            onChange={(event) => setAdminPlacementQuery(event.target.value)}
            placeholder="搜索题干、场景或技能"
          />
          <input
            type="number"
            min="1"
            max="100"
            value={adminPlacementMinDifficulty}
            onChange={(event) => setAdminPlacementMinDifficulty(event.target.value)}
            placeholder="最低难度"
          />
          <input
            type="number"
            min="1"
            max="100"
            value={adminPlacementMaxDifficulty}
            onChange={(event) => setAdminPlacementMaxDifficulty(event.target.value)}
            placeholder="最高难度"
          />
          <button className="secondary-button" type="submit" disabled={adminPlacementBusy}>
            查询
          </button>
        </form>

        {adminPlacementError && <p className="error compact">题库加载失败：{adminPlacementError}</p>}
        <p className="hint">当前只做题库查看与筛选；批量导入不放在管理员界面。</p>

        {renderAdminPagination(
          adminPlacementPage,
          adminPlacementTotalPages,
          adminPlacementTotalItems,
          adminPlacementBusy,
          (nextPage) => void loadAdminPlacementItems(nextPage)
        )}

        <div className="placement-item-list">
          {adminPlacementItems.length === 0 ? (
            <p className="hint">暂无题目，点击查询加载。</p>
          ) : (
            adminPlacementItems.map((item) => {
              const content = parseAdminPlacementContent(item);
              return (
                <article className="placement-item-card" key={item.id}>
                  <div className="word-row">
                    <strong>ID #{item.id} / {item.itemType}</strong>
                    <span className="status-pill">{item.status}</span>
                  </div>
                  <div className="metrics-row compact-metrics">
                    <div>
                      <span className="label">CEFR</span>
                      <strong>{item.cefrLevel}</strong>
                    </div>
                    <div>
                      <span className="label">难度</span>
                      <strong>{item.difficultyScore}</strong>
                    </div>
                    <div>
                      <span className="label">频率层</span>
                      <strong>{item.frequencyBand ?? '-'}</strong>
                    </div>
                    <div>
                      <span className="label">频率排名</span>
                      <strong>{item.frequencyRank ?? '-'}</strong>
                    </div>
                  </div>
                  <p>{content.question ?? '未解析题干'}</p>
                  {content.options.length > 0 && (
                    <ol className="admin-option-list">
                      {content.options.map((option) => (
                        <li key={option}>{option}</li>
                      ))}
                    </ol>
                  )}
                  <small>
                    {item.abilityDimension} / {item.scenarioTag} / {item.targetSkill} / {item.gradingType}
                  </small>
                </article>
              );
            })
          )}
        </div>
      </div>
    );
  }

  function parseAdminPlacementContent(item: AdminPlacementItem): { question: string | null; options: string[] } {
    try {
      const content = JSON.parse(item.contentJson) as { question?: unknown; options?: unknown };
      return {
        question: typeof content.question === 'string' ? content.question : null,
        options: Array.isArray(content.options) ? content.options.filter((option): option is string => typeof option === 'string') : []
      };
    } catch {
      return { question: null, options: [] };
    }
  }

  function renderAdminWordPanel() {
    return (
      <div className="admin-word-content">
        <form className="inline-form" onSubmit={searchAdminWordByQuery}>
          <input
            value={adminWordQuery}
            onChange={(event) => setAdminWordQuery(event.target.value)}
            placeholder="查询已有单词"
          />
          <button className="secondary-button" type="submit" disabled={adminWordBusy}>
            查询
          </button>
        </form>

        <form className="admin-form-grid" onSubmit={createAdminWordFromForm}>
          <h3>新增单个单词</h3>
          <input
            value={adminWordForm.canonicalText}
            onChange={(event) => setAdminWordForm({ ...adminWordForm, canonicalText: event.target.value })}
            placeholder="单词，例如 wallet"
          />
          <select
            value={adminWordForm.partOfSpeech}
            onChange={(event) => setAdminWordForm({ ...adminWordForm, partOfSpeech: event.target.value })}
          >
            {partOfSpeechOptions().map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          <textarea
            value={adminWordForm.definitionEn}
            onChange={(event) => setAdminWordForm({ ...adminWordForm, definitionEn: event.target.value })}
            placeholder="英文释义"
            rows={2}
          />
          <textarea
            value={adminWordForm.definitionZh}
            onChange={(event) => setAdminWordForm({ ...adminWordForm, definitionZh: event.target.value })}
            placeholder="中文释义"
            rows={2}
          />
          <button className="refresh-button compact-button" type="submit" disabled={adminWordBusy}>
            新增单词
          </button>
        </form>

        {adminWordDetail && (
          <div className="admin-edit-block">
            <div className="word-row">
              <strong>{adminWordDetail.canonicalText}</strong>
              <span className="status-pill">{adminWordDetail.status}</span>
            </div>

            <form className="admin-form-grid" onSubmit={updateAdminWordFromForm}>
              <h3>编辑单词</h3>
              <input
                value={adminWordUpdateForm.canonicalText}
                onChange={(event) =>
                  setAdminWordUpdateForm({ ...adminWordUpdateForm, canonicalText: event.target.value })
                }
                placeholder="标准单词"
              />
              <select
                value={adminWordUpdateForm.status}
                onChange={(event) => setAdminWordUpdateForm({ ...adminWordUpdateForm, status: event.target.value })}
              >
                <option value="ACTIVE">启用</option>
                <option value="OFFLINE">下线</option>
                <option value="REVIEW">待复核</option>
              </select>
              <button className="secondary-button" type="submit" disabled={adminWordBusy}>
                保存单词
              </button>
            </form>

            {adminWordDetail.senses[0] && (
              <form className="admin-form-grid" onSubmit={updateAdminSenseFromForm}>
                <h3>编辑首个词义</h3>
                <select
                  value={adminSenseForm.partOfSpeech}
                  onChange={(event) => setAdminSenseForm({ ...adminSenseForm, partOfSpeech: event.target.value })}
                >
                  <option value="">未设置词性</option>
                  {partOfSpeechOptions().map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
                <textarea
                  value={adminSenseForm.definitionEn}
                  onChange={(event) => setAdminSenseForm({ ...adminSenseForm, definitionEn: event.target.value })}
                  placeholder="英文释义"
                  rows={2}
                />
                <textarea
                  value={adminSenseForm.definitionZh}
                  onChange={(event) => setAdminSenseForm({ ...adminSenseForm, definitionZh: event.target.value })}
                  placeholder="中文释义"
                  rows={2}
                />
                <select
                  value={adminSenseForm.status}
                  onChange={(event) => setAdminSenseForm({ ...adminSenseForm, status: event.target.value })}
                >
                  <option value="ACTIVE">启用</option>
                  <option value="OFFLINE">下线</option>
                  <option value="REVIEW">待复核</option>
                </select>
                <button className="secondary-button" type="submit" disabled={adminWordBusy}>
                  保存词义
                </button>
              </form>
            )}

            <div className="sense-list">
              {adminWordDetail.senses.map((sense) => (
                <div className="sense-line" key={sense.id}>
                  <span>{sense.partOfSpeech ?? 'OTHER'}</span>
                  <p>{sense.definitionZh ?? sense.definitionEn}</p>
                </div>
              ))}
            </div>

            <button className="danger-button" type="button" onClick={deleteAdminWordFromDetail} disabled={adminWordBusy}>
              删除单词
            </button>
          </div>
        )}

        {adminWordError && <p className="error compact">单词管理失败：{adminWordError}</p>}
      </div>
    );
  }

  function renderAdminDialoguePanel() {
    return (
      <div className="admin-dialogue-content">
        <div className="inline-form">
          <input
            value={adminDialoguePackageId}
            onChange={(event) => setAdminDialoguePackageId(event.target.value)}
            placeholder="按 packageId 查询；留空查最近 100 条"
          />
          <button className="secondary-button" type="button" onClick={() => loadAdminDialogues(0)} disabled={adminDialogueBusy}>
            查询
          </button>
        </div>

        <p className="hint">删除对话会同步删除该 turn 对应的 learning_events，但不会回滚掌握度统计。</p>

        {renderAdminPagination(
          adminDialoguePage,
          adminDialogueTotalPages,
          adminDialogueTotalItems,
          adminDialogueBusy,
          (nextPage) => void loadAdminDialogues(nextPage)
        )}

        <div className="admin-dialogue-layout">
          <div className="admin-dialogue-list">
            {adminDialogues.length === 0 ? (
              <p className="hint">暂无对话记录，点击查询加载。</p>
            ) : (
              adminDialogues.map((turn) => (
                <button
                  className={adminDialogueSelected?.id === turn.id ? 'dialogue-list-item active' : 'dialogue-list-item'}
                  type="button"
                  key={turn.id}
                  onClick={() => selectAdminDialogue(turn)}
                >
                  <span>ID #{turn.id} / package {turn.learningPackageId} / turn {turn.turnIndex}</span>
                  <strong>{turn.userMessage}</strong>
                  <small>events {turn.learningEventCount} · user {turn.userId}</small>
                </button>
              ))
            )}
          </div>

          <form className="admin-form-grid" onSubmit={saveAdminDialogue}>
            <h3>编辑对话</h3>
            {!adminDialogueSelected && <p className="hint">先从左侧选择一条记录。</p>}
            <textarea
              value={adminDialogueForm.userMessage}
              onChange={(event) => setAdminDialogueForm({ ...adminDialogueForm, userMessage: event.target.value })}
              placeholder="用户输入"
              rows={2}
              disabled={!adminDialogueSelected}
            />
            <textarea
              value={adminDialogueForm.roleplayReply}
              onChange={(event) => setAdminDialogueForm({ ...adminDialogueForm, roleplayReply: event.target.value })}
              placeholder="Roleplay 回复"
              rows={3}
              disabled={!adminDialogueSelected}
            />
            <textarea
              value={adminDialogueForm.mentorFeedback}
              onChange={(event) => setAdminDialogueForm({ ...adminDialogueForm, mentorFeedback: event.target.value })}
              placeholder="Mentor 反馈"
              rows={3}
              disabled={!adminDialogueSelected}
            />
            <textarea
              value={adminDialogueForm.naturalExpression}
              onChange={(event) => setAdminDialogueForm({ ...adminDialogueForm, naturalExpression: event.target.value })}
              placeholder="自然表达建议"
              rows={2}
              disabled={!adminDialogueSelected}
            />
            <textarea
              value={adminDialogueForm.corrections}
              onChange={(event) => setAdminDialogueForm({ ...adminDialogueForm, corrections: event.target.value })}
              placeholder="corrections JSON"
              rows={4}
              disabled={!adminDialogueSelected}
            />
            <textarea
              value={adminDialogueForm.scoringSignal}
              onChange={(event) => setAdminDialogueForm({ ...adminDialogueForm, scoringSignal: event.target.value })}
              placeholder="scoringSignal JSON"
              rows={4}
              disabled={!adminDialogueSelected}
            />
            <div className="button-row">
              <button className="secondary-button" type="submit" disabled={adminDialogueBusy || !adminDialogueSelected}>
                保存
              </button>
              <button
                className="danger-button"
                type="button"
                onClick={deleteSelectedAdminDialogue}
                disabled={adminDialogueBusy || !adminDialogueSelected}
              >
                删除
              </button>
              <button className="secondary-button" type="button" onClick={clearAdminDialogueSelection}>
                清空选择
              </button>
            </div>
          </form>
        </div>

        {adminDialogueError && <p className="error compact">对话管理失败：{adminDialogueError}</p>}
      </div>
    );
  }

  function partOfSpeechOptions() {
    return [
      'NOUN',
      'VERB',
      'ADJECTIVE',
      'ADVERB',
      'PRONOUN',
      'DETERMINER',
      'PREPOSITION',
      'CONJUNCTION',
      'INTERJECTION',
      'NUMERAL',
      'AUXILIARY',
      'PARTICLE',
      'OTHER'
    ];
  }

  function renderReviewPlan() {
    if (!profile) {
      return <p className="hint">请先完成水平测试，学习计划会使用你的画像。</p>;
    }

    if (reviewError) {
      return <p className="error compact">学习计划加载失败：{reviewError}</p>;
    }

    if (!reviewPlan) {
      return <p className="hint">开始学习时会自动加载学习计划。</p>;
    }

    if (reviewPlan.items.length === 0) {
      return <p className="hint">当前还没有可用的复习或新词义目标。</p>;
    }

    return (
      <div className="review-plan-content">
        <div className="metrics-row">
          <div>
            <span className="label">复习</span>
            <strong>{reviewPlan.reviewTargetCount}</strong>
          </div>
          <div>
            <span className="label">新词义</span>
            <strong>{reviewPlan.newTargetCount}</strong>
          </div>
          <div>
            <span className="label">逾期复习</span>
            <strong>{reviewPlan.overdueReviewCount}</strong>
          </div>
        </div>

        <div className="review-plan-window">
          <div className="review-target-list">
            {reviewPlan.items.map((item) => (
              <article className="review-target" key={`${item.pool}-${item.learningUnitSenseId}`}>
                <span className={`target-type ${item.pool.toLowerCase()}`}>{item.pool}</span>
                <div>
                  <strong>{item.canonicalText}</strong>
                  <p>{item.definitionZh ?? item.definitionEn}</p>
                  <small>
                    {item.senseKey} · score {Number(item.score).toFixed(2)} · {item.frequencyBand ?? 'UNKNOWN'}
                  </small>
                </div>
              </article>
            ))}
          </div>
        </div>
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
      return <p className="hint">点击开始学习后，将显示下一份 READY 学习任务。</p>;
    }

    const content = parseLearningPackageContent(learningPackage);
    const scenarioCode = content.scenario?.code;
    const taskGoal = fallbackTaskGoal(scenarioCode) ?? content.learningTask?.goal ?? content.scenario?.description;
    const registerNote = taskRegisterNote(content);
    const taskFacts = Object.entries(content.learningTask?.facts ?? fallbackTaskFacts(scenarioCode) ?? {});
    const taskChecklist = buildTaskChecklist(content);
    const learningCompleted = learningPackage.status === 'COMPLETED';
    return (
      <div className="learning-content">
        <div className="scenario-header">
          <div>
            <span className="label">任务分类</span>
            <strong>{learningPackage.scenarioName}</strong>
          </div>
          <span className="status-pill">{learningPackage.status}</span>
        </div>

        {taskGoal && (
          <section className="task-goal-panel">
            <span className="label">任务目标</span>
            <p>{taskGoal}</p>
            <small>{registerNote}</small>
            {taskChecklist.length > 0 && (
              <div className="task-checklist" aria-label="待完成任务">
                <span className="label">待完成任务</span>
                {taskChecklist.map((item) => (
                  <div className={`task-checklist-item ${item.done ? 'done' : ''}`} key={item.key}>
                    <span aria-hidden="true">{item.done ? '✓' : '□'}</span>
                    <strong>{item.label}</strong>
                  </div>
                ))}
              </div>
            )}
            {taskFacts.length > 0 && (
              <div className="task-facts-grid">
                {taskFacts.map(([key, value]) => (
                  <div className="task-fact" key={key}>
                    <span>{formatTaskFactLabel(key)}</span>
                    <strong>{formatTaskFactValue(key, String(value))}</strong>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        <section className="dual-agent-layout">
          <div className="roleplay-area">
            <div className="agent-heading">
              <div>
                <span className="label">Roleplay Agent</span>
                <strong>{content.roleplayAgent?.role ?? 'Task partner'}</strong>
              </div>
            </div>

            <div className="dialogue-thread">
              {content.roleplayAgent?.openingLine && (
                <div className="message-row agent">
                  <div className="message-bubble">
                    <span>Roleplay</span>
                    <p>{content.roleplayAgent.openingLine}</p>
                  </div>
                </div>
              )}

              {dialogueTurns.map((turn) => (
                <div className="turn-group" key={turn.turnId}>
                  <div className="message-row user">
                    <div className="message-bubble">
                      <span>You</span>
                      <p>{turn.userMessage}</p>
                    </div>
                  </div>
                  <div className="message-row agent">
                    <div className="message-bubble">
                      <span>Roleplay</span>
                      <p>{turn.roleplayReply}</p>
                    </div>
                  </div>
                </div>
              ))}

              {dialogueBusy && (
                <div className="message-row agent" aria-live="polite">
                  <div className="message-bubble waiting-bubble">
                    <span>Roleplay</span>
                    <div className="waiting-indicator">
                      <span className="loading-spinner" aria-hidden="true" />
                      <p>对方正在回复...</p>
                    </div>
                  </div>
                </div>
              )}
            </div>

            <form className="dialogue-form" onSubmit={submitDialogueMessage}>
              <textarea
                value={dialogueMessage}
                onChange={(event) => setDialogueMessage(event.target.value)}
                onKeyDown={submitDialogueOnEnter}
                placeholder="Type your English reply..."
                rows={3}
                disabled={learningCompleted}
              />
              <button className="secondary-button compact-button" type="button" onClick={requestMentorHint} disabled={dialogueBusy || learningCompleted || mentorHintBusy}>
                请求 Mentor 提示
              </button>
              <button className="secondary-button compact-button" type="button" onClick={addPreciseHint} disabled={dialogueBusy || learningCompleted || mentorHintBusy || !preciseHintUnlocked}>
                精确提示
              </button>
              <button className="refresh-button compact-button" type="submit" disabled={dialogueBusy || learningCompleted}>
                发送
              </button>
            </form>
            {dialogueError && <p className="error compact">对话失败：{dialogueError}</p>}
          </div>

          <aside className="mentor-panel">
            <div className="agent-heading">
              <div>
                <span className="label">Mentor Agent</span>
                <strong>Feedback history</strong>
              </div>
            </div>

            <div className="dialogue-thread mentor-thread">
              {dialogueTurns.length === 0 && mentorHints.length === 0 && !mentorHintBusy ? (
                <p className="hint">Mentor feedback will appear after your first reply.</p>
              ) : (
                <>
                  {dialogueTurns.map((turn) => (
                    <div className="turn-group" key={`mentor-${turn.turnId}`}>
                      <div className="message-row user">
                        <div className="message-bubble">
                          <span>You</span>
                          <p>{turn.userMessage}</p>
                        </div>
                      </div>
                      <div className="message-row agent">
                        <div className="message-bubble mentor-bubble">
                          <span>Mentor</span>
                          <p>{turn.mentorFeedback}</p>
                          {turn.corrections.length > 0 && (
                            <div className="mentor-detail">
                              <span className="label">Corrections</span>
                              {turn.corrections.map((correction) => (
                                <div className="correction-item" key={`${turn.turnId}-${correction.original}-${correction.suggestion}`}>
                                  <strong>{correction.suggestion}</strong>
                                  <p>{correction.reason}</p>
                                </div>
                              ))}
                            </div>
                          )}
                          <div className="mentor-detail natural-expression">
                            <span className="label">Natural expression</span>
                            <p>{turn.naturalExpression}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                  {mentorHints.map((hint) => (
                    <div className="message-row agent" key={hint.id}>
                      <div className={`message-bubble mentor-bubble ${hint.level === 'PRECISE' ? 'precise-hint' : ''}`}>
                        <span>{hint.level === 'PRECISE' ? 'Mentor precise hint' : 'Mentor hint'}</span>
                        <p>{hint.text}</p>
                      </div>
                    </div>
                  ))}
                  {mentorHintBusy && (
                    <div className="message-row agent" aria-live="polite">
                      <div className="message-bubble mentor-bubble waiting-bubble">
                        <span>Mentor</span>
                        <div className="waiting-indicator">
                          <span className="loading-spinner" aria-hidden="true" />
                          <p>Mentor 正在准备提示...</p>
                        </div>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          </aside>
        </section>

        {content.targetVocabulary && content.targetVocabulary.length > 0 && (
          <section>
            <h3>本轮目标词</h3>
            <div className="expression-list">
              {content.targetVocabulary.map((target) => (
                <div className="expression-item" key={String(target.senseId ?? target.word ?? 'target-vocabulary')}>
                  <strong>{target.word}</strong>
                  <p>{target.definitionZh || target.definitionEn}</p>
                  <small>
                    {target.difficultyLevel || '未分级'} · {target.frequencyBand || '未分频'}
                  </small>
                </div>
              ))}
            </div>
          </section>
        )}

        {preciseHintPromptVisible && (
          <div className="hint-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="precise-hint-title">
            <div className="hint-modal">
              <h3 id="precise-hint-title">需要精确提示吗？</h3>
              <p>Mentor 可以直接给出可用单词、句型和参考表达。这样会降低自主思考比例。</p>
              <div className="hint-modal-actions">
                <button className="secondary-button compact-button" type="button" onClick={continueMentorHintAfterPrompt} disabled={mentorHintBusy}>
                  继续普通提示
                </button>
                <button className="refresh-button compact-button" type="button" onClick={addPreciseHint} disabled={mentorHintBusy}>
                  给我精确提示
                </button>
              </div>
            </div>
          </div>
        )}

        {completionPromptVisible && (
          <div className="hint-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="learning-complete-title">
            <div className="hint-modal">
              <h3 id="learning-complete-title">本轮学习已完成</h3>
              <p>{completionReason || '你已经完成当前任务目标，可以进入下一轮学习。'}</p>
              <div className="hint-modal-actions">
                <button className="secondary-button compact-button" type="button" onClick={() => setCompletionPromptVisible(false)}>
                  留在本轮
                </button>
                <button className="refresh-button compact-button" type="button" onClick={startNextRoundAfterCompletion} disabled={learningBusy}>
                  开始下一轮
                </button>
              </div>
            </div>
          </div>
        )}

        <section className="learning-plan-section">
          <div className="panel-heading compact-heading">
            <div>
              <p className="eyebrow">Learning plan</p>
              <h2>Review and new senses</h2>
            </div>
            {reviewBusy && <span className="status-pill">Loading</span>}
          </div>
          {renderReviewPlan()}
        </section>

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

  function fallbackTaskGoal(scenarioCode?: string) {
    switch (scenarioCode) {
      case 'hotel_check_in':
        return '你是 Alex Chen，今晚到 Harbor View Hotel 办理入住。你已经预订两晚，想要安静的大床房，护照已准备好。请用英语完成入住，并询问早餐时间和 Wi-Fi。';
      case 'shopping_return':
        return '你昨天买了一副无线耳机，左边没有声音。你带了收据，用银行卡付款。请用英语说明问题，并申请退款或换货。';
      case 'bank_account':
        return '你是 Alex Chen，要开一个储蓄账户。你带了护照和地址证明，想申请借记卡，并需要询问月费和所需材料。';
      case 'police_stop':
        return '你晚上正走去地铁站，被警察拦下。你带了身份证件，没有开车。请冷静询问被拦下的原因，并询问下一步该怎么做。';
      default:
        return undefined;
    }
  }

  function fallbackExpectedLearnerAction(scenarioCode?: string) {
    switch (scenarioCode) {
      case 'hotel_check_in':
        return '用英语说明要入住，给出姓名 Alex Chen，说明已预订两晚，提出想要安静的大床房，并询问早餐时间和 Wi-Fi。';
      case 'shopping_return':
        return '用英语说明耳机左边没有声音、昨天购买、带了收据，并礼貌申请退款或换货。';
      case 'bank_account':
        return '用英语说明想开储蓄账户，提到护照和地址证明，询问借记卡、月费和所需材料。';
      case 'police_stop':
        return '用英语冷静询问原因，说明你正走去地铁站，如被要求则说明带了证件，并询问下一步该怎么做。';
      default:
        return undefined;
    }
  }

  function taskRegisterNote(content: ReturnType<typeof parseLearningPackageContent>) {
    const scenarioCode = content.scenario?.code;
    const register = content.learningTask?.register ?? fallbackTaskRegister(scenarioCode);
    const guidance = fallbackRegisterGuidance(scenarioCode);
    return `语域：${formatTaskRegister(register)}。${guidance}`;
  }

  function buildTaskChecklist(content: ReturnType<typeof parseLearningPackageContent>): TaskChecklistItem[] {
    const scenarioCode = content.scenario?.code;
    const transcript = dialogueTurns.map((turn) => turn.userMessage).join(' ').toLowerCase();
    const affirmedPrompt = (...signals: string[]) => affirmedInDialogueContext(dialogueTurns, signals);
    switch (scenarioCode) {
      case 'hotel_check_in':
        return [
          {
            key: 'checkIn',
            label: '说明要办理入住',
            done: containsAnyText(transcript, 'check in', 'checking in', 'reservation', 'stay', 'room')
          },
          {
            key: 'reservation',
            label: '确认预订或给出 Alex Chen',
            done: containsAnyText(transcript, 'reservation', 'reserved', 'booked', 'here you are', 'alex')
              || affirmedPrompt('reservation')
          },
          {
            key: 'roomPreference',
            label: '说明想要安静的大床房',
            done: containsAnyText(transcript, 'quiet room', 'quiet', 'queen room')
              || affirmedPrompt('quiet room', 'room near', 'room would', 'room prefer')
          },
          {
            key: 'breakfast',
            label: '询问早餐时间或早餐服务',
            done: transcript.includes('breakfast') || affirmedPrompt('breakfast')
          },
          {
            key: 'wifi',
            label: '询问 Wi-Fi 信息',
            done: transcript.includes('wifi') || transcript.includes('wi-fi') || affirmedPrompt('wifi', 'wi-fi')
          }
        ];
      case 'shopping_return':
        return [
          { key: 'item', label: '说明物品是无线耳机', done: containsAnyText(transcript, 'headphones', 'earphones') },
          { key: 'problem', label: '说明左边没有声音', done: containsAnyText(transcript, 'left side', 'no sound') },
          { key: 'purchaseTime', label: '说明昨天购买', done: transcript.includes('yesterday') },
          { key: 'receipt', label: '说明有收据', done: transcript.includes('receipt') },
          { key: 'outcome', label: '申请退款或换货', done: containsAnyText(transcript, 'refund', 'exchange', 'return') }
        ];
      case 'bank_account':
        return [
          { key: 'accountType', label: '说明要开储蓄账户', done: containsAnyText(transcript, 'savings account', 'open an account') },
          { key: 'documents', label: '提到护照和地址证明', done: transcript.includes('passport') && containsAnyText(transcript, 'proof of address', 'address') },
          { key: 'debitCard', label: '询问借记卡', done: containsAnyText(transcript, 'debit card', 'card') },
          { key: 'fees', label: '询问月费', done: containsAnyText(transcript, 'monthly fee', 'fees') },
          { key: 'requiredDocuments', label: '询问所需材料', done: containsAnyText(transcript, 'documents', 'what do i need') }
        ];
      case 'police_stop':
        return [
          { key: 'reason', label: '询问被拦下原因', done: containsAnyText(transcript, 'why', 'reason') },
          { key: 'destination', label: '说明正走去地铁站', done: containsAnyText(transcript, 'subway', 'station') },
          { key: 'id', label: '说明带了证件', done: containsAnyText(transcript, 'id', 'identity') },
          { key: 'nextStep', label: '询问下一步怎么做', done: containsAnyText(transcript, 'next', 'what should i do') }
        ];
      default:
        return [];
    }
  }

  function containsAnyText(value: string, ...candidates: string[]) {
    return candidates.some((candidate) => value.includes(candidate));
  }

  function affirmedInDialogueContext(
    turns: LearningDialogueTurn[],
    promptSignals: string[]
  ) {
    for (let index = 1; index < turns.length; index += 1) {
      const userMessage = turns[index].userMessage.trim().toLowerCase();
      const previousRoleplayReply = turns[index - 1].roleplayReply.toLowerCase();
      if (isAffirmativeText(userMessage) && promptSignals.some((signal) => previousRoleplayReply.includes(signal))) {
        return true;
      }
    }
    return false;
  }

  function isAffirmativeText(value: string) {
    return value === 'yes'
      || value === 'yeah'
      || value === 'yep'
      || value === 'sure'
      || value === 'ok'
      || value === 'okay'
      || value === 'please'
      || value.startsWith('yes,')
      || value.startsWith('yes.')
      || value.startsWith('sure,')
      || value.startsWith('ok,')
      || value.startsWith('okay,');
  }

  function fallbackTaskRegister(scenarioCode?: string) {
    switch (scenarioCode) {
      case 'hotel_check_in':
      case 'shopping_return':
        return 'daily_service';
      case 'bank_account':
        return 'business_service';
      case 'police_stop':
        return 'formal_sensitive';
      default:
        return 'daily_conversation';
    }
  }

  function fallbackRegisterGuidance(scenarioCode?: string) {
    switch (scenarioCode) {
      case 'hotel_check_in':
        return '日常服务场景，可以用自然、简短、礼貌的口语表达，不必强行写长句。';
      case 'shopping_return':
        return '日常服务场景，可以直接说明问题；清楚自然比复杂长句更重要。';
      case 'bank_account':
        return '商务服务场景，要礼貌清楚，但仍然可以保持句子简洁。';
      case 'police_stop':
        return '严肃敏感场景，要冷静、尊重、清楚，避免俚语和对抗性表达。';
      default:
        return '日常对话场景，意思清楚时可以使用口语化和简化表达。';
    }
  }

  function formatTaskRegister(register: string) {
    const labels: Record<string, string> = {
      daily_service: '日常服务',
      business_service: '商务服务',
      formal_sensitive: '严肃敏感',
      daily_conversation: '日常口语'
    };
    return labels[register] ?? register;
  }

  function fallbackTaskFacts(scenarioCode?: string): Record<string, string> | undefined {
    switch (scenarioCode) {
      case 'hotel_check_in':
        return {
          learnerName: 'Alex Chen',
          hotelName: 'Harbor View Hotel',
          arrival: 'tonight',
          reservation: 'two nights under Alex Chen',
          roomPreference: 'quiet queen room',
          document: 'passport ready',
          questionsToAsk: 'breakfast time and Wi-Fi'
        };
      case 'shopping_return':
        return {
          item: 'wireless headphones',
          purchaseTime: 'yesterday',
          problem: 'the left side has no sound',
          receipt: 'available',
          payment: 'paid by card',
          desiredOutcome: 'refund or exchange'
        };
      case 'bank_account':
        return {
          learnerName: 'Alex Chen',
          accountType: 'savings account',
          documents: 'passport and proof of address',
          requestedService: 'debit card',
          questionsToAsk: 'monthly fees and required documents'
        };
      case 'police_stop':
        return {
          situation: 'walking to the subway at night',
          transport: 'not driving',
          document: 'ID is available',
          tone: 'calm and polite',
          questionsToAsk: 'why you were stopped and what to do next'
        };
      default:
        return undefined;
    }
  }

  function formatTaskFactLabel(value: string) {
    const labels: Record<string, string> = {
      learnerName: '姓名',
      hotelName: '地点',
      arrival: '时间',
      reservation: '预订信息',
      roomPreference: '房间偏好',
      document: '证件',
      questionsToAsk: '需要询问',
      item: '物品',
      purchaseTime: '购买时间',
      problem: '问题',
      receipt: '收据',
      payment: '付款方式',
      desiredOutcome: '目标',
      accountType: '账户类型',
      documents: '材料',
      requestedService: '服务',
      situation: '情况',
      transport: '交通状态',
      tone: '语气'
    };
    return labels[value] ?? value;
  }

  function formatTaskFactValue(key: string, value: string) {
    const normalized = value.toLowerCase();
    const byKey: Record<string, string> = {
      arrival: normalized === 'tonight' ? '今晚' : value,
      reservation: normalized === 'two nights under alex chen' ? '已预订两晚，姓名 Alex Chen' : value,
      roomPreference: normalized === 'quiet queen room' ? '安静的大床房' : value,
      document: normalized === 'passport ready' ? '护照已准备好' : normalized === 'id is available' ? '已带身份证件' : value,
      questionsToAsk: normalized === 'breakfast time and wi-fi'
        ? '早餐时间、Wi-Fi 信息'
        : normalized === 'monthly fees and required documents'
          ? '月费、所需材料'
          : normalized === 'why you were stopped and what to do next'
            ? '被拦下原因、下一步做法'
            : value,
      item: normalized === 'wireless headphones' ? '无线耳机' : value,
      purchaseTime: normalized === 'yesterday' ? '昨天' : value,
      problem: normalized === 'the left side has no sound' ? '左边没有声音' : value,
      receipt: normalized === 'available' ? '已带收据' : value,
      payment: normalized === 'paid by card' ? '银行卡付款' : value,
      desiredOutcome: normalized === 'refund or exchange' ? '退款或换货' : value,
      accountType: normalized === 'savings account' ? '储蓄账户' : value,
      documents: normalized === 'passport and proof of address' ? '护照和地址证明' : value,
      requestedService: normalized === 'debit card' ? '借记卡' : value,
      situation: normalized === 'walking to the subway at night' ? '晚上正走去地铁站' : value,
      transport: normalized === 'not driving' ? '没有开车' : value,
      tone: normalized === 'calm and polite' ? '冷静、礼貌' : value
    };
    return byKey[key] ?? value;
  }
}
