# 开发日志 / Development Log

## Phase 0：前后端基础框架 / Frontend and Backend Foundation

### 操作 / Operations

- 创建 Spring Boot 后端基础工程。 / Created the Spring Boot backend foundation.
- 创建 React + TypeScript + Vite 前端基础工程。 / Created the React + TypeScript + Vite frontend foundation.
- 新增 `/api/health` 健康检查接口。 / Added the `/api/health` health check API.
- 新增前端健康检查页面。 / Added the frontend health check page.

### 新增功能 / Added Features

- 前端可以调用后端健康检查接口。 / The frontend can call the backend health check API.

### 问题与解决方案 / Issues and Solutions

- npm 写入 `E:\nodejs\node_cache` 报 EPERM。 / npm failed with EPERM when writing to `E:\nodejs\node_cache`.
  - 解决方案：改用项目级或用户级 npm cache 目录。 / Solution: moved npm cache to a project/user-writable directory.

## Phase 0.5：项目规范 / Project Conventions

### 操作 / Operations

- 新增 `.editorconfig`。 / Added `.editorconfig`.
- 新增项目结构说明。 / Added project structure documentation.
- 新增 Git 使用建议。 / Added Git guide.
- 新增后端模块包占位。 / Added backend module package placeholders.
- 新增前端 `app/features/shared` 目录说明。 / Added frontend `app/features/shared` directory notes.

### 新增功能 / Added Features

- 明确后端模块化单体边界。 / Defined modular monolith boundaries.
- 明确前端功能模块组织方式。 / Defined frontend feature organization.

## Git 初始化 / Git Initialization

### 操作 / Operations

- 初始化本地 Git 仓库。 / Initialized the local Git repository.
- 用户手动完成远程仓库连接和 push。 / The user manually connected the remote repository and pushed.

### 问题与解决方案 / Issues and Solutions

- `git init` 普通执行遇到 `.git` 写入权限问题。 / `git init` hit `.git` write permission issues.
  - 解决方案：授权后完成本地仓库初始化。 / Solution: initialized the repository after permission approval.

## Phase 1.1：内存版登录 / In-memory Login

### 操作 / Operations

- 引入 Spring Security。 / Added Spring Security.
- 新增 `POST /api/auth/login`。 / Added `POST /api/auth/login`.
- 新增 `GET /api/auth/me`。 / Added `GET /api/auth/me`.
- 新增前端登录面板。 / Added the frontend login panel.

### 新增功能 / Added Features

- 支持 `learner / learner123` 登录。 / Supports `learner / learner123` login.
- 支持 `admin / admin123` 登录。 / Supports `admin / admin123` login.

### 问题与解决方案 / Issues and Solutions

- Maven 默认本地仓库位于 `E:\maven\...\repository`，当前用户无写权限。 / Maven default local repository was under `E:\maven\...\repository`, which was not writable.
  - 解决方案：验证时改用项目内 `.m2/repository`，并在 `.gitignore` 排除 `.m2/`。 / Solution: used project-local `.m2/repository` for verification and ignored `.m2/`.

## Phase 1.2：角色权限保护 / Role-based Access Control

### 操作 / Operations

- 新增 token 过滤器。 / Added a token authentication filter.
- 配置 `/api/learner/**` 和 `/api/admin/**` 权限规则。 / Configured access rules for `/api/learner/**` and `/api/admin/**`.
- 新增 learner/admin 权限测试接口。 / Added learner/admin access probe APIs.
- 新增前端权限验证按钮。 / Added frontend access check buttons.

### 新增功能 / Added Features

- learner 可以访问 learner API。 / learner can access learner APIs.
- learner 不能访问 admin API。 / learner cannot access admin APIs.
- admin 可以访问 learner 和 admin API。 / admin can access both learner and admin APIs.

### 问题与解决方案 / Issues and Solutions

- 命令行 Java 版本为 17，项目需要 Java 21。 / CLI Java version was 17 while the project requires Java 21.
  - 解决方案：在 IntelliJ 中使用 Java 21 运行和测试后端。 / Solution: use Java 21 in IntelliJ for backend run and tests.

## Phase 1.3：JWT 登录 / JWT Login

### 操作 / Operations

- 引入 JJWT。 / Added JJWT.
- 将 mock token 升级为真实 JWT。 / Upgraded mock tokens to real JWTs.
- 使用 JWT 恢复当前登录用户。 / Restored the current user from JWT.

### 新增功能 / Added Features

- 刷新页面后可通过 JWT 恢复登录状态。 / Login state can be restored from JWT after refresh.

## Phase 2.1：MySQL 接入 / MySQL Integration

### 操作 / Operations

- 创建本地 MySQL 开发库 `contextflow`。 / Created local MySQL development database `contextflow`.
- 创建本地 MySQL 应用用户 `contextflow`。 / Created local MySQL application user `contextflow`.
- 引入 Spring Data JPA、MySQL Driver、Flyway。 / Added Spring Data JPA, MySQL Driver, and Flyway.
- 新增 `users` 表迁移脚本。 / Added the `users` table migration script.
- 新增 DataGrip 检查用 SQL 副本。 / Added a SQL copy for DataGrip review.
- 新增 `UserEntity`、`UserRepository` 和 demo 用户种子数据。 / Added `UserEntity`, `UserRepository`, and demo user seed data.

### 新增功能 / Added Features

- 登录用户从内存数据迁移到 MySQL `users` 表。 / Login users were moved from memory to the MySQL `users` table.
- 启动时可自动创建 demo 用户。 / Demo users can be seeded on startup.

## Phase 2.2：用户注册 / User Registration

### 操作 / Operations

- 新增 `POST /api/auth/register`。 / Added `POST /api/auth/register`.
- 新增前端注册模式。 / Added frontend registration mode.

### 新增功能 / Added Features

- 普通用户可注册学习者账号。 / Users can register learner accounts.
- 注册用户默认角色固定为 `LEARNER`。 / Registered users always receive the `LEARNER` role.
- 管理员账号不能通过前端注册创建。 / Admin accounts cannot be created through frontend registration.

## Phase 3.1：水平测试题池 / Placement Item Pool

### 操作 / Operations

- 新增 `placement_items` 表迁移脚本。 / Added the `placement_items` table migration script.
- 新增 DataGrip 检查用 SQL 副本。 / Added a SQL copy for DataGrip review.
- 新增 `placement` 模块的领域模型、Repository、Service 和 Controller。 / Added domain model, repository, service, and controller for the `placement` module.
- 新增启动时写入的水平测试种子题。 / Added startup seed data for placement test items.
- 配置 `/api/placement/**` 需要 `LEARNER` 或 `ADMIN` 权限。 / Configured `/api/placement/**` to require `LEARNER` or `ADMIN`.

### 新增功能 / Added Features

- 支持读取 READY 状态的水平测试样例题。 / Supports reading READY placement sample items.
- 新增接口 `GET /api/placement/items/sample`。 / Added `GET /api/placement/items/sample`.

## Phase 3.2：水平测试会话 / Placement Session

### 操作 / Operations

- 新增 `placement_sessions` 表。 / Added the `placement_sessions` table.
- 新增 `placement_session_answers` 表。 / Added the `placement_session_answers` table.
- 新增 DataGrip 检查用 SQL 副本。 / Added a SQL copy for DataGrip review.
- 新增开始测试接口。 / Added the start placement session API.
- 新增提交答案接口。 / Added the submit placement answers API.

### 新增功能 / Added Features

- 支持从 READY 题池创建一次水平测试会话。 / Supports creating a placement session from READY items.
- 支持保存用户答案并计算正确率。 / Supports saving user answers and calculating accuracy.
- 支持返回临时 CEFR 等级判断。 / Supports returning a temporary CEFR level estimate.

## Phase 3.2.1：自适应水平测试基础 / Adaptive Placement Foundation

### 操作 / Operations

- 扩展 `placement_items`，新增难度分、能力维度、判分类型字段。 / Extended `placement_items` with difficulty score, ability dimension, and grading type.
- 扩展 `placement_sessions`，新增测试模式、已答题数、最大题数、当前难度字段。 / Extended `placement_sessions` with mode, answered count, max item count, and current difficulty.
- 扩展 `placement_session_answers`，新增判分类型、题目难度、文本答案和 AI 判分载荷字段。 / Extended `placement_session_answers` with grading type, difficulty, text answer, and AI judge payload.
- 新增自适应开始测试接口。 / Added the adaptive placement start API.
- 新增逐题提交答案接口。 / Added the step-by-step answer API.

### 新增功能 / Added Features

- 支持根据上一题对错调整下一题难度。 / Supports adjusting the next item difficulty based on the previous answer.
- 自适应接口返回题目时不暴露答案字段。 / Adaptive APIs do not expose answer fields to the client.
- 为判断题、近义词/反义词选择题、填空题、AI 判分预留结构。 / Reserved structure for true/false, synonym/antonym choice, cloze text, and AI judging.

### 问题与解决方案 / Issues and Solutions

- Postman 请求中把 `{sessionId}` 当作真实路径发送，导致后端尝试把字符串转换成 `Long`。 / Postman sent `{sessionId}` as the real path, causing the backend to convert that string to `Long`.
  - 解决方案：测试时必须替换成真实 `sessionId`；同时新增参数类型错误处理，返回清晰的 `400 PARAMETER_TYPE_MISMATCH`。 / Solution: replace it with the real `sessionId` during testing; also added type mismatch handling that returns a clear `400 PARAMETER_TYPE_MISMATCH`.

## Phase 3.3：用户水平画像 / User Level Profile

### 操作 / Operations

- 新增 `user_level_profiles` 表。 / Added the `user_level_profiles` table.
- 新增用户画像 Entity、Repository、Service 和 Controller。 / Added user profile entity, repository, service, and controller.
- 新增 `GET /api/user/profile`。 / Added `GET /api/user/profile`.
- 水平测试结束时自动写入或更新用户画像。 / Automatically writes or updates the user profile when placement testing finishes.

### 新增功能 / Added Features

- 支持保存 `cefr_level`。 / Supports storing `cefr_level`.
- 支持保存能力维度得分。 / Supports storing ability dimension scores.
- 支持保存薄弱场景和薄弱能力。 / Supports storing weak scenarios and weak abilities.

### 问题与解决方案 / Issues and Solutions

