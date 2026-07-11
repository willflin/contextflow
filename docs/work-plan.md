# ContextFlow 工作计划 / ContextFlow Work Plan

## Phase 0：基础框架 / Foundation

- 创建后端项目。 / Create backend project.
- 创建前端项目。 / Create frontend project.
- 添加后端健康检查。 / Add backend health check.
- 添加前端健康检查页面。 / Add frontend health check page.

暂不加入数据库、AI、Redis、Kafka 或 Docker。

No database, AI, Redis, Kafka, or Docker yet.

## Phase 1：认证与权限 / Auth

- 添加学习者/管理员角色。 / Add learner/admin roles.
- 添加注册/登录。 / Add register/login.
- 添加 JWT 认证。 / Add JWT authentication.
- 保护管理员接口。 / Protect admin APIs.

当前先实现内存版账号，不接数据库；Phase 1.3 将 token 升级为真实 JWT。

The current step keeps in-memory users without database; Phase 1.3 upgrades the token to a real JWT.

Phase 1.2 在 mock token 基础上保护接口路径。

Phase 1.2 protects API paths on top of mock tokens.

## Phase 2：数据库核心 / Database Core

- 添加 MySQL。 / Add MySQL.
- 添加迁移策略。 / Add migration strategy.
- 添加 users、language_units、scenario_templates、learning_events、unit_stats。 / Add users, language units, scenario templates, learning events, and unit stats.

Phase 2.1 先接入 MySQL、Flyway 和 users 表。

Phase 2.1 first adds MySQL, Flyway, and the users table.

Phase 2.2 添加普通学习者注册。

Phase 2.2 adds learner registration.

## Phase 3：水平测试 / Placement

- 添加水平测试题池。 / Add placement item pool. ✅ Phase 3.1
- 添加语境化测试流程。 / Add contextual test flow. ✅ Phase 3.2
- 生成初始用户画像。 / Generate initial user profile. ✅ Phase 3.3

Phase 3.1 先保存 READY 状态题目，并提供样例读取接口。

Phase 3.1 first stores READY items and exposes a sample read API.

Phase 3.2 添加测试会话、答题记录和临时评分。

Phase 3.2 adds placement sessions, answer records, and temporary scoring.

Phase 3.2.1 添加自适应测试基础字段与逐题接口。

Phase 3.2.1 adds adaptive placement fields and step-by-step APIs.

Phase 3.3 将水平测试结果写入用户画像。

Phase 3.3 writes placement results into the user level profile.

Phase 3.3.1 补齐网页调试入口：学习者走正式流程，管理员看到调试功能。

Phase 3.3.1 adds the web entry: learners use the normal flow, admins see debug tools.

## Phase 4：学习包 / Learning Packages

- 添加场景模板。 / Add scenario templates. ✅ Phase 4.1
- 添加学习包。 / Add learning packages. ✅ Phase 4.1
- 添加双 Agent 响应结构。 / Add dual-agent response structure. ✅ Phase 4.2
- 记录学习单元级学习事件。 / Record learning-unit learning events. ✅ Phase 4.3
- 落地语言单元分层模型。 / Implement the layered language unit model. ✅ Phase 4.4
- 增加基础语言单元只读查询。 / Add basic read-only language unit queries. ✅ Phase 4.6
- 增强单词语言单元匹配：标点、大小写、词边界与同一 unit 重叠去重；短语和句型暂不启用。 / Improve word-unit matching for punctuation, case, word boundaries, and same-unit overlap deduplication; phrases and sentence patterns are not enabled yet. ✅ Phase 4.9

Phase 4.1 先建立场景模板、READY 学习包和学习者网页入口，内容暂时用种子模板模拟未来 AI 预生成结果。

Phase 4.1 first adds scenario templates, READY learning packages, and the learner web entry. Seeded template content is used as a temporary stand-in for future AI pre-generation.

Phase 4.2 已把当前静态学习包展示替换为双 Agent 对话界面：Roleplay Agent 负责沉浸式英文场景对话，Mentor Agent 负责实时纠错、解释和更自然表达建议。当前使用本地规则模拟，后续替换为真实 AI 调用。

Phase 4.2 replaced the current static learning package view with a dual-agent dialogue UI: the Roleplay Agent handles immersive English roleplay, and the Mentor Agent gives live corrections, explanations, and more natural expression suggestions. It currently uses local rules and will later be replaced by real AI calls.

