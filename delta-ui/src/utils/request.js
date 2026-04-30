import axios from 'axios'
import { ElMessage } from 'element-plus'
import router, { redirectToLogin } from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
  // httpOnly Cookie方案：允许跨域请求携带Cookie
  withCredentials: true
})

let isRefreshing = false
let pendingRequests = []
let refreshFailCount = 0
let lastRefreshAttemptTime = 0
const MAX_REFRESH_FAILURES = 2
const MIN_REFRESH_INTERVAL = 5000

function acquireRefreshLock() {
  if (isRefreshing) {
    return false
  }
  isRefreshing = true
  return true
}

function releaseRefreshLock() {
  isRefreshing = false
}

function resolvePending(token) {
  pendingRequests.forEach(({ resolve, token: t }) => resolve(t))
  pendingRequests = []
}

function rejectPending(error) {
  pendingRequests.forEach(({ reject }) => reject(error))
  pendingRequests = []
}

const TOKEN_REFRESH_THRESHOLD = 2 * 60 * 1000

function shouldProactivelyRefresh() {
  const tokenExpiry = localStorage.getItem('tokenExpiry')
  if (!tokenExpiry) return false
  const remaining = parseInt(tokenExpiry) - Date.now()
  return remaining > 0 && remaining < TOKEN_REFRESH_THRESHOLD
}

async function proactiveRefresh() {
  if (!acquireRefreshLock()) return
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) { releaseRefreshLock(); return }

  try {
    const refreshRes = await axios.post('/api/v1/auth/refresh', { refreshToken })
    const refreshData = refreshRes.data?.data || refreshRes.data
    if (refreshData && refreshData.token) {
      localStorage.setItem('token', refreshData.token)
      localStorage.setItem('refreshToken', refreshData.refreshToken)
      localStorage.setItem('expiresIn', refreshData.expiresIn)
      localStorage.setItem('tokenExpiry', Date.now() + (refreshData.expiresIn || 900) * 1000)
      localStorage.setItem('userInfo', JSON.stringify(refreshData))
      resolvePending(refreshData.token)
    }
  } catch {
    // 主动刷新失败不阻断请求，等401再处理
  } finally {
    releaseRefreshLock()
  }
}

request.interceptors.request.use(
  async config => {
    if (config.params) {
      if ('pageNum' in config.params) {
        config.params.page = config.params.pageNum
        delete config.params.pageNum
      }
      if ('pageSize' in config.params) {
        config.params.size = config.params.pageSize
        delete config.params.pageSize
      }
    }
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    config.withCredentials = true
    if (shouldProactivelyRefresh() && !config.url?.includes('/auth/')) {
      await proactiveRefresh()
      const newToken = localStorage.getItem('token')
      if (newToken) {
        config.headers.Authorization = `Bearer ${newToken}`
      }
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  async error => {
    const { config, response } = error

    if (!response) {
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请稍后重试')
      } else {
        ElMessage.error('网络连接异常，请检查网络')
      }
      return Promise.reject(error)
    }

    const status = response.status
    const data = response.data

    if (status === 401) {
      if (refreshFailCount >= MAX_REFRESH_FAILURES) {
        refreshFailCount = 0
        ElMessage.warning('登录已过期，请重新登录')
        redirectToLogin(router.currentRoute.value?.fullPath)
        return Promise.reject(error)
      }

      const refreshToken = localStorage.getItem('refreshToken')

      if (!refreshToken) {
        ElMessage.warning('登录已过期，请重新登录')
        redirectToLogin(router.currentRoute.value?.fullPath)
        return Promise.reject(error)
      }

      if (!acquireRefreshLock()) {
        return new Promise((resolve, reject) => {
          pendingRequests.push({ resolve, reject })
        }).then(token => {
          config.headers.Authorization = `Bearer ${token}`
          return request(config)
        }).catch(err => {
          ElMessage.warning('登录已过期，请重新登录')
          redirectToLogin(router.currentRoute.value?.fullPath)
          return Promise.reject(err)
        })
      }

      try {
        const now = Date.now()
        if (now - lastRefreshAttemptTime < MIN_REFRESH_INTERVAL) {
          refreshFailCount++
          rejectPending(new Error('刷新请求过于频繁'))
          ElMessage.warning('登录已过期，请重新登录')
          redirectToLogin(router.currentRoute.value?.fullPath)
          return Promise.reject(error)
        }
        lastRefreshAttemptTime = now

        const refreshRes = await axios.post('/api/v1/auth/refresh', { refreshToken })
        const refreshData = refreshRes.data?.data || refreshRes.data

        if (refreshData && refreshData.token) {
          localStorage.setItem('token', refreshData.token)
          localStorage.setItem('refreshToken', refreshData.refreshToken)
          localStorage.setItem('expiresIn', refreshData.expiresIn)
          localStorage.setItem('tokenExpiry', Date.now() + (refreshData.expiresIn || 900) * 1000)
          localStorage.setItem('userInfo', JSON.stringify(refreshData))

          refreshFailCount = 0
          resolvePending(refreshData.token)
          config.headers.Authorization = `Bearer ${refreshData.token}`
          return request(config)
        } else {
          refreshFailCount++
          rejectPending(new Error('刷新Token失败'))
          ElMessage.warning('登录已过期，请重新登录')
          redirectToLogin(router.currentRoute.value?.fullPath)
          return Promise.reject(error)
        }
      } catch (refreshError) {
        refreshFailCount++
        rejectPending(refreshError)
        ElMessage.warning('登录已过期，请重新登录')
        redirectToLogin(router.currentRoute.value?.fullPath)
        return Promise.reject(refreshError)
      } finally {
        releaseRefreshLock()
      }
    }

    if (status === 403) {
      ElMessage.error(data?.message || '您没有权限执行此操作')
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const roleHome = { SYS_ADMIN: '/sys-users', CS_LEADER: '/pending-messages', CS_STAFF: '/messages' }
      const currentPath = router.currentRoute.value?.path
      const targetPath = roleHome[userInfo.role] || '/dashboard'
      if (currentPath !== targetPath) {
        router.push(targetPath)
      }
      return Promise.reject(error)
    }

    if (status === 429) {
      ElMessage.warning(data?.message || '请求过于频繁，请稍后重试')
      return Promise.reject(error)
    }

    if (status >= 400 && status < 500) {
      ElMessage.error(data?.message || `请求错误(${status})`)
      return Promise.reject(error)
    }

    if (status >= 500) {
      ElMessage.error('服务器异常，请稍后重试')
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default request