- 命令行 Maven 已切到 JDK 21，但 `target/classes` 被正在运行的 Java 进程占用，导致无法写入编译产物。 / Command-line Maven was switched to JDK 21, but `target/classes` was held by a running Java process, so Maven could not write compiled output.
  - 解决方案：停止正在运行的后端后再命令行编译，或直接用 IntelliJ 的 JDK 21 启动验证。 / Solution: stop the running backend before command-line compilation, or verify by running with JDK 21 in IntelliJ.

## Phase 3.3.1：网页调试入口 / Web Debug Entry

### 操作 / Operations

- 新增前端自适应水平测试 API 封装。 / Added frontend API wrappers for adaptive placement testing.
- 新增前端用户画像 API 封装。 / Added frontend API wrapper for user level profile.
- 重构首页登录后界面。 / Refactored the signed-in homepage.
- 将普通学习者界面与管理员调试界面分离。 / Separated learner-facing UI from admin debug UI.

### 新增功能 / Added Features

- 学习者可在网页中开始测试、逐题作答并查看等级结果。 / Learners can start placement, answer step by step, and view their level in the browser.
- 管理员可在网页中查看健康检查、权限探测、测试沙盒和原始画像 JSON。 / Admins can use health check, access probes, placement sandbox, and raw profile JSON in the browser.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.

## Phase 4.1：学习包基础 / Learning Package Foundation

### 操作 / Operations

- 新增 `scenario_templates` 表迁移脚本。 / Added the `scenario_templates` table migration.
- 新增 `learning_packages` 表迁移脚本。 / Added the `learning_packages` table migration.
- 新增 DataGrip 检查用 SQL 副本。 / Added a SQL copy for DataGrip inspection.
- 新增 `scenario` 模块的 Entity、Repository 和启动种子数据。 / Added entity, repository, and startup seed data for the `scenario` module.
- 新增 `learning` 模块的学习包 Entity、Repository、Service、DTO 和 Controller。 / Added learning package entity, repository, service, DTO, and controller for the `learning` module.
- 新增前端学习包 API 封装。 / Added frontend API wrappers for learning packages.
- 在学习者页面新增下一个 READY 场景学习包入口。 / Added the next READY scenario package entry to the learner page.

### 新增功能 / Added Features

- 支持保存系统级场景模板。 / Supports storing system-level scenario templates.
- 支持为用户创建或读取 READY 学习包。 / Supports creating or reading READY learning packages for a learner.
- 创建新学习包时优先避开该用户已分配过的场景。 / New package creation first avoids scenarios already assigned to that learner.
- 新增接口 `GET /api/learning/packages/next`。 / Added `GET /api/learning/packages/next`.
- 学习包内容暂时使用 `SEEDED_TEMPLATE`，为后续 `AI_GENERATED` 预生成链路预留字段。 / Learning package content currently uses `SEEDED_TEMPLATE`, reserving fields for the later `AI_GENERATED` pre-generation flow.
- 普通学习者完成水平测试并生成画像后，可以在网页获取下一个学习场景。 / Learners can fetch the next scenario in the browser after placement creates a profile.
- 当前网页学习包展示只是过渡形态，后续要替换为 Roleplay Agent + Mentor Agent 双 Agent 对话界面。 / The current web learning package display is only transitional and must later be replaced by a Roleplay Agent + Mentor Agent dual-agent dialogue UI.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.

### 问题与解决方案 / Issues and Solutions

- 后端命令行编译时，`backend/target/classes/application.yml` 和 `backend/target/maven-status/.../createdFiles.lst` 写入被拒绝，当前机器上存在正在运行的 Java 进程。 / Backend command-line compilation was denied when writing `backend/target/classes/application.yml` and `backend/target/maven-status/.../createdFiles.lst`; Java processes are currently running on this machine.
  - 解决方案：不强制结束用户进程；需要完整 Maven 编译时，先停止正在运行的后端，再用 JDK 21 编译。 / Solution: do not force-stop user processes; for a full Maven compile, stop the running backend first, then compile with JDK 21.

## Phase 4.2：双 Agent 对话骨架 / Dual-Agent Dialogue Foundation

### 操作 / Operations

- 新增 `learning_dialogue_turns` 表迁移脚本。 / Added the `learning_dialogue_turns` table migration.
- 新增 DataGrip 检查用 SQL 副本。 / Added a SQL copy for DataGrip inspection.
- 新增学习对话轮次 Entity 和 Repository。 / Added the learning dialogue turn entity and repository.
- 新增双 Agent 对话请求与响应 DTO。 / Added request and response DTOs for dual-agent dialogue.
- 新增 `POST /api/learning/packages/{packageId}/dialog`。 / Added `POST /api/learning/packages/{packageId}/dialog`.
- 将前端学习区从静态学习包展示改为双 Agent 对话界面。 / Replaced the frontend static learning package display with a dual-agent dialogue UI.

### 新增功能 / Added Features

- Roleplay Agent 在主区域返回英文场景回复。 / The Roleplay Agent returns English scenario replies in the main area.
- Mentor Agent 在侧栏返回中文纠错、解释和更自然表达。 / The Mentor Agent returns Chinese corrections, explanations, and more natural expressions in the side panel.
- 每轮对话保存用户发言、Roleplay 回复、Mentor 反馈、纠错列表、自然表达和评分信号。 / Each dialogue turn stores the learner message, Roleplay reply, Mentor feedback, corrections, natural expression, and scoring signal.
- 当前阶段使用本地规则模拟双 Agent，后续替换为真实 AI 调用。 / This phase uses local rules to simulate dual agents and will later be replaced by real AI calls.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.

### 问题与解决方案 / Issues and Solutions

- 后端 Maven 编译仍无法写入 `backend/target/classes` 或临时 class 输出目录，当前机器存在正在运行的 Java/IDE 进程。 / Backend Maven compilation still cannot write to `backend/target/classes` or a temporary class output directory; Java/IDE processes are currently running on this machine.
  - 解决方案：本次不强制结束进程；网页验证前先重启后端，让 Flyway 执行 V7 迁移。 / Solution: do not force-stop processes in this step; restart the backend before browser verification so Flyway can run the V7 migration.

## Phase 4.3：学习单元级学习事件 / Learning-Unit Learning Events

### 操作 / Operations

- 新增 `learning_units` 表迁移脚本。 / Added the `learning_units` table migration.
- 新增 `learning_events` 表迁移脚本。 / Added the `learning_events` table migration.
- 新增 DataGrip 检查用 SQL 副本。 / Added a SQL copy for DataGrip inspection.
- 新增学习单元 Entity、Repository 和启动种子数据。 / Added learning unit entity, repository, and startup seed data.
- 新增学习单元种子数据开关 `contextflow.content.seed-demo-units`。 / Added the learning unit seed toggle `contextflow.content.seed-demo-units`.
- 新增学习事件 Entity、Repository 和事件记录服务。 / Added learning event entity, repository, and event recording service.
- 在双 Agent 对话保存成功后，同一事务内按学习单元出现次数写入事件。 / After a dual-agent dialogue turn is saved, events are written in the same transaction by learning-unit occurrence count.
- 修正文档中学习事件定义，明确学习事件不是对话轮次事件。 / Updated documentation to clarify that learning events are not dialogue-turn events.
- 将 `scoringSignal.relatedLanguageUnits` 修正为 `relatedAbilityTags`，避免把能力标签误当学习单元。 / Renamed `scoringSignal.relatedLanguageUnits` to `relatedAbilityTags` to avoid treating ability tags as learning units.
- 新增网页版 Codex 交接提示词文档。 / Added a Web Codex handoff prompt document.

### 新增功能 / Added Features

- 学习单元支持 `WORD`、`PHRASE` 和 `SENTENCE_PATTERN`。 / Learning units support `WORD`, `PHRASE`, and `SENTENCE_PATTERN`.
- 用户输入命中学习单元时记录 `UNIT_ATTEMPTED`。 / Records `UNIT_ATTEMPTED` when learner input matches a learning unit.
- Roleplay Agent 回复命中学习单元时记录 `UNIT_EXPOSED`。 / Records `UNIT_EXPOSED` when Roleplay Agent output matches a learning unit.
- Mentor 纠错建议命中学习单元时记录 `UNIT_CORRECTED`。 / Records `UNIT_CORRECTED` when Mentor correction suggestions match a learning unit.
- Mentor 自然表达建议命中学习单元时记录 `UNIT_RECOMMENDED`。 / Records `UNIT_RECOMMENDED` when Mentor natural-expression suggestions match a learning unit.
- 同一句话中同一学习单元出现多次时，会写入多条事件。 / Multiple occurrences of the same learning unit in one sentence create multiple event rows.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- 使用 JDK 21 `javac` 直接编译源码时，新增的学习单元和学习事件相关 class 已生成。 / Direct JDK 21 `javac` compilation generated the new learning unit and learning event classes.

### 问题与解决方案 / Issues and Solutions

- 最初容易把学习事件误建模为整轮对话事件，不符合“学习单元每出现一次就是一次事件”的需求。 / Learning events could be incorrectly modeled as whole dialogue-turn events, which does not match the requirement that each learning-unit occurrence is one event.
  - 解决方案：新增 `learning_units`，并让 `learning_events.learning_unit_id` 指向具体学习单元；对话轮次只作为 `source_type/source_id`。 / Solution: added `learning_units` and made `learning_events.learning_unit_id` point to the specific unit; dialogue turns are only `source_type/source_id`.
- 后端 Maven 编译第一次使用命令行 JDK 17，无法支持项目的 Java 21；切到 JDK 21 后仍因 `backend/target` 写入被拒绝而无法完成 Maven 编译。 / Backend Maven compilation first used CLI JDK 17, which cannot support Java 21; after switching to JDK 21, Maven still could not finish because writes to `backend/target` were denied.
  - 解决方案：不强制结束本机 Java/IDE 进程；本次用 JDK 21 `javac` 做源码级验证，完整 Maven 验证需先释放 `backend/target`。 / Solution: did not force-stop local Java/IDE processes; used JDK 21 `javac` for source-level verification this time, and full Maven verification requires releasing `backend/target` first.

## Phase 4.4 + Phase 4.6：语言单元分层模型与查询 / Layered Language Unit Model and Queries

### 操作 / Operations

