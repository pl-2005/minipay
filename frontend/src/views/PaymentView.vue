<template>
  <section class="page-stack">
    <div class="page-title">
      <h1>支付页</h1>
      <el-button :icon="Refresh" :loading="loading" @click="loadOrder">刷新</el-button>
    </div>

    <el-skeleton v-if="loading && !order" :rows="6" animated />

    <template v-else-if="order">
      <el-descriptions border :column="1">
        <el-descriptions-item label="平台订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商户订单号">{{ order.merchantOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ order.subject }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ Number(order.amount).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="order.status === 'PAID' ? 'success' : 'warning'">{{ order.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(order.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(order.paidAt) }}</el-descriptions-item>
      </el-descriptions>

      <el-button
        type="primary"
        class="action"
        :icon="CreditCard"
        :loading="paying"
        :disabled="order.status !== 'PENDING'"
        @click="pay"
      >
        模拟支付
      </el-button>
    </template>

    <el-empty v-else description="订单不存在" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CreditCard, Refresh } from '@element-plus/icons-vue'

import { confirmPayment, getErrorMessage, getPayOrder, type OrderItem } from '../api/minipay'

const route = useRoute()
const orderNo = computed(() => String(route.params.orderNo))
const order = ref<OrderItem | null>(null)
const loading = ref(false)
const paying = ref(false)

async function loadOrder() {
  loading.value = true
  try {
    order.value = await getPayOrder(orderNo.value)
  } catch (error) {
    order.value = null
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

async function pay() {
  if (!order.value) return

  paying.value = true
  try {
    const result = await confirmPayment(order.value)
    ElMessage.success(`支付成功：${result.paymentNo ?? order.value.orderNo}`)
    await loadOrder()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    paying.value = false
  }
}

function formatTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

onMounted(loadOrder)
</script>
