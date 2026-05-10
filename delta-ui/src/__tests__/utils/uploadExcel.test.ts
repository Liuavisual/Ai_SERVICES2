/**
 * uploadExcel 工具函数单元测试
 *
 * 测试 Excel 上传工具函数，验证 FormData 构建、
 * request 调用参数、超时配置。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

/** 使用vi.hoisted提升mock函数 */
const { mockRequest } = vi.hoisted(() => ({
  mockRequest: vi.fn(() => Promise.resolve({ code: 200, data: {} }))
}))

/** Mock request模块 */
vi.mock('@/utils/request', () => ({
  default: mockRequest
}))

import uploadExcel from '@/utils/uploadExcel'

describe('uploadExcel 工具', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ============ 基本调用 ============
  describe('基本调用', () => {
    it('应调用 request 传入正确的 url', () => {
      const file = new File(['test data'], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      uploadExcel('/import/users', file)

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/import/users' })
      )
    })

    it('应使用 POST 方法', () => {
      const file = new File(['data'], 'data.xlsx')
      uploadExcel('/import', file)

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ method: 'post' })
      )
    })

    it('应设置超时时间为 60000ms', () => {
      const file = new File(['data'], 'data.xlsx')
      uploadExcel('/import', file)

      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ timeout: 60000 })
      )
    })
  })

  // ============ FormData ============
  describe('FormData 构建', () => {
    it('data 应为 FormData 实例', () => {
      const file = new File(['test content'], 'report.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      uploadExcel('/upload', file)

      const callArgs = (mockRequest.mock.calls[0] as Array<Record<string, unknown>>)[0]
      expect(callArgs.data).toBeInstanceOf(FormData)
    })

    it('FormData 应包含 file 字段', () => {
      const file = new File(['content'], 'myfile.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      uploadExcel('/upload', file)

      const callArgs = (mockRequest.mock.calls[0] as Array<Record<string, unknown>>)[0]
      const formData = callArgs.data as FormData
      expect(formData.get('file')).toBe(file)
    })

    it('大文件应正确处理', () => {
      const largeContent = 'x'.repeat(1024 * 1024)
      const file = new File([largeContent], 'large.xlsx')

      uploadExcel('/upload/large', file)

      expect(mockRequest).toHaveBeenCalledTimes(1)
      const callArgs = (mockRequest.mock.calls[0] as Array<Record<string, unknown>>)[0]
      const formData = callArgs.data as FormData
      expect(formData.get('file')).toBe(file)
    })
  })

  // ============ 返回值 ============
  describe('返回值', () => {
    it('应返回 request 调用的 Promise', async () => {
      const mockResponse = { code: 200, data: { id: '123' } }
      mockRequest.mockResolvedValueOnce(mockResponse)

      const file = new File(['data'], 'test.xlsx')
      const result = await uploadExcel('/import', file)

      expect(result).toEqual(mockResponse)
    })

    it('request 失败时应正确传递错误', async () => {
      mockRequest.mockRejectedValueOnce(new Error('网络错误'))

      const file = new File(['data'], 'test.xlsx')
      await expect(uploadExcel('/import', file)).rejects.toThrow('网络错误')
    })
  })

  // ============ URL 参数 ============
  describe('URL 参数', () => {
    it('不同的 URL 应正确传递', () => {
      const file = new File(['data'], 'a.xlsx')
      uploadExcel('/api/import/keywords', file)
      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/api/import/keywords' })
      )
    })

    it('包含查询参数的 URL', () => {
      const file = new File(['data'], 'a.xlsx')
      uploadExcel('/api/import?type=user', file)
      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({ url: '/api/import?type=user' })
      )
    })
  })
})