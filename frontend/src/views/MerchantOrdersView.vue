<template>
  <section>
    <h1>商户订单</h1>
    <el-form label-width="110px" class="form-panel">
      <el-form-item label="商户号">
        <el-input v-model="form.merchantNo" />
      </el-form-item>
      <el-form-item label="商户订单号">
        <el-input v-model="form.merchantOrderNo" />
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input v-model="form.subject" />
      </el-form-item>
      <el-form-item label="金额">
        <el-input-number v-model="form.amount" :precision="2" :min="0.01" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">创建订单</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const form = reactive({
  merchantNo: 'M10001',
  merchantOrderNo: `MO${Date.now()}`,
  subject: '测试商品',
  amount: 99.9
})
</script>

