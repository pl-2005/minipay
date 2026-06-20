import type { UserRole } from './session'

export function hasRoleAccess(role: UserRole | null, allowedRoles: readonly UserRole[]) {
  return role !== null && allowedRoles.includes(role)
}

export function homePathForRole(role: UserRole) {
  return role === 'ADMIN' ? '/admin' : '/merchant/orders'
}
