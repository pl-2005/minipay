# MiniPay 技术选型与依赖版本

## 1. 选型原则

MiniPay 是课程期末工程，版本选择遵循以下原则：

1. 与既有方案保持一致，后端采用 Spring Boot 3 + JDK 21。
2. 优先选择当前仍受支持的稳定版本，不追求刚发布的大版本。
3. 关键依赖固定版本，避免小组成员本地环境不一致。
4. 容器镜像尽量使用明确 tag，避免 `latest` 带来的不可复现问题。
5. 主链路优先，监控、链路追踪、CI 等工程能力以可演示和可复核为目标。

## 2. 后端版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21 | LTS 版本，匹配 Spring Boot 3.x |
| Maven | 3.9.11 | Docker 构建镜像使用 |
| Spring Boot | 3.5.15 | Spring Boot 3.5 系列当前稳定补丁版本 |
| Spring Cloud | 2025.0.3 | 2025.0.x 对应 Spring Boot 3.5.x |
| Spring Cloud Gateway | 由 Spring Cloud BOM 管理 | API Gateway、路由、限流入口 |
| Spring Cloud OpenFeign | 由 Spring Cloud BOM 管理 | 服务间同步调用 |
| Spring Security | 由 Spring Boot BOM 管理 | 登录认证、角色权限 |
| MyBatis-Plus Boot3 Starter | 3.5.16 | 数据访问层 |
| PostgreSQL JDBC Driver | 由 Spring Boot BOM 管理 | PostgreSQL 驱动 |
| Flyway | Docker 镜像 11.15.0 | 数据库版本迁移 |
| Micrometer Prometheus Registry | 由 Spring Boot BOM 管理 | 暴露 Prometheus 指标 |

## 3. 前端版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Node.js | 24 LTS | Vite 8 支持，CI 和 Docker 使用 Node 24 |
| Vue | 3.5.38 | 前端框架 |
| Vite | 8.0.16 | 构建工具 |
| @vitejs/plugin-vue | 6.0.7 | Vue SFC 支持 |
| TypeScript | 5.9.3 | 保守固定 5.9 系列，降低 Vue 生态兼容风险 |
| vue-tsc | 3.3.5 | Vue 类型检查 |
| Vue Router | 5.1.0 | 前端路由 |
| Pinia | 3.0.4 | 前端状态管理 |
| Axios | 1.18.0 | HTTP 客户端 |
| Element Plus | 2.14.2 | UI 组件库 |
| Vitest | 4.1.9 | 前端测试 |

## 4. 基础设施版本

| 组件 | 版本 / 镜像 | 用途 |
| --- | --- | --- |
| PostgreSQL | `postgres:18.4` | 订单、支付流水、商户、通知等权威数据 |
| Redis | `redis:8.0.6-alpine` | 订单状态缓存、幂等 token、短期锁 |
| RabbitMQ | `rabbitmq:4.3.2-management` | 支付结果事件、订单更新、通知事件 |
| Nginx | `nginx:1.29-alpine` | 前端静态资源托管和 API 反向代理 |
| Prometheus | `prom/prometheus:v3.12.0` | 指标采集 |
| Grafana | `grafana/grafana:13.0.2` | 监控面板 |
| SkyWalking OAP | `apache/skywalking-oap-server:10.3.0-java21` | 链路追踪后端 |
| SkyWalking UI | `apache/skywalking-ui:10.3.0-java21` | 链路追踪展示 |

## 5. 关键取舍

### 5.1 为什么不用 Spring Boot 4

Maven Central 当前已经提供 Spring Boot 4.x，但本项目沿用前期方案中的 Spring Boot 3 + JDK 21。Spring Cloud 官方兼容矩阵显示，Spring Cloud 2025.0.x 对应 Spring Boot 3.5.x，而 2025.1.x 对应 Spring Boot 4.0.x。课程项目更关注稳定交付，因此选择 Spring Boot 3.5.15 + Spring Cloud 2025.0.3。

### 5.2 为什么限流用 Gateway + RedisRateLimiter

限流必须落在统一入口，避免每个业务服务重复实现。Spring Cloud Gateway 自带 Redis RateLimiter，和本项目已有 Redis 依赖一致，适合期末工程快速落地。

### 5.3 为什么使用 RabbitMQ

支付成功后的订单状态更新、通知记录和后续审计动作不应阻塞支付主请求。RabbitMQ 用于支付结果事件投递，降低支付服务与订单服务、通知服务之间的耦合。

### 5.4 为什么使用 PostgreSQL 18.4

PostgreSQL 18 已发布，18.4 是当前安全修复版本。课程项目需要事务、约束、索引、JSONB 和复杂查询能力，PostgreSQL 适合作为交易原型的权威数据源。

### 5.5 为什么 TypeScript 固定 5.9.3

TypeScript npm latest 已进入 6.x，但 Vue 工具链对 5.x 的生态兼容更成熟。本项目固定 TypeScript 5.9.3，以减少课程项目中不必要的工具链风险。

## 6. 官方依据

- Spring Cloud 兼容矩阵：https://spring.io/projects/spring-cloud
- Spring Boot Maven metadata：https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/maven-metadata.xml
- MyBatis-Plus Maven metadata：https://repo1.maven.org/maven2/com/baomidou/mybatis-plus-spring-boot3-starter/maven-metadata.xml
- Node.js 发布状态：https://nodejs.org/en/about/previous-releases
- Vite 版本说明：https://vite.dev/releases
- PostgreSQL 发行说明：https://www.postgresql.org/docs/release/
- RabbitMQ 下载页：https://www.rabbitmq.com/docs/download
- Prometheus 下载页：https://prometheus.io/download/
- Grafana 下载页：https://grafana.com/grafana/download
- SkyWalking Docker 镜像：https://hub.docker.com/r/apache/skywalking-oap-server/tags

