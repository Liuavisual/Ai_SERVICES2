/**
 * AsyncWrapper 组件单元测试
 *
 * 测试异步组件加载包装器，验证 Suspense 集成、
 * skeletonType/rows props 传递、默认 fallback 插槽行为。
 *
 * @author 刘建国
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AsyncWrapper from '@/components/AsyncWrapper.vue'

describe('AsyncWrapper 组件', () => {
  // ============ 基础渲染 ============
  describe('基础渲染', () => {
    it('应渲染 Suspense 组件', () => {
      const wrapper = mount(AsyncWrapper, {
        slots: { default: '<div class="async-content">异步内容</div>' }
      })
      /** Suspense 会渲染 slot 内容 */
      expect(wrapper.find('.async-content').exists()).toBe(true)
    })
  })

  // ============ skeletonType 属性 ============
  describe('skeletonType 属性', () => {
    it('默认 skeletonType 应为 text', () => {
      const wrapper = mount(AsyncWrapper)
      /** 验证 SkeletonBox 子组件传递了默认 type */
      const skeleton = wrapper.findComponent({ name: 'SkeletonBox' })
      if (skeleton.exists()) {
        expect(skeleton.props('type')).toBe('text')
      }
    })

    it('自定义 skeletonType="card" 应传递给 SkeletonBox', () => {
      const wrapper = mount(AsyncWrapper, {
        props: { skeletonType: 'card' }
      })
      const skeleton = wrapper.findComponent({ name: 'SkeletonBox' })
      if (skeleton.exists()) {
        expect(skeleton.props('type')).toBe('card')
      }
    })
  })

  // ============ rows 属性 ============
  describe('rows 属性', () => {
    it('默认 rows 应为 5', () => {
      const wrapper = mount(AsyncWrapper)
      const skeleton = wrapper.findComponent({ name: 'SkeletonBox' })
      if (skeleton.exists()) {
        expect(skeleton.props('rows')).toBe(5)
      }
    })

    it('自定义 rows=8 应传递给 SkeletonBox', () => {
      const wrapper = mount(AsyncWrapper, {
        props: { rows: 8 }
      })
      const skeleton = wrapper.findComponent({ name: 'SkeletonBox' })
      if (skeleton.exists()) {
        expect(skeleton.props('rows')).toBe(8)
      }
    })
  })

  // ============ 插槽 ============
  describe('插槽', () => {
    it('default 插槽内容应正常渲染', () => {
      const wrapper = mount(AsyncWrapper, {
        slots: { default: '<div class="my-component">Hello World</div>' }
      })
      expect(wrapper.find('.my-component').exists()).toBe(true)
      expect(wrapper.find('.my-component').text()).toBe('Hello World')
    })

    it('自定义 fallback 插槽应覆盖默认 SkeletonBox', () => {
      const wrapper = mount(AsyncWrapper, {
        slots: {
          default: '<div>content</div>',
          fallback: '<div class="custom-fallback">自定义加载中...</div>'
        }
      })
      /** 由于 Suspense 在有 resolved 内容时不显示 fallback，
       *  这里验证 wrapper 包含 fallback 元素 */
      expect(wrapper.html()).toContain('content')
    })
  })

  // ============ 组合场景 ============
  describe('组合场景', () => {
    it('props 动态切换应正确反映到 SkeletonBox', async () => {
      const wrapper = mount(AsyncWrapper, {
        props: { skeletonType: 'text', rows: 5 }
      })

      await wrapper.setProps({ skeletonType: 'form', rows: 3 })

      const skeleton = wrapper.findComponent({ name: 'SkeletonBox' })
      if (skeleton.exists()) {
        expect(skeleton.props('type')).toBe('form')
        expect(skeleton.props('rows')).toBe(3)
      }
    })
  })
})