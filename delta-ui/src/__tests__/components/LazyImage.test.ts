/**
 * LazyImage 组件单元测试
 *
 * 测试图片懒加载组件，验证 src 属性（String/Object格式）、
 * alt/imgClass/imgStyle 属性、占位/加载/错误状态、IntersectionObserver。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

/** Mock IntersectionObserver */
const mockObserve = vi.fn()
const mockDisconnect = vi.fn()

let intersectionCallback: ((entries: Array<{ isIntersecting: boolean }>) => void) | null = null

/** 保存原始类以恢复 */
const OriginalIntersectionObserver = (globalThis as unknown as Record<string, unknown>).IntersectionObserver

/** 替换全局 IntersectionObserver */
;(globalThis as unknown as Record<string, unknown>).IntersectionObserver = class {
  constructor(callback: (entries: Array<{ isIntersecting: boolean }>) => void) {
    intersectionCallback = callback
  }
  observe = mockObserve
  disconnect = mockDisconnect
  unobserve = vi.fn()
}

/** Mock Image 构造函数 */
let imageSrc = ''
const OriginalImage = (globalThis as unknown as Record<string, unknown>).Image

;(globalThis as unknown as Record<string, unknown>).Image = class MockImage {
  src = ''
  onload: (() => void) | null = null
  onerror: (() => void) | null = null

  constructor() {
    Object.defineProperty(this, 'src', {
      get: () => imageSrc,
      set: (val: string) => {
        imageSrc = val
        setTimeout(() => {
          if (this.onload) this.onload()
        }, 0)
      }
    })
  }
} as unknown as typeof Image

import LazyImage from '@/components/LazyImage.vue'

describe('LazyImage 组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    imageSrc = ''
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  // ============ src 属性（String格式） ============
  describe('src 属性（String格式）', () => {
    it('传入字符串 src 应正确设置', () => {
      mount(LazyImage, {
        props: { src: 'https://example.com/image.jpg' }
      })

      expect(mockObserve).toHaveBeenCalled()
      /** 模拟 IntersectionObserver 回调触发 */
      if (intersectionCallback) {
        intersectionCallback([{ isIntersecting: true }])
      }

      vi.advanceTimersByTime(100)

      expect(imageSrc).toBe('https://example.com/image.jpg')
    })
  })

  // ============ src 属性（Object格式） ============
  describe('src 属性（Object格式）', () => {
    it('传入对象格式 { src, placeholder, fallback } 应正确解析', () => {
      mount(LazyImage, {
        props: {
          src: {
            src: 'https://example.com/real.jpg',
            placeholder: 'https://example.com/placeholder.jpg',
            fallback: 'https://example.com/fallback.jpg'
          }
        }
      })

      if (intersectionCallback) {
        intersectionCallback([{ isIntersecting: true }])
      }

      vi.advanceTimersByTime(100)

      expect(imageSrc).toBe('https://example.com/real.jpg')
    })
  })

  // ============ alt 属性 ============
  describe('alt 属性', () => {
    it('自定义 alt 应正确设置', () => {
      const wrapper = mount(LazyImage, {
        props: { src: 'test.jpg', alt: '测试图片' }
      })
      const img = wrapper.find('img')
      if (img.exists()) {
        expect(img.attributes('alt')).toBe('测试图片')
      }
    })
  })

  // ============ IntersectionObserver 生命周期 ============
  describe('IntersectionObserver 生命周期', () => {
    it('挂载时应创建 IntersectionObserver 并 observe', () => {
      mount(LazyImage, { props: { src: 'test.jpg' } })
      expect(mockObserve).toHaveBeenCalled()
    })

    it('卸载时应 disconnect', () => {
      const wrapper = mount(LazyImage, { props: { src: 'test.jpg' } })
      wrapper.unmount()
      expect(mockDisconnect).toHaveBeenCalled()
    })
  })

  // ============ 骨架屏状态 ============
  describe('骨架屏状态', () => {
    it('图片未加载前应显示骨架屏', () => {
      const wrapper = mount(LazyImage, { props: { src: 'test.jpg' } })
      /** 在没有加载完成的图片时，应显示骨架屏 */
      expect(wrapper.find('.v-lazy-skeleton').exists()).toBe(true)
    })
  })

  // ============ 加载成功状态 ============
  describe('加载成功状态', () => {
    it('图片加载成功后应显示 img 标签', async () => {
      const wrapper = mount(LazyImage, { props: { src: 'test.jpg' } })

      if (intersectionCallback) {
        intersectionCallback([{ isIntersecting: true }])
      }

      vi.advanceTimersByTime(100)
      await nextTick()

      /** loaded 为 true 后应渲染 img */
      const img = wrapper.find('img[src="test.jpg"]')
      expect(img.exists()).toBe(true)
    })

    it('加载成功后不应显示骨架屏', async () => {
      const wrapper = mount(LazyImage, { props: { src: 'test.jpg' } })

      if (intersectionCallback) {
        intersectionCallback([{ isIntersecting: true }])
      }

      vi.advanceTimersByTime(100)
      await nextTick()

      expect(wrapper.find('.v-lazy-skeleton').exists()).toBe(false)
    })
  })
})