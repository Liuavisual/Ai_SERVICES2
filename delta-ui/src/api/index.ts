/**
 * API接口统一导出
 *
 * 所有后端API的请求封装，按业务模块组织。
 * 使用统一的 request 实例发送请求，自动处理Token和错误。
 *
 * @author 刘建国
 */
import request from '@/utils/request'
import downloadExcel from '../utils/downloadExcel'
import uploadExcel from '../utils/uploadExcel'

export { downloadExcel, uploadExcel }

/** 消息撤回 */
export const recallMessage = (messageId: string, platform: string) =>
  request({ url: '/pending-messages/recall', method: 'post', data: { messageId, platform } })

export const authApi = {
  login: (data: Record<string, unknown>) => request({ url: '/auth/login', method: 'post', data }),
  register: (data: Record<string, unknown>) => request({ url: '/auth/register', method: 'post', data }),
  refresh: (data: Record<string, unknown>) => request({ url: '/auth/refresh', method: 'post', data, timeout: 10000 }),
  logout: () => request({ url: '/auth/logout', method: 'post' }),
  getCaptcha: () => request({ url: '/auth/captcha', method: 'get' })
}

export const keywordApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/keywords/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/keywords/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/keywords', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/keywords', method: 'put', data }),
  delete: (id: string) => request({ url: `/keywords/${id}`, method: 'delete' }),
  batchImport: (data: Record<string, unknown>) => request({ url: '/keywords/batch', method: 'post', data }),
  refresh: () => request({ url: '/keywords/refresh', method: 'post' })
}

export const replyApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/replies/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/replies/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/replies', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/replies', method: 'put', data }),
  delete: (id: string) => request({ url: `/replies/${id}`, method: 'delete' }),
  batchImport: (data: Record<string, unknown>) => request({ url: '/replies/batch', method: 'post', data })
}

export const pendingMessageApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/pending-messages/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/pending-messages/${id}`, method: 'get' }),
  reply: (data: Record<string, unknown>) => request({ url: '/pending-messages/reply', method: 'post', data }),
  assign: (data: Record<string, unknown>) => request({ url: '/pending-messages/assign', method: 'post', data }),
  process: (data: Record<string, unknown>) => request({ url: '/pending-messages/process', method: 'post', data }),
  getCount: () => request({ url: '/pending-messages/count', method: 'get' })
}

export const messageApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/messages/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/messages/${id}`, method: 'get' }),
  send: (data: Record<string, unknown>) => request({ url: '/messages/send', method: 'post', data })
}

export const sysUserApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/sys-users/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/sys-users/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/sys-users', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/sys-users', method: 'put', data }),
  delete: (id: string) => request({ url: `/sys-users/${id}`, method: 'delete' }),
  audit: (data: Record<string, unknown>) => request({ url: '/sys-users/audit', method: 'post', data })
}

export const csUserCustomerApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/cs-user-customer/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/cs-user-customer/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/cs-user-customer', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/cs-user-customer', method: 'put', data }),
  delete: (id: string) => request({ url: `/cs-user-customer/${id}`, method: 'delete' })
}

export const statsApi = {
  getPersonal: (params: Record<string, unknown>) => request({ url: '/stats/personal', method: 'get', params }),
  getTeam: (params: Record<string, unknown>) => request({ url: '/stats/team', method: 'get', params }),
  getGlobal: (params: Record<string, unknown>) => request({ url: '/stats/global', method: 'get', params })
}

export const aiConfigApi = {
  getAll: () => request({ url: '/ai-config', method: 'get' }),
  update: (data: Record<string, unknown>) => request({ url: '/ai-config', method: 'put', data })
}

export const chatTestApi = {
  send: (data: Record<string, unknown>) => request({ url: '/chat-test/send', method: 'post', data, timeout: 45000 })
}

export const customerApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/customers/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/customers/${id}`, method: 'get' }),
  toggleAiEnabled: (id: string, data: Record<string, unknown>) => request({ url: `/customers/${id}/ai-enabled`, method: 'put', data }),
  assignCustomer: (id: string, data: Record<string, unknown>) => request({ url: `/customers/${id}/assign`, method: 'put', data })
}

export const companionLevelApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/companion-levels/page', method: 'get', params }),
  getAll: () => request({ url: '/companion-levels/all', method: 'get' }),
  getById: (id: string) => request({ url: `/companion-levels/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/companion-levels', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/companion-levels', method: 'put', data }),
  delete: (id: string) => request({ url: `/companion-levels/${id}`, method: 'delete' })
}

