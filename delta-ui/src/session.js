import { ElMessage } from 'element-plus'
import { authStorage } from '@/utils/storage'
import { authApi } from '@/api'

const SessionManager = {
  config: {
    idleTimeoutDesktop: 30 * 60 * 1000,
    idleTimeoutMobile: 15 * 60 * 1000,
    heartbeatInterval: 2 * 60 * 1000,
    heartbeatGrace: 5 * 60 * 1000
  },

  state: {
    lastActivityTime: Date.now(),
    heartbeatTimer: null
  },

  setRouter(routerInstance) {
    this._router = routerInstance
  },

  init() {
    this.setupActivityTracking()
    this.setupBeforeUnload()
    this.startHeartbeat()
    this.setupOnlineHandler()
    this.setupVisibilityHandler()
  },

  setupActivityTracking() {
    const events = ['mousedown', 'keydown', 'scroll', 'touchstart']
    let throttleTimer = null
    const handler = () => {
      if (throttleTimer) return
      throttleTimer = setTimeout(() => { throttleTimer = null }, 5000)
      this.state.lastActivityTime = Date.now()
    }
    events.forEach(evt => document.addEventListener(evt, handler, { passive: true }))
  },

  getIdleDuration() {
    return Date.now() - this.state.lastActivityTime
  },

  getIdleTimeout() {
    const env = this.detectEnvironment()
    return env.isMobile ? this.config.idleTimeoutMobile : this.config.idleTimeoutDesktop
  },

  setupBeforeUnload() {
    window.addEventListener('beforeunload', () => {
      const env = this.detectEnvironment()
      if (env.isMobile) {
        authStorage.clearAuth()
        return
      }
      const payload = JSON.stringify({
        eventType: 'SESSION_END',
        userId: authStorage.getUserInfo().id,
        timestamp: Date.now(),
        idleDuration: this.getIdleDuration()
      })
      navigator.sendBeacon('/api/v1/auth/session-event', new Blob([payload], { type: 'application/json' }))
    })
  },

  startHeartbeat() {
    this.state.heartbeatTimer = setInterval(() => {
      if (!authStorage.hasAnyToken()) return
      this.checkIdleTimeout()
    }, this.config.heartbeatInterval)
  },

  checkIdleTimeout() {
    const idleDuration = this.getIdleDuration()
    const timeout = this.getIdleTimeout()
    if (idleDuration >= timeout) {
      ElMessage.warning('由于长时间未操作，已自动退出登录')
      this.redirectToLogin(this._router?.currentRoute.value?.fullPath)
    }
  },

  setupOnlineHandler() {
    window.addEventListener('online', () => {
      if (authStorage.hasAnyToken() && authStorage.isTokenExpired()) {
        const refreshToken = authStorage.getRefreshToken()
        if (refreshToken) {
          authApi.refresh({ refreshToken }).then(res => {
            const data = res.data || res
            if (data && data.token) {
              authStorage.setAuth(data)
            }
          }).catch(() => {
            this.redirectToLogin(this._router?.currentRoute.value?.fullPath)
          })
        }
      }
    })
  },

  setupVisibilityHandler() {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        this.state.lastActivityTime = Date.now()
        if (authStorage.hasAnyToken() && authStorage.isTokenExpired()) {
          const refreshToken = authStorage.getRefreshToken()
          if (!refreshToken) {
            ElMessage.warning('登录已过期，请重新登录')
            this.redirectToLogin(this._router?.currentRoute.value.fullPath)
          }
        }
      }
    })
  },

  redirectToLogin(targetPath) {
    authStorage.clearAuth()
    const query = targetPath && targetPath !== '/login' && targetPath !== '/'
      ? { redirect: targetPath }
      : {}
    this._router?.push({ path: '/login', query })
  },

  detectEnvironment() {
    const ua = navigator.userAgent
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua)
    const isTablet = /iPad|Android(?!.*Mobile)/i.test(ua)
    return { isMobile, isTablet, isDesktop: !isMobile && !isTablet }
  },

  destroy() {
    if (this.state.heartbeatTimer) {
      clearInterval(this.state.heartbeatTimer)
    }
  }
}

export { SessionManager }
