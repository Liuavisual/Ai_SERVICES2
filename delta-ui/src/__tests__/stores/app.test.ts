import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAppStore } from '@/stores/app'

describe('App Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('initial state', () => {
    const store = useAppStore()
    expect(store.sidebarCollapsed).toBe(false)
    expect(store.wsConnected).toBe(false)
    expect(store.pendingCount).toBe(0)
  })

  it('toggleSidebar', () => {
    const store = useAppStore()
    store.toggleSidebar()
    expect(store.sidebarCollapsed).toBe(true)
    store.toggleSidebar()
    expect(store.sidebarCollapsed).toBe(false)
  })

  it('setWsConnected', () => {
    const store = useAppStore()
    store.setWsConnected(true)
    expect(store.wsConnected).toBe(true)
    store.setWsConnected(false)
    expect(store.wsConnected).toBe(false)
  })

  it('setPendingCount', () => {
    const store = useAppStore()
    store.setPendingCount(5)
    expect(store.pendingCount).toBe(5)
  })

  it('incrementPending', () => {
    const store = useAppStore()
    store.setPendingCount(0)
    store.incrementPending()
    expect(store.pendingCount).toBe(1)
    store.incrementPending()
    expect(store.pendingCount).toBe(2)
  })
})
