<template>
  <el-container class="app-container">
    <el-aside :width="sidebarWidth">
      <div class="sidebar">
        <div class="sidebar-header">
          <div class="logo-icon">
            <svg viewBox="0 0 28 28" fill="none" width="24" height="24">
              <rect x="2" y="2" width="24" height="24" rx="6" fill="#6366F1"/>
              <path d="M10 14h8M14 10v8" stroke="white" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <transition name="fade">
            <div v-show="!collapsed" class="logo-text">
              <span class="logo-name">Delta</span>
              <span class="logo-badge">Companion</span>
            </div>
          </transition>
        </div>
        <nav class="sidebar-nav">
          <template v-for="(menu, idx) in menuItems" :key="menu.path || idx">
            <div v-if="menu.divider" class="nav-divider">
              <span v-show="!collapsed" class="nav-divider-label">{{ menu.label }}</span>
            </div>
            <div
              v-else
              :class="['nav-item', { active: activeMenu === menu.path }]"
              @click="navigateTo(menu.path)"
              :title="collapsed ? menu.title : ''"
            >
              <div class="nav-icon-wrap">
                <el-icon :size="18"><component :is="menu.icon" /></el-icon>
              </div>
              <span class="nav-label" v-show="!collapsed">{{ menu.title }}</span>
              <el-badge
                v-if="menu.path === '/pending-messages' && !collapsed"
                :value="pendingCountDisplay"
                :hidden="pendingCount === 0"
                class="nav-badge"
              />
              <div v-if="activeMenu === menu.path" class="nav-active-indicator"></div>
            </div>
          </template>
        </nav>
        <div class="sidebar-footer" v-show="!collapsed">
          <div class="sidebar-version">v1.0.0</div>
        </div>
      </div>
    </el-aside>

    <el-container class="main-area">
      <header class="top-bar">
        <div class="top-left">
          <button class="icon-btn" @click="toggleCollapse" :title="collapsed ? '展开菜单' : '收起菜单'">
            <el-icon :size="18"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </button>
          <div class="breadcrumb">
            <span class="breadcrumb-current">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="top-right">
          <div :class="['ws-status', wsConnected ? 'connected' : 'disconnected']" :title="wsConnected ? 'WebSocket 已连接' : 'WebSocket 未连接'">
            <span class="ws-dot"></span>
            <span class="ws-text" v-show="wsConnected">Live</span>
          </div>
          <button class="icon-btn ws-toggle" @click="toggleWebSocket" title="切换WebSocket">
            <el-icon :size="16"><Connection /></el-icon>
          </button>
          <div class="user-chip" v-if="userInfo">
            <div class="user-avatar">{{ (userInfo.realName || '?')[0] }}</div>
            <div class="user-detail">
              <span class="user-name">{{ userInfo.realName }}</span>
              <span class="user-role">{{ userInfo.roleDesc || userInfo.role }}</span>
            </div>
          </div>
          <button class="logout-btn" @click="handleLogout">
            <el-icon :size="14"><SwitchButton /></el-icon>
            <span>退出</span>
          </button>
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
import { DataLine, Key, ChatDotRound, ChatLineRound, Message, Bell, Setting, Tools, User, UserFilled, Connection, Trophy, Timer, Guide, Shop, Fold, Expand, Avatar, Monitor, List, Present, Calendar, SwitchButton } from '@element-plus/icons-vue'
import { pendingMessageApi } from '@/api'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const collapsed = ref(false)
const sidebarWidth = computed(() => collapsed.value ? '72px' : '240px')
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
  { path: '/dashboard', title: '数据总览', icon: 'DataLine', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: 'd1', title: '', icon: '', roles: [], divider: true, label: '客户管理' },
  { path: '/customers', title: '客户名录', icon: 'User', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/customer-profiles', title: '客户画像', icon: 'Avatar', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/game-configs', title: '游戏配置', icon: 'Monitor', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/service-items', title: '服务项目', icon: 'List', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/activity-packages', title: '活动套餐', icon: 'Present', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/chat-test', title: '对话试炼', icon: 'ChatDotRound', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: 'd2', title: '', icon: '', roles: [], divider: true, label: '客服中心' },
  { path: '/club-config', title: '堂口配置', icon: 'Shop', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/faq-items', title: '知识库', icon: 'Guide', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/keywords', title: '关键词', icon: 'Key', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/replies', title: '回复话术', icon: 'ChatLineRound', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/messages', title: '消息记录', icon: 'Message', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: '/pending-messages', title: '待办事项', icon: 'Bell', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: 'd3', title: '', icon: '', roles: [], divider: true, label: '团队管理' },
  { path: '/sys-users', title: '人员管理', icon: 'User', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/cs-user-customer', title: '客户分配', icon: 'UserFilled', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: 'd4', title: '', icon: '', roles: [], divider: true, label: '陪玩服务' },
  { path: '/companion-levels', title: '陪玩等级', icon: 'Trophy', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/companions', title: '陪玩师', icon: 'Timer', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: '/companion-schedule', title: '排班管理', icon: 'Calendar', roles: ['SYS_ADMIN','CS_LEADER'] },
  { path: '/orders', title: '订单管理', icon: 'Shop', roles: ['SYS_ADMIN','CS_LEADER','CS_STAFF'] },
  { path: 'd5', title: '', icon: '', roles: [], divider: true, label: '系统设置' },
  { path: '/ai-config', title: 'AI配置', icon: 'Setting', roles: ['SYS_ADMIN'] },
  { path: '/platform-configs', title: '平台配置', icon: 'Tools', roles: ['SYS_ADMIN'] }
]

const menuItems = computed(() => {
  if (!userInfo.value) return []
  return allMenus.filter(m => m.divider || m.roles.includes(userInfo.value.role))
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
  } catch (e) {}
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
  background: var(--gu-bg-sidebar);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border-right: none;
}

.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid var(--gu-border-sidebar);
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  align-items: baseline;
  gap: 6px;
  white-space: nowrap;
  overflow: hidden;
}

.logo-name {
  font-family: var(--gu-font-heading);
  font-size: 18px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: -0.02em;
}

.logo-badge {
  font-family: var(--gu-font-heading);
  font-size: 10px;
  font-weight: 600;
  color: var(--gu-primary);
  background: rgba(99, 102, 241, 0.15);
  padding: 2px 6px;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}

.sidebar-nav::-webkit-scrollbar { width: 4px; }
.sidebar-nav::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }
.sidebar-nav::-webkit-scrollbar-track { background: transparent; }

