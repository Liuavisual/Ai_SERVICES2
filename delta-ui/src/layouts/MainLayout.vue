<!--
  主布局组件，包含侧边栏导航和内容区域，古风主题设计

  @author delta
-->
<template>
  <el-container class="app-container">
    <el-aside :width="sidebarWidth">
      <div class="sidebar">
        <div class="sidebar-header">
          <span class="logo-mark">Δ</span>
          <span class="logo-text" v-show="!collapsed">三角洲</span>
        </div>
        <nav class="sidebar-nav">
          <template v-for="menu in menuItems" :key="menu.path">
            <div v-if="menu.divider" class="nav-divider"></div>
            <div
              v-else
              :class="['nav-item', { active: activeMenu === menu.path }]"
              @click="navigateTo(menu.path)"
            >
              <el-icon :size="17"><component :is="menu.icon" /></el-icon>
              <span class="nav-label" v-show="!collapsed">{{ menu.title }}</span>
              <el-badge
                v-if="menu.path === '/pending-messages' && !collapsed"
                :value="pendingCountDisplay"
                :hidden="pendingCount === 0"
                class="nav-badge"
              />
            </div>
          </template>
        </nav>
      </div>
    </el-aside>

    <el-container class="main-area">
      <header class="top-bar">
        <div class="top-left">
          <button class="icon-btn" @click="toggleCollapse">
            <el-icon :size="16"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </button>
          <h2 class="page-title">{{ currentTitle }}</h2>
        </div>
        <div class="top-right">
          <div v-if="userInfo" class="user-info">
            <span class="user-name">{{ userInfo.realName }}</span>
            <span class="user-role">{{ userInfo.roleDesc || userInfo.role }}</span>
          </div>
          <div :class="['ws-dot-wrap', wsConnected ? 'on' : 'off']">
            <span class="dot"></span>
          </div>
          <button class="icon-btn ws-btn" @click="toggleWebSocket" title="WebSocket">
            <el-icon :size="14"><Connection /></el-icon>
          </button>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </header>

      <main class="content-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { DataLine, Key, ChatDotRound, ChatLineRound, Message, Bell, Setting, Tools, User, UserFilled, Connection, Trophy, Timer, Guide, Shop, Fold, Expand, Avatar, Monitor, List, Present, Calendar } from '@element-plus/icons-vue'
import { pendingMessageApi } from '@/api'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const collapsed = ref(false)
const sidebarWidth = computed(() => collapsed.value ? '60px' : '220px')
const currentTitle = computed(() => {
  const m = allMenus.find(item => item.path === route.path)
  return m?.title || '数据总览'
})
const wsConnected = ref(false)
const pendingCount = ref(0)
const userInfo = ref(null)
let ws = null
let refreshTimer = null

const pendingCountDisplay = computed(() => pendingCount.value > 99 ? '99+' : pendingCount.value)

const refreshPendingCount = async () => {
  try {
    const res = await pendingMessageApi.getCount()
    if (res.code === 200) pendingCount.value = res.data || 0
  } catch (e) {}
}
provide('refreshPendingCount', refreshPendingCount)

