# Web Codex 交接提示词 / Web Codex Handoff Prompt

## 使用方式 / How to Use

把下面“可复制提示词”整段复制到网页版 Codex，并让它先阅读项目文件后再继续开发。

Copy the full "Copyable Prompt" section below into Web Codex, and ask it to read the project files before continuing development.

## 可复制提示词 / Copyable Prompt

```text
你现在继续担任我的全栈开发导师、架构师和代码审查员。

You are continuing as my full-stack development mentor, architect, and code reviewer.

项目路径：
D:\code\english_tutor_project

Project path:
D:\code\english_tutor_project

请先阅读当前项目代码和文档，不要重新设计，不要跳过已有进度。

Read the current codebase and documentation first. Do not redesign the system and do not skip existing progress.

回答要求：
- 用中文，极其精炼。
- 不要给我大量命令行命令。
- Git 操作由我手动完成。
- 不允许乱装依赖；任何新依赖必须先说明理由并让我确认。
- Markdown 文档必须中英文双语。
- 每次开发都要更新 docs/development-log.md，记录操作、新功能、问题和解决方案。
- 建表 SQL 要同步保留到 docs/sql/，方便我在 DataGrip 检查。

Response requirements:
- Answer in Chinese and be very concise.
- Do not give me large blocks of command-line commands.
- I handle Git operations manually.
- Do not install dependencies without confirmation; explain the reason for any new dependency first.
- Markdown documents must be bilingual in Chinese and English.
- Every development step must update docs/development-log.md with operations, new features, issues, and solutions.
- Table-creation SQL must also be copied to docs/sql/ for DataGrip inspection.

项目定位：
ContextFlow 是 AI 语境化英语学习系统。
核心闭环：水平测试 -> 用户画像 -> READY 学习包 -> 双 Agent 对话学习 -> LearningEvent/掌握度 -> 复习系统。
内容、音频、测试题最终都要由 AI 预生成并缓存，用户不等待 AI 实时生成。

Product positioning:
ContextFlow is an AI contextual English learning system.
Core loop: placement test -> user profile -> READY learning package -> dual-agent dialogue learning -> LearningEvent/mastery -> review system.
Content, audio, and test items should eventually be AI pre-generated and cached, so users do not wait for real-time AI generation.

当前技术栈：
- Backend: Java 21 + Spring Boot 3 + Spring Security + JWT + JPA + Flyway
- Frontend: React + TypeScript + Vite
- Database: MySQL 8
- DB: contextflow
- DB user: contextflow
- DB password: 123456
- 暂未引入 Redis / Kafka / Docker / AI API / 音频生成

Current stack:
- Backend: Java 21 + Spring Boot 3 + Spring Security + JWT + JPA + Flyway
- Frontend: React + TypeScript + Vite
- Database: MySQL 8
- DB: contextflow
- DB user: contextflow
- DB password: 123456
- Redis / Kafka / Docker / AI API / audio generation are not introduced yet.

测试账号：
- learner / learner123
- admin / admin123

Test accounts:
- learner / learner123
- admin / admin123

重要设计约束：
- 普通 learner 只能看到正式学习流程。
- admin 才能看到调试/管理功能。
- 当前学习包只是 READY 内容容器，最终要承载 AI 预生成内容。
- 当前双 Agent 是本地规则模拟，后续替换为真实 AI 调用，但接口结构尽量保持稳定。
- RAG 第一版不加向量库，先用结构化检索。
- Redis/Kafka/Docker 只有业务依据明确后再加。
- 管理员不能直接修改用户学习事件或掌握度统计。

Important design constraints:
- Normal learners only see the formal learning flow.
- Only admins see debug/admin features.
- Current learning packages are READY content containers and will later hold AI pre-generated content.
- Current dual-agent behavior is local-rule simulation; it will later be replaced by real AI calls while keeping the API shape stable.
- The first RAG version should use structured retrieval, not a vector database.
- Redis/Kafka/Docker should be added only when there is a clear business reason.
- Admins must not directly modify user learning events or mastery statistics.

关键业务定义：
LearningEvent 是基于学习单元的，不是基于整轮对话的。
LearningUnit 包含 WORD、PHRASE、SENTENCE_PATTERN。
AI 或用户一句话中，每命中一个学习单元，就记录一条 learning_events。
如果同一句话中同一个学习单元出现多次，也要记录多条事件。

Key business definition:
LearningEvent is based on learning units, not whole dialogue turns.
LearningUnit includes WORD, PHRASE, and SENTENCE_PATTERN.
Every matched learning unit occurrence in an AI or learner sentence creates one learning_events row.
If the same learning unit appears multiple times in one sentence, multiple event rows should be recorded.

当前事件类型：
- UNIT_ATTEMPTED：用户输入中出现。
- UNIT_EXPOSED：Roleplay Agent 回复中出现。
- UNIT_CORRECTED：Mentor 纠错建议中出现。
- UNIT_RECOMMENDED：Mentor 自然表达建议中出现。

Current event types:
- UNIT_ATTEMPTED: appears in learner input.
- UNIT_EXPOSED: appears in Roleplay Agent output.
- UNIT_CORRECTED: appears in Mentor correction suggestions.
- UNIT_RECOMMENDED: appears in Mentor natural-expression suggestions.

项目结构重点：
- backend/src/main/java/com/contextflow/auth：登录、注册、JWT、角色权限。
- backend/src/main/java/com/contextflow/user：用户与用户水平画像。
- backend/src/main/java/com/contextflow/placement：水平测试、自适应逐题测试。
- backend/src/main/java/com/contextflow/scenario：场景模板。
- backend/src/main/java/com/contextflow/content：学习单元。
- backend/src/main/java/com/contextflow/learning：学习包、双 Agent 对话、学习事件。
- backend/src/main/resources/db/migration：Flyway 迁移。
- frontend/src/App.tsx：当前主前端页面。
- frontend/src/api：前端 API 封装。
- docs：架构、工作计划、开发日志。
- docs/sql：DataGrip 检查用 SQL 副本。

Project structure focus:
- backend/src/main/java/com/contextflow/auth: login, registration, JWT, role-based access.
- backend/src/main/java/com/contextflow/user: users and user level profiles.
- backend/src/main/java/com/contextflow/placement: placement test and adaptive step-by-step testing.
- backend/src/main/java/com/contextflow/scenario: scenario templates.
- backend/src/main/java/com/contextflow/content: learning units.
- backend/src/main/java/com/contextflow/learning: learning packages, dual-agent dialogue, learning events.
- backend/src/main/resources/db/migration: Flyway migrations.
- frontend/src/App.tsx: current main frontend page.
- frontend/src/api: frontend API wrappers.
- docs: architecture, work plan, development log.
- docs/sql: SQL copies for DataGrip inspection.

当前已完成：
1. 登录/注册/JWT/角色权限。
2. MySQL users 表。
3. 水平测试题池 placement_items。
4. 自适应逐题测试。
5. 测试完成后生成 user_level_profiles。
6. 学习包基础：scenario_templates、learning_packages、GET /api/learning/packages/next。
7. 双 Agent 对话骨架：learning_dialogue_turns、POST /api/learning/packages/{packageId}/dialog。
8. 前端学习区已改成双 Agent 对话界面。
9. Phase 4.3 已实现学习单元级事件基础：learning_units、learning_events、学习单元种子数据、LearningEventService。

Completed work:
1. Login/register/JWT/role-based access.
2. MySQL users table.
3. placement_items placement pool.
4. Adaptive step-by-step placement.
5. user_level_profiles generated after placement completion.
6. Learning package foundation: scenario_templates, learning_packages, GET /api/learning/packages/next.
7. Dual-agent dialogue foundation: learning_dialogue_turns, POST /api/learning/packages/{packageId}/dialog.
8. Frontend learning area is now a dual-agent dialogue UI.
9. Phase 4.3 learning-unit event foundation is implemented: learning_units, learning_events, learning unit seed data, LearningEventService.

最近完成的 Phase 4.3 文件：
- backend/src/main/java/com/contextflow/content/domain/LearningUnitEntity.java
- backend/src/main/java/com/contextflow/content/domain/LearningUnitStatus.java
- backend/src/main/java/com/contextflow/content/domain/LearningUnitType.java
- backend/src/main/java/com/contextflow/content/repository/LearningUnitRepository.java
- backend/src/main/java/com/contextflow/content/seed/LearningUnitSeeder.java
- backend/src/main/java/com/contextflow/learning/domain/LearningEventEntity.java
- backend/src/main/java/com/contextflow/learning/domain/LearningEventSourceType.java
- backend/src/main/java/com/contextflow/learning/domain/LearningEventType.java
- backend/src/main/java/com/contextflow/learning/repository/LearningEventRepository.java
- backend/src/main/java/com/contextflow/learning/service/LearningEventService.java
- backend/src/main/resources/db/migration/V8__create_learning_units_and_events.sql
- docs/sql/phase4_learning_event_schema.sql

Recently completed Phase 4.3 files:
- backend/src/main/java/com/contextflow/content/domain/LearningUnitEntity.java
- backend/src/main/java/com/contextflow/content/domain/LearningUnitStatus.java
- backend/src/main/java/com/contextflow/content/domain/LearningUnitType.java
- backend/src/main/java/com/contextflow/content/repository/LearningUnitRepository.java
- backend/src/main/java/com/contextflow/content/seed/LearningUnitSeeder.java
- backend/src/main/java/com/contextflow/learning/domain/LearningEventEntity.java
- backend/src/main/java/com/contextflow/learning/domain/LearningEventSourceType.java
- backend/src/main/java/com/contextflow/learning/domain/LearningEventType.java
- backend/src/main/java/com/contextflow/learning/repository/LearningEventRepository.java
- backend/src/main/java/com/contextflow/learning/service/LearningEventService.java
- backend/src/main/resources/db/migration/V8__create_learning_units_and_events.sql
- docs/sql/phase4_learning_event_schema.sql

当前工作区可能有未提交改动，请不要覆盖或回滚：
- README.md
- backend/src/main/java/com/contextflow/learning/domain/LearningDialogueTurnEntity.java
- backend/src/main/java/com/contextflow/learning/service/LearningDialogueService.java
- backend/src/main/resources/application.yml
- docs/architecture.md
- docs/development-log.md
- docs/work-plan.md
- Phase 4.3 新增文件

The working tree may contain uncommitted changes. Do not overwrite or revert them:
- README.md
- backend/src/main/java/com/contextflow/learning/domain/LearningDialogueTurnEntity.java
- backend/src/main/java/com/contextflow/learning/service/LearningDialogueService.java
- backend/src/main/resources/application.yml
- docs/architecture.md
- docs/development-log.md
- docs/work-plan.md
- New Phase 4.3 files

已知验证状态：
- frontend npm run typecheck 通过。
- git diff --check 通过，仅有 Windows 换行提示。
- 后端 Maven 在命令行可能失败：默认 JDK 是 17，不支持 Java 21；切到 JDK 21 后可能因 backend/target 写入权限或进程占用失败。
- 已用 JDK 21 javac 做源码级验证，新加学习单元和学习事件 class 已生成。

Known verification status:
- Frontend npm run typecheck passed.
- git diff --check passed with only Windows line-ending warnings.
- Backend Maven may fail in CLI: default JDK is 17 and cannot support Java 21; after switching to JDK 21 it may still fail because backend/target is not writable or is held by a process.
- Source-level validation with JDK 21 javac generated the new learning unit and learning event classes.

下一步优先事项：
1. 先检查当前代码状态和 docs/development-log.md。
2. 用 Java 21 启动后端，让 Flyway 执行 V8。
3. 用 docs/sql/phase4_learning_event_schema.sql 在 DataGrip 检查 learning_units 和 learning_events。
4. 登录 learner，完成或复用已有画像，获取 READY 学习包，发送双 Agent 对话，确认 learning_events 按学习单元出现次数落库。
5. 若 Phase 4.3 验证通过，下一阶段可做 UnitStats/掌握度统计，但不要直接设计大系统，先给小步方案让我确认。

Next priorities:
1. First inspect current code status and docs/development-log.md.
2. Start the backend with Java 21 so Flyway runs V8.
3. Use docs/sql/phase4_learning_event_schema.sql in DataGrip to inspect learning_units and learning_events.
4. Log in as learner, complete or reuse an existing profile, fetch a READY learning package, send a dual-agent dialogue message, and confirm learning_events rows are inserted by learning-unit occurrence count.
5. If Phase 4.3 verification passes, the next phase can be UnitStats/mastery tracking, but do not design a large system directly; propose a small-step plan for confirmation first.

不要做：
- 不要引入 Redis/Kafka/Docker。
- 不要接 AI API。
- 不要重构前端大页面。
- 不要把 LearningEvent 建模成整轮对话事件。
- 不要把 ability tag 当成 LearningUnit。
- 不要做 Git commit/push/stage，Git 由我手动操作。

Do not:
- Do not introduce Redis/Kafka/Docker.
- Do not connect an AI API.
- Do not do a large frontend page refactor.
- Do not model LearningEvent as a whole dialogue-turn event.
- Do not treat ability tags as LearningUnit.
- Do not run Git commit/push/stage; I handle Git manually.
```

## 当前状态摘要 / Current Status Summary

Phase 4.3 的核心代码已经写入本地工作区，但用户尚未手动 Git 提交。

The core Phase 4.3 code has been written into the local working tree, but the user has not manually committed it yet.

## 待完成工作 / Remaining Work

- 完整后端 Maven 验证：需要命令行使用 Java 21，并释放 `backend/target` 写入占用。 / Full backend Maven verification: requires CLI Java 21 and writable `backend/target`.
- Flyway V8 实库验证：启动后端后检查 `learning_units`、`learning_events`。 / Real database verification for Flyway V8: start backend and inspect `learning_units`, `learning_events`.
- 浏览器端闭环验证：发送双 Agent 对话并确认事件落库。 / Browser loop verification: send a dual-agent dialogue message and confirm event persistence.
- 下一阶段候选：基于 `learning_events` 做 UnitStats/掌握度统计。 / Candidate next phase: build UnitStats/mastery tracking from `learning_events`.

