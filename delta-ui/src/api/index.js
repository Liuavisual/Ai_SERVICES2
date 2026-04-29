/**
 * 后端API接口统一封装
 *
 * 按业务模块组织所有HTTP请求，统一使用request实例发送。
 * 每个API对象对应一个后端Controller，方法名与后端接口一一对应。
 *
 * 模块清单：
 * - authApi: 认证（登录/注册/刷新Token/登出）
 * - keywordApi: 关键词管理
 * - replyApi: 自动回复规则管理
 * - messageApi: 消息记录查询
 * - pendingMessageApi: 待处理消息（人工客服工单）
 * - platformConfigApi: 平台配置
 * - sysUserApi: 系统用户管理
 * - csUserCustomerApi: 客服-客户分配
 * - statsApi: 运营统计
 * - aiConfigApi: AI配置
 * - chatTestApi: 对话测试
 * - customerApi: 客户管理
 * - companionLevelApi: 陪玩师等级
 * - companionApi: 陪玩师管理
 * - companionScheduleApi: 陪玩师排班
 * - clubConfigApi: 俱乐部配置
 * - faqItemApi: FAQ知识库
 */
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const BASE_URL = '/api'

const activeDownloads = new Map()

function buildExportUrl(path, params) {
  const url = new URL(BASE_URL + path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v)
    })
  }
  return url.toString()
}

export function downloadExcel(path, params, filename) {
  const url = buildExportUrl(path, params)

  if (activeDownloads.has(url)) {
    ElMessage.warning('正在导出中，请勿重复操作')
    return { abort: () => {} }
  }

  const token = localStorage.getItem('token')
  const controller = new AbortController()

  activeDownloads.set(url, controller)

  fetch(url, {
    headers: { Authorization: `Bearer ${token}` },
    signal: controller.signal
  })
    .then(res => {
      if (res.status === 401) {
        throw new Error('AUTH_EXPIRED')
      }
      if (!res.ok) {
        throw new Error(`SERVER_ERROR_${res.status}`)
      }
      return res.blob()
    })
    .then(blob => {
      if (blob.size === 0) {
        throw new Error('EMPTY_RESPONSE')
      }
      const blobUrl = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = blobUrl
      link.download = filename || 'export.xlsx'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(blobUrl)
    })
    .catch(err => {
      if (err.name === 'AbortError') {
        return
      }
      if (err.message === 'AUTH_EXPIRED') {
        ElMessage.warning('登录已过期，请重新登录')
      } else if (err.message.startsWith('SERVER_ERROR_')) {
        const status = err.message.split('_').pop()
        ElMessage.error(`导出失败，服务器错误(${status})`)
      } else if (err.message === 'EMPTY_RESPONSE') {
        ElMessage.warning('导出数据为空')
      } else if (err.message === 'Failed to fetch' || err.message.includes('NetworkError')) {
        ElMessage.error('网络连接异常，请检查网络后重试')
      } else {
        ElMessage.error(err.message || '导出失败')
      }
    })
    .finally(() => {
      activeDownloads.delete(url)
    })

  return {
    abort: () => {
      controller.abort()
      activeDownloads.delete(url)
    }
  }
}

