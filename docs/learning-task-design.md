# 学习任务设计 / Learning Task Design

## 核心约束 / Core Constraints

- `learningTask.goal` 是本轮学习的任务目标，Agent 必须以它作为对话推进依据。/ `learningTask.goal` is the learning task objective, and the Agent must use it as the basis for advancing the dialogue.
- `learningTask.instructionLanguage` 控制任务目标给用户的语言：低水平优先中文，高水平可直接英文。/ `learningTask.instructionLanguage` controls the language shown to the learner: Chinese for lower levels, English for higher levels.
- `learningTask.expectedLearnerAction` 描述用户下一步应尝试完成的英语行为。/ `learningTask.expectedLearnerAction` describes what the learner should try to do in English next.
- `scenarioCode` 和 `scenarioName` 只保留为分类、标签和种子来源，不再代表完整学习场景。/ `scenarioCode` and `scenarioName` remain only as category, tag, and seed-source fields; they no longer represent the full learning scenario.
- 后续 AI 预生成内容应先根据目标词义生成或选择任务，再把词义分配到适合的任务中，而不是强行塞进单一场景。/ Future AI pre-generation should generate or select tasks from target senses first, then assign senses to suitable tasks instead of forcing all senses into one fixed scenario.

## 当前实现 / Current Implementation

- READY 学习包在 `contentJson.learningTask` 中保存任务目标、指令语言和预期用户动作。/ READY learning packages store the task goal, instruction language, and expected learner action in `contentJson.learningTask`.
- Agent 输入契约在 `learningPackage` 中显式暴露 `taskGoal`、`taskInstructionLanguage` 和 `expectedLearnerAction`。/ The Agent input contract exposes `taskGoal`, `taskInstructionLanguage`, and `expectedLearnerAction` explicitly inside `learningPackage`.
- 前端学习区优先展示“任务目标”，旧学习包缺少 `learningTask` 时回退到 `scenario.description`。/ The frontend learning area prioritizes "task goal" display and falls back to `scenario.description` for older packages.
- 当前不新增数据库表；任务内容先作为 READY 学习包 JSON 的一部分缓存。/ No new database table is added now; task content is cached as part of the READY package JSON.

## 角色连续性 / Role Continuity

- `roleplayAgent.persona` 定义 Roleplay Agent 的唯一身份，不能在同一任务中切换。/ `roleplayAgent.persona` defines the Roleplay Agent's only identity and must not change inside the same task.
- `roleplayAgent.learnerRole` 定义用户身份，Roleplay Agent 永远不能替用户说话。/ `roleplayAgent.learnerRole` defines the learner identity, and the Roleplay Agent must never speak for the learner.
- `roleplayAgent.openingLine` 已经展示给用户，并作为第 0 条历史传给模型，不能重复。/ `roleplayAgent.openingLine` is already shown to the learner and sent to the model as turn 0, so it must not be repeated.
- 用户输入模糊、只有问号或语法错误时，Roleplay Agent 应保持角色并澄清；Mentor Agent 负责解释语言问题。/ When learner input is unclear, only a question mark, or grammatically wrong, the Roleplay Agent should stay in character and clarify; the Mentor Agent explains language issues.
- 后端会拦截明显重复回复和说话人错位，并替换为安全的角色回复。/ The backend guards obvious repeated replies and speaker-role breaks, replacing them with safe in-character replies.
