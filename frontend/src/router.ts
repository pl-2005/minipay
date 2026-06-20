import { createRouter, createWebHistory } from 'vue-router'

import { hasRoleAccess, homePathForRole } from './auth/access'
import { clearSession, loadSession, type UserRole } from './auth/session'
import MerchantOrdersView from './views/MerchantOrdersView.vue'
import PaymentView from './views/PaymentView.vue'
import AdminView from './views/AdminView.vue'
import LoginView from './views/LoginView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    {
      path: '/',
      redirect: () => {
        const session = loadSession()
        return session ? homePathForRole(session.role) : '/login'
      }
    },
    { path: '/merchant/orders', component: MerchantOrdersView, meta: { roles: ['MERCHANT'] } },
    { path: '/pay/:orderNo', component: PaymentView, meta: { roles: ['MERCHANT'] } },
    { path: '/admin', component: AdminView, meta: { roles: ['ADMIN'] } }
  ]
})

router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }

  const session = loadSession()
  if (!session) {
    clearSession()
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const roles = to.meta.roles as UserRole[] | undefined
  if (roles && !hasRoleAccess(session.role, roles)) {
    return homePathForRole(session.role)
  }

  return true
})