- 新增 `V9__rebuild_learning_unit_model.sql`，重建语言单元分层表。 / Added `V9__rebuild_learning_unit_model.sql` to rebuild the layered language unit tables.
- 将 `LearningUnitEntity` 从旧字段模型改为 `canonical_text`、`normalized_text`、`language_code` 模型。 / Changed `LearningUnitEntity` from the old field model to the `canonical_text`, `normalized_text`, and `language_code` model.
- 新增词义、词形、数据来源、词义来源和用户词义掌握度 Entity。 / Added entities for senses, forms, data sources, sense sources, and user sense-level stats.
- 新增对应 Repository、只读查询 Service 和 admin 查询 Controller。 / Added repositories, a read-only query service, and an admin query controller.
- 将 `LearningEventService` 从旧 `match_text` 匹配改为基于 `learning_unit_forms.normalized_form` 匹配。 / Changed `LearningEventService` from old `match_text` matching to `learning_unit_forms.normalized_form` matching.
- 更新 `docs/sql/phase4_learning_event_schema.sql`，新增完整建表文件 `docs/sql/contextflow_full_schema.sql`。 / Updated `docs/sql/phase4_learning_event_schema.sql` and added the full schema file `docs/sql/contextflow_full_schema.sql`.
- 新增 `LearningUnitQueryServiceTest` 和 `LearningEventServiceTest`。 / Added `LearningUnitQueryServiceTest` and `LearningEventServiceTest`.
- 使用 Flyway API 执行 V9，保持 `flyway_schema_history` 正常记录。 / Ran V9 through the Flyway API so `flyway_schema_history` remains consistent.
- 重新导出完整数据库建表 SQL。 / Re-exported the full database schema SQL.

### 新增功能 / Added Features

- 支持 `go/goes/went/gone/going -> go` 的词形归一查询。 / Supports form normalization such as `go/goes/went/gone/going -> go`.
- 支持 `better/best -> good` 的词形归一查询。 / Supports form normalization such as `better/best -> good`.
- `bank` 支持多个词义：金融机构和河岸。 / `bank` supports multiple senses: financial institution and river side.
- 新增 admin 只读接口：`GET /api/admin/learning-units/search`、`GET /api/admin/learning-units/{id}`、`GET /api/admin/learning-units/{id}/senses`。 / Added admin-only read APIs: `GET /api/admin/learning-units/search`, `GET /api/admin/learning-units/{id}`, and `GET /api/admin/learning-units/{id}/senses`.
- `LearningEventService` 支持可选 `learningUnitSenseId`，并校验 sense 必须属于同一个 unit。 / `LearningEventService` supports an optional `learningUnitSenseId` and validates that the sense belongs to the same unit.

### 问题与解决方案 / Issues and Solutions

- 数据库已被手工调整到新版分层结构，但代码、Seeder 和 SQL 文档仍停留在旧模型。 / The database had been manually adjusted to the new layered structure, while code, seeders, and SQL docs still used the old model.
  - 解决方案：当前语言单元相关表为空，因此新增 V9 迁移重建语言单元相关表，并同步修正代码和 SQL 文档。 / Solution: because the current language-unit tables are empty, added the V9 migration to rebuild language-unit tables and synchronized code and SQL docs.
- `learning_events.learning_unit_sense_id` 只外键到 sense，不能保证 sense 属于同一个 unit。 / `learning_events.learning_unit_sense_id` only referenced the sense and did not ensure that the sense belongs to the same unit.
  - 解决方案：新增 `(learning_unit_id, learning_unit_sense_id)` 复合外键约束。 / Solution: added a composite foreign key on `(learning_unit_id, learning_unit_sense_id)`.

### 验证 / Verification

- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- JDK 21 `javac` 未输出语法错误，但本机仍出现“无法关闭编译器资源”的退出期错误。 / JDK 21 `javac` produced no syntax errors, but this machine still reported a compiler resource closing error at exit.
- 新增后端类和测试类已由 JDK 21 `javac` 生成 class 文件。 / The new backend classes and test classes were generated as class files by JDK 21 `javac`.
- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 数据库 `flyway_schema_history` 已记录 V9 成功。 / Database `flyway_schema_history` recorded V9 as successful.
- 当前种子数据包含 4 个 unit、5 个 sense、12 个 form 和 1 个 data source。 / Current seed data contains 4 units, 5 senses, 12 forms, and 1 data source.
- Maven 编译仍被 `backend/target/classes/application.yml` 写入权限阻断。 / Maven compilation is still blocked by denied writes to `backend/target/classes/application.yml`.
- Maven Flyway 插件无法使用：全局 Maven 仓库无写权限，项目内 `.m2` 又缺少 Flyway Maven 插件解析信息；本次改用项目依赖中的 Flyway API。 / The Maven Flyway plugin could not be used: the global Maven repository is not writable, and the project `.m2` lacks Flyway Maven plugin resolution metadata; this step used the Flyway API from project dependencies instead.

## Phase 4.7：验收修复与开发轨道复位 / Acceptance Fixes and Development Track Reset

### 操作 / Operations

- 重新检查数据库迁移状态、语言单元种子数据、前端类型检查和后端测试。 / Rechecked database migration state, language-unit seed data, frontend typecheck, and backend tests.
- 修复 `HealthControllerTest` 的 MVC 切片测试依赖缺口。 / Fixed the missing dependency in the `HealthControllerTest` MVC slice test.
- 使用 JDK 21 临时编译目录完成后端源码和测试源码编译。 / Compiled backend main and test sources with JDK 21 into a temporary output directory.
- 使用 JUnit Platform Launcher 执行完整后端测试集。 / Ran the full backend test suite through the JUnit Platform Launcher.

### 新功能 / Added Features

- 本阶段无新业务功能；只修复验收阻断问题。 / No new business feature in this phase; only acceptance blockers were fixed.

### 问题与解决方案 / Issues and Solutions

- `HealthControllerTest` 使用 `@WebMvcTest` 时仍会创建安全过滤器依赖，缺少 `JwtTokenService` 导致 Spring 测试上下文启动失败。 / `HealthControllerTest` still creates security-filter dependencies under `@WebMvcTest`; the missing `JwtTokenService` caused the Spring test context to fail.
  - 解决方案：在该测试中添加 `JwtTokenService` mock，保持健康检查测试不启用过滤器。 / Solution: added a `JwtTokenService` mock while keeping filters disabled for the health endpoint test.
- 常规 Maven 测试入口先后遇到 `无法关闭编译器资源` 和项目内 Maven 缓存损坏问题。 / The regular Maven test entrypoint first hit `unable to close compiler resource`, then a corrupted project-local Maven cache.
  - 解决方案：在 `pom.xml` 中启用 fork 编译，并清理后重拉损坏的 `spring-boot-starter-jdbc` / `HikariCP` 缓存。 / Solution: enabled forked compilation in `pom.xml`, then cleared and re-fetched the corrupted `spring-boot-starter-jdbc` / `HikariCP` cache.

### 验证 / Verification

- 数据库 `flyway_schema_history` 显示 V1-V9 均成功。 / Database `flyway_schema_history` shows V1-V9 all successful.
- 当前种子数据：4 个 `learning_units`、5 个 `learning_unit_senses`、12 个 `learning_unit_forms`、0 个 `learning_events`。 / Current seed data: 4 `learning_units`, 5 `learning_unit_senses`, 12 `learning_unit_forms`, and 0 `learning_events`.
- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 后端临时 JDK 21 编译通过。 / Backend temporary JDK 21 compilation passed.
- 后端 JUnit Launcher 测试 16/16 通过。 / Backend JUnit Launcher tests passed: 16/16.
- 后端 Maven 测试在清理损坏缓存后曾通过一次；当前机器仍需关注编译器资源关闭问题。 / Backend Maven tests passed once after clearing corrupted cache; this machine still needs attention for the compiler resource closing issue.

## Phase 4.8：对话事件真实落库验收 / Dialogue Event Persistence Acceptance

### 操作 / Operations

- 新增双 Agent 对话到 `learning_events` 的后端集成测试。 / Added a backend integration test from dual-agent dialogue to `learning_events`.
- 为 `LearningEventEntity` 补充只读 getter，便于测试和后续统计读取。 / Added read-only getters to `LearningEventEntity` for testing and future stats reads.
- 为 `LearningEventRepository` 新增按 `source_type/source_id` 查询事件的方法。 / Added a repository method to query events by `source_type/source_id`.

### 新功能 / Added Features

- 验证真实 `LearningDialogueService.reply(...)` 调用后，会根据语言单元出现写入事件。 / Verified that a real `LearningDialogueService.reply(...)` call writes events by language-unit occurrence.
- 验证 `bank account` 对话会产生 `UNIT_ATTEMPTED` 和 `UNIT_RECOMMENDED` 两类事件。 / Verified that a `bank account` dialogue creates `UNIT_ATTEMPTED` and `UNIT_RECOMMENDED` events.

### 问题与解决方案 / Issues and Solutions

- 测试最初使用了错误的响应字段 `id()`。 / The test initially used the wrong response field `id()`.
  - 解决方案：改为 `turnId()`，与 `LearningDialogueResponse` 保持一致。 / Solution: changed it to `turnId()` to match `LearningDialogueResponse`.
- Maven 常规入口在当前机器仍偶发 `无法关闭编译器资源`，但独立 JDK 21 编译和 JUnit Launcher 可以稳定验证代码。 / The regular Maven entrypoint still intermittently reports `unable to close compiler resource` on this machine, while standalone JDK 21 compilation and JUnit Launcher validate the code reliably.
  - 解决方案：本阶段不保留无效 Maven 配置改动，继续记录该环境问题。 / Solution: did not keep ineffective Maven configuration changes and kept the environment issue documented.

### 验证 / Verification

- 后端 JDK 21 临时编译通过。 / Backend temporary JDK 21 compilation passed.
- 后端 JUnit Launcher 测试 17/17 通过。 / Backend JUnit Launcher tests passed: 17/17.
- 测试使用事务回滚，验收后 `learning_events` 仍为 0，不污染当前数据。 / The test rolls back its transaction; `learning_events` remains 0 after acceptance and does not pollute current data.
- 数据库 `flyway_schema_history` 最新版本为 V9 且全部成功。 / Database `flyway_schema_history` latest version is V9 and all migrations are successful.

## Phase 4.9 准备：新对话交接提示词 / Phase 4.9 Preparation: New Handoff Prompt

### 操作 / Operations

