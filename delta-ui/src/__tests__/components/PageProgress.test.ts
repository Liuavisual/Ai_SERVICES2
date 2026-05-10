/**
 * PageProgress 组件单元测试
 *
 * 测试页面加载进度条组件的 start/done 方法、active 状态、
 * percent 进度值变化逻辑。使用 fake timers 控制定时器。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import PageProgress from '@/components/PageProgress.vue'

describe('PageProgress 组件', () => {
  /** 使用 fake timers 控制 setInterval/setTimeout */
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  /** 辅助函数：获取组件暴露的方法 */
  function getVM(wrapper: ReturnType<typeof mount>) {
    return wrapper.vm as unknown as { start: () => void; done: () => void }
  }

  // ============ 初始状态 ============
  describe('初始状态', () => {
    it('初始时不应激活（is-active 类不存在）', () => {
      const wrapper = mount(PageProgress)
      expect(wrapper.find('.is-active').exists()).toBe(false)
    })

    it('初始 percent 应为 0', () => {
      const wrapper = mount(PageProgress)
      const bar = wrapper.find('.page-progress-bar')
      expect(bar.attributes('style')).toContain('width: 0%')
    })
  })

  // ============ start 方法 ============
  describe('start 方法', () => {
    it('调用 start 后应激活进度条', async () => {
      const wrapper = mount(PageProgress)
      getVM(wrapper).start()
      await nextTick()
      expect(wrapper.find('.is-active').exists()).toBe(true)
    })

    it('start 后首次 interval 推进进度到 5%', async () => {
      const wrapper = mount(PageProgress)
      getVM(wrapper).start()
      await nextTick()

      /** 推进第一次 interval (100ms) */
      vi.advanceTimersByTime(100)
      await nextTick()

      const bar = wrapper.find('.page-progress-bar')
      expect(bar.attributes('style')).toContain('width: 5%')
    })

    it('start 后进度应逐步增长到 92% 左右后停止', async () => {
      const wrapper = mount(PageProgress)
      getVM(wrapper).start()
      await nextTick()

      /** 推进足够多的 interval（100次 = 10秒） */
      vi.advanceTimersByTime(100 * 100)
      await nextTick()

      const bar = wrapper.find('.page-progress-bar')
      const style = bar.attributes('style')
      /** 进度应在 50%-95% 之间（start中step增长到30+会到80+，最终停在92） */
      expect(style).toMatch(/width:\s*(9[0-5]|[5-8]\d)%/)
    })

    it('多次调用 start 应重置进度', async () => {
      const wrapper = mount(PageProgress)

      getVM(wrapper).start()
      await nextTick()
      vi.advanceTimersByTime(500)
      await nextTick()

      /** 再次调用 start */
      getVM(wrapper).start()
      await nextTick()
      /** 第一次 interval 后应为 5% */
      vi.advanceTimersByTime(100)
      await nextTick()

      const bar = wrapper.find('.page-progress-bar')
      expect(bar.attributes('style')).toContain('width: 5%')
    })
  })

  // ============ done 方法 ============
  describe('done 方法', () => {
    it('调用 done 后进度应到 100%', async () => {
      const wrapper = mount(PageProgress)

      getVM(wrapper).start()
      await nextTick()
      vi.advanceTimersByTime(200)
      await nextTick()

      getVM(wrapper).done()
      await nextTick()

      const bar = wrapper.find('.page-progress-bar')
      expect(bar.attributes('style')).toContain('width: 100%')
    })

    it('done 后进度应为 100% 且 duration 应为 200ms', async () => {
      const wrapper = mount(PageProgress)

      getVM(wrapper).start()
      await nextTick()
      vi.advanceTimersByTime(200)
      await nextTick()

      getVM(wrapper).done()
      await nextTick()

      const bar = wrapper.find('.page-progress-bar')
      expect(bar.attributes('style')).toContain('width: 100%')
      expect(bar.attributes('style')).toContain('transition-duration: 200ms')
    })
  })

  // ============ 完整生命周期 ============
  describe('完整生命周期', () => {
    it('start 后 active 变为 true', async () => {
      const wrapper = mount(PageProgress)

      expect(wrapper.find('.is-active').exists()).toBe(false)

      getVM(wrapper).start()
      await nextTick()

      expect(wrapper.find('.is-active').exists()).toBe(true)
    })

    it('start 后进度从 0% 开始增长', async () => {
      const wrapper = mount(PageProgress)

      const bar = wrapper.find('.page-progress-bar')
      expect(bar.attributes('style')).toContain('width: 0%')

      getVM(wrapper).start()
      await nextTick()
      vi.advanceTimersByTime(100)
      await nextTick()

      expect(bar.attributes('style')).toContain('width: 5%')
    })
  })
})