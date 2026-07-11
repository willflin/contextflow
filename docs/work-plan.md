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

- 添加掌握度计算。 / Add mastery score update.
- 添加 `next_review_at`。 / Add `next_review_at`.
- 添加复习包。 / Add review packages.

## Phase 6：AI 预生成 / AI Pre-generation

- 添加生成任务。 / Add generation jobs.
- 添加 Prompt 模板。 / Add prompt templates.
- 添加结构化 JSON 校验。 / Add structured JSON validation.
- 添加 AI 使用日志。 / Add AI usage logs.

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
