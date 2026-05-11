import { defineStore } from 'pinia'
import { authApi } from '@/api'
import { authStorage } from '@/utils/storage'
import type { LoginDTO, LoginVO, UserRole } from '@/types'
import { usePermissionStore } from './permission'

interface AuthState {
  token: string
  refreshToken: string
  expiresIn: string
  tokenExpiry: string
  userInfo: Partial<LoginVO>
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: authStorage.getToken(),
    refreshToken: authStorage.getRefreshToken(),
    expiresIn: authStorage.getExpiresIn(),
    tokenExpiry: authStorage.getTokenExpiry(),
    userInfo: authStorage.getUserInfo() as Partial<LoginVO>,
  }),

  getters: {
    isLoggedIn(state): boolean {
      if (!state.token) return false
      if (!state.tokenExpiry) return false
      return Date.now() < parseInt(state.tokenExpiry)
    },

    isTokenExpired: (state): boolean => {
      if (!state.tokenExpiry) return true
      return Date.now() >= parseInt(state.tokenExpiry)
    },

    role: (state): UserRole => (state.userInfo?.role as UserRole) || ('' as UserRole),

    isAdmin: (state): boolean => state.userInfo?.role === 'SYS_ADMIN',
    isCompanion: (state): boolean => state.userInfo?.role === 'COMPANION',

    isLeader: (state): boolean => state.userInfo?.role === 'CS_LEADER',

    isStaff: (state): boolean => state.userInfo?.role === 'CS_STAFF',

    realName: (state): string => state.userInfo?.realName || '',

    userId: (state): string | null => state.userInfo?.id || null
  },

  actions: {
    setAuthData(data: LoginVO) {
      this.token = data.token || ''
      this.refreshToken = data.refreshToken || ''
      this.expiresIn = String(data.expiresIn || '')
      this.tokenExpiry = String(Date.now() + (data.expiresIn || 900) * 1000)
      this.userInfo = data
      authStorage.setAuth(data as unknown as Record<string, unknown>)
      usePermissionStore().initPermissions(data.permissions || [])
    },

    clearAuthData() {
      this.token = ''
      this.refreshToken = ''
      this.expiresIn = ''
      this.tokenExpiry = ''
      this.userInfo = {}
      authStorage.clearAuth()
      usePermissionStore().clearPermissions()
    },

    async login(loginDTO: LoginDTO) {
      const res = await authApi.login(loginDTO as unknown as Record<string, unknown>)
      if (res.code === 200 && res.data) {
        this.setAuthData(res.data)
      }
      return res
    },

    async logout() {
      try { await authApi.logout() } catch (_) { /* 忽略登出接口异常 */ }
      this.clearAuthData()
    },

    async refresh() {
      if (!this.refreshToken) return null
      const res = await authApi.refresh({ refreshToken: this.refreshToken })
      if (res.code === 200 && res.data) {
        this.setAuthData(res.data)
        return res.data
      }
      return null
    }
  }
})
