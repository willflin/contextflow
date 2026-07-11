# ContextFlow

## 项目定位 / Positioning

ContextFlow 是一个 AI 驱动的语境化英语学习系统。

ContextFlow is an AI-driven contextual English learning system.

长期目标：

Long-term goals:

- 基于真实场景的水平测试 / placement testing based on real scenarios
- AI 预生成学习包 / pre-generated learning packages
- AI 预生成复习包 / pre-generated review packages
- Roleplay Agent + Mentor Agent 双 Agent 学习 / dual-agent learning
- 学习事件与掌握度追踪 / learning events and mastery tracking
- 管理员后台、AI 审计、流量监控 / admin operations, AI audit, and traffic monitoring

## 当前范围 / Current Scope

当前已完成到 Phase 4.3 学习单元级事件基础：

This repository currently includes the Phase 4.3 learning-unit event foundation:

- Spring Boot 后端 / Spring Boot backend
- React + TypeScript + Vite 前端 / React + TypeScript + Vite frontend
- 后端健康检查接口 / backend health check API
- 前端健康检查页面 / frontend health check page
- JWT 登录、注册、角色权限 / JWT login, registration, role-based access
- MySQL 用户表 / MySQL users table
- 水平测试 READY 题池 / READY placement item pool
- 水平测试会话与临时评分 / placement sessions and temporary scoring
- 自适应逐题测试基础 / adaptive step-by-step placement foundation
- 用户水平画像 / user level profile
- 前端学习者测试流程与管理员调试台 / frontend learner placement flow and admin debug console
- 场景模板基础表 / scenario template foundation
- READY 学习包基础表 / READY learning package foundation
- 学习者网页获取下一个 READY 场景包 / learner web flow for fetching the next READY scenario package
- 双 Agent 对话接口骨架 / dual-agent dialogue API foundation
- 学习者网页双 Agent 对话界面 / learner web dual-agent dialogue UI
- 对话轮次持久化 / dialogue turn persistence
- 学习单元基础表 / learning unit foundation
- 学习单元级事件落库 / learning-unit occurrence event persistence

暂未加入：

Not included yet:

- Redis
- Docker
- Kafka
- AI API
- 音频生成 / audio generation

这些能力会在业务依据明确后再加入。

These capabilities will be added only after their business purpose is clear.

## 后端 / Backend

路径 / Path:

```text
backend/
```

健康检查接口 / Health API:

```text
GET /api/health
```

## 前端 / Frontend

路径 / Path:

```text
frontend/
```

前端通过 Vite proxy 调用 `/api/health`。

The frontend calls `/api/health` through the Vite proxy.

## 本地验证 / Local Verification

后端启动后访问：

After the backend starts, open:

```text
http://localhost:8080/api/health
```

前端启动后访问：

After the frontend starts, open:

```text
http://localhost:5173
```

验收标准：

Acceptance:

```text
Backend status: UP
Service: contextflow-backend
```

## Phase 1.1 测试账号 / Phase 1.1 Test Accounts

当前用户仍是内存账号，但 token 已升级为真实 JWT。

Current users are still in memory, but tokens are now real JWTs.

```text
learner / learner123
admin / admin123
```

## Phase 1.2 权限验证 / Phase 1.2 Access Check

当前 JWT 已用于接口权限保护。

JWTs are now used for API access control.

```text
/api/learner/**  LEARNER or ADMIN
/api/admin/**    ADMIN only
```

## Phase 2.1 数据库 / Phase 2.1 Database

本地开发数据库：

Local development database:

```text
MySQL 8
database: contextflow
username: contextflow
password: 123456
```

建表 SQL 保存在：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V1__create_users.sql
docs/sql/phase2_mysql_schema.sql
```

## Phase 2.2 注册 / Phase 2.2 Registration

普通用户可以通过 `/api/auth/register` 注册，默认角色固定为 `LEARNER`。

Learners can register through `/api/auth/register`; the default role is always `LEARNER`.

管理员账号不允许通过前端注册创建。

Admin accounts cannot be created through frontend registration.

## Phase 3.1 水平测试题池 / Phase 3.1 Placement Item Pool

水平测试题保存在 `placement_items` 表。用户只读取 `READY` 状态题目。

Placement test items are stored in the `placement_items` table. Users only receive `READY` items.

```text
GET /api/placement/items/sample
```

权限 / Access:

```text
LEARNER or ADMIN
```

建表 SQL 保存在：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V2__create_placement_items.sql
docs/sql/phase3_placement_schema.sql
```

## Phase 3.2 水平测试会话 / Phase 3.2 Placement Session

学习者可以开始一次水平测试、提交答案，并得到临时评分。

Learners can start a placement session, submit answers, and receive a temporary score.

```text
POST /api/placement/session/start
POST /api/placement/session/{sessionId}/submit
```

