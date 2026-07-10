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

当前只包含 Phase 0 基础框架：

This repository currently contains only the Phase 0 foundation:

- Spring Boot 后端 / Spring Boot backend
- React + TypeScript + Vite 前端 / React + TypeScript + Vite frontend
- 后端健康检查接口 / backend health check API
- 前端健康检查页面 / frontend health check page

暂未加入：

Not included yet:

- 数据库 / database
- 登录 / login
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

## 开发原则 / Development Principle

每个中间件都必须有清晰业务依据。

Each middleware must have a clear business reason.

- Redis：缓存 READY 学习/复习包、限流、token 黑名单 / cache READY learning/review packages, rate limiting, token blacklist
- Docker：稳定本地和演示环境 / stable local and demo environment
- Kafka/RabbitMQ：后期异步事件和生成任务处理 / later async event and generation processing
- MinIO：后期音频和对象存储 / later audio/object storage
- Prometheus/Grafana：后期 AI 成本和系统监控 / later AI cost and system monitoring
