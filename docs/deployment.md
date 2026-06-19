# MiniPay 部署说明

## 环境要求

- Docker 26+
- Docker Compose v2
- JDK 21
- Maven 3.9+
- Node.js 24 LTS

## 启动

```bash
cp .env.example .env
docker compose up -d --build
```

## 验证

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

## 访问入口

- 前端：http://localhost
- Gateway：http://localhost:8080
- RabbitMQ：http://localhost:15672
- Prometheus：http://localhost:9090
- Grafana：http://localhost:3000
- SkyWalking：http://localhost:8088