建表 SQL 保存在：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V3__create_placement_sessions.sql
docs/sql/phase3_placement_session_schema.sql
```

## Phase 3.2.1 自适应测试基础 / Phase 3.2.1 Adaptive Placement Foundation

当前新增逐题测试接口，为最终版“AI 预生成题池 + 动态调难度”铺路。

Step-by-step placement APIs are now available, preparing for the final AI pre-generated adaptive test flow.

```text
POST /api/placement/session/adaptive/start
POST /api/placement/session/{sessionId}/answer
```

关键字段 / Key fields:

```text
placement_items.difficulty_score
placement_items.ability_dimension
placement_items.grading_type
placement_sessions.mode
placement_sessions.current_difficulty_score
```

后续可支持的题型 / Future item types:

```text
TRUE_FALSE
SYNONYM_CHOICE
ANTONYM_CHOICE
CLOZE_TEXT
```

后续可支持的判分方式 / Future grading types:

```text
LOCAL_EXACT
LOCAL_ACCEPTED_ANSWERS
AI_JUDGE
```

## Phase 3.3 用户水平画像 / Phase 3.3 User Level Profile

水平测试结束后，系统会写入用户画像，作为后续学习包生成输入。

After placement testing finishes, the system stores a user level profile for later learning package generation.

```text
GET /api/user/profile
```

画像字段 / Profile fields:

```text
cefr_level
dimension_scores
weak_scenarios
weak_abilities
last_placement_session_id
```

建表 SQL 保存在：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V5__create_user_level_profiles.sql
docs/sql/phase3_user_level_profile_schema.sql
```

## Phase 4.1 学习包基础 / Phase 4.1 Learning Package Foundation

当前已加入场景模板和用户 READY 学习包基础结构。

Scenario templates and learner READY learning package foundations are now available.

```text
GET /api/learning/packages/next
```

当前阶段先使用 `SEEDED_TEMPLATE` 内容模拟未来 AI 预生成结果；后续会替换为 `AI_GENERATED` 生成链路。

This phase uses `SEEDED_TEMPLATE` content as a stand-in for future AI pre-generated packages; later phases will replace it with the `AI_GENERATED` generation pipeline.

前端学习者页面现在可以在完成水平测试并生成画像后，点击 Start learning 获取下一个 READY 场景学习包。

The learner page can now fetch the next READY scenario package after placement creates a user profile.

建表 SQL 保存位置：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V6__create_learning_foundation.sql
docs/sql/phase4_learning_foundation_schema.sql
```

## Phase 4.2 双 Agent 对话骨架 / Phase 4.2 Dual-Agent Dialogue Foundation

当前学习页面已从静态学习包展示升级为双 Agent 对话界面。

The learning page has been upgraded from a static package view to a dual-agent dialogue UI.

```text
POST /api/learning/packages/{packageId}/dialog
```

当前阶段先用本地规则模拟 AI 返回，响应结构对齐未来模型输出：

This phase uses local rules to simulate AI output, while keeping the response shape aligned with future model output:

```text
roleplayReply
mentorFeedback
corrections
naturalExpression
scoringSignal
```

建表 SQL 保存位置：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V7__create_learning_dialogue_turns.sql
docs/sql/phase4_dual_agent_dialogue_schema.sql
```

## Phase 4.3 学习单元级事件 / Phase 4.3 Learning-Unit Events

学习事件基于学习单元，而不是基于整轮对话。学习单元包括单词、词组和句型；用户输入或 Agent 输出中，每出现一次学习单元，就写入一条事件。

Learning events are based on learning units, not whole dialogue turns. Learning units include words, phrases, and sentence patterns; every occurrence in learner input or Agent output creates one event row.

当前双 Agent 对话会记录：

The current dual-agent dialogue records:

```text
UNIT_ATTEMPTED     用户输入中出现 / appears in learner input
UNIT_EXPOSED       Roleplay Agent 回复中出现 / appears in Roleplay Agent output
UNIT_CORRECTED     Mentor 纠错建议中出现 / appears in Mentor correction suggestions
UNIT_RECOMMENDED   Mentor 自然表达建议中出现 / appears in Mentor natural-expression suggestions
```

建表 SQL 保存位置：

Schema SQL is kept at:

```text
backend/src/main/resources/db/migration/V8__create_learning_units_and_events.sql
docs/sql/phase4_learning_event_schema.sql
```

## 前端调试入口 / Frontend Debug Entry

普通学习者登录后只看到正式学习流程：开始水平测试、逐题作答、查看自己的等级。

Learners only see the normal product flow: start placement, answer items, and view their level.

管理员登录后额外看到调试台：健康检查、权限探测、测试沙盒、原始画像 JSON。

Admins additionally see debug tools: health check, access probes, placement sandbox, and raw profile JSON.

## 开发原则 / Development Principle

每个中间件都必须有清晰业务依据。

Each middleware must have a clear business reason.

- Redis：缓存 READY 学习/复习包、限流、token 黑名单 / cache READY learning/review packages, rate limiting, token blacklist
- Docker：稳定本地和演示环境 / stable local and demo environment
- Kafka/RabbitMQ：后期异步事件和生成任务处理 / later async event and generation processing
- MinIO：后期音频和对象存储 / later audio/object storage
- Prometheus/Grafana：后期 AI 成本和系统监控 / later AI cost and system monitoring
