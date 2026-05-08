/**
 * 导出Excel工具函数
 *
 * 发起GET请求下载Excel文件，自动处理Blob响应。
 *
 * @author 刘建国
 */
import request from './request'

/**
 * 下载Excel文件
 * @param url - 下载接口地址
 * @param params - 查询参数
 * @param filename - 下载文件名
 */
const downloadExcel = async (url: string, params: Record<string, unknown>, filename: string): Promise<void> => {
  const res = await request({
    url,
    method: 'get',
    params,
    responseType: 'blob'
  })
  const blob = res as unknown as Blob
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

export default downloadExcel
