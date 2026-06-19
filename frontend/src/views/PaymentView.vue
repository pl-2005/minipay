<template>
  <section>
    <h1>支付页</h1>
    <el-descriptions border :column="1">
      <el-descriptions-item label="平台订单号">{{ orderNo }}</el-descriptions-item>
      <el-descriptions-item label="商品">测试商品</el-descriptions-item>
      <el-descriptions-item label="金额">99.90</el-descriptions-item>
      <el-descriptions-item label="状态">PENDING</el-descriptions-item>
    </el-descriptions>
    <el-button type="primary" class="action">模拟支付</el-button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderNo = computed(() => route.params.orderNo)
</script>

