/**
 * 应用全局状态管理 Store
 *
 * 管理侧边栏折叠状态、WebSocket连接状态、待处理数量等全局UI状态。
 *
 * @author 刘建国
 */
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    /** 侧边栏是否折叠 */
    sidebarCollapsed: false,
    /** WebSocket是否已连接 */
    wsConnected: false,
    /** 待处理消息数量 */
    pendingCount: 0
  }),

  actions: {
    /** 切换侧边栏折叠状态 */
    toggleSidebar() { this.sidebarCollapsed = !this.sidebarCollapsed },

    /**
     * 设置WebSocket连接状态
     * @param {boolean} val - 连接状态
     */
    setWsConnected(val) { this.wsConnected = val },

    /**
     * 设置待处理消息数量
     * @param {number} val - 待处理数量
     */
    setPendingCount(val) { this.pendingCount = val },

    /** 待处理数量自增1 */
    incrementPending() { this.pendingCount++ }
  }
})
