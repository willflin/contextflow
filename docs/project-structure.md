# 项目结构 / Project Structure

## 后端 / Backend

后端采用模块化单体。每个业务模块先保持清晰包边界，暂不拆微服务。

The backend uses a modular monolith. Each business module keeps a clear package boundary without early microservice splitting.

```text
com.contextflow
  auth          认证授权 / authentication
  user          用户资料与画像 / user profile
  placement     水平测试 / placement testing
  content       语言单元 / language units
  scenario      场景模板 / scenario templates
  learning      学习包与学习事件 / learning packages and events
  review        复习包与遗忘曲线 / review packages
  ai            AI 生成与校验 / AI generation and validation
  audio         音频生成与缓存 / audio generation and cache
  memory        用户记忆 / user memory
  feedback      用户反馈 / user feedback
  admin         管理员后台 / admin console
  audit         审计日志 / audit log
  analytics     数据统计 / analytics
  common        通用能力 / shared utilities
```

## 前端 / Frontend

前端按功能域组织页面和组件。

The frontend is organized by feature domain.

```text
src/
  app/          应用入口级配置 / app-level configuration
  features/     业务功能模块 / business features
  shared/       共享 API、类型、UI / shared API, types, and UI
```

## 当前原则 / Current Rule

先建立清晰边界，再逐步填充功能。

Define clear boundaries first, then add features step by step.

