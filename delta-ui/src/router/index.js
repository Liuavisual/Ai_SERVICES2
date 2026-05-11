import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { authStorage } from '@/utils/storage'
import { SessionManager } from '@/session'

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
        meta: { title: '数据总览', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
      },
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/Customer.vue'),
        meta: { title: '客户名录', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
      },
      {
        path: 'customer-profiles',
        name: 'CustomerProfiles',
        component: () => import('@/views/CustomerProfiles.vue'),
        meta: { title: '客户画像', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
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
        path: 'permission',
        name: 'Permission',
        component: () => import('@/views/Permission.vue'),
        meta: { title: '权限管理', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'pricing-plans',
        name: 'PricingPlans',
        component: () => import('@/views/PricingPlans.vue'),
        meta: { title: '定价方案', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'subscriptions',
        name: 'Subscriptions',
        component: () => import('@/views/Subscriptions.vue'),
        meta: { title: '订阅管理', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'quality-checks',
        name: 'QualityChecks',
        component: () => import('@/views/QualityChecks.vue'),
        meta: { title: '质检记录', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'settlements',
        name: 'Settlements',
        component: () => import('@/views/Settlements.vue'),
        meta: { title: '结算管理', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'trainings',
        name: 'Trainings',
        component: () => import('@/views/Trainings.vue'),
        meta: { title: '培训管理', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/Reports.vue'),
        meta: { title: '营收报表', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'campaigns',
        name: 'Campaigns',
        component: () => import('@/views/Campaigns.vue'),
        meta: { title: '营销活动', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'referrals',
        name: 'Referrals',
        component: () => import('@/views/Referrals.vue'),
        meta: { title: '裂变推荐', roles: ['SYS_ADMIN', 'CS_LEADER'] }
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
        meta: { title: '知识库', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'keywords',
        name: 'Keywords',
        component: () => import('@/views/Keywords.vue'),
        meta: { title: '关键词', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'replies',
        name: 'Replies',
        component: () => import('@/views/Replies.vue'),
        meta: { title: '回复话术', roles: ['SYS_ADMIN'] }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue'),
        meta: { title: '消息记录', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
      },
      {
        path: 'pending-messages',
        name: 'PendingMessages',
        component: () => import('@/views/PendingMessages.vue'),
        meta: { title: '待办事项', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
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
        meta: { title: '陪玩师', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
      },
      {
        path: 'companion-schedule',
        name: 'CompanionSchedule',
        component: () => import('@/views/CompanionSchedule.vue'),
        meta: { title: '日程管理', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF', 'COMPANION'], keepAlive: true }
      },
      {
        path: 'companion-schedule-calendar',
        name: 'CompanionScheduleCalendar',
        component: () => import('@/views/CompanionScheduleCalendar.vue'),
        meta: { title: '排班日历', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'companion-orders',
        name: 'CompanionOrders',
        component: () => import('@/views/CompanionOrders.vue'),
        meta: { title: '我的订单', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF', 'COMPANION'], keepAlive: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '订单管理', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
      },
      {
        path: 'work-orders',
        name: 'WorkOrders',
        component: () => import('@/views/WorkOrders.vue'),
        meta: { title: '工单管理', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'], keepAlive: true }
      },
      {
        path: 'service-tracks',
        name: 'ServiceTracks',
        component: () => import('@/views/ServiceTracks.vue'),
        meta: { title: '服务追踪', roles: ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF'] }
      },
      {
        path: 'customer-lifecycle',
        name: 'CustomerLifecycle',
        component: () => import('@/views/CustomerLifecycle.vue'),
        meta: { title: '客户生命周期', roles: ['SYS_ADMIN', 'CS_LEADER'] }
      },
      {
        path: 'satisfaction',
        name: 'Satisfaction',
        component: () => import('@/views/Satisfaction.vue'),
        meta: { title: '满意度评价', roles: ['SYS_ADMIN', 'CS_LEADER'] }
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

let isRouteRefreshing = false

router.beforeEach(async (to, from, next) => {
  if (to.meta && to.meta.public) {
    if (authStorage.hasAnyToken() && !authStorage.isTokenExpired()) {
      const userInfo = authStorage.getUserInfo()
      next(authStorage.getRoleHomePage(userInfo.role))
      return
    }
    next()
    return
  }

  if (!authStorage.hasAnyToken()) {
    ElMessage.warning('请先登录')
    next(false)
    SessionManager.redirectToLogin(to.fullPath)
    return
  }

  if (authStorage.isTokenExpired()) {
    const refreshToken = authStorage.getRefreshToken()
    if (!refreshToken) {
      ElMessage.warning('登录已过期，请重新登录')
      next(false)
      SessionManager.redirectToLogin(to.fullPath)
      return
    }

    if (isRouteRefreshing) {
      next(false)
      return
    }

    isRouteRefreshing = true

    try {
      const res = await axios.post('/api/v1/auth/refresh', { refreshToken }, { timeout: 10000 })
      const data = res.data?.data || res.data
      if (data && data.token) {
        authStorage.setAuth(data)
      } else {
        ElMessage.warning('登录已过期，请重新登录')
        next(false)
        SessionManager.redirectToLogin(to.fullPath)
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
      next(false)
      SessionManager.redirectToLogin(to.fullPath)
      return
    } finally {
      isRouteRefreshing = false
    }
  }

  if (to.path === '/login') {
    const userInfo = authStorage.getUserInfo()
    if (userInfo?.role) {
      next(authStorage.getRoleHomePage(userInfo.role))
    } else {
      next()
    }
    return
  }

  if (to.meta && to.meta.roles) {
    const userInfo = authStorage.getUserInfo()
    if (!userInfo?.role || !to.meta.roles.includes(userInfo.role)) {
      ElMessage.error('您没有权限访问该页面')
      if (userInfo?.role) {
        next(authStorage.getRoleHomePage(userInfo.role))
      } else {
        next('/login')
      }
      return
    }
  }

  next()
})

SessionManager.setRouter(router)
SessionManager.init()

export { SessionManager }
export default router