- 新增 `docs/web-codex-handoff-prompt-v2.md`。 / Added `docs/web-codex-handoff-prompt-v2.md`.
- 将协作重点调整为业务开发优先、手动测试为主、自动化测试从轻。 / Adjusted collaboration focus toward business development, manual testing first, and lighter automated testing.

### 新功能 / Added Features

- 无业务功能变更；本次仅补充新对话交接文档。 / No business feature change; this update only adds a new handoff document.

### 问题与解决方案 / Issues and Solutions

- 旧交接提示词已落后于当前语言单元、学习事件和 Agent 接入前准备状态。 / The old handoff prompt was behind the current language-unit, learning-event, and pre-Agent integration state.
  - 解决方案：新增 V2 提示词，明确当前完成项、约束、待办阶段和测试策略。 / Solution: added a V2 prompt with current completion status, constraints, next phases, and testing strategy.

## Phase 4.9：语言单元匹配增强 / Language Unit Matching Enhancement

### 操作 / Operations

- 增强 `LearningEventService` 的本地匹配逻辑。 / Improved local matching in `LearningEventService`.
- 匹配从简单字符串包含改为规范化 token 序列匹配。 / Changed matching from simple string containment to normalized token sequence matching.
- 按 word-first 策略移除短语和句型种子数据，仅保留单词语言单元。 / Removed phrase and sentence-pattern seed data under the word-first strategy, keeping only word units for now.
- 更新 `docs/work-plan.md`。 / Updated `docs/work-plan.md`.

### 新增功能 / Added Features

- 表结构继续保留 WORD / PHRASE / SENTENCE_PATTERN，但当前事件记录只启用 WORD。 / The schema still reserves WORD / PHRASE / SENTENCE_PATTERN, but current event recording only enables WORD.
- 支持单词标点、大小写和词边界归一。 / Normalizes punctuation, case, and word boundaries for word units.
- 避免明显词边界误匹配，例如 `goodbye` 不会命中 `good`。 / Avoids obvious word-boundary false positives, such as matching `good` inside `goodbye`.
- 同一 learning unit 的重叠命中只保留更长匹配，减少明显重复事件。 / Keeps the longer match for overlapping hits within the same learning unit to reduce obvious duplicate events.
- 事件 payload 保留 `occurrenceIndex`，并新增 token 范围字段，为后续位置追踪预留空间。 / Event payload keeps `occurrenceIndex` and adds token-range fields for later position tracking.

### 问题与解决方案 / Issues and Solutions

- 原匹配方式依赖空格包裹字符串，单词边界和标点处理较弱。 / The previous matcher relied on space-wrapped strings and was weak for word boundaries and punctuation.
  - 解决方案：先只对 WORD 启用 token 匹配；短语和句型留到单词与词义闭环稳定后再启用。 / Solution: enable token matching for WORD only for now; phrases and sentence patterns will wait until the word and sense loop is stable.

### 验证 / Verification

- 增加轻量单元测试覆盖多次出现、词边界误匹配，以及非 WORD 单元暂不写事件。 / Added lightweight unit coverage for repeated occurrences, word-boundary false positives, and ignoring non-WORD units for now.

## Phase 4.9.1：Word-first 数据清理脚本 / Word-first Data Cleanup Script

### 操作 / Operations

- 新增 `docs/sql/phase4_word_first_cleanup.sql`。 / Added `docs/sql/phase4_word_first_cleanup.sql`.

### 新增功能 / Added Features

- 提供手动清理旧版 PHRASE / SENTENCE_PATTERN 语言单元数据的 SQL。 / Provides manual SQL to clean legacy PHRASE / SENTENCE_PATTERN language-unit data.
- 脚本包含预览查询、事务删除和执行后检查。 / The script includes preview queries, transactional deletes, and post-check queries.

### 问题与解决方案 / Issues and Solutions

- 当前业务已收缩为 word-first，但本地数据库可能已种入旧版短语和句型数据。 / The current business scope is word-first, but the local database may already contain old phrase and sentence-pattern data.
  - 解决方案：只清理非 WORD 语言单元及其关联事件，保留表结构和未来扩展位。 / Solution: clean only non-WORD units and their related events, while keeping the schema and future extension points.

## Phase 5.1：词义级掌握度最小更新 / Minimal Sense-level Mastery Update

### 操作 / Operations

- 新增 `V10__add_event_direction_and_sense_feedback.sql`。 / Added `V10__add_event_direction_and_sense_feedback.sql`.
- 为 `learning_events` 新增 `event_direction`，区分 `LEARNER_OUTPUT` 和 `LEARNER_INPUT`。 / Added `event_direction` to `learning_events` to distinguish `LEARNER_OUTPUT` and `LEARNER_INPUT`.
- 新增 `learning_unit_sense_feedback`，用于记录 Agent 判断数据库缺失词义的反馈。 / Added `learning_unit_sense_feedback` for Agent feedback when a sense is missing from the database.
- 新增 Agent 工具接口：`GET /api/learning/agent-tools/word-senses`、`POST /api/learning/agent-tools/events`、`POST /api/learning/agent-tools/sense-feedback`。 / Added Agent tool APIs: `GET /api/learning/agent-tools/word-senses`, `POST /api/learning/agent-tools/events`, and `POST /api/learning/agent-tools/sense-feedback`.
- 新增 `LearningUnitSenseMasteryService`，把明确归属到 sense 的事件更新到 `user_learning_unit_sense_stats`。 / Added `LearningUnitSenseMasteryService` to update `user_learning_unit_sense_stats` from events with explicit senses.
- 更新 `docs/sql/phase5_sense_mastery_schema.sql` 和 `docs/sql/contextflow_full_schema.sql`。 / Updated `docs/sql/phase5_sense_mastery_schema.sql` and `docs/sql/contextflow_full_schema.sql`.

### 新增功能 / Added Features

- 明确掌握度只属于 `learning_unit_senses`，不属于 `learning_units`。 / Mastery belongs only to `learning_unit_senses`, not `learning_units`.
- 单义词事件会自动归属到唯一 active sense；多义词事件在 Agent 未明确 sense 前只保留事件，不更新掌握度。 / Single-sense word events are assigned to the only active sense; multi-sense events stay event-only until the Agent provides a sense.
- `UNIT_EXPOSED`、`UNIT_ATTEMPTED`、`UNIT_CORRECTED`、`UNIT_RECOMMENDED` 会分别更新 exposure、attempt、correction、recommendation 计数。 / `UNIT_EXPOSED`, `UNIT_ATTEMPTED`, `UNIT_CORRECTED`, and `UNIT_RECOMMENDED` update exposure, attempt, correction, and recommendation counts respectively.
- 预留缺失词义反馈流，供后续人工更新词义表。 / Reserved the missing-sense feedback flow for later manual sense table updates.

### 问题与解决方案 / Issues and Solutions

- 用户输出句子需要由 Agent 判断具体词义，不能靠本地字符串匹配乱猜。 / Learner output needs Agent judgment for the exact sense and cannot rely on local string matching guesses.
  - 解决方案：本地只自动归属单义词；多义词等待 Agent 通过工具接口明确 `learningUnitSenseId`。 / Solution: local logic only assigns single-sense words; multi-sense words wait for the Agent to provide `learningUnitSenseId` through the tool API.
- Agent 输入给学习者的句子也需要记录学习事件，方便后续复习优先级计算。 / Sentences provided by the Agent also need learning events for later review-priority calculation.
  - 解决方案：新增 `event_direction`，把学习者输出与学习者输入稳定区分。 / Solution: added `event_direction` to distinguish learner output from learner input.

### 验证 / Verification

- 新增轻量单元测试覆盖 sense stats 更新与未归属事件不更新 stats。 / Added lightweight unit coverage for sense stats updates and skipping events without senses.

## Phase 5.2：复习优先级与学习计划 / Review Priority and Learning Plan

### 操作 / Operations

- 新增 `V11__add_review_priority_and_scenario_tags.sql`。 / Added `V11__add_review_priority_and_scenario_tags.sql`.
- 为 `user_learning_unit_sense_stats` 新增 `review_priority_score` 和 `last_priority_calculated_at`。 / Added `review_priority_score` and `last_priority_calculated_at` to `user_learning_unit_sense_stats`.
- 新增 `learning_unit_sense_scenario_tags`，用于把目标词义分配到合适场景。 / Added `learning_unit_sense_scenario_tags` for assigning target senses to suitable scenarios.
- 新增 `GET /api/review/plan`。 / Added `GET /api/review/plan`.
- 前端新增学习计划面板，展示复习词义、新词义和场景分组。 / Added a frontend learning plan panel showing review senses, new senses, and scenario groups.
- 同步修复 `LearningUnitSenseMasteryServiceTest` 的构造器依赖。 / Updated `LearningUnitSenseMasteryServiceTest` for the new constructor dependency.
- 调整前端 Learning plan：移动到对话区域下方，改为固定高度滚动窗口，并取消按场景分组展示。 / Adjusted the frontend Learning plan: moved it below the dialogue area, changed it to a fixed-height scroll window, and removed scenario-grouped display.
- 点击 `Start learning` 成功加载场景后自动刷新 Learning plan，移除学习者手动刷新计划的操作。 / Automatically refresh the Learning plan after `Start learning` loads a scenario successfully, removing the learner-facing manual refresh step.
- 更新 `docs/sql/phase5_review_priority_schema.sql`、`docs/sql/contextflow_full_schema.sql` 和 `docs/work-plan.md`。 / Updated `docs/sql/phase5_review_priority_schema.sql`, `docs/sql/contextflow_full_schema.sql`, and `docs/work-plan.md`.

### 新增功能 / Added Features

- `reviewPriorityScore` 成为复习候选唯一优先级依据；接口会按用户刷新并持久化该分数。 / `reviewPriorityScore` is now the only priority basis for review candidates; the API refreshes and persists it per user.
- 复习优先级综合到期、掌握弱度、用户输出、纠错、仅曝光、时间衰减和高频词反相关系数。 / Review priority combines due status, weak mastery, learner output, corrections, exposure-only state, recency decay, and frequency-inverse weighting.
- 新词义池按频率有用性和 `levelFitScore` 排序；高频新词义优先，但已学高频词义复习会被降权。 / The new-sense pool is ranked by frequency usefulness and `levelFitScore`; high-frequency new senses are prioritized, while learned high-frequency senses are dampened for review.
- 学习计划按比例混合复习词义与新词义，并按场景标签分组，供后续 Agent 按多个场景生成内容。 / The learning plan mixes review and new senses by ratio and groups them by scenario tags for later multi-scenario Agent generation.

