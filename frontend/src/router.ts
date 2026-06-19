import { createRouter, createWebHistory } from 'vue-router'

import DashboardView from './views/DashboardView.vue'
import MerchantOrdersView from './views/MerchantOrdersView.vue'
import PaymentView from './views/PaymentView.vue'
import AdminView from './views/AdminView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: DashboardView },
    { path: '/merchant/orders', component: MerchantOrdersView },
    { path: '/pay/:orderNo', component: PaymentView },
    { path: '/admin', component: AdminView }
  ]
})

