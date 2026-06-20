<template>
  <section class="page-stack">
    <div class="page-title">
      <h1>运营后台</h1>
      <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="订单" name="orders">
        <el-table :data="orders" v-loading="loading" border>
          <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
          <el-table-column prop="merchantNo" label="商户号" width="120" />
          <el-table-column prop="merchantOrderNo" label="商户订单号" min-width="160" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="流水" name="payments">
        <el-table :data="payments" v-loading="loading" border>
          <el-table-column prop="paymentNo" label="流水号" min-width="210" />
          <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag type="success">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="idempotencyKey" label="幂等键" min-width="160" />
          <el-table-column prop="paidAt" label="支付时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.paidAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="事件" name="events">
        <el-table :data="events" v-loading="loading" border>
          <el-table-column prop="eventId" label="事件号" min-width="210" />
          <el-table-column prop="eventType" label="类型" min-width="170" />
          <el-table-column prop="aggregateNo" label="订单号" min-width="210" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="retryCount" label="重试" width="80" />
          <el-table-column prop="payload" label="载荷" min-width="260" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="通知" name="notifications">
        <el-table :data="notifications" v-loading="loading" border>
          <el-table-column prop="notifyNo" label="通知号" min-width="210" />
          <el-table-column prop="merchantNo" label="商户号" width="120" />
          <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
          <el-table-column prop="callbackUrl" label="回调地址" min-width="260" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="retryCount" label="重试" width="80" />
          <el-table-column prop="nextRetryAt" label="下次通知" min-width="180">
            <template #default="{ row }">{{ formatTime(row.nextRetryAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

import {
  getErrorMessage,
  listAdminEvents,
  listAdminNotifications,
  listAdminOrders,
  listAdminPayments,
  type AdminEvent,
  type AdminNotification,
  type AdminPayment,
  type OrderItem
} from '../api/minipay'

const activeTab = ref('orders')
const loading = ref(false)
const orders = ref<OrderItem[]>([])
const payments = ref<AdminPayment[]>([])
const events = ref<AdminEvent[]>([])
const notifications = ref<AdminNotification[]>([])

async function loadAll() {
  loading.value = true
  try {
    const [orderData, paymentData, eventData, notificationData] = await Promise.all([
      listAdminOrders(),
      listAdminPayments(),
      listAdminEvents(),
      listAdminNotifications()
    ])
    orders.value = orderData
    payments.value = paymentData
    events.value = eventData
    notifications.value = notificationData
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

function statusType(status: string) {
  if (status === 'PAID') return 'success'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

function formatTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

onMounted(loadAll)
</script>
