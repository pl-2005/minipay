# MiniPay

MiniPay 是一个教学支付平台期末工程项目，目标是实现可运行、可演示、可复核的支付主链路：

```text
商户创建订单 -> 用户发起模拟支付 -> 支付状态更新 -> 结果查询与展示
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
- API Gateway：http://localhost:8080
- RabbitMQ 管理台：http://localhost:15672
- Prometheus：http://localhost:9090
- Grafana：http://localhost:3000
- SkyWalking UI：http://localhost:8088

## 开发命令

后端构建：

```bash
mvn clean package
```

前端开发：

```bash
cd frontend
npm install
npm run dev
```

