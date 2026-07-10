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
- 添加双 Agent 响应结构。 / Add dual-agent response structure.
- 记录学习事件。 / Record learning events.

Phase 4.1 先建立场景模板、READY 学习包和学习者网页入口，内容暂时用种子模板模拟未来 AI 预生成结果。

Phase 4.1 first adds scenario templates, READY learning packages, and the learner web entry. Seeded template content is used as a temporary stand-in for future AI pre-generation.

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