export const companionApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/companions/page', method: 'get', params }),
  getAll: () => request({ url: '/companions/all', method: 'get' }),
  getAvailable: (params: Record<string, unknown>) => request({ url: '/companions/available', method: 'get', params }),
  getById: (id: string) => request({ url: `/companions/${id}`, method: 'get' }),
  getByUserId: (userId: string) => request({ url: `/companions/by-user/${userId}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/companions', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/companions', method: 'put', data }),
  delete: (id: string) => request({ url: `/companions/${id}`, method: 'delete' }),
  getRatingDashboard: (companionId: string) => request({ url: `/companions/ratings/dashboard/${companionId}`, method: 'get' }),
  getAllRatings: () => request({ url: '/companions/ratings/all', method: 'get' })
}

export const companionScheduleApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/companion-schedules/page', method: 'get', params }),
  getByCompanionDate: (params: Record<string, unknown>) => request({ url: '/companion-schedules/by-companion-date', method: 'get', params }),
  getByDate: (params: Record<string, unknown>) => request({ url: '/companion-schedules/by-date', method: 'get', params }),
  getByDateRange: (params: Record<string, unknown>) => request({ url: '/companion-schedules/by-date-range', method: 'get', params }),
  getById: (id: string) => request({ url: `/companion-schedules/${id}`, method: 'get' }),
  getAvailableSlots: (companionId: string, scheduleDate?: string) =>
    request({ url: `/companion-schedules/available/${companionId}`, method: 'get', params: { scheduleDate } }),
  getListByCompanionId: (companionId: number) =>
    request({ url: `/companion-schedules/by-companion/${companionId}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/companion-schedules', method: 'post', data }),
  createBatch: (params: Record<string, unknown>, data: Record<string, unknown>) =>
    request({ url: '/companion-schedules/batch', method: 'post', params, data }),
  update: (data: Record<string, unknown>) => request({ url: '/companion-schedules', method: 'put', data }),
  updateStatus: (params: Record<string, unknown>) => request({ url: '/companion-schedules/status', method: 'put', params }),
  delete: (id: string) => request({ url: `/companion-schedules/${id}`, method: 'delete' }),
  deleteByCompanionDate: (params: Record<string, unknown>) => request({ url: '/companion-schedules/by-companion-date', method: 'delete', params }),
  createTimeRange: (params: Record<string, unknown>) => request({ url: '/companion-schedules/time-range', method: 'post', params }),
  createTimeRangeBatch: (params: Record<string, unknown>) => request({ url: '/companion-schedules/time-range-batch', method: 'post', params })
}

export const clubConfigApi = {
  get: () => request({ url: '/club-config', method: 'get' }),
  update: (data: Record<string, unknown>) => request({ url: '/club-config', method: 'put', data })
}

export const faqItemApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/faq-items', method: 'get', params }),
  create: (data: Record<string, unknown>) => request({ url: '/faq-items', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/faq-items', method: 'put', data }),
  delete: (id: string) => request({ url: `/faq-items/${id}`, method: 'delete' })
}

export const customerProfileApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/customer-profiles/page', method: 'get', params }),
  getByUserId: (userId: string) => request({ url: `/customer-profiles/user/${userId}`, method: 'get' }),
  update: (data: Record<string, unknown>) => request({ url: '/customer-profiles', method: 'put', data }),
  addOrder: (data: Record<string, unknown>) => request({ url: '/customer-profiles/orders', method: 'post', data }),
  getOrderPage: (params: Record<string, unknown>) => request({ url: '/customer-profiles/orders/page', method: 'get', params }),
  refresh: (userId: string) => request({ url: `/customer-profiles/refresh/${userId}`, method: 'post' })
}

export const gameConfigApi = {
  getByClubId: (clubConfigId: string) => request({ url: `/game-configs/club/${clubConfigId}`, method: 'get' }),
  getAll: () => request({ url: '/game-configs/all', method: 'get' }),
  getById: (id: string) => request({ url: `/game-configs/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/game-configs', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/game-configs', method: 'put', data }),
  delete: (id: string) => request({ url: `/game-configs/${id}`, method: 'delete' })
}

export const serviceItemApi = {
  getByClubId: (clubConfigId: string) => request({ url: `/service-items/club/${clubConfigId}`, method: 'get' }),
  getByGameId: (gameConfigId: string) => request({ url: `/service-items/game/${gameConfigId}`, method: 'get' }),
  getById: (id: string) => request({ url: `/service-items/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/service-items', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/service-items', method: 'put', data }),
  delete: (id: string) => request({ url: `/service-items/${id}`, method: 'delete' }),
  getPriceRules: (serviceItemId: string) => request({ url: `/service-items/${serviceItemId}/price-rules`, method: 'get' }),
  savePriceRule: (data: Record<string, unknown>) => request({ url: '/service-items/price-rules', method: 'post', data }),
  deletePriceRule: (id: string) => request({ url: `/service-items/price-rules/${id}`, method: 'delete' })
}

export const activityPackageApi = {
  getByClubId: (clubConfigId: string) => request({ url: `/activity-packages/club/${clubConfigId}`, method: 'get' }),
  getActive: (clubConfigId: string) => request({ url: `/activity-packages/club/${clubConfigId}/active`, method: 'get' }),
  getById: (id: string) => request({ url: `/activity-packages/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/activity-packages', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/activity-packages', method: 'put', data }),
  delete: (id: string) => request({ url: `/activity-packages/${id}`, method: 'delete' })
}

export const orderApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/orders/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/orders/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/orders', method: 'post', data }),
  confirm: (id: string) => request({ url: `/orders/${id}/confirm`, method: 'put' }),
  startService: (id: string) => request({ url: `/orders/${id}/start`, method: 'put' }),
  completeOrder: (id: string) => request({ url: `/orders/${id}/complete`, method: 'put' }),
  cancelOrder: (id: string, reason: string) => request({ url: `/orders/${id}/cancel`, method: 'put', params: { reason } }),
  getActiveByUser: (userId: string) => request({ url: `/orders/active/user/${userId}`, method: 'get' }),
  getByCompanion: (companionId: string) => request({ url: `/orders/companion/${companionId}`, method: 'get' }),
  queryOrders: (params: Record<string, unknown>) => request({ url: '/orders/query', method: 'get', params }),
  accept: (id: string, companionId: number) => request({ url: `/orders/${id}/accept`, method: 'put', params: { companionId } }),
  reject: (id: string, companionId: number, reason: string) => request({ url: `/orders/${id}/reject`, method: 'put', params: { companionId, reason } }),
  getPendingByCompanion: (companionId: number) => request({ url: `/orders/companion/${companionId}/pending`, method: 'get' }),
  getStatusHistory: (id: string) => request({ url: `/orders/${id}/status-history`, method: 'get' })
}

export const workOrderApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/work-orders/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/work-orders/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/work-orders', method: 'post', data }),
  accept: (id: string) => request({ url: `/work-orders/${id}/accept`, method: 'put' }),
  submit: (id: string, data: Record<string, unknown>) => request({ url: `/work-orders/${id}/submit`, method: 'put', data }),
  confirm: (id: string, data: Record<string, unknown>) => request({ url: `/work-orders/${id}/confirm`, method: 'put', data }),
  close: (id: string, closeReason: string) => request({ url: `/work-orders/${id}/close`, method: 'put', data: { closeReason } }),
  cancel: (id: string, cancelReason: string) => request({ url: `/work-orders/${id}/cancel`, method: 'put', data: { cancelReason } }),
  reopen: (id: string, reopenReason: string) => request({ url: `/work-orders/${id}/reopen`, method: 'put', data: { reopenReason } }),
  getCount: () => request({ url: '/work-orders/count', method: 'get' })
}

export const serviceTrackApi = {
  getById: (id: string) => request({ url: `/service-tracks/${id}`, method: 'get' }),
  create: (params: Record<string, unknown>) => request({ url: '/service-tracks', method: 'post', params }),
  book: (id: string, userId: string, data: Record<string, unknown>) =>
    request({ url: `/service-tracks/${id}/book`, method: 'put', params: { userId }, data }),
  start: (id: string, companionId: string, companionName: string) =>
    request({ url: `/service-tracks/${id}/start`, method: 'put', params: { companionId, companionName } }),
  end: (id: string, data: Record<string, unknown>) => request({ url: `/service-tracks/${id}/end`, method: 'put', data }),
  rating: (id: string, rating: number, feedback: string) =>
    request({ url: `/service-tracks/${id}/rating`, method: 'put', params: { rating, feedback } }),
  listByUser: (userId: string) => request({ url: `/service-tracks/user/${userId}`, method: 'get' }),
  listByOrder: (orderId: string) => request({ url: `/service-tracks/order/${orderId}`, method: 'get' })
}

export const lifecycleApi = {
  getAtRisk: () => request({ url: '/customer-lifecycle/at-risk', method: 'get' }),
  getChurned: () => request({ url: '/customer-lifecycle/churned', method: 'get' }),
  getStage: (userId: string) => request({ url: `/customer-lifecycle/stage/${userId}`, method: 'get' }),
  updateTags: () => request({ url: '/customer-lifecycle/update-tags', method: 'post' })
}

export const satisfactionApi = {
  submit: (data: Record<string, unknown>) => request({ url: '/satisfaction', method: 'post', data }),
  getPage: (params: Record<string, unknown>) => request({ url: '/satisfaction/page', method: 'get', params }),
  getAverage: (companionId: string) => request({ url: `/satisfaction/average/${companionId}`, method: 'get' }),
  submitOrderReview: (data: Record<string, unknown>) => request({ url: '/satisfaction/order-review', method: 'post', params: data })
}

export const pricingPlanApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/pricing-plans/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/pricing-plans/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/pricing-plans', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/pricing-plans', method: 'put', data }),
  delete: (id: string) => request({ url: `/pricing-plans/${id}`, method: 'delete' })
}

export const subscriptionApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/subscriptions/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/subscriptions/${id}`, method: 'get' }),
  getByClubConfigId: (clubConfigId: string) => request({ url: `/subscriptions/by-club/${clubConfigId}`, method: 'get' }),
  subscribe: (data: Record<string, unknown>) => request({ url: '/subscriptions/subscribe', method: 'post', data }),
  trial: (data: Record<string, unknown>) => request({ url: '/subscriptions/trial', method: 'post', data }),
  cancel: (id: string) => request({ url: `/subscriptions/${id}/cancel`, method: 'put' }),
  renew: (id: string, data: Record<string, unknown>) => request({ url: `/subscriptions/${id}/renew`, method: 'put', data })
}

export const qualityCheckApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/quality-checks/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/quality-checks/${id}`, method: 'get' }),
  handle: (id: string, data: Record<string, unknown>) => request({ url: `/quality-checks/${id}/handle`, method: 'put', data })
}

export const settlementApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/settlements/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/settlements/${id}`, method: 'get' }),
  confirm: (id: string, data: Record<string, unknown>) => request({ url: `/settlements/${id}/confirm`, method: 'put', data }),
  dispute: (id: string, data: Record<string, unknown>) => request({ url: `/settlements/${id}/dispute`, method: 'put', data }),
  settle: (id: string) => request({ url: `/settlements/${id}/settle`, method: 'put' })
}

