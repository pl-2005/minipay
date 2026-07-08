# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

MiniPay 是一个教学支付平台期末工程，实现从商户创建订单到支付确认、通知回调的完整支付主链路。

## 常用命令

### 后端 (Maven, JDK 21)

```bash
# 全量构建（含测试）
mvn -B -ntp verify

# 跳过测试构建
mvn -B -ntp verify -DskipTests

# 构建单个模块及依赖
mvn -B -ntp -pl backend/<service-name> -am verify
```

### 前端 (Vue 3 + Vite)

```bash
cd frontend
npm ci                    # CI 安装依赖（使用 lock 文件）
npm install               # 本地安装依赖
npm run dev               # 启动开发服务器 (port 5173, proxy /api → :8080)
npm run build             # 类型检查 + 生产构建
npm test                  # 运行 Vitest 测试
```

### Docker Compose

```bash
cp .env.example .env                   # 首次：复制环境变量模板
docker compose up -d --build           # 启动全部服务
docker compose config --quiet          # 校验 compose 文件格式
docker compose logs -f <service-name>  # 查看服务日志
```

### 本地入口

| 入口 | 地址 |
|------|------|
| 前端 | http://localhost |
| API Gateway | http://localhost:8080 |
| RabbitMQ 管理 | http://localhost:15672 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| SkyWalking UI | http://localhost:8088 |

演示账号：`merchant-demo` / `password`（商户），`admin-demo` / `password`（运营）。

## 架构概览

### 微服务拓扑

```
Nginx (:80)
  ├── /api/*  → gateway-service (:8080)
  └── /*      → frontend (:80)

gateway-service (:8080) — Spring Cloud Gateway
  ├── 全局 AuthGatewayFilter：校验 Bearer token + 角色权限
  ├── Redis RateLimiter：按路径限流
  └── 路由分发到各业务服务

auth-service     (:8101) — 登录认证、签发/解析 token
order-service    (:8102) — 订单状态机、缓存管理
payment-service  (:8103) — 支付确认、幂等、outbox 事件发布
merchant-service (:8104) — 商户创建订单、订单列表
notify-service   (:8105) — 支付事件监听、商户回调、重试
admin-service    (:8106) — 运营后台查询（订单/支付/事件/通知）
```

### 支付主链路

```
merchant-service          payment-service           RabbitMQ
  创建订单(PENDING)  →  确认支付(幂等检查)
                         ├── 更新订单状态 PAID
                         ├── 写入 payment_record
                         ├── 写入 payment_event (outbox)
                         ├── 写入 notify_record
                         └── 发布 payment.success 事件 ──→  ├── notify-service
                                                            │     └── 商户回调(重试≤3次)
                                                            └── order-service
                                                                  └── 更新订单状态 PAID
```

### 关键设计

- **订单状态机**：`OrderStatusTransferUtil` 定义严格的状态流转（PENDING→PAYING→PAID/EXPIRED/CLOSED），终态不可变更。
- **支付幂等**：通过 `payment_record.uk_payment_idempotency (order_no, idempotency_key)` 唯一约束保证，重复请求返回已有支付结果。
- **Outbox 模式**：`payment-service` 在事务内写入 `payment_event`，`PaymentEventPublishScheduler` 定时扫描 PENDING/PUBLISH_FAILED 事件补发到 RabbitMQ，保证事件必达。
- **通知重试**：`notify-service` 回调失败后按指数退避重试（1min→2min→4min→...max 30min），最多 3 次。
- **订单缓存**：`order-service` 使用 Redis 缓存订单详情（cache-aside），状态变更时主动删除缓存。

### 模块职责

- **`backend/common`**：共享模块 — `ApiResponse<T>`（统一响应体）、`ErrorCode`（错误码枚举）、`BusinessException`（业务异常）、`MinipayTokenService`（token 签发/解析）、`PaymentEventMessaging`（RabbitMQ 交换机/队列/路由键常量）。
- **`db/migration`**：Flyway SQL 迁移脚本，`V1__init_schema.sql` 建表，`V2__seed_dev_data.sql` 插入演示数据。
- **`deploy/`**：Nginx 配置、Prometheus 配置、Grafana 数据源 provisioning。
- **前端路由**：`/login`（公开）、`/merchant/orders`（商户）、`/pay/:orderNo`（商户）、`/admin`（运营）。角色校验在 `router.beforeEach` 中完成。
- **前端权限**：token 存 localStorage，Axios 拦截器自动附加 `Authorization: Bearer <token>`，401 响应自动清除会话。

### 技术栈版本要点

- JDK 21 + Spring Boot 3.5.15 + Spring Cloud 2025.0.3
- MyBatis-Plus 3.5.16（数据访问，无 XML mapper）
- PostgreSQL 18.4（权威数据源）、Redis 8.0.6（缓存/限流）、RabbitMQ 4.3.2（异步消息）
- 密码存储使用 `{noop}` 前缀明文（演示用途，非生产安全）
- 前端 Vite 开发服务器通过 proxy 将 `/api` 转发到 `localhost:8080`
