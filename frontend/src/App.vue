<template>
  <el-container class="app-shell">
    <el-header v-if="!isPublicPage" class="app-header">
      <strong>MiniPay</strong>
      <nav>
        <RouterLink v-if="canUseMerchant" to="/merchant/orders">商户订单</RouterLink>
        <RouterLink v-if="canUseAdmin" to="/admin">运营后台</RouterLink>
      </nav>
      <div class="user-menu">
        <span>{{ authStore.username }} · {{ authStore.role }}</span>
        <el-button text @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <RouterView />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { hasRoleAccess } from './auth/access'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isPublicPage = computed(() => Boolean(route.meta.public))
const canUseAdmin = computed(() => hasRoleAccess(authStore.role, ['ADMIN']))
const canUseMerchant = computed(() => hasRoleAccess(authStore.role, ['MERCHANT']))

async function logout() {
  authStore.logout()
  await router.replace('/login')
}
</script>
