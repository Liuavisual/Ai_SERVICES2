/**
 * 导入Excel工具函数
 *
 * 上传Excel文件到指定接口，自动设置FormData和multipart头。
 *
 * @author 刘建国
 */
import request from './request'

/**
 * 上传Excel文件
 * @param url - 上传接口地址
 * @param file - Excel文件对象
 * @returns 上传结果
 */
const uploadExcel = (url: string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url,
    method: 'post',
    data: formData,
    timeout: 60000
  })
}

export default uploadExcel
