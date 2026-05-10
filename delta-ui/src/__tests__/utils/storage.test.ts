/**
 * authStorage 工具函数单元测试
 *
 * 测试认证存储工具的所有方法：getToken/getRefreshToken/getExpiresIn/
 * getTokenExpiry/getUserInfo/setAuth/clearAuth/hasAnyToken/
 * isTokenExpired/getRoleHomePage。
 *
 * @author 刘建国
 */
import { describe, it, expect, beforeEach } from 'vitest'
import { authStorage } from '@/utils/storage'

describe('authStorage 工具', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  // ============ 基本 getter ============
  describe('基本 getter', () => {
    it('getToken：无数据时应返回 null', () => {
      expect(authStorage.getToken()).toBeNull()
    })

    it('getToken：有数据时应返回正确值', () => {
      localStorage.setItem('token', 'abc123')
      expect(authStorage.getToken()).toBe('abc123')
    })

    it('getRefreshToken：无数据时应返回 null', () => {
      expect(authStorage.getRefreshToken()).toBeNull()
    })

    it('getRefreshToken：有数据时应返回正确值', () => {
      localStorage.setItem('refreshToken', 'rt-xyz')
      expect(authStorage.getRefreshToken()).toBe('rt-xyz')
    })

    it('getExpiresIn：无数据时应返回 null', () => {
      expect(authStorage.getExpiresIn()).toBeNull()
    })

    it('getExpiresIn：有数据时应返回正确值', () => {
      localStorage.setItem('expiresIn', '1800')
      expect(authStorage.getExpiresIn()).toBe('1800')
    })

    it('getTokenExpiry：无数据时应返回 null', () => {
      expect(authStorage.getTokenExpiry()).toBeNull()
    })

    it('getTokenExpiry：有数据时应返回正确值', () => {
      localStorage.setItem('tokenExpiry', '9999999999999')
      expect(authStorage.getTokenExpiry()).toBe('9999999999999')
    })
  })

  // ============ getUserInfo ============
  describe('getUserInfo', () => {
    it('无数据时应返回空对象', () => {
      expect(authStorage.getUserInfo()).toEqual({})
    })

    it('有合法 JSON 数据时应正确解析', () => {
      localStorage.setItem('userInfo', JSON.stringify({
        id: '1',
        username: 'admin',
        role: 'SYS_ADMIN'
      }))
      const info = authStorage.getUserInfo()
      expect(info.id).toBe('1')
      expect(info.username).toBe('admin')
      expect(info.role).toBe('SYS_ADMIN')
    })

    it('JSON 解析失败时应返回空对象并清除脏数据', () => {
      localStorage.setItem('userInfo', '{invalid json')
      const info = authStorage.getUserInfo()
      expect(info).toEqual({})
      expect(localStorage.getItem('userInfo')).toBeNull()
    })

    it('空字符串应返回空对象', () => {
      localStorage.setItem('userInfo', '')
      const info = authStorage.getUserInfo()
      expect(info).toEqual({})
    })
  })

  // ============ setAuth ============
  describe('setAuth', () => {
    it('应正确设置所有认证字段', () => {
      const beforeTime = Date.now()
      authStorage.setAuth({
        token: 'my-token',
        refreshToken: 'my-rt',
        expiresIn: 900
      })

      expect(localStorage.getItem('token')).toBe('my-token')
      expect(localStorage.getItem('refreshToken')).toBe('my-rt')
      expect(localStorage.getItem('expiresIn')).toBe('900')

      const tokenExpiry = parseInt(localStorage.getItem('tokenExpiry') || '0')
      expect(tokenExpiry).toBeGreaterThanOrEqual(beforeTime + 900 * 1000)
    })

    it('expiresIn 为空时应保存空字符串 tokenExpiry', () => {
      authStorage.setAuth({ token: 't', refreshToken: 'r' })
      expect(localStorage.getItem('tokenExpiry')).toBe('')
    })

    it('应正确保存 userInfo 为 JSON 字符串', () => {
      authStorage.setAuth({
        token: 't',
        refreshToken: 'r',
        expiresIn: 600,
        id: 'user-1',
        username: 'test',
        role: 'CS_STAFF',
        phone: '13800001111'
      })

      const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
      expect(stored.token).toBe('t')
      expect(stored.username).toBe('test')
      expect(stored.phone).toBe('13800001111')
    })

    it('expiresIn 为 0（falsy）时应保存空 tokenExpiry', () => {
      authStorage.setAuth({ token: 't', refreshToken: 'r', expiresIn: 0 })
      expect(localStorage.getItem('tokenExpiry')).toBe('')
    })
  })

  // ============ clearAuth ============
  describe('clearAuth', () => {
    it('应清除所有 5 个认证键', () => {
      /** 预设数据 */
      localStorage.setItem('token', 't')
      localStorage.setItem('refreshToken', 'r')
      localStorage.setItem('expiresIn', '900')
      localStorage.setItem('tokenExpiry', '999999')
      localStorage.setItem('userInfo', '{}')

      authStorage.clearAuth()

      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
      expect(localStorage.getItem('expiresIn')).toBeNull()
      expect(localStorage.getItem('tokenExpiry')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
    })

    it('无数据时调用 clearAuth 不应报错', () => {
      expect(() => authStorage.clearAuth()).not.toThrow()
    })

    it('部分数据时调用 clearAuth 不影响其他 localStorage 键', () => {
      localStorage.setItem('token', 't')
      localStorage.setItem('otherKey', 'keep-me')

      authStorage.clearAuth()

      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('otherKey')).toBe('keep-me')
    })
  })

  // ============ hasAnyToken ============
  describe('hasAnyToken', () => {
    it('无任何 token 时应返回 false', () => {
      expect(authStorage.hasAnyToken()).toBe(false)
    })

    it('仅有 token 时应返回 true', () => {
      localStorage.setItem('token', 'abc')
      expect(authStorage.hasAnyToken()).toBe(true)
    })

    it('仅有 refreshToken 时应返回 true', () => {
      localStorage.setItem('refreshToken', 'abc')
      expect(authStorage.hasAnyToken()).toBe(true)
    })

    it('两者都有时应返回 true', () => {
      localStorage.setItem('token', 'a')
      localStorage.setItem('refreshToken', 'b')
      expect(authStorage.hasAnyToken()).toBe(true)
    })
  })

  // ============ isTokenExpired ============
  describe('isTokenExpired', () => {
    it('无 tokenExpiry 时应返回 true', () => {
      expect(authStorage.isTokenExpired()).toBe(true)
    })

    it('tokenExpiry 为 0 时应返回 true', () => {
      localStorage.setItem('tokenExpiry', '0')
      expect(authStorage.isTokenExpired()).toBe(true)
    })

    it('tokenExpiry 为过去时间戳时应返回 true', () => {
      localStorage.setItem('tokenExpiry', '1')
      expect(authStorage.isTokenExpired()).toBe(true)
    })

    it('tokenExpiry 为未来时间戳时应返回 false', () => {
      localStorage.setItem('tokenExpiry', String(Date.now() + 3600000))
      expect(authStorage.isTokenExpired()).toBe(false)
    })
  })

  // ============ getRoleHomePage ============
  describe('getRoleHomePage', () => {
    it('SYS_ADMIN 应返回 /dashboard', () => {
      expect(authStorage.getRoleHomePage('SYS_ADMIN')).toBe('/dashboard')
    })

    it('CS_LEADER 应返回 /pending-messages', () => {
      expect(authStorage.getRoleHomePage('CS_LEADER')).toBe('/pending-messages')
    })

    it('CS_STAFF 应返回 /pending-messages', () => {
      expect(authStorage.getRoleHomePage('CS_STAFF')).toBe('/pending-messages')
    })

    it('未知角色应返回 /dashboard', () => {
      expect(authStorage.getRoleHomePage('UNKNOWN_ROLE')).toBe('/dashboard')
    })

    it('空字符串应返回 /dashboard', () => {
      expect(authStorage.getRoleHomePage('')).toBe('/dashboard')
    })
  })

  // ============ 完整流程测试 ============
  describe('完整流程测试', () => {
    it('setAuth → 读取 → clearAuth 完整流程', () => {
      /** 1. 初始状态：无数据 */
      expect(authStorage.hasAnyToken()).toBe(false)

      /** 2. 设置认证数据 */
      authStorage.setAuth({
        token: 'flow-token',
        refreshToken: 'flow-rt',
        expiresIn: 1800
      })

      /** 3. 验证设置成功 */
      expect(authStorage.getToken()).toBe('flow-token')
      expect(authStorage.getRefreshToken()).toBe('flow-rt')
      expect(authStorage.getExpiresIn()).toBe('1800')
      expect(authStorage.hasAnyToken()).toBe(true)
      expect(authStorage.isTokenExpired()).toBe(false)

      /** 4. 清除认证数据 */
      authStorage.clearAuth()

      /** 5. 验证清除成功 */
      expect(authStorage.getToken()).toBeNull()
      expect(authStorage.getRefreshToken()).toBeNull()
      expect(authStorage.hasAnyToken()).toBe(false)
      expect(authStorage.isTokenExpired()).toBe(true)
    })
  })
})