.nav-divider {
  padding: 16px 20px 6px;
  display: flex;
  align-items: center;
}

.nav-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--gu-border-sidebar);
  margin-left: 8px;
}

.nav-divider-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--gu-text-sidebar-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 9px 12px;
  margin: 2px 10px;
  border-radius: var(--gu-radius-lg);
  cursor: pointer;
  color: var(--gu-text-sidebar);
  transition: all 0.2s ease;
  gap: 12px;
  position: relative;
}

.nav-item:hover {
  background: var(--gu-bg-sidebar-hover);
  color: #FFFFFF;
}

.nav-item.active {
  background: var(--gu-bg-sidebar-active);
  color: #FFFFFF;
  font-weight: 500;
}

.nav-active-indicator {
  position: absolute;
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--gu-primary);
  border-radius: 0 3px 3px 0;
}

.nav-icon-wrap {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-label {
  font-size: 13px;
  white-space: nowrap;
  flex: 1;
  line-height: 1;
}

.nav-badge { margin-left: auto; }

.sidebar-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--gu-border-sidebar);
}

.sidebar-version {
  font-size: 11px;
  color: var(--gu-text-sidebar-muted);
  font-family: var(--gu-font-mono);
}

.main-area {
  display: flex;
  flex-direction: column;
  background: var(--gu-bg);
}

.top-bar {
  height: 64px;
  background: var(--gu-bg-card);
  border-bottom: 1px solid var(--gu-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  box-shadow: var(--gu-shadow-sm);
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid var(--gu-border);
  border-radius: var(--gu-radius);
  color: var(--gu-text-secondary);
  cursor: pointer;
  transition: all var(--gu-transition);
}

.icon-btn:hover {
  background: var(--gu-primary-light);
  color: var(--gu-primary);
  border-color: var(--gu-primary);
}

.breadcrumb-current {
  font-family: var(--gu-font-heading);
  font-size: 16px;
  font-weight: 600;
  color: var(--gu-text-primary);
}

.top-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ws-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: var(--gu-radius-full);
  font-size: 11px;
  font-weight: 600;
  transition: all var(--gu-transition);
}

.ws-status.connected {
  background: var(--gu-success-light);
  color: var(--gu-success);
}

.ws-status.disconnected {
  background: var(--gu-bg-secondary);
  color: var(--gu-text-muted);
}

.ws-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  transition: all 0.3s;
}

.ws-status.connected .ws-dot {
  background: var(--gu-success);
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.4);
  animation: pulse 2s infinite;
}

.ws-status.disconnected .ws-dot {
  background: var(--gu-text-muted);
}

.ws-text {
  font-family: var(--gu-font-heading);
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.ws-toggle {
  width: 32px;
  height: 32px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  background: var(--gu-bg-stripe);
  border-radius: var(--gu-radius-full);
  border: 1px solid var(--gu-border);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gu-primary), var(--gu-secondary));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  font-family: var(--gu-font-heading);
}

.user-detail {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  font-size: 13px;
  color: var(--gu-text-primary);
  font-weight: 500;
}

.user-role {
  font-size: 10px;
  color: var(--gu-text-muted);
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: transparent;
  border: 1px solid var(--gu-border);
  border-radius: var(--gu-radius);
  color: var(--gu-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--gu-transition);
  font-weight: 500;
}

.logout-btn:hover {
  background: var(--gu-danger-light);
  border-color: var(--gu-danger);
  color: var(--gu-danger);
}

.content-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .content-main { padding: 12px !important; }
  .user-detail { display: none; }
  .ws-text { display: none; }
  .logout-btn span { display: none; }
}
</style>