Phase 4.3 明确学习事件基于学习单元，而不是基于整轮对话。模型预留单词、词组和句型；当前业务先只启用单词，每出现一次单词语言单元，就写入一条 learning_events。

Phase 4.3 defines learning events as learning-unit occurrences, not whole dialogue turns. The model reserves words, phrases, and sentence patterns; current business only enables word units, and every word-unit occurrence in learner input or Agent output creates one learning_events row.

Phase 4.4 将语言单元拆为本体、词义、词形和来源：`learning_units`、`learning_unit_senses`、`learning_unit_forms`、`learning_data_sources`、`learning_unit_sense_sources`。用户词义掌握度表已建结构，但暂不实现掌握度算法。

Phase 4.4 splits language units into identity, senses, forms, and sources: `learning_units`, `learning_unit_senses`, `learning_unit_forms`, `learning_data_sources`, and `learning_unit_sense_sources`. The user sense-level mastery table exists structurally, but the mastery algorithm is not implemented yet.

Phase 4.6 增加 admin 只读查询接口，用于按标准文本或词形查询语言单元详情和词义。

Phase 4.6 adds admin-only read APIs for querying language unit details and senses by canonical text or surface form.

Phase 4.9 调整为 word-first：表结构仍保留 WORD / PHRASE / SENTENCE_PATTERN 扩展位，但当前事件记录只启用 WORD。单词按规范化 token 匹配，支持标点和大小写归一，避免 `goodbye` 误命中 `good`，并为每条事件 payload 保留 `occurrenceIndex` 与 token 范围。

Phase 4.9 is now word-first: the schema still reserves WORD / PHRASE / SENTENCE_PATTERN, but current event recording only enables WORD. Words are matched by normalized tokens, punctuation and case are normalized, false matches such as `good` inside `goodbye` are avoided, and each event payload keeps `occurrenceIndex` plus token range.

## Phase 5：复习系统 / Review

- 添加词义级掌握度最小更新。 / Add minimal sense-level mastery updates. ✅ Phase 5.1
- 添加事件输入/输出方向，用于后续复习优先级。 / Add event input/output direction for later review priority. ✅ Phase 5.1
- 添加 `next_review_at`。 / Add `next_review_at`. ✅ Phase 5.1 minimal version
- 添加持久化复习优先级分数。 / Add persisted review priority score. ✅ Phase 5.2
- 添加复习/新词义混合学习计划接口。 / Add mixed review/new-sense learning plan API. ✅ Phase 5.2
- 添加遗忘曲线调度字段和定期优先级刷新。 / Add forgetting-curve scheduling fields and periodic priority refresh. ✅ Phase 5.3
- 添加复习包。 / Add review packages.

Phase 5.1 明确掌握度只存在于 `learning_unit_senses`，不存在“语言单元本体掌握度”。单义词事件可自动归属到唯一 active sense 并更新 `user_learning_unit_sense_stats`；多义词在 Agent 未明确 sense 前只保留事件，不更新掌握度。

Phase 5.1 defines mastery as sense-level only, never language-unit-level. Events for single-sense words can be assigned to the only active sense and update `user_learning_unit_sense_stats`; multi-sense word events remain event-only until the Agent provides an explicit sense.

Phase 5.1 预留 Agent 工具接口：查询某个单词的所有释义、记录 Agent 判定后的学习事件、提交数据库缺失词义反馈。

Phase 5.1 reserves Agent tool APIs for listing all senses of a word, recording Agent-judged learning events, and submitting missing-sense feedback for manual content updates.

Phase 5.2 新增 `review_priority_score`，它是已学词义进入复习队列的唯一优先级依据；高频简单词仍记录事件和掌握度，但通过频率反相关系数降低复习压力。

Phase 5.2 adds `review_priority_score` as the only priority basis for learned senses entering the review queue; high-frequency simple words still record events and mastery, but frequency-inverse weighting reduces review pressure.

Phase 5.2 的学习计划先按比例混合复习词义与新词义，再把目标词义分配到 `learning_unit_sense_scenario_tags` 对应的场景组，避免强行把一批词塞进单一场景。

Phase 5.2 mixes review senses and new senses by ratio first, then assigns target senses into scenario groups through `learning_unit_sense_scenario_tags` instead of forcing one batch into one scenario.

