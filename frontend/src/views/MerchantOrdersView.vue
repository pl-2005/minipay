<template>
  <section class="page-stack">
    <div class="page-title">
      <h1>商户订单</h1>
      <el-button :icon="Refresh" :loading="loading" @click="loadOrders">刷新</el-button>
    </div>

    <el-form label-width="110px" class="form-panel">
      <el-row :gutter="16">
        <el-col :xs="24" :md="12">
          <el-form-item label="商户号">
            <el-select
              v-model="form.merchantNo"
              filterable
              placeholder="请选择商户"
              :loading="merchantLoading"
              class="full-control"
              @change="handleMerchantChange"
            >
              <el-option
                v-for="merchant in merchantOptions"
                :key="merchant.merchantNo"
                :label="`${merchant.merchantName}（${merchant.merchantNo}）`"
                :value="merchant.merchantNo"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-form-item label="商户订单号">
            <el-input v-model="form.merchantOrderNo" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-form-item label="商品描述">
            <el-input v-model="form.subject" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-form-item label="金额">
            <el-input-number v-model="form.amount" :precision="2" :min="0.01" class="full-control" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button
          type="primary"
          :icon="Plus"
          :loading="creating"
          :disabled="!form.merchantNo || merchantLoading"
          @click="submitOrder"
        >创建订单</el-button>
      </el-form-item>
    </el-form>

    <el-alert v-if="createdOrder" type="success" :closable="false" show-icon>
      <template #title>
        订单 {{ createdOrder.orderNo }} 已创建
        <RouterLink :to="`/pay/${createdOrder.orderNo}`" class="inline-link">进入支付</RouterLink>
      </template>
    </el-alert>

    <div class="section-title">
      <h2>订单记录</h2>
    </div>

    <ListFilterBar
      v-model:keyword="filters.keyword"
      v-model:status="filters.status"
      keyword-placeholder="平台订单号、商户订单号、商品"
      :status-options="orderStatusOptions"
      :total="orders.length"
      :loading="loading"
      @search="loadOrders"
      @reset="resetFilters"
    />

    <el-table :data="orders" v-loading="loading" border>
      <el-table-column prop="orderNo" label="平台订单号" min-width="210" />
      <el-table-column prop="merchantOrderNo" label="商户订单号" min-width="160" />
      <el-table-column prop="subject" label="商品" min-width="160" />
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
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <RouterLink :to="`/pay/${row.orderNo}`">支付页</RouterLink>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

import {
  createMerchantOrder,
  getErrorMessage,
  listMerchantOptions,
  listMerchantOrders,
  type MerchantOption,
  type OrderItem
} from '../api/minipay'
import ListFilterBar from '../components/ListFilterBar.vue'

const form = reactive({
  merchantNo: '',
  merchantOrderNo: `MO${Date.now()}`,
  subject: '测试商品',
  amount: 99.9
})

const orders = ref<OrderItem[]>([])
const createdOrder = ref<OrderItem | null>(null)
const loading = ref(false)
const creating = ref(false)
const merchantLoading = ref(false)
const merchantOptions = ref<MerchantOption[]>([])
const filters = reactive({
  keyword: '',
  status: ''
})
const orderStatusOptions = [
  { label: '待支付', value: 'PENDING' },
  { label: '已支付', value: 'PAID' }
]

async function loadOrders() {
  if (!form.merchantNo) {
    orders.value = []
    return
  }
  loading.value = true
  try {
    orders.value = await listMerchantOrders(form.merchantNo, { ...filters })
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

async function loadMerchants() {
  merchantLoading.value = true
  try {
    merchantOptions.value = await listMerchantOptions()
    if (!merchantOptions.value.some((merchant) => merchant.merchantNo === form.merchantNo)) {
      form.merchantNo = merchantOptions.value[0]?.merchantNo ?? ''
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    merchantLoading.value = false
  }
}

async function handleMerchantChange() {
  createdOrder.value = null
  await loadOrders()
}

async function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  await loadOrders()
}

async function submitOrder() {
  creating.value = true
  try {
    const order = await createMerchantOrder({ ...form })
    createdOrder.value = order
    form.merchantOrderNo = `MO${Date.now()}`
    await loadOrders()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    creating.value = false
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

onMounted(async () => {
  await loadMerchants()
  await loadOrders()
})
</script>
