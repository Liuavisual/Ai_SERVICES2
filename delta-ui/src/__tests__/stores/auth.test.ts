import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initial state should be empty', () => {
    const store = useAuthStore()
    expect(store.token).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(store.isAdmin).toBe(false)
  })

  it('setAuthData should update state', () => {
    const store = useAuthStore()
    store.setAuthData({
      token: 'test-token',
      refreshToken: 'test-refresh',
      expiresIn: 900,
      id: '1',
      username: 'admin',
      realName: '管理员',
      role: 'SYS_ADMIN',
    })
    expect(store.token).toBe('test-token')
    expect(store.isLoggedIn).toBe(true)
    expect(store.isAdmin).toBe(true)
    expect(store.isLeader).toBe(false)
    expect(store.isStaff).toBe(false)
  })

  it('clearAuthData should reset state', () => {
    const store = useAuthStore()
    store.setAuthData({
      token: 'test-token',
      refreshToken: 'test-refresh',
      expiresIn: 900,
      id: '1',
      username: 'admin',
      realName: '管理员',
      role: 'SYS_ADMIN',
    })
    store.clearAuthData()
    expect(store.token).toBe('')
    expect(store.isLoggedIn).toBe(false)
  })

  it('isTokenExpired should return true when expired', () => {
    const store = useAuthStore()
    store.tokenExpiry = '0'
    expect(store.isTokenExpired).toBe(true)
  })

  it('isTokenExpired should return false when not expired', () => {
    const store = useAuthStore()
    store.tokenExpiry = String(Date.now() + 100000)
    store.token = 'test-token'
    expect(store.isTokenExpired).toBe(false)
  })

  it('role getters should work correctly for CS_LEADER', () => {
    const store = useAuthStore()
    store.setAuthData({
      token: 'test',
      refreshToken: 'test',
      expiresIn: 900,
      id: '2',
      username: 'leader',
      realName: '组长',
      role: 'CS_LEADER',
    })
    expect(store.isAdmin).toBe(false)
    expect(store.isLeader).toBe(true)
    expect(store.isStaff).toBe(false)
  })

  it('role getters should work correctly for CS_STAFF', () => {
    const store = useAuthStore()
    store.setAuthData({
      token: 'test',
      refreshToken: 'test',
      expiresIn: 900,
      id: '3',
      username: 'staff',
      realName: '客服',
      role: 'CS_STAFF',
    })
    expect(store.isAdmin).toBe(false)
    expect(store.isLeader).toBe(false)
    expect(store.isStaff).toBe(true)
  })
})
