import { describe, expect, it } from 'vitest'

import { hasRoleAccess, homePathForRole } from './access'

describe('role access', () => {
  it('keeps merchant and admin areas separate', () => {
    expect(hasRoleAccess('MERCHANT', ['MERCHANT'])).toBe(true)
    expect(hasRoleAccess('MERCHANT', ['ADMIN'])).toBe(false)
    expect(hasRoleAccess('ADMIN', ['ADMIN'])).toBe(true)
    expect(hasRoleAccess('ADMIN', ['MERCHANT'])).toBe(false)
  })

  it('returns the role-specific home page', () => {
    expect(homePathForRole('MERCHANT')).toBe('/merchant/orders')
    expect(homePathForRole('ADMIN')).toBe('/admin')
  })
})
