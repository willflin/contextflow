# ContextFlow 架构 / ContextFlow Architecture

## 项目定位 / Product Positioning

ContextFlow 是一个“预生成驱动”的 AI 英语学习系统。

ContextFlow is a pre-generation-driven AI English learning system.

AI 提前生成学习内容、复习内容和音频。用户只消费 READY 状态内容，学习过程不应被 AI 生成等待阻塞。

AI generates learning content, review content, and audio in advance. Users only consume READY content, so learning should not block on AI generation.

## 核心流程 / Core Flow

```text
水平测试 / placement test
-> 用户画像 / user profile
-> 预生成学习包 / pre-generated learning packages
-> 双 Agent 学习 / dual-agent learning
-> 学习事件 / learning events
-> 掌握度统计 / unit stats
-> 预生成复习包 / pre-generated review packages
-> 复习 / review
```

## 用户角色 / User Roles

- 学习者：学习、复习、提交反馈、管理个人资料。 / Learner: learns, reviews, submits feedback, manages personal profile.
- 管理员：管理场景模板、Prompt 模板、用户、反馈、AI 审计和流量指标。 / Admin: manages scenario templates, prompt templates, users, feedback, AI audit, and traffic metrics.

管理员不能直接修改用户学习事件或掌握度统计。

Admins must not directly modify user learning events or mastery statistics.

## 三大系统 / Three Main Systems

### 水平测试 / Placement

测试语境理解能力，而不只是词汇量。

Tests contextual understanding, not only vocabulary size.

### 学习系统 / Learning

使用短场景，例如酒店入住、银行开户、购物、街上被警察拦下。

Uses short scenarios such as hotel check-in, banking, shopping, or police stop.

### 复习系统 / Review

根据薄弱语言单元和 `next_review_at` 生成新语境复习包。

Generates new-context review packages based on weak language units and `next_review_at`.

## 双 Agent / Dual Agent

- Roleplay Agent：沉浸式英文对话。 / immersive English conversation.
- Mentor Agent：纠错、解释和更自然表达建议。 / correction, explanation, and natural expression suggestions.

第一版应使用一次结构化 AI 响应，而不是两个模型调用。

The first implementation should use one structured AI response instead of two model calls.

## 学习单元与学习事件 / Learning Units and Learning Events

学习单元是掌握度统计的入口对象，包括单词、词组和句型。

Learning units are the entry objects for mastery tracking, including words, phrases, and sentence patterns.

`learning_units` 只保存语言单元本体；`learning_unit_senses` 保存具体词义或用法；`learning_unit_forms` 保存词形，用于 `went -> go`、`better -> good` 这类匹配。

`learning_units` stores only the language unit identity; `learning_unit_senses` stores concrete senses or usages; `learning_unit_forms` stores surface forms for matching cases such as `went -> go` and `better -> good`.

学习事件不是一次对话轮次，而是某个学习单元在学习上下文中出现一次的记录。事件必须关联 `learning_unit_id`，能可靠确认具体词义时才填 `learning_unit_sense_id`。

A learning event is not a dialogue turn. It records one occurrence of one learning unit in a learning context. Events must reference `learning_unit_id`; `learning_unit_sense_id` is filled only when the concrete sense can be reliably identified.

第一版先用结构化学习单元表做匹配；RAG 和向量库等检索增强在数据规模证明必要后再加入。

The first version uses structured learning-unit matching. RAG and vector search are added only after data volume justifies them.

当前不做 AI 词义消歧、不做大词库导入、不自动更新词义掌握度统计。

The current phase does not implement AI sense disambiguation, large vocabulary import, or automatic sense-level mastery updates.

## 后端模块 / Backend Modules

```text
auth          认证授权 / authentication and authorization
user          用户资料与画像 / user profile
placement     水平测试 / placement testing
content       语言单元 / language units
scenario      场景模板 / scenario templates
learning      学习包与学习事件 / learning packages and events
review        复习包与遗忘曲线 / review packages and scheduling
ai            AI 生成与校验 / AI generation and validation
audio         音频生成与缓存 / audio generation and cache
memory        用户记忆 / user memory
feedback      用户反馈 / user feedback
admin         管理员后台 / admin console
audit         AI 审计 / AI audit
analytics     数据统计 / analytics
common        通用能力 / shared utilities
```

## 中间件原则 / Middleware Policy

只有存在明确业务依据时才加入中间件。

Middleware is added only when there is a clear business reason.

- Redis：缓存 READY 内容并限制 AI 生成频率。 / cache READY packages and rate-limit AI generation.
- Docker：稳定本地和演示环境。 / stable local/demo environment.
- Kafka/RabbitMQ：规模需要时处理异步生成和学习事件。 / async generation and learning event processing after scale requires it.
- MinIO：加入音频生成后存储音频/对象资源。 / audio/object storage after audio generation is added.
- pgvector/Elasticsearch：数据量证明有必要后增强搜索/RAG。 / search/RAG enhancement after data volume justifies it.
