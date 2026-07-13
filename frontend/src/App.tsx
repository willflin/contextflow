import { useEffect, useState } from 'react';
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
import { CurrentUser, fetchCurrentUser, login, register } from './api/auth';
import { fetchHealth, HealthStatus } from './api/health';
import {
  fetchNextLearningPackage,
  LearningDialogueTurn,
  LearningPackage,
  parseLearningPackageContent,
  sendLearningDialogueMessage
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
import { fetchVocabulary, VocabularyList, VocabularyStatusFilter } from './api/vocabulary';

type LoadState = 'idle' | 'loading' | 'success' | 'error';
type AuthMode = 'login' | 'register';
type LearnerView = 'home' | 'vocabulary';
type MentorHint = {
  id: number;
  level: 'HINT' | 'PRECISE';
  text: string;
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

export default function App() {
  const [state, setState] = useState<LoadState>('idle');
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [learnerView, setLearnerView] = useState<LearnerView>('home');
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
  const [vocabularyBusy, setVocabularyBusy] = useState(false);
  const [vocabularyError, setVocabularyError] = useState<string | null>(null);
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
  const [learningPackage, setLearningPackage] = useState<LearningPackage | null>(null);
  const [learningBusy, setLearningBusy] = useState(false);
  const [learningError, setLearningError] = useState<string | null>(null);
  const [dialogueTurns, setDialogueTurns] = useState<LearningDialogueTurn[]>([]);
  const [dialogueMessage, setDialogueMessage] = useState('');
  const [dialogueBusy, setDialogueBusy] = useState(false);
  const [dialogueError, setDialogueError] = useState<string | null>(null);
  const [mentorHints, setMentorHints] = useState<MentorHint[]>([]);
  const [mentorHintCount, setMentorHintCount] = useState(0);
  const [preciseHintPromptVisible, setPreciseHintPromptVisible] = useState(false);

  const isAdmin = currentUser?.role === 'ADMIN';
  const itemContent = currentItem ? parsePlacementItemContent(currentItem) : null;
  const options = itemContent?.options ?? [];

  useEffect(() => {
    void restoreCurrentUser();
  }, []);

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
      setPreciseHintPromptVisible(true);
    }, 60000);
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
      resetAgentRuntime();
      setLearnerView('home');
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
    resetPlacement();
    resetLearning();
    resetReviewPlan();
    resetAgentRuntime();
    setLearnerView('home');
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

  async function loadVocabulary() {
    const token = tokenOrNull();

    if (!token) {
      setVocabularyError('请先登录。');
      return;
    }

    setVocabularyBusy(true);
    setVocabularyError(null);

    try {
      const result = await fetchVocabulary(token, vocabularyStatus, vocabularyQuery, 500);
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
    setVocabularyBusy(false);
    setVocabularyError(null);
  }

  async function openVocabularyPage() {
    setLearnerView('vocabulary');
    if (!vocabulary && !vocabularyBusy) {
      await loadVocabulary();
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
    setDialogueTurns([]);
    setDialogueMessage('');
    setDialogueError(null);
    setDialogueBusy(false);
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
    } catch (exception) {
      setDialogueError(exception instanceof Error ? exception.message : 'Dialogue failed.');
    } finally {
      setDialogueBusy(false);
    }
  }

  function resetMentorHints() {
    setMentorHints([]);
    setMentorHintCount(0);
    setPreciseHintPromptVisible(false);
  }

  function requestMentorHint() {
    if (!learningPackage) {
      return;
    }
    if (mentorHintCount >= 3) {
      setPreciseHintPromptVisible(true);
      return;
    }
    const nextCount = mentorHintCount + 1;
    setMentorHintCount(nextCount);
    setMentorHints((previous) => [
      ...previous,
      {
        id: Date.now(),
        level: 'HINT',
        text: buildMentorHint(nextCount)
      }
    ]);
    if (nextCount >= 3) {
      setPreciseHintPromptVisible(true);
    }
  }

  function addPreciseHint() {
    setPreciseHintPromptVisible(false);
    setMentorHints((previous) => [
      ...previous,
      {
        id: Date.now(),
        level: 'PRECISE',
        text: buildPreciseHint()
      }
    ]);
  }

  function buildMentorHint(step: number) {
    const content = learningPackage ? parseLearningPackageContent(learningPackage) : {};
    const facts = content.learningTask?.facts ?? fallbackTaskFacts(content.scenario?.code) ?? {};
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
      return `先别急着写完整答案。看任务卡：${factSummary}。${wordNote}`;
    }

    if (step === 2) {
      return '先想这句话的功能：你是在提出请求、说明事实，还是询问信息？请先用一个礼貌开头，再接任务卡里的一个固定事实。';
    }

    return '可以先搭一个空框架，但不要直接套答案：I would like to ... / I have ... / Could you tell me ... ? 你需要自己把任务卡里的信息放进去。';
  }

  function buildPreciseHint() {
    const content = learningPackage ? parseLearningPackageContent(learningPackage) : {};
    const scenarioCode = content.scenario?.code;
    const targets = reviewPlan?.items.slice(0, 4).map((item) => item.canonicalText).join('、');
    const targetNote = targets ? `重点词可优先考虑：${targets}。` : '优先使用任务卡里的关键词。';
    switch (scenarioCode) {
      case 'hotel_check_in':
        return `${targetNote} 可以使用结构：I'd like to check in. The reservation is under Alex Chen. Could I have a quiet queen room? Also, could you tell me the breakfast time and Wi-Fi information?`;
      case 'shopping_return':
        return `${targetNote} 可以使用结构：I'd like to return or exchange these wireless headphones. I bought them yesterday, but the left side has no sound. I have the receipt.`;
      case 'bank_account':
        return `${targetNote} 可以使用结构：I'd like to open a savings account. I have my passport and proof of address. Could you tell me about the debit card, monthly fees, and required documents?`;
      case 'police_stop':
        return `${targetNote} 可以使用结构：Could you explain why I was stopped? I am walking to the subway, and I have my ID with me. What should I do next?`;
      default:
        return `${targetNote} 可以先用：I'd like to ... / I have ... / Could you tell me ... ?`;
    }
  }

  function tokenOrNull() {
    return window.localStorage.getItem('contextflow_token');
  }

  return (
    <main className="app-shell">
      <section className="hero">
        <p className="eyebrow">ContextFlow</p>
        <h1>语境化英语学习</h1>
        <p className="summary">
          先完成水平测试，再基于用户画像进入对话学习、词义掌握度和复习闭环。
        </p>
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
              <h2>下一个场景</h2>
            </div>
            <button
              className="secondary-button"
              type="button"
              onClick={loadNextLearningPackage}
              disabled={learningBusy || !profile}
            >
              开始学习
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
              <button className="secondary-button" type="button" onClick={loadVocabulary} disabled={vocabularyBusy}>
                刷新
              </button>
            </div>
          </div>
          {renderVocabularyPanel()}
        </section>
      </section>
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
              </div>
            ) : (
              <label className="text-answer">
                答案
                <input value={textAnswer} onChange={(event) => setTextAnswer(event.target.value)} />
              </label>
            )}
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
        <div className="level-badge">{profile.cefrLevel}</div>
        <p className="hint">后续学习会使用你的水平和薄弱项。</p>
      </div>
    );
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
          className="inline-form"
          onSubmit={(event) => {
            event.preventDefault();
            void loadVocabulary();
          }}
        >
          <select
            value={vocabularyStatus}
            onChange={(event) => setVocabularyStatus(event.target.value as VocabularyStatusFilter)}
          >
            <option value="ALL">全部</option>
            <option value="LEARNED">已学</option>
            <option value="UNLEARNED">未学</option>
          </select>
          <input
            value={vocabularyQuery}
            onChange={(event) => setVocabularyQuery(event.target.value)}
            placeholder="搜索单词，例如 go"
          />
          <button className="secondary-button" type="submit" disabled={vocabularyBusy}>
            查询
          </button>
        </form>

        <p className="hint">
          当前测试词库是按有效频率排名取前 500 个清洗候选，不是原始 CSV 的前 500 行。
        </p>

        {vocabularyError && <p className="error compact">词库加载失败：{vocabularyError}</p>}

        {!vocabulary && !vocabularyError && <p className="hint">登录后会加载当前测试词库。</p>}

        {vocabulary && (
          <div className="vocabulary-window">
            <p className="hint">
              匹配 {vocabulary.totalMatchedWords} 个单词，当前显示 {vocabulary.items.length} 个。
            </p>
            <div className="vocabulary-list">
              {vocabulary.items.map((word) => (
                <article className="vocabulary-card" key={word.learningUnitId}>
                  <div className="word-row">
                    <strong>{word.canonicalText}</strong>
                    <span className={`target-type ${word.learnedStatus.toLowerCase()}`}>
                      {word.learnedStatus === 'UNLEARNED' ? '未学' : word.learnedStatus === 'PARTIAL' ? '部分已学' : '已学'}
                    </span>
                  </div>
                  <small>
                    {word.learnedSenseCount}/{word.totalSenseCount} 个词义已学习
                  </small>
                  <div className="sense-list">
                    {word.senses.map((sense) => (
                      <div className="sense-line" key={sense.senseId}>
                        <span>{sense.partOfSpeech ?? 'OTHER'}</span>
                        <p>{sense.definitionZh ?? sense.definitionEn}</p>
                      </div>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          </div>
        )}
      </div>
    );
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
    const taskGoal = content.learningTask?.goal ?? fallbackTaskGoal(scenarioCode) ?? content.scenario?.description;
    const expectedLearnerAction = content.learningTask?.expectedLearnerAction ?? fallbackExpectedLearnerAction(scenarioCode);
    const taskFacts = Object.entries(content.learningTask?.facts ?? fallbackTaskFacts(scenarioCode) ?? {});
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
            {expectedLearnerAction && <small>{expectedLearnerAction}</small>}
            {taskFacts.length > 0 && (
              <div className="task-facts-grid">
                {taskFacts.map(([key, value]) => (
                  <div className="task-fact" key={key}>
                    <span>{formatTaskFactLabel(key)}</span>
                    <strong>{value}</strong>
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
                placeholder="Type your English reply..."
                rows={3}
              />
              <button className="secondary-button compact-button" type="button" onClick={requestMentorHint} disabled={dialogueBusy}>
                请求 Mentor 提示
              </button>
              <button className="refresh-button compact-button" type="submit" disabled={dialogueBusy}>
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
              {dialogueTurns.length === 0 && mentorHints.length === 0 ? (
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
                </>
              )}
            </div>
          </aside>
        </section>

        {preciseHintPromptVisible && (
          <div className="hint-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="precise-hint-title">
            <div className="hint-modal">
              <h3 id="precise-hint-title">需要精确提示吗？</h3>
              <p>Mentor 可以直接给出可用单词、句型和参考表达。这样会降低自主思考比例。</p>
              <div className="hint-modal-actions">
                <button className="secondary-button compact-button" type="button" onClick={() => setPreciseHintPromptVisible(false)}>
                  我再想想
                </button>
                <button className="refresh-button compact-button" type="button" onClick={addPreciseHint}>
                  给我精确提示
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
}
