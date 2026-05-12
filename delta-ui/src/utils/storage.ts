/** 认证相关的 localStorage 键名列表 */
const AUTH_KEYS = ['token', 'refreshToken', 'expiresIn', 'tokenExpiry', 'userInfo']

/** 认证数据结构 */
export interface AuthData {
  /** 访问令牌 */
  token?: string
  /** 刷新令牌 */
  refreshToken?: string
  /** 过期时间（秒） */
  expiresIn?: number
  /** 用户信息 */
  [key: string]: unknown
}

/** 用户信息结构 */
export interface UserInfo {
  /** 用户角色 */
  role?: string
  /** 用户ID */
  id?: string
  [key: string]: unknown
}

/**
 * 认证存储工具
 *
 * 封装 localStorage 操作，管理登录态 Token 和用户信息。
 * 提供 get/set/clear 等便捷方法，统一管理认证相关数据。
 *
 * @author 刘建国
 */
export const authStorage = {
  /** 获取访问令牌 */
  getToken: (): string | null => localStorage.getItem('token'),
  /** 获取刷新令牌 */
  getRefreshToken: (): string | null => localStorage.getItem('refreshToken'),
  /** 获取过期时间（秒） */
  getExpiresIn: (): string | null => localStorage.getItem('expiresIn'),
  /** 获取令牌过期时间戳 */
  getTokenExpiry: (): string | null => localStorage.getItem('tokenExpiry'),
  /**
   * 获取用户信息
   * @returns 用户信息对象，解析失败返回空对象
   */
  getUserInfo: (): UserInfo => {
    try {
      return JSON.parse(localStorage.getItem('userInfo') || '{}')
    } catch {
      localStorage.removeItem('userInfo')
      return {}
    }
  },
  /**
   * 设置认证信息
   * @param data - 认证数据对象
   */
  setAuth: (data: AuthData): void => {
    const expiry = data.expiresIn ? Date.now() + Number(data.expiresIn) * 1000 : ''
    localStorage.setItem('token', data.token || '')
    localStorage.setItem('refreshToken', data.refreshToken || '')
    localStorage.setItem('expiresIn', String(data.expiresIn))
    localStorage.setItem('tokenExpiry', String(expiry))
    localStorage.setItem('userInfo', JSON.stringify(data))
  },
  /** 清除所有认证信息 */
  clearAuth: (): void => {
    AUTH_KEYS.forEach(key => localStorage.removeItem(key))
  },
  /**
   * 检查是否存有任意 Token
   * @returns 存在则返回 true
   */
  hasAnyToken: (): boolean => {
    return !!(localStorage.getItem('token') || localStorage.getItem('refreshToken'))
  },
  /**
   * 检查令牌是否已过期
   * @returns 过期返回 true
   */
  isTokenExpired: (): boolean => {
    const tokenExpiry = localStorage.getItem('tokenExpiry')
    if (!tokenExpiry) return true
    return Date.now() >= parseInt(tokenExpiry)
  },
  /**
   * 根据角色获取首页路径
   * @param role - 用户角色标识
   * @returns 首页路由路径
   */
  getRoleHomePage: (role: string): string => {
    const map: Record<string, string> = {
      SYS_ADMIN: '/dashboard',
      CS_LEADER: '/pending-messages',
      CS_STAFF: '/pending-messages',
      COMPANION: '/companion-orders'
    }
    return map[role] || '/dashboard'
  }
}