export function uploadExcel(path, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: path,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

/** 关键词管理API */
export const keywordApi = {
  /** 分页查询关键词 */
  getPage: (params) => request({ url: '/keywords/page', method: 'get', params }),
  /** 根据ID查询关键词 */
  getById: (id) => request({ url: `/keywords/${id}`, method: 'get' }),
  /** 创建关键词 */
  create: (data) => request({ url: '/keywords', method: 'post', data }),
  /** 更新关键词 */
  update: (data) => request({ url: '/keywords', method: 'put', data }),
  /** 删除关键词 */
  delete: (id) => request({ url: `/keywords/${id}`, method: 'delete' }),
  /** 刷新关键词缓存 */
  refresh: () => request({ url: '/keywords/refresh', method: 'post' })
}

/** 自动回复规则API */
export const replyApi = {
  getPage: (params) => request({ url: '/replies/page', method: 'get', params }),
  getById: (id) => request({ url: `/replies/${id}`, method: 'get' }),
  create: (data) => request({ url: '/replies', method: 'post', data }),
  update: (data) => request({ url: '/replies', method: 'put', data }),
  delete: (id) => request({ url: `/replies/${id}`, method: 'delete' })
}

/** 消息记录API */
export const messageApi = {
  /** 分页查询消息记录 */
  getPage: (params) => request({ url: '/messages/page', method: 'get', params })
}

/** 待处理消息（人工客服工单）API */
export const pendingMessageApi = {
  /** 分页查询待处理消息 */
  getPage: (params) => request({ url: '/pending-messages/page', method: 'get', params }),
  /** 处理待办（接手/完成等操作） */
  handle: (data) => request({ url: '/pending-messages/handle', method: 'post', data }),
  /** 获取待处理消息数量 */
  getCount: () => request({ url: '/pending-messages/count', method: 'get' })
}

/** 平台配置API */
export const platformConfigApi = {
  /** 获取所有平台配置 */
  getAll: () => request({ url: '/platform-configs', method: 'get' }),
  /** 根据平台标识获取配置 */
  getByPlatform: (platform) => request({ url: `/platform-configs/${platform}`, method: 'get' }),
  /** 更新平台配置 */
  update: (data) => request({ url: '/platform-configs', method: 'put', data })
}

/** 认证API */
export const authApi = {
  /** 用户登录 */
  login: (data) => request({ url: '/auth/login', method: 'post', data }),
  /** 用户注册 */
  register: (data) => request({ url: '/auth/register', method: 'post', data }),
  /** 刷新Token */
  refresh: (data) => request({ url: '/auth/refresh', method: 'post', data }),
  /** 登出 */
  logout: () => request({ url: '/auth/logout', method: 'post' })
}

/** 系统用户管理API */
export const sysUserApi = {
  getPage: (params) => request({ url: '/sys-users/page', method: 'get', params }),
  getById: (id) => request({ url: `/sys-users/${id}`, method: 'get' }),
  create: (data) => request({ url: '/sys-users', method: 'post', data }),
  update: (data) => request({ url: '/sys-users', method: 'put', data }),
  delete: (id) => request({ url: `/sys-users/${id}`, method: 'delete' }),
  /** 审核用户（启用/禁用） */
  audit: (data) => request({ url: '/sys-users/audit', method: 'post', data })
}

/** 客服-客户分配API */
export const csUserCustomerApi = {
  getPage: (params) => request({ url: '/cs-user-customer/page', method: 'get', params }),
  getById: (id) => request({ url: `/cs-user-customer/${id}`, method: 'get' }),
  create: (data) => request({ url: '/cs-user-customer', method: 'post', data }),
  update: (data) => request({ url: '/cs-user-customer', method: 'put', data }),
  delete: (id) => request({ url: `/cs-user-customer/${id}`, method: 'delete' })
}

/** 运营统计API */
export const statsApi = {
  /** 个人统计 */
  getPersonal: (params) => request({ url: '/stats/personal', method: 'get', params }),
  /** 团队统计 */
  getTeam: (params) => request({ url: '/stats/team', method: 'get', params }),
  /** 全局统计 */
  getGlobal: (params) => request({ url: '/stats/global', method: 'get', params })
}

/** AI配置API */
export const aiConfigApi = {
  /** 获取所有AI配置 */
  getAll: () => request({ url: '/ai-config', method: 'get' }),
  /** 批量更新AI配置 */
  update: (data) => request({ url: '/ai-config', method: 'put', data })
}

/** 对话测试API */
export const chatTestApi = {
  /** 发送测试消息，超时45秒（AI响应可能较慢） */
  send: (data) => request({ url: '/chat-test/send', method: 'post', data, timeout: 45000 })
}

/** 客户管理API */
export const customerApi = {
  getPage: (params) => request({ url: '/customers/page', method: 'get', params }),
  getById: (id) => request({ url: `/customers/${id}`, method: 'get' }),
  /** 切换客户AI开关 */
  toggleAiEnabled: (id, data) => request({ url: `/customers/${id}/ai-enabled`, method: 'put', data }),
  /** 分配专属客服 */
  assignCustomer: (id, data) => request({ url: `/customers/${id}/assign`, method: 'put', data })
}

/** 陪玩师等级API */
export const companionLevelApi = {
  getPage: (params) => request({ url: '/companion-levels/page', method: 'get', params }),
  /** 获取所有等级（不分页，用于下拉选择） */
  getAll: () => request({ url: '/companion-levels/all', method: 'get' }),
  getById: (id) => request({ url: `/companion-levels/${id}`, method: 'get' }),
  create: (data) => request({ url: '/companion-levels', method: 'post', data }),
  update: (data) => request({ url: '/companion-levels', method: 'put', data }),
  delete: (id) => request({ url: `/companion-levels/${id}`, method: 'delete' })
}

/** 陪玩师管理API */
export const companionApi = {
  getPage: (params) => request({ url: '/companions/page', method: 'get', params }),
  /** 获取所有陪玩师（不分页） */
  getAll: () => request({ url: '/companions/all', method: 'get' }),
  /** 获取可预约的陪玩师 */
  getAvailable: (params) => request({ url: '/companions/available', method: 'get', params }),
  getById: (id) => request({ url: `/companions/${id}`, method: 'get' }),
  create: (data) => request({ url: '/companions', method: 'post', data }),
  update: (data) => request({ url: '/companions', method: 'put', data }),
  delete: (id) => request({ url: `/companions/${id}`, method: 'delete' })
}

/** 陪玩师排班API */
export const companionScheduleApi = {
  getPage: (params) => request({ url: '/companion-schedules/page', method: 'get', params }),
  /** 按陪玩师+日期查询排班 */
  getByCompanionDate: (params) => request({ url: '/companion-schedules/by-companion-date', method: 'get', params }),
  /** 按日期查询所有排班 */
  getByDate: (params) => request({ url: '/companion-schedules/by-date', method: 'get', params }),
  getById: (id) => request({ url: `/companion-schedules/${id}`, method: 'get' }),
  create: (data) => request({ url: '/companion-schedules', method: 'post', data }),
  /** 批量创建排班 */
  createBatch: (params, data) => request({
    url: '/companion-schedules/batch',
    method: 'post',
    params: params,
    data: data
  }),
  update: (data) => request({ url: '/companion-schedules', method: 'put', data }),
  /** 更新排班状态（可预约/已预约/不可用） */
  updateStatus: (params) => request({ url: '/companion-schedules/status', method: 'put', params }),
  delete: (id) => request({ url: `/companion-schedules/${id}`, method: 'delete' }),
  /** 按陪玩师+日期删除排班 */
  deleteByCompanionDate: (params) => request({ url: '/companion-schedules/by-companion-date', method: 'delete', params }),
  /** 创建自由时间段(单日) */
  createTimeRange: (params) => request({ url: '/companion-schedules/time-range', method: 'post', params }),
  /** 批量创建自由时间段(多日) */
  createTimeRangeBatch: (params) => request({ url: '/companion-schedules/time-range-batch', method: 'post', params })
}

/** 俱乐部配置API */
export const clubConfigApi = {
  /** 获取俱乐部配置（单例） */
  get: () => request({ url: '/club-config', method: 'get' }),
  /** 更新俱乐部配置 */
  update: (data) => request({ url: '/club-config', method: 'put', data })
}

/** FAQ知识库API */
export const faqItemApi = {
  getPage: (params) => request({ url: '/faq-items', method: 'get', params }),
  create: (data) => request({ url: '/faq-items', method: 'post', data }),
  update: (data) => request({ url: '/faq-items', method: 'put', data }),
  delete: (id) => request({ url: `/faq-items/${id}`, method: 'delete' })
}

/** 客户画像API */
export const customerProfileApi = {
  getPage: (params) => request({ url: '/customer-profiles/page', method: 'get', params }),
  getByUserId: (userId) => request({ url: `/customer-profiles/user/${userId}`, method: 'get' }),
  update: (data) => request({ url: '/customer-profiles', method: 'put', data }),
  addOrder: (data) => request({ url: '/customer-profiles/orders', method: 'post', data }),
  getOrderPage: (params) => request({ url: '/customer-profiles/orders/page', method: 'get', params }),
  refresh: (userId) => request({ url: `/customer-profiles/refresh/${userId}`, method: 'post' })
}

export const gameConfigApi = {
  getByClubId: (clubConfigId) => request({ url: `/game-configs/club/${clubConfigId}`, method: 'get' }),
  getById: (id) => request({ url: `/game-configs/${id}`, method: 'get' }),
  create: (data) => request({ url: '/game-configs', method: 'post', data }),
  update: (data) => request({ url: '/game-configs', method: 'put', data }),
  delete: (id) => request({ url: `/game-configs/${id}`, method: 'delete' })
}

export const serviceItemApi = {
  getByClubId: (clubConfigId) => request({ url: `/service-items/club/${clubConfigId}`, method: 'get' }),
  getByGameId: (gameConfigId) => request({ url: `/service-items/game/${gameConfigId}`, method: 'get' }),
  getById: (id) => request({ url: `/service-items/${id}`, method: 'get' }),
  create: (data) => request({ url: '/service-items', method: 'post', data }),
  update: (data) => request({ url: '/service-items', method: 'put', data }),
  delete: (id) => request({ url: `/service-items/${id}`, method: 'delete' }),
  getPriceRules: (serviceItemId) => request({ url: `/service-items/${serviceItemId}/price-rules`, method: 'get' }),
  savePriceRule: (data) => request({ url: '/service-items/price-rules', method: 'post', data }),
  deletePriceRule: (id) => request({ url: `/service-items/price-rules/${id}`, method: 'delete' })
}

export const activityPackageApi = {
  getByClubId: (clubConfigId) => request({ url: `/activity-packages/club/${clubConfigId}`, method: 'get' }),
  getActive: (clubConfigId) => request({ url: `/activity-packages/club/${clubConfigId}/active`, method: 'get' }),
  getById: (id) => request({ url: `/activity-packages/${id}`, method: 'get' }),
  create: (data) => request({ url: '/activity-packages', method: 'post', data }),
  update: (data) => request({ url: '/activity-packages', method: 'put', data }),
  delete: (id) => request({ url: `/activity-packages/${id}`, method: 'delete' })
}

/** 订单管理API */
export const orderApi = {
  getById: (id) => request({ url: `/orders/${id}`, method: 'get' }),
  create: (data) => request({ url: '/orders', method: 'post', data }),
  confirm: (id) => request({ url: `/orders/${id}/confirm`, method: 'put' }),
  startService: (id) => request({ url: `/orders/${id}/start`, method: 'put' }),
  completeOrder: (id) => request({ url: `/orders/${id}/complete`, method: 'put' }),
  cancelOrder: (id, params) => request({ url: `/orders/${id}/cancel`, method: 'put', params }),
  getActiveByUser: (userId) => request({ url: `/orders/active/user/${userId}`, method: 'get' }),
  getByCompanion: (companionId) => request({ url: `/orders/companion/${companionId}`, method: 'get' }),
  queryOrders: (params) => request({ url: '/orders/query', method: 'get', params })
}

/** 工单管理API */
export const workOrderApi = {
  /** 分页查询工单 */
  getPage: (params) => request({ url: '/work-orders/page', method: 'get', params }),
  /** 根据ID查询工单 */
  getById: (id) => request({ url: `/work-orders/${id}`, method: 'get' }),
  /** 创建工单 */
  create: (data) => request({ url: '/work-orders', method: 'post', data }),
  /** 接手工单 */
  accept: (id) => request({ url: `/work-orders/${id}/accept`, method: 'put' }),
  /** 提交处理结果 */
  submit: (id, data) => request({ url: `/work-orders/${id}/submit`, method: 'put', data }),
  /** 确认完成工单 */
  confirm: (id, data) => request({ url: `/work-orders/${id}/confirm`, method: 'put', data }),
  /** 关闭工单 */
  close: (id, closeReason) => request({ url: `/work-orders/${id}/close`, method: 'put', params: { closeReason } }),
  /** 取消工单 */
  cancel: (id, cancelReason) => request({ url: `/work-orders/${id}/cancel`, method: 'put', params: { cancelReason } }),
  /** 重新打开工单（仅SYS_ADMIN） */
  reopen: (id, reopenReason) => request({ url: `/work-orders/${id}/reopen`, method: 'put', params: { reopenReason } }),
  /** 获取待处理工单数量 */
  getCount: () => request({ url: '/work-orders/count', method: 'get' })
}

/** 服务追踪API */
export const serviceTrackApi = {
  /** 根据ID查询服务追踪 */
  getById: (id) => request({ url: `/service-tracks/${id}`, method: 'get' }),
  /** 创建咨询 */
  create: (params) => request({ url: '/service-tracks', method: 'post', params }),
  /** 预约服务 */
  book: (id, userId, data) => request({ url: `/service-tracks/${id}/book`, method: 'put', params: { userId }, data }),
  /** 开始服务 */
  start: (id, companionId, companionName) => request({ url: `/service-tracks/${id}/start`, method: 'put', params: { companionId, companionName } }),
  /** 结束服务 */
  end: (id, data) => request({ url: `/service-tracks/${id}/end`, method: 'put', data }),
  /** 提交评价 */
  rating: (id, rating, feedback) => request({ url: `/service-tracks/${id}/rating`, method: 'put', params: { rating, feedback } }),
  /** 按用户ID查询服务追踪 */
  listByUser: (userId) => request({ url: `/service-tracks/user/${userId}`, method: 'get' }),
  /** 按订单ID查询服务追踪 */
  listByOrder: (orderId) => request({ url: `/service-tracks/order/${orderId}`, method: 'get' })
}
