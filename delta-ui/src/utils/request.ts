/**
 * HTTP请求封装
 *
 * 基于 axios 的请求实例，统一处理：
 * - 请求拦截：自动注入 Token 和用户ID
 * - 响应拦截：自动解包为 Result<T>，统一错误提示
 * - Token 刷新：401 时自动尝试刷新 Token，支持并发请求等待
 *
 * @author 刘建国
 */
import axios, { type AxiosRequestConfig, type AxiosDefaults, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { authStorage } from '@/utils/storage'

/** 扩展请求配置，添加自定义属性 */
interface ExtendedAxiosRequestConfig extends InternalAxiosRequestConfig {
  /** 标记是否已重试刷新Token */
  _retry?: boolean
}

/** 统一响应结果 */
export interface Result<T = unknown> {
  /** 响应状态码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
}

/** 自定义请求实例类型，所有方法返回 Result<T> 而不是 AxiosResponse */
interface RequestInstance {
  <T = any>(config: AxiosRequestConfig): Promise<Result<T>>
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>>
  post<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<Result<T>>
  put<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<Result<T>>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<Result<T>>
  defaults: AxiosDefaults
  interceptors: {
    request: { use: (...args: unknown[]) => unknown; eject: (id: number) => void; clear: () => void }
    response: { use: (...args: unknown[]) => unknown; eject: (id: number) => void; clear: () => void }
  }
}

const http = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

/** Token 刷新锁，防止并发刷新 */
let refreshLock = false
/** Token 刷新订阅者队列 */
let refreshSubscribers: Array<(token: string) => void> = []
/** Token 刷新失败计数 */
let refreshFailCount = 0
/** 上次刷新尝试时间 */
let lastRefreshAttemptTime = 0

http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const method = (config.method || '').toLowerCase()
    if (['post', 'put', 'patch'].includes(method) && config.data === undefined) {
      config.data = {}
    }
    if (config.params) {
      if (config.params.pageNum !== undefined) {
        config.params.page = config.params.pageNum
        delete config.params.pageNum
      }
      if (config.params.pageSize !== undefined) {
        config.params.size = config.params.pageSize
        delete config.params.pageSize
      }
    }
    const token = authStorage.getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      config.headers['X-User-ID'] = authStorage.getUserInfo()?.id || ''
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * 通知所有订阅者 Token 已刷新
 * @param token - 新的访问令牌
 */
function onRefreshed(token: string): void {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

/**
 * 添加 Token 刷新订阅者
 * @param cb - Token 刷新完成后的回调
 */
function addRefreshSubscriber(cb: (token: string) => void): void {
  refreshSubscribers.push(cb)
}

http.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && data.code === 401) {
      const url = typeof response.config.url === 'string' ? response.config.url : ''
      if (url.includes('/auth/login')) {
        const rejectError: Error & { response?: unknown } = new Error(data.message || '登录失败')
        rejectError.response = response
        return Promise.reject(rejectError)
      }
      return handle401(response.config)
    }
    if (data && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      const rejectError: Error & { response?: unknown } = new Error(data.message || '请求失败')
      rejectError.response = response
      return Promise.reject(rejectError)
    }
    return data
  },
  async (error) => {
    const { response, config } = error
    if (!response) {
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请稍后重试')
      } else {
        ElMessage.error('网络连接异常，请检查网络')
      }
      return Promise.reject(error)
    }

    if (response.status === 401 && config && !config._retry) {
      return handle401(config)
    }

    if (response.status === 403) {
      ElMessage.error(response.data?.message || '无权操作')
      return Promise.reject(error)
    }

    if (response.status === 404) {
      ElMessage.error(response.data?.message || '请求的资源不存在')
      return Promise.reject(error)
    }

    if (response.status === 429) {
      ElMessage.warning(response.data?.message || '请求过于频繁')
      return Promise.reject(error)
    }

    if (response.status >= 400 && response.status < 500) {
      ElMessage.error(response.data?.message || `请求错误(${response.status})`)
      return Promise.reject(error)
    }

    if (response.status >= 500) {
      ElMessage.error(response.data?.message || '系统异常，请联系管理员')
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

/**
 * 处理 401 未授权
 *
 * 尝试使用 refreshToken 刷新令牌，支持并发请求等待机制。
 * 刷新成功则重试原请求，失败则清除登录态并跳转登录页。
 *
 * @param config - 原始请求配置
 * @returns 重试请求的 Promise
 */
async function handle401(config: ExtendedAxiosRequestConfig): Promise<unknown> {
  const now = Date.now()
  if (now - lastRefreshAttemptTime < 3000 && refreshFailCount >= 3) {
    authStorage.clearAuth()
    ElMessage.warning('登录已过期，请重新登录')
    window.location.href = '/login'
    return Promise.reject(new Error('登录已过期'))
  }

  const refreshToken = authStorage.getRefreshToken()
  if (!refreshToken) {
    authStorage.clearAuth()
    ElMessage.warning('登录已过期，请重新登录')
    window.location.href = '/login'
    return Promise.reject(new Error('登录已过期'))
  }

  if (refreshLock) {
    return new Promise((resolve) => {
      addRefreshSubscriber((token: string) => {
        config.headers['Authorization'] = `Bearer ${token}`
        resolve(http(config))
      })
    })
  }

  config._retry = true
  refreshLock = true
  lastRefreshAttemptTime = now

  try {
    const res = await axios.post('/api/v1/auth/refresh', { refreshToken }, { timeout: 10000 })
    const data = res.data?.data || res.data
    if (!data || !data.token) {
      throw new Error('刷新失败')
    }
    authStorage.setAuth(data)
    refreshFailCount = 0
    refreshLock = false
    onRefreshed(data.token)
    config.headers['Authorization'] = `Bearer ${data.token}`
    return http(config)
  } catch (err) {
    refreshFailCount++
    refreshLock = false
    refreshSubscribers = []
    authStorage.clearAuth()
    ElMessage.warning('登录已过期，请重新登录')
    window.location.href = '/login'
    return Promise.reject(err)
  }
}

/** 导出类型安全的请求实例 */
const request = http as unknown as RequestInstance
export default request
