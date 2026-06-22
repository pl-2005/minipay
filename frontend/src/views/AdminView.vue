<template>
  <section class="page-stack">
    <div class="page-title">
      <h1>运营后台</h1>
      <el-button :icon="Refresh" :loading="loadingTab !== null" @click="loadActiveTab">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="订单" name="orders">
        <ListFilterBar
          v-model:keyword="orderFilters.keyword"
          v-model:status="orderFilters.status"
          keyword-placeholder="订单号、商户号、商品"
          :status-options="orderStatusOptions"
          :total="orders.length"
          :loading="tabLoading('orders')"
          @search="loadOrders"
          @reset="resetOrderFilters"
        />
        <el-table :data="orders" v-loading="tabLoading('orders')" empty-text="无匹配结果" border>
          <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
          <el-table-column prop="merchantNo" label="商户号" width="120" />
          <el-table-column prop="merchantOrderNo" label="商户订单号" min-width="160" />
          <el-table-column prop="subject" label="商品" min-width="150" />
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
        <ListFilterBar
          v-model:keyword="paymentFilters.keyword"
          v-model:status="paymentFilters.status"
          keyword-placeholder="流水号、订单号、商户号、幂等键"
          :status-options="paymentStatusOptions"
          :total="payments.length"
          :loading="tabLoading('payments')"
          @search="loadPayments"
          @reset="resetPaymentFilters"
        />
        <el-table :data="payments" v-loading="tabLoading('payments')" empty-text="无匹配结果" border>
          <el-table-column prop="paymentNo" label="流水号" min-width="210" />
          <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
          <el-table-column prop="merchantNo" label="商户号" width="120" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="idempotencyKey" label="幂等键" min-width="160" />
          <el-table-column prop="paidAt" label="支付时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.paidAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="事件" name="events">
        <ListFilterBar
          v-model:keyword="eventFilters.keyword"
          v-model:status="eventFilters.status"
          v-model:extra-value="eventFilters.eventType"
          keyword-placeholder="事件号、订单号、载荷"
          extra-label="事件类型"
          :extra-options="eventTypeOptions"
          :status-options="eventStatusOptions"
          :total="events.length"
          :loading="tabLoading('events')"
          @search="loadEvents"
          @reset="resetEventFilters"
        />
        <el-table :data="events" v-loading="tabLoading('events')" empty-text="无匹配结果" border>
          <el-table-column prop="eventId" label="事件号" min-width="210" />
          <el-table-column prop="eventType" label="类型" min-width="170" />
          <el-table-column prop="aggregateNo" label="订单号" min-width="210" />
          <el-table-column prop="status" label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="retryCount" label="重试" width="80" />
          <el-table-column prop="payload" label="载荷" min-width="260" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="通知" name="notifications">
        <ListFilterBar
          v-model:keyword="notificationFilters.keyword"
          v-model:status="notificationFilters.status"
          keyword-placeholder="通知号、订单号、商户号、回调或响应"
          :status-options="notificationStatusOptions"
          :total="notifications.length"
          :loading="tabLoading('notifications')"
          @search="loadNotifications"
          @reset="resetNotificationFilters"
        />
        <el-table
          :data="notifications"
          v-loading="tabLoading('notifications')"
          empty-text="无匹配结果"
          border
        >
          <el-table-column prop="notifyNo" label="通知号" min-width="210" />
          <el-table-column prop="merchantNo" label="商户号" width="120" />
          <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
          <el-table-column prop="callbackUrl" label="回调地址" min-width="260" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="retryCount" label="重试" width="80" />
          <el-table-column prop="nextRetryAt" label="下次通知" min-width="180">
            <template #default="{ row }">{{ formatTime(row.nextRetryAt) }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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
import ListFilterBar from '../components/ListFilterBar.vue'

type TabName = 'orders' | 'payments' | 'events' | 'notifications'

