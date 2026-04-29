/**
 * 认证状态管理 Store（TypeScript版）
 *
 * 管理用户登录状态、Token信息、用户角色等认证相关数据。
 * 提供登录、登出、刷新Token等操作，并自动持久化到localStorage。
 *
 * @author 刘建国
 */
import { defineStore } from 'pinia'
import { authApi } from '@/api'
import type { LoginDTO, LoginVO, UserRole } from '@/types'

/** 认证状态接口 */
interface AuthState {
  /** 访问令牌 */
  token: string
  /** 刷新令牌 */
  refreshToken: string
  /** 过期时间（秒） */
  expiresIn: string
  /** 令牌过期时间戳（毫秒） */
  tokenExpiry: string
  /** 用户信息对象 */
  userInfo: Partial<LoginVO>
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    expiresIn: localStorage.getItem('expiresIn') || '',
    tokenExpiry: localStorage.getItem('tokenExpiry') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    /**
     * 是否已登录（Token存在且未过期）
     * @param {AuthState} state - 状态对象
     * @returns {boolean} 登录状态
     */
    isLoggedIn: (state): boolean => !!(state.token && !state.isTokenExpired),

    /**
     * Token是否已过期
     * @param {AuthState} state - 状态对象
     * @returns {boolean} 过期状态
     */
    isTokenExpired: (state): boolean => {
      if (!state.tokenExpiry) return true
      return Date.now() >= parseInt(state.tokenExpiry)
    },

    /**
     * 当前用户角色
     * @param {AuthState} state - 状态对象
     * @returns {UserRole} 角色标识
     */
    role: (state): UserRole => (state.userInfo?.role as UserRole) || ('' as UserRole),

    /**
     * 是否为系统管理员
     * @param {AuthState} state - 状态对象
     * @returns {boolean} 管理员标识
     */
    isAdmin: (state): boolean => state.userInfo?.role === 'SYS_ADMIN',

    /**
     * 是否为客服主管
     * @param {AuthState} state - 状态对象
     * @returns {boolean} 主管标识
     */
    isLeader: (state): boolean => state.userInfo?.role === 'CS_LEADER',

    /**
     * 是否为客服人员
     * @param {AuthState} state - 状态对象
     * @returns {boolean} 客服标识
     */
    isStaff: (state): boolean => state.userInfo?.role === 'CS_STAFF',

    /**
     * 当前用户真实姓名
     * @param {AuthState} state - 状态对象
     * @returns {string} 真实姓名
     */
    realName: (state): string => state.userInfo?.realName || '',

    /**
     * 当前用户ID
     * @param {AuthState} state - 状态对象
     * @returns {string|null} 用户ID
     */
    userId: (state): string | null => state.userInfo?.id || null
  },

  actions: {
    /**
     * 设置认证数据并持久化到localStorage
     * @param {LoginVO} data - 认证数据（含token、refreshToken、expiresIn等）
     */
    setAuthData(data: LoginVO) {
      this.token = data.token || ''
      this.refreshToken = data.refreshToken || ''
      this.expiresIn = String(data.expiresIn || '')
      this.tokenExpiry = String(Date.now() + (data.expiresIn || 900) * 1000)
      this.userInfo = data
      localStorage.setItem('token', this.token)
      localStorage.setItem('refreshToken', this.refreshToken)
      localStorage.setItem('expiresIn', this.expiresIn)
      localStorage.setItem('tokenExpiry', this.tokenExpiry)
      localStorage.setItem('userInfo', JSON.stringify(data))
    },

    /**
     * 清除认证数据（登出时调用）
     */
    clearAuthData() {
      this.token = ''
      this.refreshToken = ''
      this.expiresIn = ''
      this.tokenExpiry = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('expiresIn')
      localStorage.removeItem('tokenExpiry')
      localStorage.removeItem('userInfo')
    },

    /**
     * 用户登录
     * @param {LoginDTO} loginDTO - 登录参数（username、password等）
     * @returns {Promise<Result<LoginVO>>} 登录响应
     */
    async login(loginDTO: LoginDTO) {
      const res = await authApi.login(loginDTO)
      if (res.code === 200 && res.data) {
        this.setAuthData(res.data)
      }
      return res
    },

    /**
     * 用户登出
     */
    async logout() {
      try { await authApi.logout() } catch (_) { /* 忽略登出接口异常 */ }
      this.clearAuthData()
    },

    /**
     * 刷新Token
     * @returns {Promise<LoginVO|null>} 刷新成功返回新认证数据，失败返回null
     */
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
