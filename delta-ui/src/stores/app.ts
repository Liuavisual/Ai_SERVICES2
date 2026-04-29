/**
 * 应用全局状态管理 Store（TypeScript版）
 *
 * 管理侧边栏折叠状态、WebSocket连接状态、待处理数量等全局UI状态。
 *
 * @author 刘建国
 */
import { defineStore } from 'pinia'

/** 应用状态接口 */
interface AppState {
  /** 侧边栏是否折叠 */
  sidebarCollapsed: boolean
  /** WebSocket是否已连接 */
  wsConnected: boolean
  /** 待处理消息数量 */
  pendingCount: number
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebarCollapsed: false,
    wsConnected: false,
    pendingCount: 0
  }),

  actions: {
    /** 切换侧边栏折叠状态 */
    toggleSidebar() { this.sidebarCollapsed = !this.sidebarCollapsed },

    /**
     * 设置WebSocket连接状态
     * @param {boolean} val - 连接状态
     */
    setWsConnected(val: boolean) { this.wsConnected = val },

    /**
     * 设置待处理消息数量
     * @param {number} val - 待处理数量
     */
    setPendingCount(val: number) { this.pendingCount = val },

    /** 待处理数量自增1 */
    incrementPending() { this.pendingCount++ }
  }
})
