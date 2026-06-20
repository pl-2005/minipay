# MiniPay API 说明

统一 API 前缀为 `/api`，所有请求先进入 `gateway-service`。

## 统一响应

```json
{
  "code": "SUCCESS",
  "message": "ok",
  "data": {}
}
```

## 鉴权

除 `/api/auth/login` 和健康检查接口外，业务接口都需要携带 token：

```http
Authorization: Bearer <token>
```

角色权限：

| 角色 | 可访问范围 |
| --- | --- |
| `MERCHANT` | 商户订单、订单查询、模拟支付 |
| `ADMIN` | 运营后台的订单、流水、事件和通知查询 |

无 token 返回 `401 UNAUTHORIZED`，角色不匹配返回 `403 FORBIDDEN`。

## 核心接口规划

| 场景 | 方法 | 路径 | 服务 |
| --- | --- | --- | --- |
| 登录 | POST | `/api/auth/login` | auth-service |
| 创建订单 | POST | `/api/merchant/orders` | merchant-service / order-service |
| 查询支付页订单 | GET | `/api/pay/orders/{orderNo}` | order-service |
| 模拟支付 | POST | `/api/pay/orders/{orderNo}/confirm` | payment-service |
| 查询订单详情 | GET | `/api/orders/{orderNo}` | order-service |
| 查询商户订单 | GET | `/api/merchant/orders` | merchant-service |
| 查询运营订单 | GET | `/api/admin/orders` | admin-service |
| 查询支付流水 | GET | `/api/admin/payments` | admin-service |
| 查询支付事件 | GET | `/api/admin/events` | admin-service |
| 查询通知记录 | GET | `/api/admin/notifications` | admin-service |

列表查询参数：

| 接口 | 参数 |
| --- | --- |
| `/api/merchant/orders` | `merchantNo`、`keyword`、`status` |
| `/api/admin/orders` | `keyword`、`status` |
| `/api/admin/payments` | `keyword`、`status` |
| `/api/admin/events` | `keyword`、`eventType`、`status` |
| `/api/admin/notifications` | `keyword`、`status` |

`keyword` 为不区分大小写的模糊搜索，筛选在数据库查询中执行后再返回最近 100 条记录。

## 演示账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `merchant-demo` | `password` | 商户 |
| `admin-demo` | `password` | 运营 |

## 登录请求

```json
{
  "username": "merchant-demo",
  "password": "password"
}
```

登录成功返回：

```json
{
  "token": "<bearer-token>",
  "username": "merchant-demo",
  "role": "MERCHANT",
  "expiresAt": 1781880000
}
```

## 创建订单请求

```json
{
  "merchantNo": "M10001",
  "merchantOrderNo": "MO1781851711247",
  "subject": "测试商品",
  "amount": 99.9
}
```

## 模拟支付请求

```json
{
  "amount": 99.9,
  "idempotencyKey": "FRONTEND_CONFIRM"
}
```

## 支付通知链路

模拟支付成功后，`payment-service` 会在同一事务内写入：

- `payment_record`：支付流水，状态为 `SUCCESS`
- `payment_event`：支付成功事件，初始状态为 `PENDING`
- `notify_record`：商户通知记录，初始状态为 `PENDING`

事务提交后，`payment-service` 发布 `payment.success` 消息到 RabbitMQ，并同步等待 broker publisher confirm。只有 RabbitMQ 确认接收后，本次支付确认接口才返回成功；如果 broker 未确认，支付数据已落库但接口返回 `MESSAGE_PUBLISH_FAILED`，事件会标记为 `PUBLISH_FAILED`，由 payment-service 的 outbox 重发任务继续投递。

`notify-service` 只通过 RabbitMQ 消费支付成功消息来触发首次通知。它不会直接扫描 `PENDING` 通知绕过消息队列；定时任务只处理已进入 `RETRY` 或超时 `PROCESSING` 的通知。消费消息后，notify-service 读取事件和通知记录，向商户 `callbackUrl` 发送支付成功回调；演示商户 `merchant.example.local` 会被模拟为回调成功。

通知状态流转：

| 状态 | 含义 |
| --- | --- |
| `PENDING` | 已创建，等待通知 |
| `PROCESSING` | 正在执行回调 |
| `SUCCESS` | 商户回调成功 |
| `RETRY` | 回调失败，等待下次重试 |
| `FAILED` | 达到最大重试次数后失败 |

事件状态流转：

| 状态 | 含义 |
| --- | --- |
| `PENDING` | 已创建，等待通知服务处理 |
| `PUBLISHED` | 已被 RabbitMQ 确认接收，等待消费者处理 |
| `DONE` | 对应商户通知已成功 |
| `RETRY` | 对应商户通知失败，等待重试 |
| `PUBLISH_FAILED` | RabbitMQ 未确认接收，等待 payment-service 重发 |
| `FAILED` | 对应商户通知最终失败 |

如果 RabbitMQ 发布异常，`payment-service` 会定时扫描 `PENDING/PUBLISH_FAILED` 支付事件并重发到 RabbitMQ。如果商户回调失败，`notify-service` 会定时扫描到期的 `RETRY` 通知记录继续回调。