### 问题与解决方案 / Issues and Solutions

- 只做一个“复习词列表”会导致后续 Agent 难以把词义合理塞进场景。 / A single flat review list would make it hard for the later Agent to place senses into suitable scenarios.
  - 解决方案：先选词义，再通过 `learning_unit_sense_scenario_tags` 分组到场景。 / Solution: select senses first, then group them into scenarios through `learning_unit_sense_scenario_tags`.
- 高频简单词既要记录曝光，又不应频繁占用复习队列。 / High-frequency simple words should still record exposure but should not dominate review queues.
  - 解决方案：事件和掌握度照常更新，复习调度阶段用频率反相关系数降低优先级。 / Solution: keep event and mastery updates unchanged, then reduce review priority through a frequency-inverse multiplier.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- Maven 默认入口仍使用 Java 17，报“不支持发行版本 21”；切换 JDK 21 后又遇到已知 `target/classes/application.yml` 写入拒绝。 / The default Maven entry still uses Java 17 and reports unsupported release 21; after switching to JDK 21 it still hits the known denied write to `target/classes/application.yml`.
- JDK 21 临时 `javac` 未输出业务代码语法错误，但本机仍返回已知“无法关闭编译器资源”。 / Temporary JDK 21 `javac` produced no business-code syntax errors, but this machine still returns the known compiler-resource closing error.
- 登录失败排查发现后端未监听 8080，根因是 DataGrip/MySQL 连接导致 Flyway V10 的 `ALTER TABLE learning_events` 等待元数据锁；释放连接后 V10-V11 已成功应用。 / Login failure investigation found that the backend was not listening on 8080 because DataGrip/MySQL connections made Flyway V10 wait for a metadata lock on `ALTER TABLE learning_events`; after releasing the connections, V10-V11 applied successfully.
- 使用临时 18080 后端验证 `/api/health` 和 `learner / learner123` 登录均成功。 / Verified `/api/health` and `learner / learner123` login successfully against a temporary backend on port 18080.

## Phase 5.3：遗忘曲线与复习调度稳定化 / Forgetting Curve and Review Scheduling Stabilization

### 操作 / Operations

- 新增 `V12__add_review_schedule_fields.sql`。 / Added `V12__add_review_schedule_fields.sql`.
- 为 `user_learning_unit_sense_stats` 新增 `stability_score`、`difficulty_score`、`last_reviewed_at` 和 `review_interval_hours`。 / Added `stability_score`, `difficulty_score`, `last_reviewed_at`, and `review_interval_hours` to `user_learning_unit_sense_stats`.
- 新增 `ReviewScheduleCalculator`，在有明确词义的学习事件写入后更新复习间隔和 `next_review_at`。 / Added `ReviewScheduleCalculator` to update review intervals and `next_review_at` after learning events with explicit senses.
- 扩展 `ReviewPriorityCalculator`，把遗忘风险和调度难度纳入 `reviewPriorityScore`。 / Extended `ReviewPriorityCalculator` to include forgetting risk and scheduling difficulty in `reviewPriorityScore`.
- 新增 `ReviewPriorityRefreshService` 和定时刷新任务，周期性刷新已学词义优先级。 / Added `ReviewPriorityRefreshService` and a scheduled refresh job to periodically refresh learned-sense priorities.
- 在 `application.yml` 显式配置复习优先级刷新开关、首次延迟和刷新间隔。 / Explicitly configured the review-priority refresh switch, initial delay, and refresh interval in `application.yml`.
- 调整 `/api/review/plan`，只读取持久化 `reviewPriorityScore`，不在请求中重算并写库。 / Adjusted `/api/review/plan` to read persisted `reviewPriorityScore` without recalculating and writing during the request.
- 新增 `docs/sql/phase5_review_schedule_schema.sql`，并更新完整 schema 和工作计划。 / Added `docs/sql/phase5_review_schedule_schema.sql`, and updated the full schema and work plan.

### 新增功能 / Added Features

- 词义复习间隔现在由事件类型、掌握度、稳定度和难度共同决定。 / Sense review intervals are now determined by event type, mastery, stability, and difficulty.
- `reviewPriorityScore` 仍是复习候选唯一排序依据，但其刷新来源变为事件写入和定时任务。 / `reviewPriorityScore` remains the only ordering basis for review candidates, but it is now refreshed by event writes and scheduled jobs.
- 高频词仍会被频率反相关系数降权；纠错事件仍可抬高复习压力。 / High-frequency words are still dampened by the frequency-inverse multiplier, while correction events can still raise review pressure.

### 问题与解决方案 / Issues and Solutions

- 请求时刷新所有用户词义优先级会把调度计算耦合到用户等待路径。 / Refreshing sense priorities during requests couples scheduling work to the learner waiting path.
  - 解决方案：计划接口只读持久化分数；事件写入即时刷新当前词义，定时任务刷新全量已学词义。 / Solution: the plan API only reads persisted scores; event writes refresh the current sense immediately, and a scheduled job refreshes all learned senses.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- JDK 21 临时 `javac` 未输出业务代码语法错误，但本机仍返回已知“无法关闭编译器资源”。 / Temporary JDK 21 `javac` produced no business-code syntax errors, but this machine still returns the known compiler-resource closing error.

## Phase 6.1：Agent 对话契约 / Agent Dialogue Contract

### 操作 / Operations

- 新增 `agent-dialogue.v1` 对话输入/输出 DTO。 / Added `agent-dialogue.v1` dialogue input/output DTOs.
- 新增 `AgentDialogueContractService`，用于生成本地模拟输出并校验真实 Agent 输出结构。 / Added `AgentDialogueContractService` to produce local mock output and validate real Agent output shape.
- 新增 admin 示例接口 `GET /api/admin/agent-contract/dialogue/sample`。 / Added the admin sample endpoint `GET /api/admin/agent-contract/dialogue/sample`.
- 将当前 `LearningDialogueService` 的本地规则输出包进统一 Agent 契约，再保存对话轮次与学习事件。 / Wrapped the current local-rule output in the unified Agent contract before saving dialogue turns and learning events.
- 新增 `docs/agent-contract.md`，说明输入、输出、`unitMentions`、事件方向和 Agent 工具接口。 / Added `docs/agent-contract.md` documenting input, output, `unitMentions`, event directions, and Agent tools.
- 更新 `docs/work-plan.md`。 / Updated `docs/work-plan.md`.

### 新增功能 / Added Features

- 固定真实 Agent 输出字段：`reply`、`feedback`、`corrections`、`naturalExpression`、`unitMentions`、`scoringSignal`。 / Fixed real Agent output fields: `reply`, `feedback`, `corrections`, `naturalExpression`, `unitMentions`, and `scoringSignal`.
- `unitMentions` 要求可记录事件必须携带 `learningUnitId` 和 `learningUnitSenseId`，继续保持词义级掌握度。 / `unitMentions` requires recordable events to carry `learningUnitId` and `learningUnitSenseId`, preserving sense-level mastery.
- 契约明确用户输出、用户输入、拼写/变形错误、乱输入、缺失词义反馈的处理边界。 / The contract clarifies handling boundaries for learner output, learner input, spelling/form errors, nonsense input, and missing-sense feedback.

### 验证 / Verification

- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- 常规 Maven 入口仍使用 Java 17，报“不支持发行版本 21”；本机未找到可直接使用的 JDK 21，未继续绕环境。 / The regular Maven entry still uses Java 17 and reports unsupported release 21; no directly usable JDK 21 was found on this machine, so no further environment workaround was attempted.

## Phase 6.1.1：Spring AI 与 DeepSeek 入口 / Spring AI and DeepSeek Entry

### 操作 / Operations

- 将 Spring Boot parent 从 `3.3.5` 升级到 `3.5.0`，以匹配 Spring AI 1.0.x 支持线。 / Upgraded the Spring Boot parent from `3.3.5` to `3.5.0` to match the Spring AI 1.0.x support line.
- 引入 `spring-ai-bom:1.0.9` 和 `spring-ai-starter-model-deepseek`。 / Added `spring-ai-bom:1.0.9` and `spring-ai-starter-model-deepseek`.
- 新增 `AgentModelClient` 抽象和 `SpringAiAgentModelClient` 实现。 / Added the `AgentModelClient` abstraction and `SpringAiAgentModelClient` implementation.
- 新增 `AgentRuntimeService` 和 `contextflow.agent.provider` 开关。 / Added `AgentRuntimeService` and the `contextflow.agent.provider` switch.
- 新增 DeepSeek 配置：`CONTEXTFLOW_AI_CHAT_MODEL`、`DEEPSEEK_API_KEY`、`DEEPSEEK_BASE_URL`、`DEEPSEEK_MODEL`。 / Added DeepSeek configuration: `CONTEXTFLOW_AI_CHAT_MODEL`, `DEEPSEEK_API_KEY`, `DEEPSEEK_BASE_URL`, and `DEEPSEEK_MODEL`.
- 新增 admin 运行时状态接口 `GET /api/admin/agent-contract/runtime`。 / Added the admin runtime status endpoint `GET /api/admin/agent-contract/runtime`.
- 更新 `docs/agent-contract.md` 和 `docs/work-plan.md`。 / Updated `docs/agent-contract.md` and `docs/work-plan.md`.

### 新增功能 / Added Features

- 默认仍为本地规则 Agent，真实模型调用默认关闭。 / The default Agent remains local-rule based, with real model calls disabled by default.
- 设置 `CONTEXTFLOW_AGENT_PROVIDER=spring-ai` 且 `CONTEXTFLOW_AI_CHAT_MODEL=deepseek` 后，学习对话可通过 Spring AI 调 DeepSeek chat model。 / After setting `CONTEXTFLOW_AGENT_PROVIDER=spring-ai` and `CONTEXTFLOW_AI_CHAT_MODEL=deepseek`, learning dialogue can call a DeepSeek chat model through Spring AI.
- Spring AI 输出会解析为 `AgentDialogueOutput` 并执行 `agent-dialogue.v1` 契约校验。 / Spring AI output is parsed into `AgentDialogueOutput` and validated against the `agent-dialogue.v1` contract.
- 模型失败默认回退本地规则，避免阻断当前学习流程。 / Model failures fall back to local rules by default to avoid blocking the current learning flow.

