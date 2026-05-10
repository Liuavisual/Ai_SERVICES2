/**
 * LoadingOverlay 组件单元测试
 *
 * 测试加载遮罩组件的渲染、props、class切换等。
 * 验证 visible/fullscreen/transparent/text/iconSize 属性的行为。
 *
 * @author 刘建国
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingOverlay from '@/components/LoadingOverlay.vue'

describe('LoadingOverlay 组件', () => {
  // ============ visible 属性 ============
  describe('visible 属性', () => {
    it('visible=false 时不应渲染任何内容', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: false }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(false)
    })

    it('visible=true 时应渲染加载遮罩', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(true)
    })

    it('默认 visible 应为 false', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: undefined }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(false)
    })
  })

  // ============ fullscreen 属性 ============
  describe('fullscreen 属性', () => {
    it('fullscreen=true（默认）应添加 is-fullscreen 类', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true }
      })
      expect(wrapper.find('.loading-overlay.is-fullscreen').exists()).toBe(true)
    })

    it('fullscreen=false 不应添加 is-fullscreen 类', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, fullscreen: false }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(true)
      expect(wrapper.find('.loading-overlay.is-fullscreen').exists()).toBe(false)
    })
  })

  // ============ transparent 属性 ============
  describe('transparent 属性', () => {
    it('transparent=true 应添加 is-transparent 类', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, transparent: true }
      })
      expect(wrapper.find('.loading-overlay.is-transparent').exists()).toBe(true)
    })

    it('transparent=false（默认）不应添加 is-transparent 类', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true }
      })
      expect(wrapper.find('.loading-overlay.is-transparent').exists()).toBe(false)
    })
  })

  // ============ text 属性 ============
  describe('text 属性', () => {
    it('默认应显示"加载中..."文本', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true }
      })
      expect(wrapper.find('.loading-text').text()).toBe('加载中...')
    })

    it('自定义 text 应正确显示', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, text: '数据加载中，请稍候...' }
      })
      expect(wrapper.find('.loading-text').text()).toBe('数据加载中，请稍候...')
    })

    it('text 为空字符串时，不应渲染 text span', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, text: '' }
      })
      expect(wrapper.find('.loading-text').exists()).toBe(false)
    })
  })

  // ============ iconSize 属性 ============
  describe('iconSize 属性', () => {
    it('默认应渲染 overlay 容器', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(true)
      expect(wrapper.find('.loading-icon').exists()).toBe(true)
    })

    it('自定义 iconSize 不应导致渲染异常', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, iconSize: 64 }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(true)
    })
  })

  // ============ 组合场景 ============
  describe('组合场景', () => {
    it('全屏非透明模式应同时有 is-fullscreen 类且无 is-transparent 类', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, fullscreen: true, transparent: false }
      })
      expect(wrapper.find('.loading-overlay.is-fullscreen').exists()).toBe(true)
      expect(wrapper.find('.loading-overlay.is-transparent').exists()).toBe(false)
    })

    it('内嵌透明模式应无 is-fullscreen 类且有 is-transparent 类', () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: true, fullscreen: false, transparent: true }
      })
      expect(wrapper.find('.loading-overlay.is-fullscreen').exists()).toBe(false)
      expect(wrapper.find('.loading-overlay.is-transparent').exists()).toBe(true)
    })

    it('visible 切换时应正确显示/隐藏', async () => {
      const wrapper = mount(LoadingOverlay, {
        props: { visible: false }
      })
      expect(wrapper.find('.loading-overlay').exists()).toBe(false)

      await wrapper.setProps({ visible: true })
      expect(wrapper.find('.loading-overlay').exists()).toBe(true)

      await wrapper.setProps({ visible: false })
      expect(wrapper.find('.loading-overlay').exists()).toBe(false)
    })
  })
})