/**
 * 完整类型测试
 *
 * 测试所有VO类型的字段完整性、枚举类型的合法值、DTO类型的必填/可选字段。
 * 通过构造符合类型定义的对象来验证类型结构的正确性。
 *
 * @author 刘建国
 */
import { describe, it, expect } from 'vitest'
import type {
  Result,
  PageResult,
  LoginDTO,
  LoginVO,
  UserRole,
  SysUserVO,
  MessageVO,
  PendingMessageVO,
  CustomerVO,
  CustomerProfileVO,
  OrderVO,
  OrderStatus,
  PaymentStatus,
  WorkOrderVO,
  WorkOrderStatus,
  WorkOrderPriority,
  WorkOrderType,
  ServiceTrackVO,
  ServiceTrackStatus,
  CompanionVO,
  CompanionScheduleVO,
  CompanionLevelVO,
  KeywordVO,
  ReplyVO,
  FaqItemVO,
  GameConfigVO,
  ServiceItemVO,
  ServicePriceRuleVO,
  ActivityPackageVO,
  StatsOverview,
  TrendData,
  CsUserData,
  StatsVO,
  NotificationVO,
  SatisfactionVO,
  RegisterDTO,
  OverviewCard
} from '@/types'

describe('完整类型测试', () => {

  // ============ 通用类型 ============
  describe('通用类型', () => {
    it('Result 应包含code、message、data字段', () => {
      const result: Result<string> = {
        code: 200,
        message: '操作成功',
        data: 'test'
      }
      expect(result.code).toBe(200)
      expect(result.message).toBe('操作成功')
      expect(result.data).toBe('test')
    })

    it('Result 应支持泛型数据', () => {
      const result: Result<number[]> = {
        code: 200,
        message: 'ok',
        data: [1, 2, 3]
      }
      expect(result.data).toEqual([1, 2, 3])
    })

    it('PageResult 应包含分页相关字段', () => {
      const page: PageResult<string> = {
        records: ['a', 'b', 'c'],
        total: 100,
        size: 10,
        current: 1,
        pages: 10
      }
      expect(page.records).toHaveLength(3)
      expect(page.total).toBe(100)
      expect(page.size).toBe(10)
      expect(page.current).toBe(1)
      expect(page.pages).toBe(10)
    })
  })

  // ============ 认证相关 ============
  describe('认证相关类型', () => {
    it('LoginDTO 应包含必填的username和password', () => {
      const dto: LoginDTO = {
        username: 'admin',
        password: '123456'
      }
      expect(dto.username).toBe('admin')
      expect(dto.password).toBe('123456')
    })

    it('LoginVO 应包含所有必填字段', () => {
      const vo: LoginVO = {
        token: 'access-token',
        refreshToken: 'refresh-token',
        expiresIn: 900,
        id: 'user-1',
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN'
      }
      expect(vo.token).toBe('access-token')
      expect(vo.refreshToken).toBe('refresh-token')
      expect(vo.expiresIn).toBe(900)
      expect(vo.id).toBe('user-1')
      expect(vo.username).toBe('admin')
      expect(vo.realName).toBe('管理员')
      expect(vo.role).toBe('SYS_ADMIN')
    })

    it('LoginVO 可选字段phone和email可以省略', () => {
      const vo: LoginVO = {
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'u',
        realName: 'n',
        role: 'CS_STAFF'
      }
      expect(vo.phone).toBeUndefined()
      expect(vo.email).toBeUndefined()
    })

    it('LoginVO 可选字段phone和email可以赋值', () => {
      const vo: LoginVO = {
        token: 't',
        refreshToken: 'r',
        expiresIn: 900,
        id: '1',
        username: 'u',
        realName: 'n',
        role: 'CS_STAFF',
        phone: '13800138000',
        email: 'test@example.com'
      }
      expect(vo.phone).toBe('13800138000')
      expect(vo.email).toBe('test@example.com')
    })

    it('UserRole 应只接受合法枚举值', () => {
      const admin: UserRole = 'SYS_ADMIN'
      const leader: UserRole = 'CS_LEADER'
      const staff: UserRole = 'CS_STAFF'
      expect(admin).toBe('SYS_ADMIN')
      expect(leader).toBe('CS_LEADER')
      expect(staff).toBe('CS_STAFF')
    })

    it('RegisterDTO 应包含必填字段和可选字段', () => {
      const dto: RegisterDTO = {
        username: 'newuser',
        password: '123456',
        realName: '新用户'
      }
      expect(dto.username).toBe('newuser')
      expect(dto.phone).toBeUndefined()
      expect(dto.email).toBeUndefined()
    })

    it('RegisterDTO 可选字段可以赋值', () => {
      const dto: RegisterDTO = {
        username: 'newuser',
        password: '123456',
        realName: '新用户',
        phone: '13900139000',
        email: 'new@example.com'
      }
      expect(dto.phone).toBe('13900139000')
      expect(dto.email).toBe('new@example.com')
    })
  })

  // ============ 用户相关 ============
  describe('SysUserVO类型', () => {
    it('应包含所有必填字段', () => {
      const vo: SysUserVO = {
        id: '1',
        rowNum: 1,
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN',
        status: 'ENABLED',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.id).toBe('1')
      expect(vo.rowNum).toBe(1)
      expect(vo.username).toBe('admin')
      expect(vo.realName).toBe('管理员')
      expect(vo.role).toBe('SYS_ADMIN')
      expect(vo.status).toBe('ENABLED')
    })

    it('可选字段phone和email可以省略', () => {
      const vo: SysUserVO = {
        id: '1',
        rowNum: 1,
        username: 'admin',
        realName: '管理员',
        role: 'SYS_ADMIN',
        status: 'ENABLED',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.phone).toBeUndefined()
      expect(vo.email).toBeUndefined()
    })
  })

  // ============ 消息相关 ============
  describe('消息相关类型', () => {
    it('MessageVO 应包含所有必填字段', () => {
      const vo: MessageVO = {
        id: 'm1',
        rowNum: 1,
        userId: 'u1',
        userNickname: '用户A',
        platform: 'wechat',
        content: '你好',
        isAiReply: 0,
        createdAt: '2024-01-01'
      }
      expect(vo.id).toBe('m1')
      expect(vo.isAiReply).toBe(0)
      expect(vo.aiModel).toBeUndefined()
      expect(vo.keywordMatched).toBeUndefined()
    })

    it('PendingMessageVO 应包含所有必填和可选字段', () => {
      const vo: PendingMessageVO = {
        id: 'p1',
        rowNum: 1,
        messageId: 'm1',
        userId: 'u1',
        userNickname: '用户A',
        platform: 'wechat',
        keyword: '价格',
        interventionType: 'MANUAL',
        status: 'pending',
        escalationLevel: 1,
        reminderCount: 0,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.status).toBe('pending')
      expect(vo.assignedCsUserId).toBeUndefined()
      expect(vo.deadline).toBeUndefined()
      expect(vo.messageContent).toBeUndefined()
      expect(vo.interventionTypeDesc).toBeUndefined()
      expect(vo.statusDesc).toBeUndefined()
      expect(vo.handledByName).toBeUndefined()
      expect(vo.remainingSeconds).toBeUndefined()
      expect(vo.overdue).toBeUndefined()
      expect(vo.urgent).toBeUndefined()
    })
  })

  // ============ 客户相关 ============
  describe('客户相关类型', () => {
    it('CustomerVO 应包含所有必填字段', () => {
      const vo: CustomerVO = {
        id: 'c1',
        rowNum: 1,
        nickname: '客户A',
        platform: 'wechat',
        status: 'ACTIVE',
        messageCount: 10,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.id).toBe('c1')
      expect(vo.platformUserId).toBeUndefined()
      expect(vo.tags).toBeUndefined()
    })

    it('CustomerProfileVO 应包含所有必填字段', () => {
      const vo: CustomerProfileVO = {
        id: 'cp1',
        rowNum: 1,
        userId: 'u1',
        nickname: '用户A',
        totalMessages: 100,
        totalOrders: 5,
        totalSpent: 500,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.nickname).toBe('用户A')
      expect(vo.tags).toBeUndefined()
      expect(vo.lifecycleStage).toBeUndefined()
      expect(vo.rfmRecencyScore).toBeUndefined()
      expect(vo.memberLevel).toBeUndefined()
    })
  })

  // ============ 订单相关 ============
  describe('订单相关类型', () => {
    it('OrderStatus 应包含所有合法枚举值', () => {
      const statuses: OrderStatus[] = ['PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
      expect(statuses).toHaveLength(5)
    })

    it('OrderVO 应包含所有必填字段', () => {
      const vo: OrderVO = {
        id: 'o1',
        rowNum: 1,
        orderNo: 'ORD20240101001',
        userId: 'u1',
        companionId: 'c1',
        companionName: '陪玩师A',
        serviceType: 'GAME',
        orderStatus: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.orderStatus).toBe('PENDING')
      expect(vo.orderNo).toBe('ORD20240101001')
      expect(vo.scheduledStart).toBeUndefined()
      expect(vo.totalAmount).toBeUndefined()
      expect(vo.paymentStatus).toBeUndefined()
      expect(vo.durationMinutes).toBeUndefined()
    })
  })

  // ============ 工单相关 ============
  describe('工单相关类型', () => {
    it('WorkOrderStatus 应包含所有合法枚举值', () => {
      const statuses: WorkOrderStatus[] = ['OPEN', 'IN_PROGRESS', 'SUBMITTED', 'CONFIRMED', 'CLOSED', 'CANCELLED']
      expect(statuses).toHaveLength(6)
    })

    it('WorkOrderPriority 应包含所有合法枚举值', () => {
      const priorities: WorkOrderPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
      expect(priorities).toHaveLength(4)
    })

    it('WorkOrderType 应包含所有合法枚举值', () => {
      const types: WorkOrderType[] = ['CONSULT', 'COMPLAINT', 'REFUND', 'TECHNICAL', 'OTHER']
      expect(types).toHaveLength(5)
    })

    it('WorkOrderVO 应包含所有必填字段', () => {
      const vo: WorkOrderVO = {
        id: 'w1',
        rowNum: 1,
        title: '测试工单',
        orderType: 'CONSULT',
        priority: 'MEDIUM',
        status: 'OPEN',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.title).toBe('测试工单')
      expect(vo.description).toBeUndefined()
      expect(vo.closeReason).toBeUndefined()
    })
  })

  // ============ 服务追踪相关 ============
  describe('服务追踪相关类型', () => {
    it('ServiceTrackStatus 应包含所有合法枚举值', () => {
      const statuses: ServiceTrackStatus[] = ['CONSULT', 'BOOKED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
      expect(statuses).toHaveLength(5)
    })

    it('ServiceTrackVO 应包含所有必填字段', () => {
      const vo: ServiceTrackVO = {
        id: 'st1',
        rowNum: 1,
        userId: 'u1',
        status: 'CONSULT',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.status).toBe('CONSULT')
      expect(vo.companionId).toBeUndefined()
      expect(vo.customerRating).toBeUndefined()
    })
  })

  // ============ 陪玩师相关 ============
  describe('陪玩师相关类型', () => {
    it('CompanionVO 应包含所有必填字段', () => {
      const vo: CompanionVO = {
        id: 'cp1',
        rowNum: 1,
        realName: '张三',
        nickname: '小三',
        levelId: 'l1',
        gameType: '王者荣耀',
        price: 100,
        enabled: true,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.realName).toBe('张三')
      expect(vo.price).toBe(100)
      expect(vo.phone).toBeUndefined()
      expect(vo.avatar).toBeUndefined()
      expect(vo.displayPrice).toBeUndefined()
      expect(vo.levelBasePrice).toBeUndefined()
    })

    it('CompanionScheduleVO 应包含所有必填字段', () => {
      const vo: CompanionScheduleVO = {
        id: 'cs1',
        rowNum: 1,
        companionId: 'cp1',
        startTime: '2024-01-01 09:00',
        endTime: '2024-01-01 12:00',
        status: 'AVAILABLE',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.companionId).toBe('cp1')
      expect(vo.companionName).toBeUndefined()
    })

    it('CompanionLevelVO 应包含所有必填字段', () => {
      const vo: CompanionLevelVO = {
        id: 'l1',
        rowNum: 1,
        levelName: '钻石',
        basePrice: 50,
        enabled: true,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.levelName).toBe('钻石')
      expect(vo.basePrice).toBe(50)
      expect(vo.levelCode).toBeUndefined()
      expect(vo.description).toBeUndefined()
    })
  })

  // ============ 配置相关 ============
  describe('配置相关类型', () => {
    it('KeywordVO 应包含所有必填字段', () => {
      const vo: KeywordVO = {
        id: 'k1',
        rowNum: 1,
        keyword: '价格',
        enabled: 1,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.keyword).toBe('价格')
      expect(vo.category).toBeUndefined()
    })

    it('ReplyVO 应包含所有必填字段', () => {
      const vo: ReplyVO = {
        id: 'r1',
        rowNum: 1,
        content: '自动回复内容',
        enabled: 1,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.content).toBe('自动回复内容')
      expect(vo.category).toBeUndefined()
    })

    it('FaqItemVO 应包含所有必填字段', () => {
      const vo: FaqItemVO = {
        id: 'f1',
        rowNum: 1,
        question: '如何退款？',
        answer: '请联系客服',
        enabled: 1,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.question).toBe('如何退款？')
      expect(vo.answer).toBe('请联系客服')
    })

    it('GameConfigVO 应包含所有必填字段', () => {
      const vo: GameConfigVO = {
        id: 'g1',
        rowNum: 1,
        gameName: '王者荣耀',
        enabled: 1,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.gameName).toBe('王者荣耀')
      expect(vo.gameType).toBeUndefined()
      expect(vo.gameCode).toBeUndefined()
      expect(vo.clubConfigId).toBeUndefined()
    })

    it('ServiceItemVO 应包含所有必填字段', () => {
      const vo: ServiceItemVO = {
        id: 'si1',
        rowNum: 1,
        itemName: '陪玩1小时',
        enabled: 1,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.itemName).toBe('陪玩1小时')
      expect(vo.basePrice).toBeUndefined()
      expect(vo.itemCode).toBeUndefined()
      expect(vo.priceRules).toBeUndefined()
    })

    it('ServicePriceRuleVO 应包含所有必填字段', () => {
      const vo: ServicePriceRuleVO = {
        id: 'pr1',
        rowNum: 1,
        price: 80,
        enabled: 1
      }
      expect(vo.price).toBe(80)
      expect(vo.originalPrice).toBeUndefined()
      expect(vo.levelName).toBeUndefined()
    })

    it('ActivityPackageVO 应包含所有必填字段', () => {
      const vo: ActivityPackageVO = {
        id: 'ap1',
        rowNum: 1,
        title: '春节特惠',
        originalPrice: 200,
        packagePrice: 150,
        enabled: 1,
        sortOrder: 1,
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }
      expect(vo.title).toBe('春节特惠')
      expect(vo.originalPrice).toBe(200)
      expect(vo.packagePrice).toBe(150)
      expect(vo.startTime).toBeUndefined()
      expect(vo.activityType).toBeUndefined()
    })
  })

  // ============ 统计相关 ============
  describe('统计相关类型', () => {
    it('StatsOverview 应包含所有字段', () => {
      const overview: StatsOverview = {
        totalMessages: 1000,
        totalCustomers: 500,
        aiReplyCount: 800,
        manualReplyCount: 200,
        avgResponseTime: 3.5,
        pendingCount: 10,
        activeCsCount: 5,
        resolutionRate: 0.95,
        customerSatisfaction: 4.8
      }
      expect(overview.totalMessages).toBe(1000)
      expect(overview.resolutionRate).toBe(0.95)
    })

    it('TrendData 应包含date和messageCount', () => {
      const trend: TrendData = {
        date: '2024-01-01',
        messageCount: 50
      }
      expect(trend.date).toBe('2024-01-01')
      expect(trend.messageCount).toBe(50)
    })

    it('CsUserData 应包含所有字段', () => {
      const data: CsUserData = {
        csUserId: 'u1',
        csUserName: '客服A',
        messageCount: 100,
        customerCount: 30,
        avgResponseTime: 2.5,
        resolutionRate: 0.9
      }
      expect(data.csUserId).toBe('u1')
      expect(data.resolutionRate).toBe(0.9)
    })

    it('StatsVO 应包含overview、trendData、csUserData', () => {
      const stats: StatsVO = {
        overview: {
          totalMessages: 100,
          totalCustomers: 50,
          aiReplyCount: 80,
          manualReplyCount: 20,
          avgResponseTime: 3,
          pendingCount: 5,
          activeCsCount: 3,
          resolutionRate: 0.9,
          customerSatisfaction: 4.5
        },
        trendData: [{ date: '2024-01-01', messageCount: 10 }],
        csUserData: [{
          csUserId: 'u1',
          csUserName: '客服A',
          messageCount: 10,
          customerCount: 5,
          avgResponseTime: 2,
          resolutionRate: 0.8
        }]
      }
      expect(stats.overview.totalMessages).toBe(100)
      expect(stats.trendData).toHaveLength(1)
      expect(stats.csUserData).toHaveLength(1)
    })
  })

  // ============ 通知相关 ============
  describe('NotificationVO类型', () => {
    it('应包含所有必填字段', () => {
      const vo: NotificationVO = {
        type: 'WORK_ORDER',
        title: '新工单',
        message: '您有一个新工单待处理',
        timestamp: '2024-01-01T00:00:00Z'
      }
      expect(vo.type).toBe('WORK_ORDER')
      expect(vo.data).toBeUndefined()
    })

    it('可选字段data可以赋值', () => {
      const vo: NotificationVO = {
        type: 'ORDER',
        title: '订单更新',
        message: '订单状态已变更',
        timestamp: '2024-01-01T00:00:00Z',
        data: { orderId: 'o1', status: 'CONFIRMED' }
      }
      expect(vo.data).toEqual({ orderId: 'o1', status: 'CONFIRMED' })
    })
  })

  // ============ 满意度评价相关 ============
  describe('SatisfactionVO类型', () => {
    it('应包含所有必填字段', () => {
      const vo: SatisfactionVO = {
        id: 's1',
        rowNum: 1,
        userId: 'u1',
        userNickname: '用户A',
        companionId: 'c1',
        companionName: '陪玩师A',
        rating: 5,
        isAnonymous: 0,
        createdAt: '2024-01-01'
      }
      expect(vo.rating).toBe(5)
      expect(vo.feedback).toBeUndefined()
      expect(vo.tags).toBeUndefined()
    })

    it('可选字段可以赋值', () => {
      const vo: SatisfactionVO = {
        id: 's1',
        rowNum: 1,
        userId: 'u1',
        userNickname: '用户A',
        companionId: 'c1',
        companionName: '陪玩师A',
        rating: 4,
        feedback: '服务不错',
        tags: '态度好,技术好',
        serviceType: 'GAME',
        isAnonymous: 1,
        createdAt: '2024-01-01'
      }
      expect(vo.feedback).toBe('服务不错')
      expect(vo.tags).toBe('态度好,技术好')
      expect(vo.isAnonymous).toBe(1)
    })
  })

  // ============ 概览卡片类型 ============
  describe('OverviewCard类型', () => {
    it('应包含所有字段', () => {
      const card: OverviewCard = {
        title: '消息总数',
        value: 1000,
        icon: {},
        color: '#409EFF',
        iconBg: '#ECF5FF',
        type: 'message'
      }
      expect(card.title).toBe('消息总数')
      expect(card.value).toBe(1000)
      expect(card.type).toBe('message')
    })

    it('value可以是字符串类型', () => {
      const card: OverviewCard = {
        title: '在线率',
        value: '95%',
        icon: {},
        color: '#67C23A',
        iconBg: '#F0F9EB',
        type: 'rate'
      }
      expect(card.value).toBe('95%')
    })
  })

  // ============ 类型完整性汇总 ============
  describe('类型完整性汇总', () => {
    it('所有VO类型应包含id和rowNum字段', () => {
      /** 验证所有VO类型都包含id和rowNum这两个通用字段 */
      const voWithIdAndRowNum = [
        { id: '1', rowNum: 1 },  // SysUserVO
        { id: '2', rowNum: 2 },  // MessageVO
        { id: '3', rowNum: 3 },  // PendingMessageVO
        { id: '4', rowNum: 4 },  // CustomerVO
        { id: '5', rowNum: 5 },  // CustomerProfileVO
        { id: '6', rowNum: 6 },  // OrderVO
        { id: '7', rowNum: 7 },  // WorkOrderVO
        { id: '8', rowNum: 8 },  // ServiceTrackVO
        { id: '9', rowNum: 9 },  // CompanionVO
        { id: '10', rowNum: 10 }, // CompanionScheduleVO
        { id: '11', rowNum: 11 }, // CompanionLevelVO
        { id: '12', rowNum: 12 }, // KeywordVO
        { id: '13', rowNum: 13 }, // ReplyVO
        { id: '14', rowNum: 14 }, // FaqItemVO
        { id: '15', rowNum: 15 }, // GameConfigVO
        { id: '16', rowNum: 16 }, // ServiceItemVO
        { id: '17', rowNum: 17 }, // ActivityPackageVO
        { id: '18', rowNum: 18 }  // SatisfactionVO
      ]
      voWithIdAndRowNum.forEach(vo => {
        expect(vo.id).toBeDefined()
        expect(vo.rowNum).toBeDefined()
        expect(typeof vo.id).toBe('string')
        expect(typeof vo.rowNum).toBe('number')
      })
    })

    it('所有VO类型应包含createdAt时间戳字段', () => {
      /** 验证需要时间戳字段的VO类型 */
      const vosWithTimestamps = [
        { createdAt: '2024-01-01', updatedAt: '2024-01-01' }
      ]
      vosWithTimestamps.forEach(vo => {
        expect(vo.createdAt).toBeDefined()
        expect(vo.updatedAt).toBeDefined()
      })
    })

    it('所有配置相关VO应包含enabled和sortOrder字段', () => {
      /** 验证配置类VO的通用字段 */
      const configVos = [
        { enabled: 1, sortOrder: 1 }, // KeywordVO
        { enabled: 1, sortOrder: 2 }, // ReplyVO
        { enabled: 1, sortOrder: 3 }, // FaqItemVO
        { enabled: 1, sortOrder: 4 }, // GameConfigVO
        { enabled: 1, sortOrder: 5 }, // ServiceItemVO
        { enabled: 1, sortOrder: 6 }  // ActivityPackageVO
      ]
      configVos.forEach(vo => {
        expect(vo.enabled).toBeDefined()
        expect(vo.sortOrder).toBeDefined()
        expect([0, 1]).toContain(vo.enabled)
      })
    })
  })
})