### 验证 / Verification

- 使用 JDK 21 和临时 Maven settings 编译通过：`mvn -q -DskipTests compile`。 / Compiled successfully with JDK 21 and a temporary Maven settings file: `mvn -q -DskipTests compile`.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- 修复默认配置会在无 API key 时创建 `deepSeekChatModel` 的问题；默认改为 `spring.ai.model.chat=none`，显式设置 `CONTEXTFLOW_AI_CHAT_MODEL=deepseek` 才启用。 / Fixed the issue where default configuration created `deepSeekChatModel` without an API key; default is now `spring.ai.model.chat=none`, and DeepSeek is enabled only by explicitly setting `CONTEXTFLOW_AI_CHAT_MODEL=deepseek`.
- 使用 JDK 21 和临时 Maven settings 跑通 `mvn -q test`。 / Ran `mvn -q test` successfully with JDK 21 and a temporary Maven settings file.

## Phase 6.2 raw：ECDICT 原始导入表 / ECDICT Raw Import Tables

### 操作 / Operations

- 新增 `V13__create_ecdict_raw_import_tables.sql`。 / Added `V13__create_ecdict_raw_import_tables.sql`.
- 新增 `ecdict_import_batches`，记录 ECDICT 文件来源、许可、导入状态和行数统计。 / Added `ecdict_import_batches` to record ECDICT source, license, import status, and row counts.
- 新增 `ecdict_import_entries`，原样保存 ECDICT CSV 字段，并保留 `bnc_rank`、`frq_rank` 解析列。 / Added `ecdict_import_entries` to store raw ECDICT CSV fields with parsed `bnc_rank` and `frq_rank`.
- 新增 `docs/sql/phase6_ecdict_raw_import_schema.sql` 和 `docs/sql/ecdict_raw_import_template.sql`。 / Added `docs/sql/phase6_ecdict_raw_import_schema.sql` and `docs/sql/ecdict_raw_import_template.sql`.
- 更新 `docs/sql/contextflow_full_schema.sql` 和 `docs/work-plan.md`。 / Updated `docs/sql/contextflow_full_schema.sql` and `docs/work-plan.md`.

### 新增功能 / Added Features

- 支持先把 ECDICT 原始数据落到 staging 表，再由后续任务规范化到 `learning_units` 和 `learning_unit_senses`。 / Supports loading ECDICT raw data into staging tables before later normalization into `learning_units` and `learning_unit_senses`.
- 不新增 Java CSV 解析依赖；当前导入模板使用 MySQL `LOAD DATA LOCAL INFILE`。 / Added no Java CSV parsing dependency; the current import template uses MySQL `LOAD DATA LOCAL INFILE`.
- 新增 `tools/EcdictRawImporter.java`，用于在 MySQL 禁用 `local_infile` 时通过 JDBC 批量导入原始 CSV。 / Added `tools/EcdictRawImporter.java` to batch-import raw CSV through JDBC when MySQL disables `local_infile`.
- 将 `data/*.csv` 加入 `.gitignore`，避免误提交大体积外部词典文件。 / Added `data/*.csv` to `.gitignore` to avoid accidentally committing large external dictionary files.

### 验证 / Verification

- 本次新增 SQL、文档和本地 JDBC 导入工具；未运行完整后端编译。 / This change added SQL, documentation, and a local JDBC importer; full backend compilation was not run.
- 已下载 ECDICT `ecdict.csv` 到本地忽略目录 `data/`，并导入当前 MySQL 原始表。 / Downloaded ECDICT `ecdict.csv` into the ignored local `data/` directory and imported it into the current MySQL raw tables.
- 当前成功批次 `batchId=2`：`ecdict_import_entries=770611`，`frq_rank` 非空 42231 行，`bnc_rank` 非空 45443 行。 / Current successful batch `batchId=2`: `ecdict_import_entries=770611`, with 42231 rows containing `frq_rank` and 45443 rows containing `bnc_rank`.
- MySQL 服务端禁用了 `local_infile` 且当前用户无权开启，因此改用 JDBC 批量导入工具完成真实落库。 / MySQL server disabled `local_infile` and the current user cannot enable it, so the real database load was completed with the JDBC batch importer.
- Flyway 历史当前仍停在 V12；已将 V13 改为 `CREATE TABLE IF NOT EXISTS`，后续正常启动后端时可补记 V13，不会覆盖已导入数据。 / Flyway history is still at V12; V13 now uses `CREATE TABLE IF NOT EXISTS`, so a later normal backend start can record V13 without overwriting imported data.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.

## Phase 6.2 clean：ECDICT 清洗单词表 / ECDICT Cleaned Word Table

### 操作 / Operations

- 新增 `V14__create_ecdict_clean_word_entries.sql`。 / Added `V14__create_ecdict_clean_word_entries.sql`.
- 新增 `ecdict_clean_word_entries`，作为清洗 staging 表，不属于正式学习单元表。 / Added `ecdict_clean_word_entries` as a cleaned staging table, not a formal learning unit table.
- 新增 `docs/sql/phase6_ecdict_clean_word_entries_schema.sql` 和 `docs/sql/ecdict_clean_word_entries_fill.sql`。 / Added `docs/sql/phase6_ecdict_clean_word_entries_schema.sql` and `docs/sql/ecdict_clean_word_entries_fill.sql`.
- 更新 `docs/sql/contextflow_full_schema.sql` 和 `docs/work-plan.md`。 / Updated `docs/sql/contextflow_full_schema.sql` and `docs/work-plan.md`.

### 新增功能 / Added Features

- `word-frequency-v3` 清洗规则只保留普通单词形态、必须有有效频率，并排除缩写、专名和重复字母噪声。 / The `word-frequency-v3` cleaning rule keeps ordinary word-shaped entries with valid frequency and excludes abbreviations, proper names, and repeated-letter noise.
- 清洗表保留 raw 追溯字段、释义、翻译、词性、标签、词形和有效频率排名，不修改 `ecdict_import_entries`。 / The cleaned table keeps raw trace fields, definitions, translations, POS, tags, forms, and valid frequency ranks without modifying `ecdict_import_entries`.

### 验证 / Verification

- 已从 `ecdict_import_entries` 清洗生成 `ecdict_clean_word_entries`。 / Generated `ecdict_clean_word_entries` from `ecdict_import_entries`.
- 当前原始表仍为 770611 行；清洗表按 `word-frequency-v3` 重建后为 46552 行，且全部有有效频率排名。 / The raw table remains at 770611 rows; the cleaned table was rebuilt with `word-frequency-v3` to 46552 rows, all with valid frequency ranks.
- `ecdict_clean_word_entries` 中非 `^[A-Za-z]+$` 的行数为 0，且 `aaa`、`aaad` 等缩写/字母串噪声不再进入清洗表。 / `ecdict_clean_word_entries` has 0 rows outside `^[A-Za-z]+$`, and abbreviation/letter-string noise such as `aaa` and `aaad` no longer enters the cleaned table.

### 调整 / Adjustment

- 将 `effective_frequency_rank` 改为 `COALESCE(frq_rank, bnc_rank)`，即优先使用 `frq_rank`，缺失时用 `bnc_rank`。 / Changed `effective_frequency_rank` to `COALESCE(frq_rank, bnc_rank)`, preferring `frq_rank` and falling back to `bnc_rank`.
- 新增生成列 `frequency_source`，取值为 `FRQ` 或 `BNC`，用于解释当前有效频率来源。 / Added generated column `frequency_source` with `FRQ` or `BNC` to explain the effective frequency source.
- 排除由同一字母重复组成的 token，例如 `zz`、`zzz`、`xxx`、`mmm`。 / Excluded tokens made of one repeated letter, such as `zz`, `zzz`, `xxx`, and `mmm`.

## Phase 6.2 formal-test：ECDICT 小批量正式导入 / ECDICT Small Formal Import

### 操作 / Operations

- 新增 `tools/EcdictFormalTestImporter.java`，用于从 `ecdict_clean_word_entries` 受控导入正式语言单元表。 / Added `tools/EcdictFormalTestImporter.java` to import controlled batches from `ecdict_clean_word_entries` into formal language-unit tables.
- 新增 `docs/sql/ecdict_formal_test_cleanup.sql`，保留本次清理旧 demo/测试正式数据的 SQL。 / Added `docs/sql/ecdict_formal_test_cleanup.sql` to preserve the cleanup SQL for old demo/test formal data.
- 将 `contextflow.content.seed-demo-units` 默认改为 `false`，避免后端启动后重新混入旧 demo 语言单元。 / Changed the default `contextflow.content.seed-demo-units` to `false` to avoid reseeding old demo language units on backend startup.
- 清理旧 `MANUAL_SEED` 语言单元、旧学习事件和旧来源数据后，从清洗表导入 top 500 高频候选。 / Cleaned old `MANUAL_SEED` language units, old learning events, and old source data before importing the top 500 high-frequency candidates from the cleaned table.

### 新增功能 / Added Features

- 导入 `learning_units`、`learning_unit_senses`、`learning_unit_forms`、`learning_data_sources` 和 `learning_unit_sense_sources`。 / Imported into `learning_units`, `learning_unit_senses`, `learning_unit_forms`, `learning_data_sources`, and `learning_unit_sense_sources`.
- `definition_raw` 按英文释义行拆分为多个 sense；中文释义暂保留整段原文，避免错误逐行对齐。 / `definition_raw` is split into multiple senses by English definition lines; Chinese translation keeps the full raw text for now to avoid incorrect line-by-line alignment.
- `exchange_raw` 解析为 lemma、复数、第三人称单数、过去式、过去分词、现在分词、比较级和最高级词形。 / `exchange_raw` is parsed into lemma, plural, third-person singular, past tense, past participle, present participle, comparative, and superlative forms.

### 验证 / Verification

- 本次正式表测试导入结果：`learning_units=499`，`learning_unit_senses=1638`，`learning_unit_forms=1727`，`learning_unit_sense_sources=6552`。 / Formal test import result: `learning_units=499`, `learning_unit_senses=1638`, `learning_unit_forms=1727`, and `learning_unit_sense_sources=6552`.
- 旧学习事件和旧掌握度统计已清空：`learning_events=0`，`user_learning_unit_sense_stats=0`。 / Old learning events and mastery stats were cleared: `learning_events=0`, `user_learning_unit_sense_stats=0`.

