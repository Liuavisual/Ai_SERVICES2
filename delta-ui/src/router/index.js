import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据总览', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/Customer.vue'),
        meta: { title: '客户名录', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'customer-profiles',
        name: 'CustomerProfiles',
        component: () => import('@/views/CustomerProfiles.vue'),
        meta: { title: '客户画像', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'game-configs',
        name: 'GameConfigs',
        component: () => import('@/views/GameConfigs.vue'),
        meta: { title: '游戏配置', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'service-items',
        name: 'ServiceItems',
        component: () => import('@/views/ServiceItems.vue'),
        meta: { title: '服务项目', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'activity-packages',
        name: 'ActivityPackages',
        component: () => import('@/views/ActivityPackages.vue'),
        meta: { title: '活动套餐', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'chat-test',
        name: 'ChatTest',
        component: () => import('@/views/ChatTest.vue'),
        meta: { title: '对话试炼', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'club-config',
        name: 'ClubConfig',
        component: () => import('@/views/ClubConfig.vue'),
        meta: { title: '堂口配置', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'faq-items',
        name: 'FaqItems',
        component: () => import('@/views/FaqItems.vue'),
        meta: { title: '知识库', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'keywords',
        name: 'Keywords',
        component: () => import('@/views/Keywords.vue'),
        meta: { title: '关键词', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'replies',
        name: 'Replies',
        component: () => import('@/views/Replies.vue'),
        meta: { title: '回复话术', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue'),
        meta: { title: '消息记录', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'pending-messages',
        name: 'PendingMessages',
        component: () => import('@/views/PendingMessages.vue'),
        meta: { title: '待办事项', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'sys-users',
        name: 'SysUsers',
        component: () => import('@/views/SysUsers.vue'),
        meta: { title: '人员管理', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'cs-user-customer',
        name: 'CsUserCustomer',
        component: () => import('@/views/CsUserCustomer.vue'),
        meta: { title: '客户分配', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'companion-levels',
        name: 'CompanionLevels',
        component: () => import('@/views/CompanionLevels.vue'),
        meta: { title: '陪玩等级', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'companions',
        name: 'Companions',
        component: () => import('@/views/Companions.vue'),
        meta: { title: '陪玩师', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'companion-schedule',
        name: 'CompanionSchedule',
        component: () => import('@/views/CompanionSchedule.vue'),
        meta: { title: '排班管理', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '订单管理', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'ai-config',
        name: 'AIConfig',
        component: () => import('@/views/AIConfig.vue'),
        meta: { title: 'AI配置', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'platform-configs',
        name: 'PlatformConfigs',
        component: () => import('@/views/PlatformConfigs.vue'),
        meta: { title: '平台配置', roles: ['SYS_ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const ROLE_HOME_MAP = {
  SYS_ADMIN: '/sys-users',
  CS_LEADER: '/pending-messages',
  CS_STAFF: '/messages'
}

function getRoleHomePage(role) {
  return ROLE_HOME_MAP[role] || '/dashboard'
}

function clearAuthData() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('expiresIn')
  localStorage.removeItem('tokenExpiry')
  localStorage.removeItem('userInfo')
}

function isTokenExpired() {
  const tokenExpiry = localStorage.getItem('tokenExpiry')
  if (!tokenExpiry) return true
  return Date.now() >= parseInt(tokenExpiry)
}

function hasAnyToken() {
  return !!(localStorage.getItem('token') || localStorage.getItem('refreshToken'))
}

function getUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    localStorage.removeItem('userInfo')
    return {}
  }
}

function redirectToLogin(targetPath) {
  clearAuthData()
  const query = targetPath && targetPath !== '/login' && targetPath !== '/'
    ? { redirect: targetPath }
    : {}
  router.push({ path: '/login', query })
}

let isRouteRefreshing = false

router.beforeEach(async (to, from, next) => {
  if (to.meta && to.meta.public) {
    if (hasAnyToken() && !isTokenExpired()) {
      const userInfo = getUserInfo()
      next(getRoleHomePage(userInfo.role))
      return
    }
    next()
    return
  }

  if (!hasAnyToken()) {
    ElMessage.warning('请先登录')
    redirectToLogin(to.fullPath)
    return
  }

  if (isTokenExpired()) {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      ElMessage.warning('登录已过期，请重新登录')
      redirectToLogin(to.fullPath)
      return
    }

    if (isRouteRefreshing) {
      next(false)
      return
    }

    isRouteRefreshing = true

    try {
      const res = await axios.post('/api/auth/refresh', { refreshToken }, { timeout: 10000 })
      const data = res.data?.data || res.data
      if (data && data.token) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('refreshToken', data.refreshToken)
        localStorage.setItem('expiresIn', data.expiresIn)
        localStorage.setItem('tokenExpiry', Date.now() + (data.expiresIn || 900) * 1000)
        localStorage.setItem('userInfo', JSON.stringify(data))
      } else {
        ElMessage.warning('登录已过期，请重新登录')
        redirectToLogin(to.fullPath)
        return
      }
    } catch (err) {
      if (err.code === 'ECONNABORTED') {
        ElMessage.error('网络请求超时，请稍后重试')
      } else if (!err.response) {
        ElMessage.error('网络连接异常，请检查网络')
      } else {
        ElMessage.warning('登录已过期，请重新登录')
      }
      redirectToLogin(to.fullPath)
      return
    } finally {
      isRouteRefreshing = false
    }
  }

  if (to.path === '/login') {
    const userInfo = getUserInfo()
    next(getRoleHomePage(userInfo.role))
    return
  }

  if (to.meta && to.meta.roles) {
    const userInfo = getUserInfo()
    if (!to.meta.roles.includes(userInfo.role)) {
      ElMessage.error('您没有权限访问该页面')
      next(getRoleHomePage(userInfo.role))
      return
    }
  }

  next()
})

function detectEnvironment() {
  const ua = navigator.userAgent
  const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua)
  const isTablet = /iPad|Android(?!.*Mobile)/i.test(ua)
  return { isMobile, isTablet, isDesktop: !isMobile && !isTablet }
}

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

  init() {
    this.setupActivityTracking()
    this.setupBeforeUnload()
    this.startHeartbeat()
    this.setupOnlineHandler()
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
    const env = detectEnvironment()
    return env.isMobile ? this.config.idleTimeoutMobile : this.config.idleTimeoutDesktop
  },

  setupBeforeUnload() {
    window.addEventListener('beforeunload', () => {
      const env = detectEnvironment()
      if (env.isMobile) {
        clearAuthData()
        return
      }
      const payload = JSON.stringify({
        eventType: 'SESSION_END',
        userId: getUserInfo().id,
        timestamp: Date.now(),
        idleDuration: this.getIdleDuration()
      })
      navigator.sendBeacon('/api/auth/session-event', new Blob([payload], { type: 'application/json' }))
    })
  },

  startHeartbeat() {
    this.state.heartbeatTimer = setInterval(() => {
      if (!hasAnyToken()) return
      this.checkIdleTimeout()
    }, this.config.heartbeatInterval)
  },

  checkIdleTimeout() {
    const idleDuration = this.getIdleDuration()
    const timeout = this.getIdleTimeout()
    if (idleDuration >= timeout) {
      ElMessage.warning('由于长时间未操作，已自动退出登录')
      redirectToLogin(router.currentRoute.value?.fullPath)
    }
  },

  setupOnlineHandler() {
    window.addEventListener('online', () => {
      if (hasAnyToken() && isTokenExpired()) {
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
          axios.post('/api/auth/refresh', { refreshToken }, { timeout: 10000 })
            .then(res => {
              const data = res.data?.data || res.data
              if (data && data.token) {
                localStorage.setItem('token', data.token)
                localStorage.setItem('refreshToken', data.refreshToken)
                localStorage.setItem('expiresIn', data.expiresIn)
                localStorage.setItem('tokenExpiry', Date.now() + (data.expiresIn || 900) * 1000)
                localStorage.setItem('userInfo', JSON.stringify(data))
              }
            })
            .catch(() => {
              redirectToLogin(router.currentRoute.value?.fullPath)
            })
        }
      }
    })
  },

  destroy() {
    if (this.state.heartbeatTimer) {
      clearInterval(this.state.heartbeatTimer)
    }
  }
}

SessionManager.init()

document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible') {
    SessionManager.state.lastActivityTime = Date.now()
    if (hasAnyToken() && isTokenExpired()) {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        ElMessage.warning('登录已过期，请重新登录')
        redirectToLogin(router.currentRoute.value.fullPath)
      }
    }
  }
})

export { getRoleHomePage, redirectToLogin, clearAuthData, isTokenExpired, hasAnyToken, getUserInfo }
export default router
