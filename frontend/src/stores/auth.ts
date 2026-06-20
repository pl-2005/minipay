import { defineStore } from 'pinia'

import { login, type LoginPayload } from '../api/minipay'
import { clearSession, loadSession, saveSession, type AuthSession, type UserRole } from '../auth/session'

interface AuthState {
  session: AuthSession | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    session: loadSession()
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.session),
    role: (state): UserRole | null => state.session?.role ?? null,
    username: (state): string => state.session?.username ?? ''
  },
  actions: {
    async login(payload: LoginPayload) {
      const session = await login(payload)
      this.session = session
      saveSession(session)
      return session
    },
    logout() {
      this.session = null
      clearSession()
    },
    refresh() {
      this.session = loadSession()
    }
  }
})