## Phase 6.2 app-data：单词数据管理与用户词库 / Word Data Management and Learner Vocabulary

### 操作 / Operations

- 撤销 admin 侧 ECDICT 导入入口；admin 不负责批量导入词库。 / Removed the admin-side ECDICT import entry; admin is not responsible for bulk dictionary import.
- 扩展 `GET /api/admin/learning-units/search` 和详情查询，使 admin 可查看非 active 词义状态。 / Extended `GET /api/admin/learning-units/search` and detail queries so admin can see non-active sense states.
- 新增 admin 单词 CRUD 接口：单个单词新增、单词本体更新、首级词义字段更新、安全删除。 / Added admin word CRUD APIs: single-word creation, word update, sense-field update, and safe deletion.
- 新增 learner 词库接口 `GET /api/learning/vocabulary`，支持全部、已学、未学和 query 查询。 / Added learner vocabulary API `GET /api/learning/vocabulary`, supporting all/learned/unlearned filters and query search.
- 新增前端 learner 词库模块和 admin 单词管理模块。 / Added the frontend learner vocabulary module and admin word-management module.

### 新增功能 / Added Features

- admin 新增单词一次只允许一个 WORD，并写入 `ADMIN_MANUAL/v1` 数据来源。 / Admin creation allows only one WORD at a time and records the `ADMIN_MANUAL/v1` data source.
- admin 删除单词时，如果已有 learning event 或用户词义掌握度统计，后端拒绝物理删除，建议改为 OFFLINE。 / Admin deletion is rejected when learning events or user sense stats exist, recommending OFFLINE instead.
- learner 词库按用户自己的 sense-level stats 标记 `LEARNED`、`PARTIAL`、`UNLEARNED`。 / Learner vocabulary marks words as `LEARNED`, `PARTIAL`, or `UNLEARNED` from the user's own sense-level stats.
- 当前 500 测试词库来自清洗表按 `effective_frequency_rank` 升序取前 500 个候选，不是原始 CSV 文件前 500 行。 / The current 500-word test set is selected from cleaned candidates by ascending `effective_frequency_rank`, not the first 500 raw CSV rows.
- 管理员界面、按钮和非学习内容提示改为中文。 / Admin UI, buttons, and non-learning hints were changed to Chinese.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过；默认 Maven 仍会因 E 盘仓库权限失败。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file; the default Maven path still fails because the E drive repository is not writable.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.

## Phase 6.3：真实 Agent 对话运行时 / Real Agent Dialogue Runtime

### 操作 / Operations

- 将学习对话输入的 `targetSenses` 接到 `/api/review/plan`，让真实 Agent 能看到当前复习/新词义目标。 / Wired dialogue `targetSenses` to `/api/review/plan` so the real Agent can see current review/new-sense targets.
- 增强 Spring AI Prompt，固定双 Agent 职责、输出 JSON 形状和 learning event 记录规则。 / Strengthened the Spring AI prompt with dual-agent roles, output JSON shape, and learning-event rules.
- 将 Agent 输出中的可记录 `unitMentions` 写入 `learning_events`，并继续触发词义级掌握度更新。 / Writes recordable `unitMentions` from Agent output into `learning_events`, continuing sense-level mastery updates.
- 新增前端 admin Agent 运行时状态面板。 / Added a frontend admin panel for Agent runtime status.
- 更新 `docs/agent-contract.md`。 / Updated `docs/agent-contract.md`.

### 新增功能 / Added Features

- 真实模型只允许使用输入中已有的 `learningUnitId` 和 `learningUnitSenseId`，不得编造 id。 / The real model may use only input-provided `learningUnitId` and `learningUnitSenseId`, never invented ids.
- 模型明确返回 non-recordable mention 时，不再用本地字符串匹配补事件，避免误记学习事件。 / When the model returns a non-recordable mention, local string matching no longer adds fallback events, avoiding false learning events.
- 模型输出如果带有前后多余文本，后端会提取首尾 JSON 对象再做契约校验。 / If model output contains extra surrounding text, the backend extracts the JSON object before contract validation.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过；默认 Maven 仍会因 E 盘仓库权限失败。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file; the default Maven path still fails because the E drive repository is not writable.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.

## Phase 6.3.1：DeepSeek API 探测入口 / DeepSeek API Probe Entry

### 操作 / Operations

- 新增 admin-only `POST /api/admin/agent-contract/dialogue/probe`，使用 `agent-dialogue.v1` 样例输入探测当前 Agent 运行时。 / Added admin-only `POST /api/admin/agent-contract/dialogue/probe` to probe the current Agent runtime with the `agent-dialogue.v1` sample input.
- `AgentRuntimeService` 新增 probe 结果，明确返回 provider、Spring AI client 状态、是否尝试模型、是否 fallback、契约校验、错误信息和示例输出。 / `AgentRuntimeService` now returns provider, Spring AI client status, model-attempted, fallback, contract validation, error, and sample output details for probes.
- 前端 admin Agent 运行时面板新增“探测模型”按钮和 probe 结果展示。 / Added a "Probe model" action and result display to the frontend admin Agent runtime panel.

### 说明 / Notes

- 本阶段没有新增依赖；继续使用已接入的 Spring AI DeepSeek starter。 / No new dependency was added; this continues to use the existing Spring AI DeepSeek starter.
- 未新增 SQL 变更。 / No SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
- 已用本地 Spring AI 1.0.9 配置元数据核对 DeepSeek 相关配置名。 / DeepSeek property names were checked against the local Spring AI 1.0.9 configuration metadata.

## Phase 6.3.2：Agent 运行时诊断 / Agent Runtime Diagnostics

### 操作 / Operations

- 扩展 Agent runtime status/probe 响应，返回 Spring AI 和 DeepSeek 自动配置诊断信息。 / Extended Agent runtime status/probe responses with Spring AI and DeepSeek auto-configuration diagnostics.
- 诊断字段包含 `spring.ai.model.chat`、DeepSeek key 是否已配置、base URL、模型名、ChatModel bean 名称和 AgentModelClient bean 名称。 / Diagnostics include `spring.ai.model.chat`, whether the DeepSeek key is configured, base URL, model name, ChatModel bean names, and AgentModelClient bean names.
- 前端 admin Agent 运行时面板展示诊断 JSON，便于判断 Spring AI 不可用的真实原因。 / The frontend admin Agent runtime panel now shows diagnostics JSON to identify the real cause when Spring AI is unavailable.

### 说明 / Notes

- 诊断只显示 key 是否存在，不返回 key 明文。 / Diagnostics show only whether the key exists and never return the key value.
- 未新增依赖，未新增 SQL。 / No dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file.

## Phase 6.3.3：Spring AI Client 注册修复 / Spring AI Client Registration Fix

### 问题 / Problem

- 运行时诊断显示 DeepSeek `ChatModel` 已创建，但 `AgentModelClient` bean 为空，导致真实模型未被尝试。 / Runtime diagnostics showed the DeepSeek `ChatModel` was created, but no `AgentModelClient` bean existed, so the real model was never attempted.

### 操作 / Operations

- 移除普通 `@Service` 上的 `@ConditionalOnBean(ChatModel.class)`，避免条件判断早于 Spring AI 自动配置。 / Removed `@ConditionalOnBean(ChatModel.class)` from the regular `@Service` to avoid evaluating the condition before Spring AI auto-configuration.
- `SpringAiAgentModelClient` 改为通过 `ObjectProvider<ChatModel>` 延迟读取模型。 / `SpringAiAgentModelClient` now reads `ChatModel` lazily through `ObjectProvider<ChatModel>`.
- `AgentRuntimeService` 改为用 `AgentModelClient.isAvailable()` 判断真实 Spring AI client 是否可用。 / `AgentRuntimeService` now uses `AgentModelClient.isAvailable()` to determine whether the real Spring AI client is usable.

### 说明 / Notes

- 本地 Agent 默认模式仍可启动；没有 `ChatModel` 时不会阻塞应用启动。 / The local Agent default mode can still start; missing `ChatModel` no longer blocks application startup.
- 未新增依赖，未新增 SQL。 / No dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file.

## Phase 6.3.4：Agent 输出契约调试增强 / Agent Output Contract Debugging

### 问题 / Problem

- DeepSeek API 已被调用，但模型返回的 `unitMentions` 缺少必填字段，导致契约校验失败并回退本地输出。 / DeepSeek was called, but the model returned `unitMentions` without required fields, causing contract validation failure and local fallback.

### 操作 / Operations

- 强化 Spring AI system prompt，明确 `unitMentions` 的必填字段、枚举值、`occurrenceIndex`、`confidence` 和 `payload` 要求。 / Strengthened the Spring AI system prompt with required `unitMentions` fields, enum values, `occurrenceIndex`, `confidence`, and `payload` rules.
- 新增 `AgentModelResponseException`，在模型输出不合约或无法解析时保留模型原始内容、解析后的模型输出和校验错误。 / Added `AgentModelResponseException` to preserve raw model content, parsed model output, and validation errors when model output fails the contract or cannot be parsed.
- probe 响应和前端 admin 面板区分“模型实际输出”和“最终输出”，避免把 fallback 样例误判为模型结果。 / Probe responses and the frontend admin panel now distinguish the actual model output from the final output, avoiding confusion with fallback samples.

### 说明 / Notes

- 契约不合格时仍不写学习事件，保持学习数据安全。 / Invalid contract output still does not write learning events, preserving learning-data safety.
- 未新增依赖，未新增 SQL。 / No dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file.

## Phase 6.3.5：Agent 工作流语义调试 / Agent Workflow Semantic Debugging

### 问题 / Problem

- DeepSeek API 已成功调用且契约校验通过，但模型返回了 `go/show/SKIP_UNRECOGNIZABLE` 这类未命中却带词义 id 的 `unitMention`。 / DeepSeek was called and contract validation passed, but the model returned a `unitMention` like `go/show/SKIP_UNRECOGNIZABLE`, carrying sense ids for an unmatched target.
- probe 样例自身不一致：输入目标词义是 `go`，样例输出却写 `bank`，且 `bank` 未真实出现在样例 reply 中。 / The probe sample was internally inconsistent: the target sense was `go`, while the sample output used `bank`, and `bank` did not actually appear in the sample reply.

### 操作 / Operations

