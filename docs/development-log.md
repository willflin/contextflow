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
