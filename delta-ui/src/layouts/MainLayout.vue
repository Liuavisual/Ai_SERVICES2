<!--
  主布局组件

  核心优化：
  - 使用 <Suspense> 包裹路由视图，监听 @pending/@resolve 精确追踪异步组件加载
  - 顶部 PageProgress 进度条提供视觉反馈，消除白屏感知
  - SkeletonBox 作为异步组件加载时的占位内容
  - 菜单项 hover 时预加载对应路由 chunk
  - 侧边栏菜单按角色过滤，完整覆盖所有26个路由页面

  @author 刘建国
-->
<template>
  <el-container class="main-layout">
    <el-header class="main-header">
      <div class="header-left">
        <h1 class="app-title">Delta AI 客服管理系统</h1>
      </div>
      <div class="header-right">
        <span class="user-info" v-if="userInfo">
          <el-icon><UserFilled /></el-icon>
          {{ userInfo.username }}
          <el-tag :type="userInfo.role === 'SYS_ADMIN' ? 'danger' : 'info'" size="small" class="role-tag">
            {{ roleLabel }}
          </el-tag>
        </span>
        <el-button type="danger" plain size="small" @click="handleLogout" :loading="logoutLoading">
          退出登录
        </el-button>
      </div>
    </el-header>
    <el-container class="main-body">
      <el-aside class="main-aside" :class="{ 'is-collapsed': sidebarCollapsed }">
        <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :router="true"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <template v-for="item in menuItems" :key="item.path || item.dividerKey">
            <el-divider v-if="item.divider" class="menu-divider" />
            <el-menu-item
              v-else
              :index="item.path"
              :key="item.path"
              @mouseenter="preloadRoute(item.path)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-main class="content-main">
        <PageProgress ref="progressBar" />
        <Suspense @pending="onRouteLoading" @resolve="onRouteLoaded">
          <template #default>
            <router-view v-slot="{ Component }">
              <transition name="fade-slide" mode="out-in">
                <keep-alive :include="cachedViews">
                  <component :is="Component" :key="$route.fullPath" />
                </keep-alive>
              </transition>
            </router-view>
          </template>
          <template #fallback>
            <div class="route-loading-container">
              <SkeletonBox type="table" :rows="8" :columns="5" />
            </div>
          </template>
        </Suspense>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import PageProgress from '@/components/PageProgress.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)
const logoutLoading = ref(false)
const progressBar = ref(null)
const preloadedRoutes = new Set()

const userInfo = computed(() => authStore.userInfo)
const cachedViews = ref([])

const roleLabel = computed(() => {
  if (!userInfo.value?.role) return ''
  return userInfo.value.role === 'SYS_ADMIN' ? '超级管理员' : userInfo.value.role === 'CS_LEADER' ? '客服主管' : '客服人员'
})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) return meta.activeMenu
  return path
})

/**
 * 完整菜单配置
 * 所有路由页面均在此定义，按业务模块分组，用分隔线区分
 * roles 为空或未定义表示所有角色可见
 */