- 修正 probe 样例，使目标词义、reply 文本和 `unitMention` 都指向 `bank:financial-institution`。 / Fixed the probe sample so the target sense, reply text, and `unitMention` all point to `bank:financial-institution`.
- 后端契约校验改为只允许 `RECORD_EVENT` 和 `RECORD_SPELLING_OR_FORM_ERROR` 出现在 `unitMentions`。 / Backend contract validation now allows only `RECORD_EVENT` and `RECORD_SPELLING_OR_FORM_ERROR` inside `unitMentions`.
- 后端新增 `sourceField` 与 `occurrenceText` 校验，要求 occurrence 真实出现在对应输出字段。 / Added backend validation for `sourceField` and `occurrenceText`, requiring the occurrence to actually appear in the referenced output field.
- Spring AI prompt 明确未命中、错误用法、不可识别输入必须返回 `unitMentions: []`，不能返回 SKIP mention。 / The Spring AI prompt now requires unmatched, wrong-usage, or unrecognizable input to return `unitMentions: []`, not SKIP mentions.

### 说明 / Notes

- 本次仍不新增依赖，未新增 SQL。 / No dependency or SQL change was added.
- 合约不合格时仍 fallback，不写学习事件。 / Invalid contract output still falls back and does not write learning events.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file.

## Phase 6.3.6：Agent Mention 安全过滤 / Agent Mention Safety Filter

### 问题 / Problem

- 真实模型仍可能生成顶层合规但局部错误的 `unitMention`，例如声称 `reply` 中出现了 `bank`，但实际 `reply` 没有该词。 / The real model can still produce a top-level valid response with locally wrong `unitMention` items, such as claiming `bank` appears in `reply` when it does not.
- 如果整轮 fallback，会浪费已经可用的真实 Roleplay/Mentor 回复。 / Falling back for the whole turn wastes usable real Roleplay/Mentor output.

### 操作 / Operations

- 新增 `AgentOutputSanitizationResult`，在模型输出后先逐条过滤 `unitMentions`。 / Added `AgentOutputSanitizationResult` to filter `unitMentions` item by item after model output.
- 无效 mention 会被丢弃；顶层字段合约通过时继续使用真实模型回复。 / Invalid mentions are dropped; if top-level fields pass the contract, the real model reply is still used.
- 顶层 JSON 或必填输出字段不合约时仍 fallback。 / Top-level JSON or required output-field failures still fall back.

### 说明 / Notes

- 过滤策略只减少误写事件，不放宽学习事件写入条件。 / The filter only reduces false event writes and does not loosen learning-event write requirements.
- 未新增依赖，未新增 SQL。 / No dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- 使用 JDK 21 和临时 Maven settings 执行 `mvn -q -DskipTests compile` 通过。 / Backend `mvn -q -DskipTests compile` passed with JDK 21 and a temporary Maven settings file.

## Phase 6.3.7：Agent 调试面板布局修复 / Agent Debug Panel Layout Fix

### 问题 / Problem

- admin Agent 运行时面板中的诊断 JSON 和 probe 输出过长，在三列调试网格中被右侧遮挡。 / Long diagnostics JSON and probe output in the admin Agent runtime panel were clipped in the three-column debug grid.

### 操作 / Operations

- Agent 运行时面板改为横跨 admin 调试区整行。 / The Agent runtime panel now spans the full admin debug grid width.
- 为面板、指标卡、JSON `pre` 和输出 label 增加 `min-width: 0`、换行和滚动约束。 / Added `min-width: 0`, wrapping, and scrolling constraints to panels, metric cards, JSON `pre`, and output labels.

### 说明 / Notes

- 仅前端布局调整，未新增依赖，未新增 SQL。 / Frontend layout change only; no dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.

## Phase 6.3.8：Admin 控制台清理 / Admin Console Cleanup

### 问题 / Problem

- admin 页面仍展示水平测试沙盒和管理员账号画像原始数据，但 admin 不作为具体学习者使用这些流程。 / The admin page still showed the placement sandbox and raw admin profile data, but admin does not use learner-specific flows.

### 操作 / Operations

- 移除 admin 控制台中的“测试沙盒”和“用户画像原始数据”面板。 / Removed the "Test sandbox" and "Raw user profile" panels from the admin console.
- admin 登录后不再自动加载自己的 learner profile；learner 登录仍正常加载画像和词库。 / Admin login no longer auto-loads its own learner profile; learner login still loads the profile and vocabulary normally.

### 说明 / Notes

- learner 的正式水平测试和画像展示未移除。 / The learner placement flow and profile display were not removed.
- 仅前端调整，未新增依赖，未新增 SQL。 / Frontend change only; no dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.

## Phase 6.3.9：Learner 词库二级页面 / Learner Vocabulary Secondary Page

### 问题 / Problem

- learner 首页直接展示完整词库，占用主学习界面空间。 / The learner home page displayed the full vocabulary list directly, taking space from the main learning interface.

### 操作 / Operations

- learner 首页改为只展示“进入词库”入口。 / The learner home page now shows only an "Enter vocabulary" entry.
- 新增词库二级页面，包含返回学习首页、刷新、筛选和查询。 / Added a secondary vocabulary page with return, refresh, filter, and search actions.
- learner 登录后不再自动加载词库；进入词库页时再加载。 / Learner login no longer auto-loads vocabulary; it loads when entering the vocabulary page.

### 说明 / Notes

- learner 正式学习、水平测试和画像流程不变。 / Learner learning, placement, and profile flows are unchanged.
- 仅前端调整，未新增依赖，未新增 SQL。 / Frontend change only; no dependency or SQL change was added.

### 验证 / Verification

- 前端 `npm run typecheck` 通过。 / Frontend `npm run typecheck` passed.
- `git diff --check` 通过，仅有 Windows 换行提示。 / `git diff --check` passed with only Windows line-ending warnings.
# Phase 6.3.10：学习任务目标契约 / Learning Task Goal Contract

## 问题 / Problem

- 原实现把 `scenario` 同时当分类和完整学习场景，无法表达“AI 根据待学词义提出任务目标”的真实流程。/ The previous implementation used `scenario` as both category and full learning scenario, which did not express the real flow where AI proposes a task goal from target senses.

## 操作 / Operations

- READY 学习包内容新增 `learningTask.goal`、`learningTask.instructionLanguage`、`learningTask.expectedLearnerAction`。/ Added `learningTask.goal`, `learningTask.instructionLanguage`, and `learningTask.expectedLearnerAction` to READY package content.
- Agent 输入契约新增 `taskGoal`、`taskInstructionLanguage`、`expectedLearnerAction`。/ Added `taskGoal`, `taskInstructionLanguage`, and `expectedLearnerAction` to the Agent input contract.
- Spring AI prompt 改为按学习任务推进对话，`scenarioCode`/`scenarioName` 只作为分类和种子标签。/ Updated the Spring AI prompt to advance the dialogue by learning task; `scenarioCode`/`scenarioName` are only category and seed labels.
- 前端学习区改为展示“任务目标”，旧包缺少 `learningTask` 时回退到 `scenario.description`。/ Updated the frontend learning area to show "task goal", with fallback to `scenario.description` for older packages.
- 新增 `docs/learning-task-design.md` 记录任务目标设计约束。/ Added `docs/learning-task-design.md` for learning-task design constraints.

## 说明 / Notes

- 未新增依赖，未新增 SQL；任务目标先作为 READY 学习包 JSON 缓存。/ No dependency or SQL change was added; task goals are cached in READY package JSON for now.
# Phase 6.3.11：旧 READY 包中文任务兜底 / Chinese Task Fallback for Old READY Packages

## 问题 / Problem

- 旧 READY 包缺少 `learningTask`，前端回退到英文 `scenario.description`，导致低水平任务目标显示英文。/ Old READY packages lacked `learningTask`, so the frontend fell back to English `scenario.description`, causing lower-level task goals to appear in English.

## 操作 / Operations

- 前端为旧包按 `scenario.code` 提供中文 `taskGoal` 和 `expectedLearnerAction` 兜底。/ Added frontend Chinese fallbacks for `taskGoal` and `expectedLearnerAction` by `scenario.code`.
- 后端 Agent 输入为旧包提供同样的中文任务目标兜底，避免模型收到英文旧描述。/ Added backend Agent-input fallbacks so old packages do not send English legacy descriptions to the model.
- 清理当前数据库中缺少 `learningTask` 的旧 READY 包。/ Cleaned current READY packages missing `learningTask`.

## 说明 / Notes

- 未新增依赖，未新增 SQL 迁移。/ No dependency or SQL migration was added.
# Phase 6.3.12：Agent 角色连续性与 Mentor 历史 / Agent Role Continuity and Mentor History

## 问题 / Problem

- Roleplay Agent 可能重复 opening line，或在用户输入模糊时替 learner 说话，破坏沉浸式角色体验。/ The Roleplay Agent could repeat the opening line or speak for the learner when input was unclear, breaking immersion.
- Mentor 面板只显示最新反馈，用户无法回看历史 Mentor 建议。/ The Mentor panel only showed the latest feedback, so learners could not review past Mentor guidance.

## 操作 / Operations

- Agent 输入契约新增 `roleplayPersona`、`learnerRole`、`openingLine`。/ Added `roleplayPersona`, `learnerRole`, and `openingLine` to the Agent input contract.
- `openingLine` 作为第 0 条历史传给模型，避免模型重复开场。/ Added `openingLine` as turn 0 in dialogue history to prevent repeated openings.
- Spring AI prompt 增加角色边界：Roleplay 不能替 learner 说话，不能编造用户事实，模糊输入要原角色澄清。/ Hardened the Spring AI prompt: Roleplay must not speak for the learner, invent learner facts, or mishandle unclear input.
- 后端增加轻量 guard：重复 opening line/近期回复或明显说话人错位时，替换为安全角色回复并丢弃该轮 `unitMentions`。/ Added a lightweight backend guard: repeated openings/recent replies or clear speaker-role breaks are replaced with safe in-character replies and the turn's `unitMentions` are dropped.
- 前端 Mentor 面板改为滚动历史窗口，每轮同时显示用户输入和 Mentor 回复。/ Changed the Mentor panel into a scrollable history window showing both learner input and Mentor reply for each turn.

## 说明 / Notes

- 未新增依赖，未新增 SQL 迁移。/ No dependency or SQL migration was added.