Phase 5.3 新增 `stability_score`、`difficulty_score`、`last_reviewed_at` 和 `review_interval_hours`，用简单遗忘曲线和事件类型更新词义复习间隔。

Phase 5.3 adds `stability_score`, `difficulty_score`, `last_reviewed_at`, and `review_interval_hours`, using a simple forgetting curve and event type to update sense review intervals.

Phase 5.3 将 `/api/review/plan` 调整为读取持久化 `review_priority_score`；事件写入和后端定时任务负责刷新该分数。

Phase 5.3 changes `/api/review/plan` to read persisted `review_priority_score`; event writes and a backend scheduled job are responsible for refreshing the score.

## Phase 6：AI 预生成 / AI Pre-generation

- 固定 Agent 对话输入/输出 JSON 契约。 / Fix the Agent dialogue input/output JSON contract. ✅ Phase 6.1
- 引入 Spring AI + DeepSeek 运行时入口。 / Add the Spring AI + DeepSeek runtime entry. ✅ Phase 6.1.1
- 导入 ECDICT 原始词典表。 / Import ECDICT raw dictionary tables. ✅ Phase 6.2 raw
- 清洗 ECDICT 单词候选表。 / Clean ECDICT word candidate table. ✅ Phase 6.2 clean
- 添加生成任务。 / Add generation jobs.
- 添加 Prompt 模板。 / Add prompt templates.
- 添加结构化 JSON 校验。 / Add structured JSON validation.
- 添加 AI 使用日志。 / Add AI usage logs.

Phase 6.1 固定 `agent-dialogue.v1`：输入包含用户画像、READY 学习包、目标词义、历史对话和 Agent 工具入口；输出包含 `reply`、`feedback`、`corrections`、`naturalExpression`、`unitMentions` 和 `scoringSignal`。当前仍使用本地规则模拟，不接真实 AI。

Phase 6.1 fixes `agent-dialogue.v1`: input contains the learner profile, READY package, target senses, dialogue history, and Agent tool access; output contains `reply`, `feedback`, `corrections`, `naturalExpression`, `unitMentions`, and `scoringSignal`. The current implementation still uses local rules and does not call a real AI service.

Phase 6.1.1 引入 Spring AI DeepSeek starter，并预留真实模型入口；默认 `local`，只有显式配置 `spring-ai` provider 和 API key 后才调用模型，失败可回退本地规则。

Phase 6.1.1 adds the Spring AI DeepSeek starter and reserves a real model entry. The default provider is still `local`; model calls happen only after explicitly enabling the `spring-ai` provider and API key, with local fallback available on failure.

Phase 6.2 raw 新增 ECDICT 原始导入批次表和原始词条表，只保存外部 CSV 原始字段与频率排名，不直接写入业务词义表。

Phase 6.2 raw adds ECDICT raw import batch and entry tables, storing external CSV fields and frequency ranks without writing directly into business sense tables.

Phase 6.2 clean 新增 `ecdict_clean_word_entries`，只保留普通单词形态且有有效频率的候选项，排除短语、数字、连字符、撇号、缩写、专名和词根前后缀，不写入正式学习单元表。

Phase 6.2 clean adds `ecdict_clean_word_entries`, keeping ordinary word candidates with valid frequency and excluding phrases, digits, hyphens, apostrophes, abbreviations, proper names, and affix/root noise without writing into formal learning unit tables.

## Phase 7：Redis

只在需要 READY 内容缓存和 AI 限流后加入。

Add only after READY package caching and AI rate limiting are needed.

## Phase 8：音频 / Audio

- 添加音频资产元数据。 / Add audio asset metadata.
- 添加 TTS 生成。 / Add TTS generation.
- 添加缓存和过期策略。 / Add cache and expiry.

## Phase 9：管理员后台 / Admin

- 用户管理。 / User management.
- 场景模板管理。 / Scenario template management.
- Prompt 模板管理。 / Prompt template management.
- 反馈处理。 / Feedback handling.
- AI 审计。 / AI audit.
- 流量统计。 / Traffic analytics.

## Phase 10：异步中间件 / Async Middleware

只有当数据库任务轮询不够用时，才加入 Kafka 或 RabbitMQ。

Kafka or RabbitMQ is added only after database task polling is not enough.
