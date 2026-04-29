/**
 * 认证状态管理 Store
 *
 * 管理用户登录状态、Token信息、用户角色等认证相关数据。
 * 提供登录、登出、刷新Token等操作，并自动持久化到localStorage。
 *
 * @author 刘建国
 */
import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    /** 访问令牌 */
    token: localStorage.getItem('token') || '',
    /** 刷新令牌 */
    refreshToken: localStorage.getItem('refreshToken') || '',
    /** 过期时间（秒） */
    expiresIn: localStorage.getItem('expiresIn') || '',
    /** 令牌过期时间戳（毫秒） */
    tokenExpiry: localStorage.getItem('tokenExpiry') || '',
    /** 用户信息对象 */
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    /**
     * 是否已登录（Token存在且未过期）
     * @param {Object} state - 状态对象
     * @returns {boolean} 登录状态
     */
    isLoggedIn: (state) => !!(state.token && !state.isTokenExpired),

    /**
     * Token是否已过期
     * @param {Object} state - 状态对象
     * @returns {boolean} 过期状态
     */
    isTokenExpired: (state) => {
      if (!state.tokenExpiry) return true
      return Date.now() >= parseInt(state.tokenExpiry)
    },

    /**
     * 当前用户角色
     * @param {Object} state - 状态对象
     * @returns {string} 角色标识
     */
    role: (state) => state.userInfo?.role || '',

    /**
     * 是否为系统管理员
     * @param {Object} state - 状态对象
     * @returns {boolean} 管理员标识
     */
    isAdmin: (state) => state.userInfo?.role === 'SYS_ADMIN',

    /**
     * 是否为客服主管
     * @param {Object} state - 状态对象
     * @returns {boolean} 主管标识
     */
    isLeader: (state) => state.userInfo?.role === 'CS_LEADER',

    /**
     * 是否为客服人员
     * @param {Object} state - 状态对象
     * @returns {boolean} 客服标识
     */
    isStaff: (state) => state.userInfo?.role === 'CS_STAFF',

    /**
     * 当前用户真实姓名
     * @param {Object} state - 状态对象
     * @returns {string} 真实姓名
     */
    realName: (state) => state.userInfo?.realName || '',

    /**
     * 当前用户ID
     * @param {Object} state - 状态对象
     * @returns {number|null} 用户ID
     */
    userId: (state) => state.userInfo?.id || null
  },

  actions: {
    /**
     * 设置认证数据并持久化到localStorage
     * @param {Object} data - 认证数据（含token、refreshToken、expiresIn等）
     */
    setAuthData(data) {
      this.token = data.token || ''
      this.refreshToken = data.refreshToken || ''
      this.expiresIn = data.expiresIn || ''
      this.tokenExpiry = Date.now() + (data.expiresIn || 900) * 1000
      this.userInfo = data
      localStorage.setItem('token', this.token)
      localStorage.setItem('refreshToken', this.refreshToken)
      localStorage.setItem('expiresIn', this.expiresIn)
      localStorage.setItem('tokenExpiry', String(this.tokenExpiry))
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
     * @param {Object} loginDTO - 登录参数（username、password等）
     * @returns {Promise<Object>} 登录响应
     */
    async login(loginDTO) {
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
      try { await authApi.logout() } catch (e) {}
      this.clearAuthData()
    },

    /**
     * 刷新Token
     * @returns {Promise<Object|null>} 刷新成功返回新认证数据，失败返回null
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
