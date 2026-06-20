export type UserRole = 'MERCHANT' | 'ADMIN'

export interface AuthSession {
  token: string
  username: string
  role: UserRole
  expiresAt: number
}

const STORAGE_KEY = 'minipay.auth'

export function loadSession(): AuthSession | null {
  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) return null

  try {
    const session = JSON.parse(raw) as AuthSession
    if (!session.token || !session.username || !session.role || isExpired(session)) {
      clearSession()
      return null
    }
    return session
  } catch {
    clearSession()
    return null
  }
}

export function saveSession(session: AuthSession) {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearSession() {
  window.localStorage.removeItem(STORAGE_KEY)
}

export function isExpired(session: AuthSession) {
  return session.expiresAt * 1000 <= Date.now()
}