const allMenus = [
  { path: '/dashboard', title: '数据总览', icon: 'DataLine', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'], divider: false },
  { path: 'd1', title: '', icon: '', roles: [], divider: true },
  { path: '/customers', title: '客户名录', icon: 'User', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/customer-profiles', title: '客户画像', icon: 'Avatar', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/game-configs', title: '游戏配置', icon: 'Monitor', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/service-items', title: '服务项目', icon: 'List', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/activity-packages', title: '活动套餐', icon: 'Present', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/chat-test', title: '对话试炼', icon: 'ChatDotRound', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: 'd2', title: '', icon: '', roles: [], divider: true },
  { path: '/club-config', title: '堂口配置', icon: 'Shop', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/faq-items', title: '知识库', icon: 'Guide', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/keywords', title: '关键词', icon: 'Key', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/replies', title: '回复话术', icon: 'ChatLineRound', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/messages', title: '消息记录', icon: 'Message', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: '/pending-messages', title: '待办事项', icon: 'Bell', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: 'd3', title: '', icon: '', roles: [], divider: true },
  { path: '/sys-users', title: '人员管理', icon: 'User', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/cs-user-customer', title: '客户分配', icon: 'UserFilled', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: 'd4', title: '', icon: '', roles: [], divider: true },
  { path: '/companion-levels', title: '陪玩等级', icon: 'Trophy', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/companions', title: '陪玩师', icon: 'Timer', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: '/companion-schedule', title: '排班管理', icon: 'Calendar', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: 'd5', title: '', icon: '', roles: [], divider: true },
  { path: '/ai-config', title: 'AI配置', icon: 'Setting', roles: ['SYS_ADMIN'] },
  { path: '/platform-configs', title: '平台配置', icon: 'Tools', roles: ['SYS_ADMIN'] }
]

const menuItems = computed(() => {
  if (!userInfo.value) return []
  return allMenus.filter(m => !m.divider && m.roles.includes(userInfo.value.role))
})

const toggleCollapse = () => { collapsed.value = !collapsed.value }
const navigateTo = (path) => router.push(path)

const getUserInfo = () => {
  const info = localStorage.getItem('userInfo')
  if (info) userInfo.value = JSON.parse(info)
}
const handleLogout = async () => {
  try {
    const { authApi } = await import('@/api')
    await authApi.logout()
  } catch (e) { /* ignore */ }
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('expiresIn')
  localStorage.removeItem('tokenExpiry')
  localStorage.removeItem('userInfo')
  disconnectWebSocket()
  router.push('/login')
}

const getWsUrl = () => {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.hostname
  const port = import.meta.env.DEV ? '8080' : window.location.port
  const token = localStorage.getItem('token')
  return `${proto}//${host}:${port}/api/ws/notify?token=${encodeURIComponent(token || '')}`
}

const connectWebSocket = () => {
  const token = localStorage.getItem('token')
  if (!token) return
  ws = new WebSocket(getWsUrl())
  ws.onopen = () => { wsConnected.value = true; ElNotification({ title: '连接成功', type: 'success', duration: 2000 }) }
  ws.onmessage = (event) => {
    try {
      const n = JSON.parse(event.data)
      if (n.type === 'pending_message') { pendingCount.value++; ElNotification({ title: '新待办', message: `${n.userNickname} 需要人工介入`, type: 'warning', duration: 0 }) }
    } catch (e) {}
  }
  ws.onclose = () => { wsConnected.value = false }
  ws.onerror = () => { wsConnected.value = false; ElNotification({ title: '连接失败', type: 'error', duration: 2000 }) }
}

const disconnectWebSocket = () => { if (ws) { ws.close(); ws = null }; wsConnected.value = false }
const toggleWebSocket = () => wsConnected.value ? disconnectWebSocket() : connectWebSocket()

onMounted(() => { getUserInfo(); refreshPendingCount(); refreshTimer = setInterval(refreshPendingCount, 30000) })
onUnmounted(() => { disconnectWebSocket(); if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped>
.app-container { height: 100vh; background: var(--gu-bg); }

.el-aside {
  background: var(--gu-bg-secondary);
  border-right: 1px solid var(--gu-border);
  transition: width 0.2s ease;
  overflow: hidden;
}

.sidebar { height: 100%; display: flex; flex-direction: column; }

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  border-bottom: 1px solid var(--gu-border);
  flex-shrink: 0;
}

.logo-mark {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700;
  color: var(--gu-accent); border: 1.5px solid var(--gu-accent);
  border-radius: 4px; flex-shrink: 0;
}

.logo-text { font-size: 15px; font-weight: 700; color: var(--gu-text-primary); letter-spacing: 3px; white-space: nowrap; }

.sidebar-nav { flex: 1; overflow-y: auto; padding: 8px 0; }

.nav-divider { height: 1px; background: var(--gu-border); margin: 10px 14px; }

.nav-item {
  display: flex; align-items: center;
  padding: 10px 16px; margin: 2px 8px;
  border-radius: 4px; cursor: pointer;
  color: var(--gu-text-secondary); transition: all 0.15s ease; gap: 10px;
  position: relative;
}

.nav-item:hover { background: var(--gu-accent-light); color: var(--gu-text-primary); }

.nav-item.active {
  background: var(--gu-accent-light); color: var(--gu-accent);
  font-weight: 600;
}

.nav-item.active::before {
  content: '';
  position: absolute; left: 0;
  width: 3px; height: 22px;
  background: var(--gu-accent);
  border-radius: 0 2px 2px 0;
}

.nav-label { font-size: 13px; white-space: nowrap; flex: 1; }
.nav-badge { margin-left: auto; }

.main-area { display: flex; flex-direction: column; background: var(--gu-bg); }

.top-bar {
  height: 56px;
  background: var(--gu-bg-card);
  border-bottom: 1px solid var(--gu-border);
  display: flex; align-items: center;
  justify-content: space-between;
  padding: 0 20px; flex-shrink: 0;
}

.top-left { display: flex; align-items: center; gap: 12px; }

.icon-btn {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: transparent; border: 1px solid var(--gu-border);
  border-radius: 4px; color: var(--gu-text-secondary); cursor: pointer;
  transition: all 0.15s ease;
}
.icon-btn:hover { background: var(--gu-accent-light); color: var(--gu-accent); border-color: var(--gu-border-light); }

.page-title { font-size: 16px; font-weight: 600; color: var(--gu-text-primary); letter-spacing: 1px; }

.top-right { display: flex; align-items: center; gap: 14px; }

.user-info { display: flex; align-items: center; gap: 8px; }
.user-name { font-size: 13px; color: var(--gu-text-primary); font-weight: 500; }
.user-role {
  font-size: 11px; color: var(--gu-text-muted);
  background: var(--gu-bg-secondary); padding: 2px 8px; border-radius: 3px;
  border: 1px solid var(--gu-border);
}

.ws-dot-wrap { display: flex; align-items: center; }
.dot { width: 7px; height: 7px; border-radius: 50%; transition: all 0.3s; }
.ws-dot-wrap.on .dot { background: var(--gu-success); box-shadow: 0 0 5px rgba(90,138,90,0.4); }
.ws-dot-wrap.off .dot { background: var(--gu-border); }

.logout-btn {
  padding: 4px 14px;
  background: transparent;
  border: 1px solid rgba(166,61,64,0.25);
  border-radius: 4px;
  color: var(--gu-danger); font-size: 12px; cursor: pointer;
  transition: all 0.15s ease;
}
.logout-btn:hover { background: rgba(166,61,64,0.06); border-color: rgba(166,61,64,0.45); }

.content-main { flex: 1; overflow-y: auto; padding: 20px; }
</style>