const allMenus = [
  // ==================== 工作台 ====================
  { path: '/dashboard',  title: '工作台',    icon: 'Monitor' },
  { divider: true },

  // ==================== 客户管理 ====================
  { path: '/customers',           title: '客户名录',     icon: 'User' },
  { path: '/customer-profiles',   title: '客户画像',     icon: 'Histogram' },
  { path: '/cs-user-customer',    title: '客户分配',     icon: 'Connection',    roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/customer-lifecycle',  title: '客户生命周期',  icon: 'RefreshRight',  roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/satisfaction',        title: '满意度评价',    icon: 'Star' },
  { divider: true },

  // ==================== 陪玩管理 ====================
  { path: '/companions',          title: '陪玩师',       icon: 'UserFilled' },
  { path: '/companion-levels',    title: '陪玩等级',     icon: 'Medal',           roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/companion-schedule',  title: '日程管理',     icon: 'Calendar',        roles: ['SYS_ADMIN', 'CS_LEADER', 'COMPANION'] },
  { path: '/companion-schedule-calendar', title: '排班日历', icon: 'Date', roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/companion-orders',  title: '我的订单',     icon: 'Tickets' },
  { divider: true },

  // ==================== 业务运营 ====================
  { path: '/orders',             title: '订单管理',     icon: 'Tickets' },
  { path: '/work-orders',        title: '工单管理',     icon: 'Document' },
  { path: '/service-items',      title: '服务项目',     icon: 'Notebook',          roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/service-tracks',     title: '服务追踪',     icon: 'Timer' },
  { path: '/activity-packages',  title: '活动套餐',     icon: 'Present',           roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { divider: true },

  // ==================== 消息处理 ====================
  { path: '/pending-messages',   title: '待办事项',     icon: 'Bell' },
  { path: '/messages',           title: '消息记录',     icon: 'ChatLineSquare' },
  { divider: true },

  // ==================== 系统配置 ====================
  { path: '/club-config',        title: '俱乐部配置',   icon: 'HomeFilled',        roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/game-configs',       title: '游戏配置',     icon: 'VideoCamera',       roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/platform-configs',   title: '平台配置',     icon: 'Setting',           roles: ['SYS_ADMIN'] },
  { path: '/ai-config',          title: 'AI配置',       icon: 'Cpu',               roles: ['SYS_ADMIN'] },
  { path: '/faq-items',          title: '知识库',       icon: 'Collection',        roles: ['SYS_ADMIN'] },
  { path: '/keywords',           title: '关键词',       icon: 'Key',               roles: ['SYS_ADMIN'] },
  { path: '/replies',            title: '回复话术',     icon: 'ChatLineRound',     roles: ['SYS_ADMIN'] },
  { path: '/sys-users',          title: '人员管理',     icon: 'Avatar',            roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/permission',         title: '权限管理',     icon: 'Lock',              roles: ['SYS_ADMIN'] },
  { divider: true },

  // ==================== 商业化 ====================
  { path: '/pricing-plans',      title: '定价方案',     icon: 'PriceTag',          roles: ['SYS_ADMIN'] },
  { path: '/subscriptions',      title: '订阅管理',     icon: 'CreditCard',        roles: ['SYS_ADMIN'] },
  { divider: true },

  // ==================== 质检与结算 ====================
  { path: '/quality-checks',     title: '质检记录',     icon: 'Warning',           roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/settlements',        title: '结算管理',     icon: 'Money',             roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/trainings',          title: '培训管理',     icon: 'Reading',           roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { divider: true },

  // ==================== 数据分析 ====================
  { path: '/reports',            title: '营收报表',     icon: 'TrendCharts',       roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { divider: true },

  // ==================== 营销增长 ====================
  { path: '/campaigns',          title: '营销活动',     icon: 'Promotion',         roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { path: '/referrals',          title: '裂变推荐',     icon: 'Share',             roles: ['SYS_ADMIN', 'CS_LEADER'] },
  { divider: true },

  // ==================== 开发工具 ====================
  { path: '/chat-test',          title: '对话试炼',     icon: 'ChatDotSquare',     roles: ['SYS_ADMIN', 'CS_LEADER'] }
]

const menuItems = computed(() => {
  if (!userInfo.value) return []
  return allMenus.filter(m => m.divider || !m.roles || m.roles.length === 0 || m.roles.includes(userInfo.value.role))
})

/**
 * 路由异步加载开始时触发
 * 启动顶部进度条动画
 */
function onRouteLoading() {
  progressBar.value?.start()
}

/**
 * 路由异步加载完成时触发
 * 停止进度条动画
 */
function onRouteLoaded() {
  progressBar.value?.done()
}

/**
 * 预加载路由的异步chunk
 * 鼠标悬停菜单时提前加载，减少点击后等待时间
 *
 * @param {string} path 路由路径
 */
function preloadRoute(path) {
  if (!path || preloadedRoutes.has(path)) return
  preloadedRoutes.add(path)

  const matched = router.resolve(path)
  if (!matched?.matched?.length) return

  matched.matched.forEach(record => {
    const comp = record.components?.default
    if (typeof comp === 'function') {
      try { comp() } catch (e) { /* 预加载失败不影响主流程 */ }
    }
  })
}

/**
 * 退出登录
 */
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    logoutLoading.value = true
    await authStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  } finally {
    logoutLoading.value = false
  }
}

/**
 * 从路由配置中收集需要缓存的组件名称
 */
function collectCachedViews() {
  const views = []
  router.getRoutes().forEach(record => {
    if (record.meta?.keepAlive && record.name) {
      views.push(record.name)
    }
  })
  cachedViews.value = views
}

onMounted(() => {
  collectCachedViews()
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #304156;
  color: #fff;
  height: 60px;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
  flex-shrink: 0;
}

.app-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #e0e0e0;
}

.role-tag {
  margin-left: 6px;
}

.main-body {
  flex: 1;
  overflow: hidden;
}

.main-aside {
  background: #304156;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s ease;
  width: 220px;
  flex-shrink: 0;
}

.main-aside.is-collapsed {
  width: 64px;
}

.sidebar-toggle {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 44px;
  flex-shrink: 0;
  color: #bfcbd9;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: color 0.3s;
}

.sidebar-toggle:hover {
  color: #409eff;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  border-right: none;
}

.menu-divider {
  margin: 8px 0;
  border-top-color: rgba(255, 255, 255, 0.08);
}

.content-main {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
  background: #f0f2f5;
  position: relative;
}

.route-loading-container {
  padding: 20px;
  min-height: 400px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
