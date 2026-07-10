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
