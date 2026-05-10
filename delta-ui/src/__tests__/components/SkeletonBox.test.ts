/**
 * SkeletonBox 组件单元测试
 *
 * 测试骨架屏占位组件的四种类型（text/card/table/form）的渲染、
 * rows/count/columns/animated/lastRowShorter 属性验证。
 *
 * @author 刘建国
 */
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SkeletonBox from '@/components/SkeletonBox.vue'

describe('SkeletonBox 组件', () => {
  // ============ type 属性 ============
  describe('type 属性', () => {
    it('默认 type 应为 text', () => {
      const wrapper = mount(SkeletonBox)
      expect(wrapper.find('.skeleton-text').exists()).toBe(true)
    })

    it('type="text" 应渲染文本骨架行', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'text', rows: 3 } })
      expect(wrapper.find('.skeleton-text').exists()).toBe(true)
      const lines = wrapper.findAll('.skeleton-line')
      expect(lines).toHaveLength(3)
    })

    it('type="card" 应渲染卡片骨架', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'card', count: 2 } })
      expect(wrapper.find('.skeleton-card').exists()).toBe(true)
      /** 使用更精确的选择器：skeleton-card 内部的 skeleton-card */
      const cards = wrapper.findAll('.skeleton-card .skeleton-card-body')
      expect(cards).toHaveLength(2)
    })

    it('type="table" 应渲染表格骨架', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'table', rows: 3, columns: 4 } })
      expect(wrapper.find('.skeleton-table-header').exists()).toBe(true)
      expect(wrapper.find('.skeleton-table-row').exists()).toBe(true)
      const rows = wrapper.findAll('.skeleton-table-row')
      expect(rows).toHaveLength(3)
    })

    it('type="form" 应渲染表单骨架', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'form', rows: 2 } })
      expect(wrapper.find('.skeleton-form-group').exists()).toBe(true)
      const groups = wrapper.findAll('.skeleton-form-group')
      expect(groups).toHaveLength(2)
    })

    it('无效 type 不应渲染对应骨架（validator阻止）', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'invalid' } })
      expect(wrapper.find('.skeleton-text').exists()).toBe(false)
      expect(wrapper.find('.skeleton-card').exists()).toBe(false)
      expect(wrapper.find('.skeleton-table-header').exists()).toBe(false)
      expect(wrapper.find('.skeleton-form-group').exists()).toBe(false)
    })
  })

  // ============ rows 属性 ============
  describe('rows 属性', () => {
    it('text 类型默认 rows 应为 5', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'text' } })
      expect(wrapper.findAll('.skeleton-line')).toHaveLength(5)
    })

    it('text 类型自定义 rows=3 应渲染 3 行', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'text', rows: 3 } })
      expect(wrapper.findAll('.skeleton-line')).toHaveLength(3)
    })

    it('table 类型 rows 控制表格行数', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'table', rows: 2, columns: 3 } })
      expect(wrapper.findAll('.skeleton-table-row')).toHaveLength(2)
    })

    it('form 类型 rows 控制表单项数', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'form', rows: 4 } })
      expect(wrapper.findAll('.skeleton-form-group')).toHaveLength(4)
    })
  })

  // ============ count 属性（卡片数量） ============
  describe('count 属性', () => {
    it('card 类型默认 count 应为 3', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'card' } })
      expect(wrapper.findAll('.skeleton-card .skeleton-card-body')).toHaveLength(3)
    })

    it('card 类型自定义 count=5 应渲染 5 张卡片', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'card', count: 5 } })
      expect(wrapper.findAll('.skeleton-card .skeleton-card-body')).toHaveLength(5)
    })
  })

  // ============ columns 属性（表格列数） ============
  describe('columns 属性', () => {
    it('table 类型默认 columns 应为 5', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'table' } })
      const headerCells = wrapper.find('.skeleton-table-header').findAll('.skeleton-cell')
      expect(headerCells).toHaveLength(5)
    })

    it('table 类型自定义 columns=3', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'table', columns: 3 } })
      const headerCells = wrapper.find('.skeleton-table-header').findAll('.skeleton-cell')
      expect(headerCells).toHaveLength(3)
    })
  })

  // ============ animated 属性 ============
  describe('animated 属性', () => {
    it('默认 animated=true 应包含 skeleton-animated 类', () => {
      const wrapper = mount(SkeletonBox)
      expect(wrapper.find('.skeleton-animated').exists()).toBe(true)
    })

    it('animated=false 不应包含 skeleton-animated 类', () => {
      const wrapper = mount(SkeletonBox, { props: { animated: false } })
      expect(wrapper.find('.skeleton-animated').exists()).toBe(false)
    })
  })

  // ============ lastRowShorter 属性 ============
  describe('lastRowShorter 属性', () => {
    it('默认 lastRowShorter=true 时末行应有 skeleton-line--short 类', () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'text', rows: 3 } })
      const lines = wrapper.findAll('.skeleton-line')
      expect(lines[2].classes()).toContain('skeleton-line--short')
    })

    it('lastRowShorter=false 时末行不应有 skeleton-line--short 类', () => {
      const wrapper = mount(SkeletonBox, {
        props: { type: 'text', rows: 3, lastRowShorter: false }
      })
      const lines = wrapper.findAll('.skeleton-line')
      expect(lines[2].classes()).not.toContain('skeleton-line--short')
    })
  })

  // ============ 组合场景 ============
  describe('组合场景', () => {
    it('type 动态切换应正确渲染', async () => {
      const wrapper = mount(SkeletonBox, { props: { type: 'text' } })
      expect(wrapper.find('.skeleton-text').exists()).toBe(true)

      await wrapper.setProps({ type: 'card' })
      expect(wrapper.find('.skeleton-card').exists()).toBe(true)
      expect(wrapper.find('.skeleton-text').exists()).toBe(false)

      await wrapper.setProps({ type: 'form' })
      expect(wrapper.find('.skeleton-form-group').exists()).toBe(true)
      expect(wrapper.find('.skeleton-card').exists()).toBe(false)
    })

    it('animated 动态切换应正确添加/移除类名', async () => {
      const wrapper = mount(SkeletonBox, { props: { animated: false } })
      expect(wrapper.find('.skeleton-animated').exists()).toBe(false)

      await wrapper.setProps({ animated: true })
      expect(wrapper.find('.skeleton-animated').exists()).toBe(true)
    })
  })
})