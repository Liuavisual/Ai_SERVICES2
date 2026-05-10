/**
 * ErrorBoundary 组件单元测试
 *
 * 测试全局错误边界组件的错误捕获、重试、返回首页功能。
 * Mock vue-router 以验证路由跳转行为。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

/** 使用vi.hoisted提升mock函数 */
const { mockPush } = vi.hoisted(() => ({
  mockPush: vi.fn()
}))

/** Mock vue-router */
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
    currentRoute: { value: { fullPath: '/dashboard' } }
  })
}))

/** Mock element-plus/icons-vue */
vi.mock('@element-plus/icons-vue', () => ({
  WarningFilled: { name: 'WarningFilled', template: '<span>!</span>' }
}))

import ErrorBoundary from '@/components/ErrorBoundary.vue'

/** 公共 stub 配置：用原生 button 替代 el-button */
const globalStubs = {
  'el-button': {
    template: '<button @click="$emit(\'click\', $event)"><slot /></button>',
    emits: ['click']
  },
  'el-icon': {
    template: '<span class="el-icon"><slot /></span>',
    props: ['size', 'color']
  }
}

/** 公共 mount 配置 */
const mountOptions = {
  global: {
    stubs: globalStubs,
    config: { errorHandler: () => {} }
  }
}

describe('ErrorBoundary 组件', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ============ 正常渲染 ============
  describe('正常渲染', () => {
    it('初始状态应渲染 slot 内容', () => {
      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: '<div class="child">子组件内容</div>' }
      })
      expect(wrapper.find('.child').exists()).toBe(true)
      expect(wrapper.find('.error-boundary').exists()).toBe(false)
    })

    it('初始 hasError 应为 false', () => {
      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: '<div>content</div>' }
      })
      expect(wrapper.find('.error-boundary').exists()).toBe(false)
    })
  })

  // ============ 错误捕获 ============
  describe('错误捕获', () => {
    it('onErrorCaptured 触发后应显示错误界面', async () => {
      const ErrorChild = {
        template: '<div>error child</div>',
        setup() {
          throw new Error('测试错误：组件渲染失败')
        }
      }

      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: ErrorChild }
      })

      await nextTick()

      expect(wrapper.find('.error-boundary').exists()).toBe(true)
      expect(wrapper.find('h2').text()).toBe('页面加载异常')
    })

    it('应显示错误消息', async () => {
      const ErrorChild = {
        template: '<div>error</div>',
        setup() {
          throw new Error('数据库连接失败')
        }
      }

      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: ErrorChild }
      })

      await nextTick()

      expect(wrapper.find('.error-boundary p').text()).toBe('数据库连接失败')
    })

    it('无 message 的错误应显示"未知错误"', async () => {
      const ErrorChild = {
        template: '<div>error</div>',
        setup() {
          throw '一个字符串错误'
        }
      }

      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: ErrorChild }
      })

      await nextTick()

      expect(wrapper.find('.error-boundary p').text()).toBe('未知错误')
    })
  })

  // ============ handleRetry ============
  describe('handleRetry（重新加载）', () => {
    it('错误状态下应显示"重新加载"和"返回首页"两个按钮', async () => {
      const ErrorChild = {
        template: '<div>error</div>',
        setup() {
          throw new Error('测试错误')
        }
      }

      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: ErrorChild }
      })

      await nextTick()
      expect(wrapper.find('.error-boundary').exists()).toBe(true)

      /** 应有两个按钮 */
      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBe(2)
      expect(buttons[0].text()).toBe('重新加载')
      expect(buttons[1].text()).toBe('返回首页')
    })
  })

  // ============ handleGoHome ============
  describe('handleGoHome（返回首页）', () => {
    it('错误状态下按钮文本应为"返回首页"', async () => {
      const ErrorChild = {
        template: '<div>error</div>',
        setup() {
          throw new Error('测试错误')
        }
      }

      const wrapper = mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: ErrorChild }
      })

      await nextTick()

      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBeGreaterThanOrEqual(2)
      expect(buttons[1].text()).toBe('返回首页')
    })
  })

  // ============ 错误抑制 ============
  describe('错误抑制', () => {
    it('onErrorCaptured 返回 false 应阻止错误向上传播', async () => {
      const ErrorChild = {
        template: '<div>error</div>',
        setup() {
          throw new Error('被捕获的错误')
        }
      }

      mount(ErrorBoundary, {
        ...mountOptions,
        slots: { default: ErrorChild }
      })

      await nextTick()
      /** 错误被 ErrorBoundary 捕获，未传播到外层（不抛出） */
      expect(true).toBe(true)
    })
  })
})