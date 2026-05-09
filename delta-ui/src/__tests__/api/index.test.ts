/**
 * API模块测试
 *
 * 测试所有API模块的导出是否正确，验证各API方法调用参数。
 * Mock request函数，确保API方法传递正确的url、method等配置。
 *
 * @author 刘建国
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

/** 使用vi.hoisted提升mock函数，确保vi.mock工厂函数可以访问 */
const { mockRequest } = vi.hoisted(() => ({
  mockRequest: vi.fn(() => Promise.resolve({ code: 200, data: {} }))
}))

/** Mock request函数，捕获调用参数 */
vi.mock('@/utils/request', () => ({
  default: mockRequest
}))

/** Mock element-plus，避免ElMessage在测试环境中报错 */
vi.mock('element-plus', () => ({
  ElMessage: {
    warning: vi.fn(),
    error: vi.fn(),
    success: vi.fn()
  }
}))

/** 导入所有API模块 */
import {
  authApi,
  sysUserApi,
  messageApi,
  pendingMessageApi,
  customerApi,
  customerProfileApi,
  orderApi,
  workOrderApi,
  serviceTrackApi,
  companionApi,
  companionScheduleApi,
  companionLevelApi,
  keywordApi,
  replyApi,
  faqItemApi,
  gameConfigApi,
  serviceItemApi,
  activityPackageApi,
  platformConfigApi,
  clubConfigApi,
  aiConfigApi,
  statsApi,
  lifecycleApi,
  satisfactionApi,
  csUserCustomerApi,
  chatTestApi,
  downloadExcel,
  uploadExcel
} from '@/api'

