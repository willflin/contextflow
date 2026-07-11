# Web Codex 交接提示词 V2 / Web Codex Handoff Prompt V2

## 使用方式 / How to Use

复制下面整段提示词到新的 Codex 对话。 / Copy the full prompt below into the new Codex conversation.

## 可复制提示词 / Copyable Prompt

```text
你现在继续担任我的全栈开发导师、架构师和代码审查员。
项目路径：D:\code\english_tutor_project

请先阅读当前项目代码和文档，不要重新设计，不要跳过已有进度。

协作要求：
- 用中文，极其精炼。
- 不要给我大量命令行命令。
- Git 操作由我手动完成；不要 stage/commit/push。
- 不允许乱装依赖；任何新依赖必须先说明业务理由并让我确认。
- Markdown 文档必须中英文双语。
- 每次开发都要更新 docs/development-log.md。
- 建表 SQL 要同步保留到 docs/sql/。
- 以后测试以我手动测试为主；你只做必要的编译/轻量验收，不要为了测试拖慢业务开发。
- 当前重点不是补大量自动化测试，而是尽快完成接入真实 Agent 前的业务准备。

项目定位：
ContextFlow 是 AI 语境化英语学习系统。
核心闭环：水平测试 -> 用户画像 -> READY 学习包 -> 双 Agent 对话学习 -> LearningEvent/掌握度 -> 复习系统。
内容、音频、测试题最终都要由 AI 预生成并缓存，用户不等待 AI 实时生成。

当前技术栈：
- Backend: Java 21 + Spring Boot 3 + Spring Security + JWT + JPA + Flyway
- Frontend: React + TypeScript + Vite
- Database: MySQL 8
- DB: contextflow
- DB user: contextflow
- DB password: 123456
- 暂未引入 Redis / Kafka / Docker / AI API / 音频生成

测试账号：
- learner / learner123
- admin / admin123

重要设计约束：
- learner 只能看到正式学习流程。
- admin 才能看到调试/管理功能。
- admin 不直接修改用户学习事件或掌握度统计。
- 当前学习包只是 READY 内容容器，最终承载 AI 预生成内容。
- 当前双 Agent 是本地规则模拟，后续替换为真实 AI 调用，但接口结构尽量保持稳定。
- RAG 第一版不加向量库，先用结构化检索。
- Redis/Kafka/Docker 只有业务依据明确后再加。

核心业务定义：
- LearningEvent 基于语言学习单元，不是整轮对话事件。
- LearningUnit 包含 WORD、PHRASE、SENTENCE_PATTERN。
- 用户或 Agent 的一句话中，每出现一次语言单元，就写入一条 learning_events。
- 同一句话里同一语言单元出现多次，也要记录多条事件。
- learning_unit_sense_id 可以为空；后续可由更强解析或 Agent 输出补充词义归属。

当前事件类型：
- UNIT_ATTEMPTED：用户输入中出现。
- UNIT_EXPOSED：Roleplay Agent 回复中出现。
- UNIT_CORRECTED：Mentor 纠错建议中出现。
- UNIT_RECOMMENDED：Mentor 自然表达建议中出现。

当前已完成：
1. 登录/注册/JWT/角色权限。
2. users 表与 demo 用户。
3. placement_items 水平测试题池。
4. 自适应逐题测试。
5. 测试完成后生成 user_level_profiles。
6. scenario_templates + learning_packages + GET /api/learning/packages/next。
7. 双 Agent 对话骨架：learning_dialogue_turns + POST /api/learning/packages/{packageId}/dialog。
8. 前端学习区已改成双 Agent 对话界面。
9. 语言单元分层模型：
   - learning_units
   - learning_unit_senses
   - learning_unit_forms
   - learning_data_sources
   - learning_unit_sense_sources
   - user_learning_unit_sense_stats
10. learning_events 已按语言单元出现记录。
11. admin 只读语言单元查询接口：
   - GET /api/admin/learning-units/search?text=went
   - GET /api/admin/learning-units/{id}
   - GET /api/admin/learning-units/{id}/senses
12. 种子语言单元：
   - go: go/goes/went/gone/going
   - bank: financial-institution / river-side
   - book
   - good: good/better/best
13. 完整建表 SQL 已导出到 docs/sql/contextflow_full_schema.sql。

重要文件：
- backend/src/main/java/com/contextflow/content：语言单元模型、查询、种子数据。
- backend/src/main/java/com/contextflow/learning：学习包、双 Agent 对话、学习事件。
- backend/src/main/resources/db/migration/V8__create_learning_units_and_events.sql：保留旧 V8，避免 Flyway checksum 问题。
- backend/src/main/resources/db/migration/V9__rebuild_learning_unit_model.sql：当前语言单元分层模型。
- docs/sql/phase4_learning_event_schema.sql
- docs/sql/contextflow_full_schema.sql
- docs/development-log.md
- docs/work-plan.md

当前验证状态：
- 数据库 Flyway V1-V9 成功。
- learning_units=4，learning_unit_senses=5，learning_unit_forms=12。
- learning_events 当前为 0；之前集成测试事务回滚，不污染数据。
- 前端 npm run typecheck 通过。
- 后端 JDK 21 临时编译通过。
- JUnit Launcher 17/17 通过。
- Maven 常规入口在这台机器仍可能报“无法关闭编译器资源”，不要在这个问题上耗太久；业务开发优先，必要时提醒我手动测试。

当前工作区可能有未提交改动，不要覆盖或回滚：
- README.md
- backend/src/main/java/com/contextflow/content/**
- backend/src/main/java/com/contextflow/learning/**
- backend/src/main/resources/application.yml
- backend/src/main/resources/db/migration/V8__create_learning_units_and_events.sql
- backend/src/main/resources/db/migration/V9__rebuild_learning_unit_model.sql
- backend/src/test/java/com/contextflow/**
- docs/**

下一步目标：接入真实 Agent 前的准备，优先业务闭环。

建议开发顺序：
1. Phase 4.9：增强语言单元匹配。
   - 支持 PHRASE / SENTENCE_PATTERN。
   - 处理标点、大小写、缩写、词边界。
   - 避免明显重复误匹配。
   - 记录 occurrenceIndex，后续可扩展位置。

2. Phase 5.1：实现最小掌握度更新。
   - 根据 learning_events 更新 user_learning_unit_sense_stats。
   - 先做简单规则，不做复杂算法。
   - 目标是让复习系统有数据基础。

3. Phase 5.2：实现最小复习队列。
   - 根据 stats 产出待复习语言单元。
   - 先后端接口，不急着做复杂前端。

4. Phase 6.1：Agent 接入前的数据契约。
   - 固定 Roleplay Agent / Mentor Agent 的输入输出 JSON。
   - 明确 AI 需要返回哪些字段：reply、feedback、corrections、naturalExpression、unitMentions、scoringSignal。
   - unitMentions 用于以后直接写 learning_events，减少本地字符串匹配误差。

5. Phase 6.2：AI 预生成任务模型。
   - 先设计 DB 表和状态流，不接真实 AI。
   - 内容必须预生成并缓存为 READY，用户不等待实时生成。

不要做：
- 不要先接 AI API。
- 不要加 Redis/Kafka/Docker。
- 不要大重构前端。
- 不要把学习事件改回整轮对话事件。
- 不要把 ability tag 当成 LearningUnit。
- 不要为自动化测试投入过多时间；以手动验收为主。

开始后请先简短说明你读到的当前状态，然后直接进入下一步业务开发。
```

## 当前摘要 / Current Summary

这份 V2 提示词以业务推进为主，明确弱化自动化测试，优先完成真实 Agent 接入前的数据、事件、掌握度和复习准备。 / This V2 prompt prioritizes business progress, reduces emphasis on automated tests, and focuses on data, event, mastery, and review preparation before real Agent integration.