export const trainingApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/trainings/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/trainings/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/trainings', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/trainings', method: 'put', data }),
  startTraining: (id: string) => request({ url: `/trainings/${id}/start`, method: 'put' }),
  completeTraining: (id: string, data: Record<string, unknown>) => request({ url: `/trainings/${id}/complete`, method: 'put', data }),
  delete: (id: string) => request({ url: `/trainings/${id}`, method: 'delete' })
}

export const reportApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/reports/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/reports/${id}`, method: 'get' }),
  generate: (data: Record<string, unknown>) => request({ url: '/reports/generate', method: 'post', data })
}

export const platformConfigApi = {
  getAll: () => request({ url: '/platform-configs', method: 'get' }),
  getByPlatform: (platform: string) => request({ url: `/platform-configs/${platform}`, method: 'get' }),
  update: (id: string, data: Record<string, unknown>) => request({ url: `/platform-configs/${id}`, method: 'put', data })
}

export const campaignApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/campaigns/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/campaigns/${id}`, method: 'get' }),
  create: (data: Record<string, unknown>) => request({ url: '/campaigns', method: 'post', data }),
  update: (data: Record<string, unknown>) => request({ url: '/campaigns', method: 'put', data }),
  start: (id: string) => request({ url: `/campaigns/${id}/start`, method: 'put' }),
  pause: (id: string) => request({ url: `/campaigns/${id}/pause`, method: 'put' }),
  end: (id: string) => request({ url: `/campaigns/${id}/end`, method: 'put' }),
  delete: (id: string) => request({ url: `/campaigns/${id}`, method: 'delete' })
}

export const referralApi = {
  getPage: (params: Record<string, unknown>) => request({ url: '/referrals/page', method: 'get', params }),
  getById: (id: string) => request({ url: `/referrals/${id}`, method: 'get' }),
  issueReward: (id: string) => request({ url: `/referrals/${id}/issue-reward`, method: 'put' }),
  cancelReward: (id: string) => request({ url: `/referrals/${id}/cancel-reward`, method: 'put' })
}