describe('API模块测试', () => {
  beforeEach(() => {
    mockRequest.mockClear()
  })

  // ============ 认证API ============
  describe('authApi', () => {
    it('login 应发送POST请求到 /auth/login', () => {
      const data = { username: 'admin', password: '123456' }
      authApi.login(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/auth/login',
        method: 'post',
        data
      })
    })

    it('register 应发送POST请求到 /auth/register', () => {
      const data = { username: 'new', password: '123', realName: '新用户' }
      authApi.register(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/auth/register',
        method: 'post',
        data
      })
    })

    it('refresh 应发送POST请求到 /auth/refresh', () => {
      const data = { refreshToken: 'rt-xxx' }
      authApi.refresh(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/auth/refresh',
        method: 'post',
        data,
        timeout: 10000
      })
    })

    it('logout 应发送POST请求到 /auth/logout', () => {
      authApi.logout()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/auth/logout',
        method: 'post'
      })
    })
  })

  // ============ 系统用户管理API ============
  describe('sysUserApi', () => {
    it('getPage 应发送GET请求到 /sys-users/page', () => {
      const params = { current: 1, size: 10 }
      sysUserApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/sys-users/page',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /sys-users/:id', () => {
      sysUserApi.getById('123')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/sys-users/123',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /sys-users', () => {
      const data = { username: 'test' }
      sysUserApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/sys-users',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /sys-users', () => {
      const data = { id: '123', username: 'test' }
      sysUserApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/sys-users',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /sys-users/:id', () => {
      sysUserApi.delete('123')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/sys-users/123',
        method: 'delete'
      })
    })

    it('audit 应发送POST请求到 /sys-users/audit', () => {
      const data = { id: '123', status: 'ENABLED' }
      sysUserApi.audit(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/sys-users/audit',
        method: 'post',
        data
      })
    })
  })

  // ============ 消息记录API ============
  describe('messageApi', () => {
    it('getPage 应发送GET请求到 /messages/page', () => {
      const params = { current: 1, size: 10 }
      messageApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/messages/page',
        method: 'get',
        params
      })
    })
  })

  // ============ 待处理消息API ============
  describe('pendingMessageApi', () => {
    it('getPage 应发送GET请求到 /pending-messages/page', () => {
      const params = { current: 1 }
      pendingMessageApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/pending-messages/page',
        method: 'get',
        params
      })
    })

    it('process 应发送POST请求到 /pending-messages/process', () => {
      const data = { id: '1', action: 'accept' }
      pendingMessageApi.process(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/pending-messages/process',
        method: 'post',
        data
      })
    })

    it('getCount 应发送GET请求到 /pending-messages/count', () => {
      pendingMessageApi.getCount()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/pending-messages/count',
        method: 'get'
      })
    })
  })

  // ============ 客户管理API ============
  describe('customerApi', () => {
    it('getPage 应发送GET请求到 /customers/page', () => {
      const params = { current: 1 }
      customerApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customers/page',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /customers/:id', () => {
      customerApi.getById('c1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customers/c1',
        method: 'get'
      })
    })

    it('toggleAiEnabled 应发送PUT请求到 /customers/:id/ai-enabled', () => {
      const data = { enabled: 1 }
      customerApi.toggleAiEnabled('c1', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customers/c1/ai-enabled',
        method: 'put',
        data
      })
    })

    it('assignCustomer 应发送PUT请求到 /customers/:id/assign', () => {
      const data = { csUserId: 'u1' }
      customerApi.assignCustomer('c1', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customers/c1/assign',
        method: 'put',
        data
      })
    })
  })

  // ============ 客户画像API ============
  describe('customerProfileApi', () => {
    it('getPage 应发送GET请求到 /customer-profiles/page', () => {
      const params = { current: 1 }
      customerProfileApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-profiles/page',
        method: 'get',
        params
      })
    })

    it('getByUserId 应发送GET请求到 /customer-profiles/user/:userId', () => {
      customerProfileApi.getByUserId('u1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-profiles/user/u1',
        method: 'get'
      })
    })

    it('update 应发送PUT请求到 /customer-profiles', () => {
      const data = { id: '1', tags: 'vip' }
      customerProfileApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-profiles',
        method: 'put',
        data
      })
    })

    it('addOrder 应发送POST请求到 /customer-profiles/orders', () => {
      const data = { userId: 'u1', orderId: 'o1' }
      customerProfileApi.addOrder(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-profiles/orders',
        method: 'post',
        data
      })
    })

    it('getOrderPage 应发送GET请求到 /customer-profiles/orders/page', () => {
      const params = { current: 1 }
      customerProfileApi.getOrderPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-profiles/orders/page',
        method: 'get',
        params
      })
    })

    it('refresh 应发送POST请求到 /customer-profiles/refresh/:userId', () => {
      customerProfileApi.refresh('u1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-profiles/refresh/u1',
        method: 'post'
      })
    })
  })

  // ============ 订单管理API ============
  describe('orderApi', () => {
    it('getById 应发送GET请求到 /orders/:id', () => {
      orderApi.getById('o1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/o1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /orders', () => {
      const data = { userId: 'u1', companionId: 'c1' }
      orderApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders',
        method: 'post',
        data
      })
    })

    it('confirm 应发送PUT请求到 /orders/:id/confirm', () => {
      orderApi.confirm('o1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/o1/confirm',
        method: 'put'
      })
    })

    it('startService 应发送PUT请求到 /orders/:id/start', () => {
      orderApi.startService('o1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/o1/start',
        method: 'put'
      })
    })

    it('completeOrder 应发送PUT请求到 /orders/:id/complete', () => {
      orderApi.completeOrder('o1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/o1/complete',
        method: 'put'
      })
    })

    it('cancelOrder 应发送PUT请求到 /orders/:id/cancel', () => {
      orderApi.cancelOrder('o1', '不想要了')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/o1/cancel',
        method: 'put',
        params: { reason: '不想要了' }
      })
    })

    it('getActiveByUser 应发送GET请求到 /orders/active/user/:userId', () => {
      orderApi.getActiveByUser('u1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/active/user/u1',
        method: 'get'
      })
    })

    it('getByCompanion 应发送GET请求到 /orders/companion/:companionId', () => {
      orderApi.getByCompanion('c1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/companion/c1',
        method: 'get'
      })
    })

    it('queryOrders 应发送GET请求到 /orders/query', () => {
      const params = { status: 'PENDING' }
      orderApi.queryOrders(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/orders/query',
        method: 'get',
        params
      })
    })
  })

  // ============ 工单管理API ============
  describe('workOrderApi', () => {
    it('getPage 应发送GET请求到 /work-orders/page', () => {
      const params = { current: 1 }
      workOrderApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/page',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /work-orders/:id', () => {
      workOrderApi.getById('w1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /work-orders', () => {
      const data = { title: '测试工单' }
      workOrderApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders',
        method: 'post',
        data
      })
    })

    it('accept 应发送PUT请求到 /work-orders/:id/accept', () => {
      workOrderApi.accept('w1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1/accept',
        method: 'put'
      })
    })

    it('submit 应发送PUT请求到 /work-orders/:id/submit', () => {
      const data = { result: '已处理' }
      workOrderApi.submit('w1', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1/submit',
        method: 'put',
        data
      })
    })

    it('confirm 应发送PUT请求到 /work-orders/:id/confirm', () => {
      const data = { satisfaction: 5 }
      workOrderApi.confirm('w1', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1/confirm',
        method: 'put',
        data
      })
    })

    it('close 应发送PUT请求到 /work-orders/:id/close，携带closeReason', () => {
      workOrderApi.close('w1', '已解决')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1/close',
        method: 'put',
        data: { closeReason: '已解决' }
      })
    })

    it('cancel 应发送PUT请求到 /work-orders/:id/cancel，携带cancelReason', () => {
      workOrderApi.cancel('w1', '误操作')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1/cancel',
        method: 'put',
        data: { cancelReason: '误操作' }
      })
    })

    it('reopen 应发送PUT请求到 /work-orders/:id/reopen，携带reopenReason', () => {
      workOrderApi.reopen('w1', '需重新处理')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/w1/reopen',
        method: 'put',
        data: { reopenReason: '需重新处理' }
      })
    })

    it('getCount 应发送GET请求到 /work-orders/count', () => {
      workOrderApi.getCount()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/work-orders/count',
        method: 'get'
      })
    })
  })

  // ============ 服务追踪API ============
  describe('serviceTrackApi', () => {
    it('getById 应发送GET请求到 /service-tracks/:id', () => {
      serviceTrackApi.getById('s1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/s1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /service-tracks', () => {
      const params = { userId: 'u1' }
      serviceTrackApi.create(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks',
        method: 'post',
        params
      })
    })

    it('book 应发送PUT请求到 /service-tracks/:id/book', () => {
      const data = { serviceType: 'game' }
      serviceTrackApi.book('s1', 'u1', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/s1/book',
        method: 'put',
        params: { userId: 'u1' },
        data
      })
    })

    it('start 应发送PUT请求到 /service-tracks/:id/start', () => {
      serviceTrackApi.start('s1', 'c1', '陪玩师A')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/s1/start',
        method: 'put',
        params: { companionId: 'c1', companionName: '陪玩师A' }
      })
    })

    it('end 应发送PUT请求到 /service-tracks/:id/end', () => {
      const data = { durationMinutes: 60 }
      serviceTrackApi.end('s1', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/s1/end',
        method: 'put',
        data
      })
    })

    it('rating 应发送PUT请求到 /service-tracks/:id/rating', () => {
      serviceTrackApi.rating('s1', 5, '非常好')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/s1/rating',
        method: 'put',
        params: { rating: 5, feedback: '非常好' }
      })
    })

    it('listByUser 应发送GET请求到 /service-tracks/user/:userId', () => {
      serviceTrackApi.listByUser('u1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/user/u1',
        method: 'get'
      })
    })

    it('listByOrder 应发送GET请求到 /service-tracks/order/:orderId', () => {
      serviceTrackApi.listByOrder('o1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-tracks/order/o1',
        method: 'get'
      })
    })
  })

  // ============ 陪玩师管理API ============
  describe('companionApi', () => {
    it('getPage 应发送GET请求到 /companions/page', () => {
      const params = { current: 1 }
      companionApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions/page',
        method: 'get',
        params
      })
    })

    it('getAll 应发送GET请求到 /companions/all', () => {
      companionApi.getAll()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions/all',
        method: 'get'
      })
    })

    it('getAvailable 应发送GET请求到 /companions/available', () => {
      const params = { date: '2024-01-01' }
      companionApi.getAvailable(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions/available',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /companions/:id', () => {
      companionApi.getById('c1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions/c1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /companions', () => {
      const data = { realName: '张三' }
      companionApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /companions', () => {
      const data = { id: 'c1', realName: '李四' }
      companionApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /companions/:id', () => {
      companionApi.delete('c1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companions/c1',
        method: 'delete'
      })
    })
  })

  // ============ 陪玩师排班API ============
  describe('companionScheduleApi', () => {
    it('getPage 应发送GET请求到 /companion-schedules/page', () => {
      const params = { current: 1 }
      companionScheduleApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/page',
        method: 'get',
        params
      })
    })

    it('getByCompanionDate 应发送GET请求到 /companion-schedules/by-companion-date', () => {
      const params = { companionId: 'c1', date: '2024-01-01' }
      companionScheduleApi.getByCompanionDate(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/by-companion-date',
        method: 'get',
        params
      })
    })

    it('getByDate 应发送GET请求到 /companion-schedules/by-date', () => {
      const params = { date: '2024-01-01' }
      companionScheduleApi.getByDate(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/by-date',
        method: 'get',
        params
      })
    })

    it('createBatch 应发送POST请求到 /companion-schedules/batch', () => {
      const params = { companionId: 'c1' }
      const data = [{ date: '2024-01-01' }]
      companionScheduleApi.createBatch(params, data as unknown as Record<string, unknown>)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/batch',
        method: 'post',
        params,
        data
      })
    })

    it('updateStatus 应发送PUT请求到 /companion-schedules/status', () => {
      const data = { id: 's1', status: 'BOOKED' }
      companionScheduleApi.updateStatus(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/status',
        method: 'put',
        params: data
      })
    })

    it('deleteByCompanionDate 应发送DELETE请求到 /companion-schedules/by-companion-date', () => {
      const params = { companionId: 'c1', date: '2024-01-01' }
      companionScheduleApi.deleteByCompanionDate(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/by-companion-date',
        method: 'delete',
        params
      })
    })

    it('createTimeRange 应发送POST请求到 /companion-schedules/time-range', () => {
      const data = { companionId: 'c1', startTime: '09:00', endTime: '12:00' }
      companionScheduleApi.createTimeRange(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/time-range',
        method: 'post',
        params: data
      })
    })

    it('createTimeRangeBatch 应发送POST请求到 /companion-schedules/time-range-batch', () => {
      const data = { companionId: 'c1', dates: '2024-01-01,2024-01-02' }
      companionScheduleApi.createTimeRangeBatch(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-schedules/time-range-batch',
        method: 'post',
        params: data
      })
    })
  })

  // ============ 陪玩师等级API ============
  describe('companionLevelApi', () => {
    it('getPage 应发送GET请求到 /companion-levels/page', () => {
      const params = { current: 1 }
      companionLevelApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-levels/page',
        method: 'get',
        params
      })
    })

    it('getAll 应发送GET请求到 /companion-levels/all', () => {
      companionLevelApi.getAll()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-levels/all',
        method: 'get'
      })
    })

    it('getById 应发送GET请求到 /companion-levels/:id', () => {
      companionLevelApi.getById('l1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-levels/l1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /companion-levels', () => {
      const data = { name: '钻石' }
      companionLevelApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-levels',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /companion-levels', () => {
      const data = { id: 'l1', name: '王者' }
      companionLevelApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-levels',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /companion-levels/:id', () => {
      companionLevelApi.delete('l1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/companion-levels/l1',
        method: 'delete'
      })
    })
  })

  // ============ 关键词管理API ============
  describe('keywordApi', () => {
    it('getPage 应发送GET请求到 /keywords/page', () => {
      const params = { current: 1 }
      keywordApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/keywords/page',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /keywords/:id', () => {
      keywordApi.getById('k1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/keywords/k1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /keywords', () => {
      const data = { keyword: '价格' }
      keywordApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/keywords',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /keywords', () => {
      const data = { id: 'k1', keyword: '费用' }
      keywordApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/keywords',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /keywords/:id', () => {
      keywordApi.delete('k1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/keywords/k1',
        method: 'delete'
      })
    })

    it('refresh 应发送POST请求到 /keywords/refresh', () => {
      keywordApi.refresh()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/keywords/refresh',
        method: 'post'
      })
    })
  })

  // ============ 自动回复规则API ============
  describe('replyApi', () => {
    it('getPage 应发送GET请求到 /replies/page', () => {
      const params = { current: 1 }
      replyApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/replies/page',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /replies/:id', () => {
      replyApi.getById('r1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/replies/r1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /replies', () => {
      const data = { content: '自动回复内容' }
      replyApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/replies',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /replies', () => {
      const data = { id: 'r1', content: '更新内容' }
      replyApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/replies',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /replies/:id', () => {
      replyApi.delete('r1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/replies/r1',
        method: 'delete'
      })
    })
  })

  // ============ FAQ知识库API ============
  describe('faqItemApi', () => {
    it('getPage 应发送GET请求到 /faq-items', () => {
      const params = { current: 1 }
      faqItemApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/faq-items',
        method: 'get',
        params
      })
    })

    it('create 应发送POST请求到 /faq-items', () => {
      const data = { question: '如何退款？', answer: '请联系客服' }
      faqItemApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/faq-items',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /faq-items', () => {
      const data = { id: 'f1', question: '如何退款？' }
      faqItemApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/faq-items',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /faq-items/:id', () => {
      faqItemApi.delete('f1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/faq-items/f1',
        method: 'delete'
      })
    })
  })

  // ============ 游戏配置API ============
  describe('gameConfigApi', () => {
    it('getByClubId 应发送GET请求到 /game-configs/club/:clubConfigId', () => {
      gameConfigApi.getByClubId('club1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/game-configs/club/club1',
        method: 'get'
      })
    })

    it('getById 应发送GET请求到 /game-configs/:id', () => {
      gameConfigApi.getById('g1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/game-configs/g1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /game-configs', () => {
      const data = { name: '王者荣耀' }
      gameConfigApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/game-configs',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /game-configs', () => {
      const data = { id: 'g1', name: '和平精英' }
      gameConfigApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/game-configs',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /game-configs/:id', () => {
      gameConfigApi.delete('g1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/game-configs/g1',
        method: 'delete'
      })
    })
  })

  // ============ 服务项目API ============
  describe('serviceItemApi', () => {
    it('getByClubId 应发送GET请求到 /service-items/club/:clubConfigId', () => {
      serviceItemApi.getByClubId('club1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-items/club/club1',
        method: 'get'
      })
    })

    it('getByGameId 应发送GET请求到 /service-items/game/:gameConfigId', () => {
      serviceItemApi.getByGameId('g1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-items/game/g1',
        method: 'get'
      })
    })

    it('getPriceRules 应发送GET请求到 /service-items/:serviceItemId/price-rules', () => {
      serviceItemApi.getPriceRules('si1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-items/si1/price-rules',
        method: 'get'
      })
    })

    it('savePriceRule 应发送POST请求到 /service-items/price-rules', () => {
      const data = { serviceItemId: 'si1', price: 100 }
      serviceItemApi.savePriceRule(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-items/price-rules',
        method: 'post',
        data
      })
    })

    it('deletePriceRule 应发送DELETE请求到 /service-items/price-rules/:id', () => {
      serviceItemApi.deletePriceRule('pr1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/service-items/price-rules/pr1',
        method: 'delete'
      })
    })
  })

  // ============ 活动套餐API ============
  describe('activityPackageApi', () => {
    it('getByClubId 应发送GET请求到 /activity-packages/club/:clubConfigId', () => {
      activityPackageApi.getByClubId('club1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/activity-packages/club/club1',
        method: 'get'
      })
    })

    it('getActive 应发送GET请求到 /activity-packages/club/:clubConfigId/active', () => {
      activityPackageApi.getActive('club1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/activity-packages/club/club1/active',
        method: 'get'
      })
    })

    it('getById 应发送GET请求到 /activity-packages/:id', () => {
      activityPackageApi.getById('ap1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/activity-packages/ap1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /activity-packages', () => {
      const data = { name: '春节特惠' }
      activityPackageApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/activity-packages',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /activity-packages', () => {
      const data = { id: 'ap1', name: '元旦特惠' }
      activityPackageApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/activity-packages',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /activity-packages/:id', () => {
      activityPackageApi.delete('ap1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/activity-packages/ap1',
        method: 'delete'
      })
    })
  })

  // ============ 平台配置API ============
  describe('platformConfigApi', () => {
    it('getAll 应发送GET请求到 /platform-configs', () => {
      platformConfigApi.getAll()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/platform-configs',
        method: 'get'
      })
    })

    it('getByPlatform 应发送GET请求到 /platform-configs/:platform', () => {
      platformConfigApi.getByPlatform('wechat')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/platform-configs/wechat',
        method: 'get'
      })
    })

    it('update 应发送PUT请求到 /platform-configs/:id', () => {
      const data = { apiKey: 'xxx' }
      platformConfigApi.update('wechat', data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/platform-configs/wechat',
        method: 'put',
        data
      })
    })
  })

  // ============ 俱乐部配置API ============
  describe('clubConfigApi', () => {
    it('get 应发送GET请求到 /club-config', () => {
      clubConfigApi.get()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/club-config',
        method: 'get'
      })
    })

    it('update 应发送PUT请求到 /club-config', () => {
      const data = { name: '测试俱乐部' }
      clubConfigApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/club-config',
        method: 'put',
        data
      })
    })
  })

  // ============ AI配置API ============
  describe('aiConfigApi', () => {
    it('getAll 应发送GET请求到 /ai-config', () => {
      aiConfigApi.getAll()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/ai-config',
        method: 'get'
      })
    })

    it('update 应发送PUT请求到 /ai-config', () => {
      const data = { model: 'gpt-4' }
      aiConfigApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/ai-config',
        method: 'put',
        data
      })
    })
  })

  // ============ 运营统计API ============
  describe('statsApi', () => {
    it('getPersonal 应发送GET请求到 /stats/personal', () => {
      const params = { period: 'week' }
      statsApi.getPersonal(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/stats/personal',
        method: 'get',
        params
      })
    })

    it('getTeam 应发送GET请求到 /stats/team', () => {
      const params = { period: 'month' }
      statsApi.getTeam(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/stats/team',
        method: 'get',
        params
      })
    })

    it('getGlobal 应发送GET请求到 /stats/global', () => {
      const params = { period: 'year' }
      statsApi.getGlobal(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/stats/global',
        method: 'get',
        params
      })
    })
  })

  // ============ 客户生命周期API ============
  describe('lifecycleApi', () => {
    it('getAtRisk 应发送GET请求到 /customer-lifecycle/at-risk', () => {
      lifecycleApi.getAtRisk()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-lifecycle/at-risk',
        method: 'get'
      })
    })

    it('getChurned 应发送GET请求到 /customer-lifecycle/churned', () => {
      lifecycleApi.getChurned()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-lifecycle/churned',
        method: 'get'
      })
    })

    it('getStage 应发送GET请求到 /customer-lifecycle/stage/:userId', () => {
      lifecycleApi.getStage('u1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-lifecycle/stage/u1',
        method: 'get'
      })
    })

    it('updateTags 应发送POST请求到 /customer-lifecycle/update-tags', () => {
      lifecycleApi.updateTags()
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/customer-lifecycle/update-tags',
        method: 'post'
      })
    })
  })

  // ============ 客户满意度评价API ============
  describe('satisfactionApi', () => {
    it('submit 应发送POST请求到 /satisfaction', () => {
      const data = { rating: 5, feedback: '非常好' }
      satisfactionApi.submit(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/satisfaction',
        method: 'post',
        data
      })
    })

    it('getPage 应发送GET请求到 /satisfaction/page', () => {
      const params = { current: 1 }
      satisfactionApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/satisfaction/page',
        method: 'get',
        params
      })
    })

    it('getAverage 应发送GET请求到 /satisfaction/average/:companionId', () => {
      satisfactionApi.getAverage('c1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/satisfaction/average/c1',
        method: 'get'
      })
    })
  })

  // ============ 客服-客户分配API ============
  describe('csUserCustomerApi', () => {
    it('getPage 应发送GET请求到 /cs-user-customer/page', () => {
      const params = { current: 1 }
      csUserCustomerApi.getPage(params)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/cs-user-customer/page',
        method: 'get',
        params
      })
    })

    it('getById 应发送GET请求到 /cs-user-customer/:id', () => {
      csUserCustomerApi.getById('1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/cs-user-customer/1',
        method: 'get'
      })
    })

    it('create 应发送POST请求到 /cs-user-customer', () => {
      const data = { csUserId: 'u1', customerId: 'c1' }
      csUserCustomerApi.create(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/cs-user-customer',
        method: 'post',
        data
      })
    })

    it('update 应发送PUT请求到 /cs-user-customer', () => {
      const data = { id: '1', csUserId: 'u2' }
      csUserCustomerApi.update(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/cs-user-customer',
        method: 'put',
        data
      })
    })

    it('delete 应发送DELETE请求到 /cs-user-customer/:id', () => {
      csUserCustomerApi.delete('1')
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/cs-user-customer/1',
        method: 'delete'
      })
    })
  })

  // ============ 对话测试API ============
  describe('chatTestApi', () => {
    it('send 应发送POST请求到 /chat-test/send，超时45秒', () => {
      const data = { message: '你好' }
      chatTestApi.send(data)
      expect(mockRequest).toHaveBeenCalledWith({
        url: '/chat-test/send',
        method: 'post',
        data,
        timeout: 45000
      })
    })
  })

  // ============ 工具函数 ============
  describe('uploadExcel', () => {
    it('应发送POST请求，携带FormData和超时配置', () => {
      const file = new File(['test'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      uploadExcel('/import', file)
      expect(mockRequest).toHaveBeenCalledWith(
        expect.objectContaining({
          url: '/import',
          method: 'post',
          timeout: 60000
        })
      )
      /** 验证data是FormData实例 */
      const callArgs = (mockRequest.mock.calls[0] as Array<Record<string, unknown>>)[0]
      expect(callArgs.data).toBeInstanceOf(FormData)
    })
  })

  // ============ API模块导出完整性 ============
  describe('API模块导出完整性', () => {
    it('所有API模块应正确导出', () => {
      expect(authApi).toBeDefined()
      expect(sysUserApi).toBeDefined()
      expect(messageApi).toBeDefined()
      expect(pendingMessageApi).toBeDefined()
      expect(customerApi).toBeDefined()
      expect(customerProfileApi).toBeDefined()
      expect(orderApi).toBeDefined()
      expect(workOrderApi).toBeDefined()
      expect(serviceTrackApi).toBeDefined()
      expect(companionApi).toBeDefined()
      expect(companionScheduleApi).toBeDefined()
      expect(companionLevelApi).toBeDefined()
      expect(keywordApi).toBeDefined()
      expect(replyApi).toBeDefined()
      expect(faqItemApi).toBeDefined()
      expect(gameConfigApi).toBeDefined()
      expect(serviceItemApi).toBeDefined()
      expect(activityPackageApi).toBeDefined()
      expect(platformConfigApi).toBeDefined()
      expect(clubConfigApi).toBeDefined()
      expect(aiConfigApi).toBeDefined()
      expect(statsApi).toBeDefined()
      expect(lifecycleApi).toBeDefined()
      expect(satisfactionApi).toBeDefined()
      expect(csUserCustomerApi).toBeDefined()
      expect(chatTestApi).toBeDefined()
    })

    it('工具函数应正确导出', () => {
      expect(downloadExcel).toBeDefined()
      expect(typeof downloadExcel).toBe('function')
      expect(uploadExcel).toBeDefined()
      expect(typeof uploadExcel).toBe('function')
    })
  })
})
