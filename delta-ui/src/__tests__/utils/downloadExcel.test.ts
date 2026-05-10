/**
 * downloadExcel 工具函数单元测试
 *
 * 测试 Excel 下载工具函数，验证 request 调用参数、
 * Blob 处理、DOM 操作（createObjectURL/createElement/click）。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

/** 使用vi.hoisted提升mock函数 */
const { mockRequest } = vi.hoisted(() => ({
  mockRequest: vi.fn()
}))

/** Mock request模块 */
vi.mock('@/utils/request', () => ({
  default: mockRequest
}))

/** Mock DOM API */
const mockCreateObjectURL = vi.fn(() => 'blob:mock-url')
const mockRevokeObjectURL = vi.fn()
const mockClick = vi.fn()

/** 保存原始 DOM 方法 */
const originalCreateObjectURL = URL.createObjectURL
const originalRevokeObjectURL = URL.revokeObjectURL
const originalCreateElement = document.createElement

import downloadExcel from '@/utils/downloadExcel'

describe('downloadExcel 工具', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    /** 设置 URL mock */
    URL.createObjectURL = mockCreateObjectURL
    URL.revokeObjectURL = mockRevokeObjectURL

    /** 设置 document.createElement mock */
    document.createElement = vi.fn((tag: string) => {
      if (tag === 'a') {
        return {
          href: '',
          download: '',
          click: mockClick
        } as unknown as HTMLElement
      }
      return originalCreateElement.call(document, tag)
    })
  })

  // ============ 基本调用 ============
  describe('基本调用', () => {
    it('应发送 GET 请求到正确的 URL', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/api/export/orders', {}, 'orders.xlsx')

      expect(mockRequest).toHaveBeenCalledWith({
        url: '/api/export/orders',
        method: 'get',
        params: {},
        responseType: 'blob'
      })
    })

    it('应传递查询参数', async () => {
      const mockBlob = new Blob(['test'])
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/api/export', { status: 'active', date: '2024-01-01' }, 'export.xlsx')

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({
          params: { status: 'active', date: '2024-01-01' }
        })
      )
    })

    it('应使用 responseType: blob', async () => {
      const mockBlob = new Blob(['test'])
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/export', {}, 'file.xlsx')

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ responseType: 'blob' })
      )
    })
  })

  // ============ 文件名 ============
  describe('文件名', () => {
    it('应使用传入的 filename 作为下载文件名', async () => {
      const mockBlob = new Blob(['test'])
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/export', {}, '订单导出_2024.xlsx')

      /** 验证 download 属性设置 */
      expect(mockClick).toHaveBeenCalled()
    })

    it('中文文件名应正确处理', async () => {
      const mockBlob = new Blob(['test'])
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/export', {}, '客户列表.xlsx')

      expect(mockClick).toHaveBeenCalled()
    })
  })

  // ============ Blob 处理 ============
  describe('Blob 处理', () => {
    it('应创建 ObjectURL 并传递给 <a> 标签', async () => {
      const mockBlob = new Blob(['excel data'], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/export', {}, 'test.xlsx')

      expect(mockCreateObjectURL).toHaveBeenCalledWith(mockBlob)
      expect(mockClick).toHaveBeenCalled()
      expect(mockRevokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
    })

    it('空 Blob 也应正确处理', async () => {
      const mockBlob = new Blob([])
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/export', {}, 'empty.xlsx')

      expect(mockCreateObjectURL).toHaveBeenCalledWith(mockBlob)
      expect(mockRevokeObjectURL).toHaveBeenCalled()
    })
  })

  // ============ 错误处理 ============
  describe('错误处理', () => {
    it('request 失败时应抛出错误', async () => {
      mockRequest.mockRejectedValueOnce(new Error('下载失败：权限不足'))

      await expect(
        downloadExcel('/export', {}, 'file.xlsx')
      ).rejects.toThrow('下载失败：权限不足')
    })

    it('网络错误时应正确传播', async () => {
      const networkError = new Error('Network Error')
      mockRequest.mockRejectedValueOnce(networkError)

      await expect(
        downloadExcel('/export', {}, 'file.xlsx')
      ).rejects.toThrow('Network Error')
    })
  })

  // ============ 参数完整性 ============
  describe('参数完整性', () => {
    it('空 params 对象应正确处理', async () => {
      const mockBlob = new Blob(['test'])
      mockRequest.mockResolvedValueOnce(mockBlob)

      await downloadExcel('/export', {}, 'test.xlsx')

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ params: {} })
      )
    })

    it('包含多个参数的 params 应正确传递', async () => {
      const mockBlob = new Blob(['test'])
      mockRequest.mockResolvedValueOnce(mockBlob)

      const params = {
        startDate: '2024-01-01',
        endDate: '2024-12-31',
        status: 'ALL',
        page: 1,
        size: 1000
      }

      await downloadExcel('/api/reports', params, '报表.xlsx')

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ params })
      )
    })
  })
})