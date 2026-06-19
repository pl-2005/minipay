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
| 查询通知记录 | GET | `/api/admin/notifications` | admin-service |

