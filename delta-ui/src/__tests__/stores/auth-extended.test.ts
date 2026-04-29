/**
 * Auth Store扩展测试
 *
 * 测试login、logout、refresh等action，以及localStorage持久化、
 * token过期判断的边界条件等场景。
 *
 * @author 刘建国
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

/** 使用vi.hoisted提升mock函数，确保vi.mock工厂函数可以访问 */
const { mockLogin, mockLogout, mockRefresh } = vi.hoisted(() => ({
  mockLogin: vi.fn(),
  mockLogout: vi.fn(),
  mockRefresh: vi.fn()
}))

/** Mock authApi模块 */
vi.mock('@/api', () => ({
  authApi: {
    login: mockLogin,
    logout: mockLogout,
    refresh: mockRefresh
  }
}))

import { useAuthStore } from '@/stores/auth'

describe('Auth Store扩展测试', () => {
  beforeEach(() => {
    /** 每次测试前重置Pinia实例和所有mock */
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  // ============ login action ============
  describe('login action', () => {
    it('登录成功应设置认证数据', async () => {
      const loginData = {
        code: 200,
        data: {
          token: 'new-access-token',
          refreshToken: 'new-refresh-token',
          expiresIn: 900,
          id: 'user-1',
          username: 'admin',
          realName: '管理员',
          role: 'SYS_ADMIN' as const
        }
      }
      mockLogin.mockResolvedValue(loginData)

      const store = useAuthStore()
      const result = await store.login({ username: 'admin', password: '123456' })

      expect(mockLogin).toHaveBeenCalledWith({ username: 'admin', password: '123456' })
      expect(store.token).toBe('new-access-token')
      expect(store.refreshToken).toBe('new-refresh-token')
      expect(store.isLoggedIn).toBe(true)
      expect(store.isAdmin).toBe(true)
      expect(result).toEqual(loginData)
    })

    it('登录成功应持久化到localStorage', async () => {
      const loginData = {
        code: 200,
        data: {
          token: 'persist-token',
          refreshToken: 'persist-refresh',
          expiresIn: 1800,
          id: 'user-2',
          username: 'leader',
          realName: '组长',
          role: 'CS_LEADER' as const
        }
      }
      mockLogin.mockResolvedValue(loginData)

      const store = useAuthStore()
      await store.login({ username: 'leader', password: '123456' })

      expect(localStorage.getItem('token')).toBe('persist-token')
      expect(localStorage.getItem('refreshToken')).toBe('persist-refresh')
      expect(localStorage.getItem('expiresIn')).toBe('1800')
      expect(localStorage.getItem('tokenExpiry')).toBeDefined()
      expect(JSON.parse(localStorage.getItem('userInfo') || '{}').username).toBe('leader')
    })

    it('登录失败（code非200）不应设置认证数据', async () => {
      const failData = {
        code: 401,
        message: '用户名或密码错误'
      }
      mockLogin.mockResolvedValue(failData)

      const store = useAuthStore()
      await store.login({ username: 'wrong', password: 'wrong' })

      expect(store.token).toBe('')
      expect(store.isLoggedIn).toBe(false)
    })

    it('登录失败（无data）不应设置认证数据', async () => {
      const failData = {
        code: 200,
        message: 'ok'
      }
      mockLogin.mockResolvedValue(failData)

      const store = useAuthStore()
      await store.login({ username: 'test', password: 'test' })

      expect(store.token).toBe('')
      expect(store.isLoggedIn).toBe(false)
    })

    it('登录接口异常应抛出错误', async () => {
      mockLogin.mockRejectedValue(new Error('网络异常'))

      const store = useAuthStore()
      await expect(store.login({ username: 'admin', password: '123' })).rejects.toThrow('网络异常')
      expect(store.token).toBe('')
    })
  })

  // ============ logout action ============
  describe('logout action', () => {
    it('登出应清除所有认证数据', async () => {
      mockLogout.mockResolvedValue({})

      const store = useAuthStore()
      /** 先设置认证数据 */
      store.setAuthData({
        token: 'active-token',
        refreshToken: 'active-refresh',
        expiresIn: 900,
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      })
      expect(store.isLoggedIn).toBe(true)

      await store.logout()

      expect(store.token).toBe('')
      expect(store.refreshToken).toBe('')
      expect(store.expiresIn).toBe('')
      expect(store.tokenExpiry).toBe('')
      expect(store.userInfo).toEqual({})
      expect(store.isLoggedIn).toBe(false)
    })

    it('登出应清除localStorage中的认证信息', async () => {
      mockLogout.mockResolvedValue({})

      const store = useAuthStore()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      })

      await store.logout()

      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
      expect(localStorage.getItem('expiresIn')).toBeNull()
      expect(localStorage.getItem('tokenExpiry')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
    })

    it('登出接口异常时仍应清除认证数据', async () => {
      mockLogout.mockRejectedValue(new Error('接口异常'))

      const store = useAuthStore()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      })

      await store.logout()

      /** 即使接口异常，本地认证数据也应被清除 */
      expect(store.token).toBe('')
      expect(store.isLoggedIn).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  // ============ refresh action ============
  describe('refresh action', () => {
    it('刷新成功应更新认证数据', async () => {
      const refreshData = {
        code: 200,
        data: {
          token: 'refreshed-token',
          refreshToken: 'refreshed-rt',
          expiresIn: 900,
          id: '1',
          username: 'admin',
          realName: '管理员',
          role: 'SYS_ADMIN'
        }
      }
      mockRefresh.mockResolvedValue(refreshData)

      const store = useAuthStore()
      /** 先设置初始refreshToken */
      store.refreshToken = 'old-rt'

      const result = await store.refresh()

      expect(mockRefresh).toHaveBeenCalledWith({ refreshToken: 'old-rt' })
      expect(store.token).toBe('refreshed-token')
      expect(store.refreshToken).toBe('refreshed-rt')
      expect(result).toEqual(refreshData.data)
    })

    it('无refreshToken时应返回null', async () => {
      const store = useAuthStore()
      store.refreshToken = ''

      const result = await store.refresh()

      expect(mockRefresh).not.toHaveBeenCalled()
      expect(result).toBeNull()
    })

    it('刷新失败（code非200）应返回null', async () => {
      mockRefresh.mockResolvedValue({ code: 401, message: 'Token已过期' })

      const store = useAuthStore()
      store.refreshToken = 'expired-rt'

      const result = await store.refresh()

      expect(result).toBeNull()
    })

    it('刷新失败（无data）应返回null', async () => {
      mockRefresh.mockResolvedValue({ code: 200, message: 'ok' })

      const store = useAuthStore()
      store.refreshToken = 'some-rt'

      const result = await store.refresh()

      expect(result).toBeNull()
    })

    it('刷新接口异常应抛出错误', async () => {
      mockRefresh.mockRejectedValue(new Error('网络异常'))

      const store = useAuthStore()
      store.refreshToken = 'some-rt'

      await expect(store.refresh()).rejects.toThrow('网络异常')
    })
  })

  // ============ localStorage持久化 ============
  describe('localStorage持久化', () => {
    it('setAuthData应将token写入localStorage', () => {
      const store = useAuthStore()
      store.setAuthData({
        token: 'ls-token',
        refreshToken: 'ls-rt',
        expiresIn: 600,
        id: '1',
        username: 'test',
        realName: '测试',
        role: 'CS_STAFF'
      })

      expect(localStorage.getItem('token')).toBe('ls-token')
      expect(localStorage.getItem('refreshToken')).toBe('ls-rt')
    })

    it('setAuthData应将userInfo以JSON格式写入localStorage', () => {
      const store = useAuthStore()
      const userData = {
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN' as const,
        phone: '13800138000'
      }
      store.setAuthData(userData)

      const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
      expect(stored.username).toBe('admin')
      expect(stored.phone).toBe('13800138000')
    })

    it('clearAuthData应从localStorage移除所有认证键', () => {
      /** 先设置数据 */
      localStorage.setItem('token', 't')
      localStorage.setItem('refreshToken', 'r')
      localStorage.setItem('expiresIn', '900')
      localStorage.setItem('tokenExpiry', '9999999999999')
      localStorage.setItem('userInfo', '{}')

      const store = useAuthStore()
      store.clearAuthData()

      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
      expect(localStorage.getItem('expiresIn')).toBeNull()
      expect(localStorage.getItem('tokenExpiry')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
    })

    it('初始化时应从localStorage恢复认证状态', () => {
      /** 预设localStorage数据 */
      localStorage.setItem('token', 'restored-token')
      localStorage.setItem('refreshToken', 'restored-rt')
      localStorage.setItem('expiresIn', '900')
      localStorage.setItem('tokenExpiry', String(Date.now() + 100000))
      localStorage.setItem('userInfo', JSON.stringify({
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      }))

      /** 重新创建Pinia实例以触发store初始化 */
      const newPinia = createPinia()
      setActivePinia(newPinia)
      const store = useAuthStore()

      expect(store.token).toBe('restored-token')
      expect(store.refreshToken).toBe('restored-rt')
      expect(store.userInfo.username).toBe('admin')
    })
  })

  // ============ token过期判断边界条件 ============
  describe('token过期判断边界条件', () => {
    it('tokenExpiry为空时应判定为已过期', () => {
      const store = useAuthStore()
      store.tokenExpiry = ''
      expect(store.isTokenExpired).toBe(true)
    })

    it('tokenExpiry为0时应判定为已过期', () => {
      const store = useAuthStore()
      store.tokenExpiry = '0'
      expect(store.isTokenExpired).toBe(true)
    })

    it('tokenExpiry为负数时应判定为已过期', () => {
      const store = useAuthStore()
      store.tokenExpiry = '-1'
      expect(store.isTokenExpired).toBe(true)
    })

    it('tokenExpiry为当前时间戳时应判定为已过期（边界：>=）', () => {
      const store = useAuthStore()
      const now = Date.now()
      store.tokenExpiry = String(now)
      /** Date.now() >= parseInt(tokenExpiry) 应为true */
      expect(store.isTokenExpired).toBe(true)
    })

    it('tokenExpiry为未来时间戳时应判定为未过期', () => {
      const store = useAuthStore()
      store.tokenExpiry = String(Date.now() + 60000)
      store.token = 'valid-token'
      expect(store.isTokenExpired).toBe(false)
    })

    it('isLoggedIn：token为空时即使未过期也应为false', () => {
      const store = useAuthStore()
      store.token = ''
      store.tokenExpiry = String(Date.now() + 60000)
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn：token存在但已过期应为false', () => {
      const store = useAuthStore()
      store.token = 'expired-token'
      store.tokenExpiry = '0'
      expect(store.isLoggedIn).toBe(false)
    })

    it('isLoggedIn：token存在且未过期应为true', () => {
      const store = useAuthStore()
      store.token = 'valid-token'
      store.tokenExpiry = String(Date.now() + 60000)
      expect(store.isLoggedIn).toBe(true)
    })

    it('setAuthData应根据expiresIn计算tokenExpiry', () => {
      const store = useAuthStore()
      const beforeTime = Date.now()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      })
      const afterTime = Date.now()

      const tokenExpiry = parseInt(store.tokenExpiry)
      /** tokenExpiry应约为当前时间 + 900秒 */
      const expectedMin = beforeTime + 900 * 1000
      const expectedMax = afterTime + 900 * 1000
      expect(tokenExpiry).toBeGreaterThanOrEqual(expectedMin)
      expect(tokenExpiry).toBeLessThanOrEqual(expectedMax)
    })

    it('setAuthData中expiresIn为0时使用默认值900', () => {
      const store = useAuthStore()
      const beforeTime = Date.now()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 0,
        id: '1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      })

      const tokenExpiry = parseInt(store.tokenExpiry)
      /** expiresIn为0（falsy）时使用默认值900 */
      const expectedMin = beforeTime + 900 * 1000
      expect(tokenExpiry).toBeGreaterThanOrEqual(expectedMin)
    })
  })

  // ============ getter完整性 ============
  describe('getter完整性', () => {
    it('realName应返回用户真实姓名', () => {
      const store = useAuthStore()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'admin',
        realName: '张三',
        role: 'SYS_ADMIN'
      })
      expect(store.realName).toBe('张三')
    })

    it('realName在无用户信息时应返回空字符串', () => {
      const store = useAuthStore()
      expect(store.realName).toBe('')
    })

    it('userId应返回用户ID', () => {
      const store = useAuthStore()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: 'user-123',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      })
      expect(store.userId).toBe('user-123')
    })

    it('userId在无用户信息时应返回null', () => {
      const store = useAuthStore()
      expect(store.userId).toBeNull()
    })

    it('role应返回用户角色', () => {
      const store = useAuthStore()
      store.setAuthData({
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'staff',
        realName: '客服',
        role: 'CS_STAFF'
      })
      expect(store.role).toBe('CS_STAFF')
    })

    it('role在无用户信息时应返回空字符串', () => {
      const store = useAuthStore()
      expect(store.role).toBe('')
    })
  })
})
