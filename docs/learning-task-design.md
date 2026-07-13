# 学习任务设计 / Learning Task Design

## 核心约束 / Core Constraints

- `learningTask.goal` 是本轮学习的任务目标，Agent 必须以它作为对话推进依据。/ `learningTask.goal` is the learning task objective, and the Agent must use it as the basis for advancing the dialogue.
- `learningTask.instructionLanguage` 控制任务目标给用户的语言：低水平优先中文，高水平可直接英文。/ `learningTask.instructionLanguage` controls the language shown to the learner: Chinese for lower levels, English for higher levels.
- `learningTask.expectedLearnerAction` 描述用户下一步应尝试完成的英语行为。/ `learningTask.expectedLearnerAction` describes what the learner should try to do in English next.
- `learningTask.register` 和 `learningTask.registerGuidance` 描述任务语域，例如日常口语、商务服务、严肃敏感场景。/ `learningTask.register` and `learningTask.registerGuidance` describe the task register, such as daily spoken, business service, or formal sensitive contexts.
- `learningTask.facts` 必须给出用户完成任务所需的全部固定事实，不能要求用户临场编造姓名、房型、预订号、账户信息等。/ `learningTask.facts` must provide all fixed facts needed for the task, so the learner is never forced to invent names, room types, reservation codes, account information, or similar details.
- `learningTask.constraints` 必须限制 Agent 后续提问只能围绕已给事实和学习目标展开。/ `learningTask.constraints` must restrict Agent follow-up questions to the given facts and learning goals.
- `scenarioCode` 和 `scenarioName` 只保留为分类、标签和种子来源，不再代表完整学习场景。/ `scenarioCode` and `scenarioName` remain only as category, tag, and seed-source fields; they no longer represent the full learning scenario.
- 后续 AI 预生成内容应先根据目标词义生成或选择任务，再把词义分配到适合的任务中，而不是强行塞进单一场景。/ Future AI pre-generation should generate or select tasks from target senses first, then assign senses to suitable tasks instead of forcing all senses into one fixed scenario.

## 当前实现 / Current Implementation

- READY 学习包在 `contentJson.learningTask` 中保存任务目标、指令语言和预期用户动作。/ READY learning packages store the task goal, instruction language, and expected learner action in `contentJson.learningTask`.
- Agent 输入契约在 `learningPackage` 中显式暴露 `taskGoal`、`taskInstructionLanguage`、`expectedLearnerAction`、`taskFacts` 和 `taskConstraints`。/ The Agent input contract exposes `taskGoal`, `taskInstructionLanguage`, `expectedLearnerAction`, `taskFacts`, and `taskConstraints` explicitly inside `learningPackage`.
- 前端学习区优先展示“任务目标”，旧学习包缺少 `learningTask` 时回退到 `scenario.description`。/ The frontend learning area prioritizes "task goal" display and falls back to `scenario.description` for older packages.
- 当前不新增数据库表；任务内容先作为 READY 学习包 JSON 的一部分缓存。/ No new database table is added now; task content is cached as part of the READY package JSON.

## 角色连续性 / Role Continuity

- `roleplayAgent.persona` 定义 Roleplay Agent 的唯一身份，不能在同一任务中切换。/ `roleplayAgent.persona` defines the Roleplay Agent's only identity and must not change inside the same task.
- `roleplayAgent.learnerRole` 定义用户身份，Roleplay Agent 永远不能替用户说话。/ `roleplayAgent.learnerRole` defines the learner identity, and the Roleplay Agent must never speak for the learner.
- `roleplayAgent.openingLine` 已经展示给用户，并作为第 0 条历史传给模型，不能重复。/ `roleplayAgent.openingLine` is already shown to the learner and sent to the model as turn 0, so it must not be repeated.
- 用户输入模糊、只有问号或语法错误时，Roleplay Agent 应保持角色并澄清；Mentor Agent 负责解释语言问题。/ When learner input is unclear, only a question mark, or grammatically wrong, the Roleplay Agent should stay in character and clarify; the Mentor Agent explains language issues.
- 后端会拦截明显重复回复和说话人错位，并替换为安全的角色回复。/ The backend guards obvious repeated replies and speaker-role breaks, replacing them with safe in-character replies.

## 目标词义驱动 / Target-Sense Driven Dialogue

