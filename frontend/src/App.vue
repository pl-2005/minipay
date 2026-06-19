<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <strong>MiniPay</strong>
      <nav>
        <RouterLink to="/">控制台</RouterLink>
        <RouterLink to="/merchant/orders">商户订单</RouterLink>
        <RouterLink to="/admin">运营后台</RouterLink>
      </nav>
    </el-header>
    <el-main>
      <RouterView />
    </el-main>
  </el-container>
</template>