const activeTab = ref<TabName>('orders')
const loadingTab = ref<TabName | 'all' | null>(null)
const orders = ref<OrderItem[]>([])
const payments = ref<AdminPayment[]>([])
const events = ref<AdminEvent[]>([])
const notifications = ref<AdminNotification[]>([])

const orderFilters = reactive({ keyword: '', status: '' })
const paymentFilters = reactive({ keyword: '', status: '' })
const eventFilters = reactive({ keyword: '', eventType: '', status: '' })
const notificationFilters = reactive({ keyword: '', status: '' })

const orderStatusOptions = [
  { label: '待支付', value: 'PENDING' },
  { label: '已支付', value: 'PAID' }
]
const paymentStatusOptions = [{ label: '成功', value: 'SUCCESS' }]
const eventTypeOptions = [{ label: '支付成功', value: 'PAYMENT_SUCCESS' }]
const eventStatusOptions = [
  { label: '待发布', value: 'PENDING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已完成', value: 'DONE' },
  { label: '等待重试', value: 'RETRY' },
  { label: '发布失败', value: 'PUBLISH_FAILED' },
  { label: '处理失败', value: 'FAILED' }
]
const notificationStatusOptions = [
  { label: '等待通知', value: 'PENDING' },
  { label: '通知中', value: 'PROCESSING' },
  { label: '成功', value: 'SUCCESS' },
  { label: '等待重试', value: 'RETRY' },
  { label: '失败', value: 'FAILED' }
]

function tabLoading(tab: TabName) {
  return loadingTab.value === 'all' || loadingTab.value === tab
}

async function loadAll() {
  loadingTab.value = 'all'
  try {
    const [orderData, paymentData, eventData, notificationData] = await Promise.all([
      listAdminOrders({ ...orderFilters }),
      listAdminPayments({ ...paymentFilters }),
      listAdminEvents({ ...eventFilters }),
      listAdminNotifications({ ...notificationFilters })
    ])
    orders.value = orderData
    payments.value = paymentData
    events.value = eventData
    notifications.value = notificationData
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loadingTab.value = null
  }
}

async function loadOrders() {
  await loadTab('orders', async () => {
    orders.value = await listAdminOrders({ ...orderFilters })
  })
}

async function loadPayments() {
  await loadTab('payments', async () => {
    payments.value = await listAdminPayments({ ...paymentFilters })
  })
}

async function loadEvents() {
  await loadTab('events', async () => {
    events.value = await listAdminEvents({ ...eventFilters })
  })
}

async function loadNotifications() {
  await loadTab('notifications', async () => {
    notifications.value = await listAdminNotifications({ ...notificationFilters })
  })
}

async function loadTab(tab: TabName, request: () => Promise<void>) {
  loadingTab.value = tab
  try {
    await request()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loadingTab.value = null
  }
}

async function loadActiveTab() {
  if (activeTab.value === 'orders') return loadOrders()
  if (activeTab.value === 'payments') return loadPayments()
  if (activeTab.value === 'events') return loadEvents()
  return loadNotifications()
}

async function resetOrderFilters() {
  Object.assign(orderFilters, { keyword: '', status: '' })
  await loadOrders()
}

async function resetPaymentFilters() {
  Object.assign(paymentFilters, { keyword: '', status: '' })
  await loadPayments()
}

async function resetEventFilters() {
  Object.assign(eventFilters, { keyword: '', eventType: '', status: '' })
  await loadEvents()
}

async function resetNotificationFilters() {
  Object.assign(notificationFilters, { keyword: '', status: '' })
  await loadNotifications()
}

function statusType(status: string) {
  if (status === 'PAID' || status === 'SUCCESS' || status === 'DONE') return 'success'
  if (status === 'PENDING' || status === 'PUBLISHED' || status === 'PROCESSING') return 'warning'
  if (status === 'FAILED' || status === 'PUBLISH_FAILED') return 'danger'
  return 'info'
}

function formatTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

onMounted(loadAll)
</script>
