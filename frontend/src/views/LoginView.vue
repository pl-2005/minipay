<template>
  <section class="login-shell">
    <el-form class="login-panel" label-position="top" @submit.prevent="submit">
      <h1>MiniPay 登录</h1>
      <el-form-item label="账号">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
      </el-form-item>
      <el-form-item label="演示身份">
        <el-segmented v-model="demoRole" :options="demoOptions" @change="fillDemoAccount" />
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading" class="full-control">登录</el-button>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getErrorMessage } from '../api/minipay'
import { homePathForRole } from '../auth/access'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const demoRole = ref('MERCHANT')

const demoOptions = [
  { label: '商户', value: 'MERCHANT' },
  { label: '运营', value: 'ADMIN' }
]

const form = reactive({
  username: 'merchant-demo',
  password: 'password'
})

function fillDemoAccount() {
  form.username = demoRole.value === 'ADMIN' ? 'admin-demo' : 'merchant-demo'
  form.password = 'password'
}

async function submit() {
  loading.value = true
  try {
    const session = await authStore.login({ ...form })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : homePathForRole(session.role)
    await router.replace(redirect)
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}
</script>
