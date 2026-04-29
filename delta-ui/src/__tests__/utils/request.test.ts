/**
 * HTTP请求工具测试
 *
 * 测试axios实例创建配置、请求拦截器、响应拦截器等核心功能。
 * 包括Token注入、401处理、错误状态码处理等场景。
 *
 * 策略：不mock axios本身，让request.js创建真实的axios实例，
 * 通过mock adapter验证拦截器行为，通过defaults验证实例配置。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

/** 使用vi.hoisted提升mock函数，确保vi.mock工厂函数可以访问 */
const { mockRedirectToLogin } = vi.hoisted(() => ({
  mockRedirectToLogin: vi.fn()
}))

/** Mock router模块，避免真实路由依赖 */
vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
    currentRoute: { value: { fullPath: '/dashboard' } }
  },
  redirectToLogin: mockRedirectToLogin
}))

/** Mock element-plus */
vi.mock('element-plus', () => ({
  ElMessage: {
    warning: vi.fn(),
    error: vi.fn(),
    success: vi.fn()
  }
}))

import axios from 'axios'
import request from '@/utils/request'

describe('HTTP请求工具测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ============ axios实例创建配置 ============
  describe('axios实例创建配置', () => {
    it('应使用正确的baseURL', () => {
      expect(request.defaults.baseURL).toBe('/api/v1')
    })

    it('应设置超时时间为30000毫秒', () => {
      expect(request.defaults.timeout).toBe(30000)
    })

    it('应启用withCredentials以支持Cookie跨域', () => {
      expect(request.defaults.withCredentials).toBe(true)
    })

    it('应设置默认Content-Type为application/json', () => {
      /** axios将create中的headers配置存储在defaults.headers中 */
      const contentType = request.defaults.headers['Content-Type'] || request.defaults.headers.common?.['Content-Type']
      expect(contentType).toBe('application/json')
    })
  })

  // ============ 请求拦截器 ============
  describe('请求拦截器', () => {
    it('当localStorage有token时，应添加Authorization header', async () => {
      localStorage.setItem('token', 'test-token-123')

      /** 使用adapter捕获请求配置 */
      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 200, message: 'ok', data: null },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await request.get('/test')
        expect(adapterSpy).toHaveBeenCalled()
        const config = adapterSpy.mock.calls[0][0]
        expect(config.headers.Authorization).toBe('Bearer test-token-123')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当localStorage无token时，不应添加Authorization header', async () => {
      /** 确保localStorage中没有token */
      localStorage.removeItem('token')

      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 200, message: 'ok', data: null },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await request.get('/test')
        expect(adapterSpy).toHaveBeenCalled()
        const config = adapterSpy.mock.calls[0][0]
        expect(config.headers.Authorization).toBeFalsy()
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('应确保withCredentials为true', async () => {
      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 200, message: 'ok', data: null },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await request.get('/test')
        const config = adapterSpy.mock.calls[0][0]
        expect(config.withCredentials).toBe(true)
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 成功响应 ============
  describe('响应拦截器 - 成功响应', () => {
    it('当code为200时，应返回response.data', async () => {
      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 200, message: 'ok', data: { id: '1' } },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        const result = await request.get('/test')
        expect(result).toEqual({ code: 200, message: 'ok', data: { id: '1' } })
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当code为0时，应返回response.data', async () => {
      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 0, message: 'success', data: [] },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        const result = await request.get('/test')
        expect(result).toEqual({ code: 0, message: 'success', data: [] })
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当code非200/0时，应显示错误消息并reject', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 500, message: '服务器内部错误' },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow('服务器内部错误')
        expect(ElMessage.error).toHaveBeenCalledWith('服务器内部错误')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当code非200/0且无message时，应显示默认错误消息', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => Promise.resolve({
        data: { code: 400 },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
        request: {}
      }))
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow('请求失败')
        expect(ElMessage.error).toHaveBeenCalledWith('请求失败')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 网络错误 ============
  describe('响应拦截器 - 网络错误', () => {
    it('当请求超时（ECONNABORTED）时，应提示请求超时', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('timeout')
        error.code = 'ECONNABORTED'
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow('timeout')
        expect(ElMessage.error).toHaveBeenCalledWith('请求超时，请稍后重试')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当网络异常时，应提示网络连接异常', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Network Error')
        error.code = 'NETWORK_ERROR'
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow('Network Error')
        expect(ElMessage.error).toHaveBeenCalledWith('网络连接异常，请检查网络')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 401状态码 ============
  describe('响应拦截器 - 401状态码', () => {
    it('当无refreshToken时，应提示登录过期并调用redirectToLogin', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Unauthorized')
        error.response = { status: 401, data: {} }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.warning).toHaveBeenCalledWith('登录已过期，请重新登录')
        expect(mockRedirectToLogin).toHaveBeenCalled()
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 403状态码 ============
  describe('响应拦截器 - 403状态码', () => {
    it('应显示权限错误消息', async () => {
      const { ElMessage } = await import('element-plus')
      localStorage.setItem('userInfo', JSON.stringify({ role: 'CS_STAFF' }))

      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Forbidden')
        error.response = { status: 403, data: { message: '无权操作' } }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.error).toHaveBeenCalledWith('无权操作')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 429状态码 ============
  describe('响应拦截器 - 429状态码', () => {
    it('应提示请求过于频繁', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Too Many Requests')
        error.response = { status: 429, data: { message: '请求过于频繁' } }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.warning).toHaveBeenCalledWith('请求过于频繁')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 其他4xx错误 ============
  describe('响应拦截器 - 其他4xx错误', () => {
    it('当状态码为404时，应显示请求错误消息', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Not Found')
        error.response = { status: 404, data: { message: '资源不存在' } }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.error).toHaveBeenCalledWith('资源不存在')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当4xx无message时，应显示默认格式消息', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Unprocessable')
        error.response = { status: 422, data: {} }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.error).toHaveBeenCalledWith('请求错误(422)')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ 响应拦截器 - 5xx错误 ============
  describe('响应拦截器 - 5xx错误', () => {
    it('当状态码为500时，应显示服务器异常消息', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Internal Server Error')
        error.response = { status: 500, data: {} }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.error).toHaveBeenCalledWith('服务器异常，请稍后重试')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })

    it('当状态码为502时，应显示服务器异常消息', async () => {
      const { ElMessage } = await import('element-plus')
      const adapterSpy = vi.fn(() => {
        const error: any = new Error('Bad Gateway')
        error.response = { status: 502, data: {} }
        return Promise.reject(error)
      })
      const originalAdapter = request.defaults.adapter
      request.defaults.adapter = adapterSpy

      try {
        await expect(request.get('/test')).rejects.toThrow()
        expect(ElMessage.error).toHaveBeenCalledWith('服务器异常，请稍后重试')
      } finally {
        request.defaults.adapter = originalAdapter
      }
    })
  })

  // ============ request默认导出 ============
  describe('request默认导出', () => {
    it('应正确导出request实例', () => {
      expect(request).toBeDefined()
    })

    it('request应具有interceptors属性', () => {
      expect(request.interceptors).toBeDefined()
      expect(request.interceptors.request).toBeDefined()
      expect(request.interceptors.response).toBeDefined()
    })

    it('request应是axios实例', () => {
      expect(typeof request.get).toBe('function')
      expect(typeof request.post).toBe('function')
      expect(typeof request.put).toBe('function')
      expect(typeof request.delete).toBe('function')
    })
  })
})
