# MiniPay

MiniPay 是一个教学支付平台期末工程项目，目标是实现可运行、可演示、可复核的支付主链路：

```text
商户创建订单 -> 用户发起模拟支付 -> 支付状态更新 -> 商户结果通知 -> 结果查询与展示
```

## 技术栈

- JDK 21
- Spring Boot 3.5.15
- Spring Cloud 2025.0.3
- MyBatis-Plus 3.5.16
- PostgreSQL 18.4
- Redis 8.0.6
- RabbitMQ 4.3.2
- Vue 3.5.38 + Vite 8.0.16
- Docker Compose
- Prometheus 3.12.0 + Grafana 13.0.2
- SkyWalking 10.3.0

完整选型说明见 [docs/tech-stack.md](docs/tech-stack.md)。

## 项目结构

```text
minipay/
├── backend/
│   ├── common/
│   ├── gateway-service/
│   ├── auth-service/
│   ├── order-service/
│   ├── payment-service/
│   ├── merchant-service/
│   ├── notify-service/
│   └── admin-service/
├── frontend/
├── db/migration/
├── deploy/
│   ├── nginx/
│   ├── prometheus/
│   └── grafana/
├── docs/
└── docker-compose.yml
```

## 本地启动

复制环境变量模板：

```bash
cp .env.example .env
```

启动基础设施和应用：

```bash
docker compose up -d --build
```

常用入口：

- 前端入口：http://localhost
- API Gateway：http://localhost:8080（访问根路径会跳转到前端）
- RabbitMQ 管理台：http://localhost:15672
- Prometheus：http://localhost:9090
- Grafana：http://localhost:3000
- SkyWalking UI：http://localhost:8088

演示账号：

| 用户名 | 密码 | 角色 | 关联商户 |
| --- | --- | --- | --- |
| `merchant-demo` | `password` | 商户 | 全部演示商户 |
| `merchant-sunrise` | `password` | 商户 | `M10002` Sunrise Market |
| `merchant-harbor` | `password` | 商户 | `M10003` Blue Harbor Hotel |
| `merchant-northwind` | `password` | 商户 | `M10004` Northwind Books |
| `merchant-greenfield` | `password` | 商户 | `M10005` Green Field Cafe |
| `merchant-nova` | `password` | 商户 | `M10006` Nova Digital |
| `admin-demo` | `password` | 运营 | 不关联商户 |

访问业务页面前需要先登录。前端会保存登录 token，并在请求中自动添加 `Authorization: Bearer <token>`；网关会统一校验 token 和角色权限。

支付成功后会通过 RabbitMQ 触发 `notify-service` 执行商户回调。`payment-service` 会等待 RabbitMQ publisher confirm，并通过 outbox 定时任务重发未被 broker 确认的支付事件；`notify-service` 的定时任务只重试已经被消息触发过但回调失败的通知。演示商户回调地址 `merchant.example.local` 在本地环境会模拟成功，方便完整演示通知闭环。

## 开发命令

后端构建：

```bash
mvn -B -ntp verify
```

前端开发：

```bash
cd frontend
npm install
npm run dev
```

前端 CI 构建：

```bash
cd frontend
npm ci
npm run build
```

Compose 配置校验：

```bash
docker compose config --quiet
```

## CI

GitHub Actions workflow 位于 `.github/workflows/ci.yml`，会在推送或 PR 到 `main`、`develop` 时自动运行，也支持手动触发。

当前 CI 包含三个检查：

- 后端：JDK 21 + Maven，执行 `mvn -B -ntp verify`
- 前端：Node.js 24 + npm，执行 `npm ci`、`npm test` 和 `npm run build`
- Compose：执行 `docker compose config --quiet`
