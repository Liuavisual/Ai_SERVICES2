<!--
  主布局组件

  核心优化：
  - 使用 <Suspense> 包裹路由视图，监听 @pending/@resolve 精确追踪异步组件加载
  - 顶部 PageProgress 进度条提供视觉反馈，消除白屏感知
  - SkeletonBox 作为异步组件加载时的占位内容
  - 菜单项 hover 时预加载对应路由 chunk
  - 侧边栏菜单按角色过滤

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
          <template v-for="item in menuItems" :key="item.path">
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

const roleLabel = computed(() => userInfo.value?.role === 'SYS_ADMIN' ? '超级管理员' : userInfo.value?.role === 'CS_LEADER' ? '客服主管' : '客服人员')

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) return meta.activeMenu
  return path
})

const allMenus = [
  { path: '/dashboard', title: '工作台', icon: 'Monitor' },
  { path: '/users', title: '用户管理', icon: 'User' },
  { path: '/companions', title: '陪玩管理', icon: 'Avatar' },
  { divider: true },
  { path: '/orders', title: '订单管理', icon: 'Tickets' },
  { path: '/work-orders', title: '工单管理', icon: 'Document' },
  { path: '/pending-messages', title: '待办消息', icon: 'Bell' },
  { divider: true },
  { path: '/faq-items', title: '知识库', icon: 'Guide', roles: ['SYS_ADMIN'] },
  { path: '/keywords', title: '关键词', icon: 'Key', roles: ['SYS_ADMIN'] },
  { path: '/replies', title: '回复话术', icon: 'ChatLineRound', roles: ['SYS_ADMIN'] },
  { path: '/platform-configs', title: '平台配置', icon: 'Setting', roles: ['SYS_ADMIN'] },
  { path: '/ai-config', title: 'AI配置', icon: 'Cpu', roles: ['SYS_ADMIN'] },
  { path: '/club-config', title: '俱乐部配置', icon: 'HomeFilled' },
  { path: '/game-config', title: '游戏配置', icon: 'VideoGame' },
  { path: '/service-items', title: '服务项目', icon: 'Service' }
]

const menuItems = computed(() => {
  if (!userInfo.value) return []
  return allMenus.filter(m => m.divider || !m.roles || m.roles.includes(userInfo.value.role))
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
 */
function preloadRoute(path) {
  if (preloadedRoutes.has(path)) return
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
  overflow-y: auto;
  overflow-x: hidden;
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
  color: #bfcbd9;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: color 0.3s;
}

.sidebar-toggle:hover {
  color: #409eff;
}

.sidebar-menu {
  border-right: none;
}

.menu-divider {
  margin: 12px 0;
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
