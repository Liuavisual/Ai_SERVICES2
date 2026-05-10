/**
 * App Store 扩展测试
 *
 * 补充测试 App Store 的边界场景：连续切换、多次增量、值为0时增量、
 * 大量数值、Pinia 实例隔离性、$subscribe 等。
 *
 * @author 刘建国
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAppStore } from '@/stores/app'

describe('App Store 扩展测试', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  // ============ toggleSidebar 边界 ============
  describe('toggleSidebar 边界', () => {
    it('连续切换应正确翻转', () => {
      const store = useAppStore()

      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(true)

      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(false)

      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(true)

      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(false)
    })

    it('从 true 切换应变为 false', () => {
      const store = useAppStore()
      store.toggleSidebar() // true
      store.toggleSidebar() // false
      expect(store.sidebarCollapsed).toBe(false)
    })
  })

  // ============ setWsConnected 边界 ============
  describe('setWsConnected 边界', () => {
    it('连续设置为相同值不应报错', () => {
      const store = useAppStore()

      store.setWsConnected(true)
      expect(store.wsConnected).toBe(true)

      store.setWsConnected(true)
      expect(store.wsConnected).toBe(true)

      store.setWsConnected(true)
      expect(store.wsConnected).toBe(true)
    })

    it('快速切换应正确更新', () => {
      const store = useAppStore()

      store.setWsConnected(true)
      store.setWsConnected(false)
      store.setWsConnected(true)

      expect(store.wsConnected).toBe(true)
    })
  })

  // ============ setPendingCount 边界 ============
  describe('setPendingCount 边界', () => {
    it('设置为 0', () => {
      const store = useAppStore()
      store.setPendingCount(5)
      store.setPendingCount(0)
      expect(store.pendingCount).toBe(0)
    })

    it('设置为负数', () => {
      const store = useAppStore()
      store.setPendingCount(-1)
      expect(store.pendingCount).toBe(-1)
    })

    it('设置为极大值', () => {
      const store = useAppStore()
      store.setPendingCount(999999)
      expect(store.pendingCount).toBe(999999)
    })
  })

  // ============ incrementPending 边界 ============
  describe('incrementPending 边界', () => {
    it('从 0 开始多次自增', () => {
      const store = useAppStore()
      for (let i = 0; i < 10; i++) {
        store.incrementPending()
      }
      expect(store.pendingCount).toBe(10)
    })

    it('从负数开始自增', () => {
      const store = useAppStore()
      store.setPendingCount(-5)
      store.incrementPending()
      expect(store.pendingCount).toBe(-4)
    })
  })

  // ============ Pinia 实例隔离 ============
  describe('Pinia 实例隔离', () => {
    it('不同 Pinia 实例的 store 应相互隔离', () => {
      const pinia1 = createPinia()
      setActivePinia(pinia1)
      const store1 = useAppStore()
      store1.toggleSidebar()
      expect(store1.sidebarCollapsed).toBe(true)

      const pinia2 = createPinia()
      setActivePinia(pinia2)
      const store2 = useAppStore()
      expect(store2.sidebarCollapsed).toBe(false)
    })
  })

  // ============ 状态快照验证 ============
  describe('状态快照验证', () => {
    it('初始状态应与定义一致', () => {
      const store = useAppStore()
      expect(store.$state).toEqual({
        sidebarCollapsed: false,
        wsConnected: false,
        pendingCount: 0
      })
    })

    it('$patch 应正确更新多个状态', () => {
      const store = useAppStore()
      store.$patch({
        sidebarCollapsed: true,
        pendingCount: 5
      })
      expect(store.sidebarCollapsed).toBe(true)
      expect(store.pendingCount).toBe(5)
      /** 未修改的字段保持原值 */
      expect(store.wsConnected).toBe(false)
    })

    it('$reset 应重置所有状态到初始值', () => {
      const store = useAppStore()
      store.toggleSidebar()
      store.setPendingCount(10)
      store.setWsConnected(true)

      store.$reset()

      expect(store.sidebarCollapsed).toBe(false)
      expect(store.pendingCount).toBe(0)
      expect(store.wsConnected).toBe(false)
    })
  })
})