- Roleplay Agent 的每一句回复都必须服务于目标词义曝光、目标词义诱发，或有明确学习价值的任务相关表达。/ Every Roleplay reply must serve target-sense exposure, target-sense elicitation, or clearly useful task-related language.
- 允许少量自由发散，但必须带学习目的，例如引入更自然或更高级的表达。/ Limited expansion is allowed only when it has learning value, such as introducing a more natural or advanced expression.
- 禁止低学习价值的服务流程填充，例如查系统、拼姓名、询问预订号、反复索要证件或等待。/ Low-learning-value service filler is forbidden, such as system lookup, spelling names, reservation codes, repeated document checks, or waiting.
- 对话应围绕意图、选择、描述、原因、偏好、澄清等可产生语言学习价值的内容推进。/ Dialogue should advance through intent, choices, descriptions, reasons, preferences, and clarification that create learning value.
- 后续提问不能超出任务卡事实范围；如果真实服务流程需要额外事实，应跳过该流程，转向已给事实内的学习型问题。/ Follow-up questions must not exceed task-card facts; if a real service step needs extra facts, skip that step and ask a learning-focused question within the known facts.
- 后端会拦截明显低价值回复，并替换为更能诱发目标语言的角色回复。/ The backend guards obvious low-value replies and replaces them with role-consistent prompts that better elicit target language.

## Mentor 渐进提示 / Progressive Mentor Hints

- 用户不知道如何回复时，可以请求 Mentor 提示。/ When learners do not know how to reply, they can request Mentor hints.
- 普通提示最多三轮，只能给思考方向、任务事实、功能意图和空句型框架，不能直接给完整答案。/ Ordinary hints are limited to three rounds and may give thinking direction, task facts, communicative function, and blank sentence frames, but not full answers.
- 如果提示中涉及用户可能不会的新词，Mentor 可以告诉用户词义；如果只涉及已学词，Mentor 应提示用户这些词已经学过并鼓励回忆。/ If hints involve likely unknown new words, Mentor may explain their meanings; if only learned words are needed, Mentor should tell the learner they have learned them and encourage recall.
- 三轮普通提示后不立即打断；第 4 次请求提示时先拦截并询问是否需要精确提示。/ The system does not interrupt immediately after three ordinary hints; the fourth hint request is intercepted to ask whether a precise hint is needed.
- 用户超过三分钟未回复时，系统也会询问是否需要精确提示。/ If the learner does not reply for more than three minutes, the system also asks whether a precise hint is needed.
- 精确提示按钮前三轮禁用；提示确认框出现后解锁。/ The precise-hint button is disabled during the first three ordinary hints and unlocks after the confirmation prompt appears.
- 精确提示可以包含要用到的单词、句法和参考表达，但应明确这是降低自主思考比例的帮助。/ Precise hints may include words, syntax, and reference expressions, but should make clear that this reduces autonomous thinking.
- 精确提示必须围绕当前 Roleplay Agent 正在问的问题，不应直接给完整任务背景答案。/ Precise hints must focus on the current Roleplay Agent question and should not provide a full background-level task answer.
- Mentor 建议必须考虑任务语域；日常口语场景不应一概要求长句或商务化表达。/ Mentor suggestions must respect the task register; daily spoken contexts should not always be corrected into long or business-like sentences.

## 任务完成 / Task Completion

- Agent 只有在用户完成 `expectedLearnerAction` 的全部目标，并且没有超出 `taskFacts` 的固定事实时，才能设置 `scoringSignal.taskComplete=true`。/ The Agent may set `scoringSignal.taskComplete=true` only when the learner has completed all objectives in `expectedLearnerAction` without going beyond fixed `taskFacts`.
- 后端只信任结构化 `scoringSignal.taskComplete=true` 或用户显式点击“结束当前对话”，不会根据结束语文本猜测任务完成。/ The backend trusts only structured `scoringSignal.taskComplete=true` or the learner explicitly clicking "end current dialogue"; it does not infer completion from closing text.
- 后端收到完成信号或手动结束请求后把当前学习包标记为 `COMPLETED`，后续点击“开始学习/开始下一轮”会进入新的 READY 任务。/ After receiving a completion signal or manual end request, the backend marks the current learning package as `COMPLETED`; later "start learning/start next round" fetches a new READY task.
- 前端只负责展示完成弹窗和手动结束入口，不自行判断任务是否完成。/ The frontend only displays the completion dialog and manual end action; it does not decide task completion by itself.

## 跳过任务 / Skip Task

- 用户跳过当前任务时，系统会把当前学习计划中的词义写入延后记录，并适度降低已有复习词义的 `reviewPriorityScore`。/ When the learner skips the current task, the system writes deferral records for the current plan senses and moderately lowers `reviewPriorityScore` for existing review senses.
- 当前学习包标记为 `EXPIRED`，下一次进入学习会加载新的 READY 任务。/ The current learning package is marked `EXPIRED`, so the next learning entry loads a new READY task.
