<template>
  <section class="login-shell">
    <el-form class="login-panel register-panel" label-position="top" @submit.prevent="submit">
      <h1>商户注册</h1>
      <el-form-item label="登录账号">
        <el-input v-model="form.username" autocomplete="username" maxlength="64" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="form.password"
          type="password"
          autocomplete="new-password"
          maxlength="72"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          autocomplete="new-password"
          maxlength="72"
          show-password
        />
      </el-form-item>
      <el-form-item label="商户名称">
        <el-input v-model="form.merchantName" maxlength="128" />
      </el-form-item>
      <el-form-item label="支付回调地址（可选）">
        <el-input v-model="form.callbackUrl" placeholder="https://example.com/pay/callback" maxlength="255" />
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="loading" class="full-control">
        注册并登录
      </el-button>
      <div class="auth-switch">
        <RouterLink to="/login">已有账号，返回登录</RouterLink>
      </div>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getErrorMessage } from '../api/minipay'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  merchantName: '',
  callbackUrl: ''
})

function validateForm() {
  if (!/^[A-Za-z0-9_-]{4,64}$/.test(form.username)) {
    return '账号需为 4-64 位字母、数字、下划线或短横线'
  }
  if (form.password.length < 8 || new TextEncoder().encode(form.password).length > 72) {
    return '密码长度至少 8 位且不能超过 72 字节'
  }
  if (form.password !== form.confirmPassword) {
    return '两次输入的密码不一致'
  }
  if (form.merchantName.trim().length < 2) {
    return '商户名称至少需要 2 个字符'
  }
  if (form.callbackUrl && !isHttpUrl(form.callbackUrl)) {
    return '支付回调地址必须是有效的 HTTP(S) 地址'
  }
  return ''
}

function isHttpUrl(value: string) {
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

async function submit() {
  const validationMessage = validateForm()
  if (validationMessage) {
    ElMessage.warning(validationMessage)
    return
  }

  loading.value = true
  try {
    await authStore.registerMerchant({
      username: form.username.trim(),
      password: form.password,
      merchantName: form.merchantName.trim(),
      callbackUrl: form.callbackUrl.trim() || undefined
    })
    ElMessage.success('商户注册成功')
    await router.replace('/merchant/orders')
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}
</script>
