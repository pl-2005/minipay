<template>
  <section>
    <h1>MiniPay 控制台</h1>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>
          <template #header>主链路</template>
          <p>创建订单、模拟支付、状态更新、结果查询。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>工程能力</template>
          <p>Redis、RabbitMQ、CI、监控、链路追踪。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>演示入口</template>
          <RouterLink to="/merchant/orders">进入商户订单</RouterLink>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

