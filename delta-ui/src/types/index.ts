/**
 * 核心业务类型定义
 *
 * 定义所有核心业务实体的TypeScript类型，包括：
 * - 通用类型（Result、PageResult）
 * - 认证相关（LoginDTO、LoginVO、UserRole）
 * - 用户相关（SysUserVO）
 * - 消息相关（MessageVO、PendingMessageVO）
 * - 客户相关（CustomerVO、CustomerProfileVO）
 * - 订单相关（OrderVO）
 * - 工单相关（WorkOrderVO）
 * - 服务追踪相关（ServiceTrackVO）
 * - 陪玩师相关（CompanionVO、CompanionScheduleVO、CompanionLevelVO）
 * - 配置相关（KeywordVO、ReplyVO、FaqItemVO、GameConfigVO、ServiceItemVO、ActivityPackageVO）
 * - 统计相关（StatsOverview、TrendData、CsUserData、StatsVO）
 * - 通知相关（NotificationVO）
 *
 * @author 刘建国
 */

// ============ 通用类型 ============

/** 统一响应结果 */
export interface Result<T = unknown> {
  /** 响应状态码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
}

/** 分页查询结果 */
export interface PageResult<T> {
  /** 数据记录列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 每页大小 */
  size: number
  /** 当前页码 */
  current: number
  /** 总页数 */
  pages: number
}

// ============ 认证相关 ============

/** 登录请求参数 */
export interface LoginDTO {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
}

/** 登录响应数据 */
export interface LoginVO {
  /** 访问令牌 */
  token: string
  /** 刷新令牌 */
  refreshToken: string
  /** 过期时间（秒） */
  expiresIn: number
  /** 用户ID */
  id: string
  /** 用户名 */
  username: string
  /** 真实姓名 */
  realName: string
  /** 用户角色 */
  role: UserRole
  /** 手机号 */
  phone?: string
  /** 邮箱 */
  email?: string
}

/** 用户角色枚举 */
export type UserRole = 'SYS_ADMIN' | 'CS_LEADER' | 'CS_STAFF'

// ============ 用户相关 ============

/** 系统用户视图对象 */
export interface SysUserVO {
  /** 用户ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 用户名 */
  username: string
  /** 真实姓名 */
  realName: string
  /** 手机号 */
  phone?: string
  /** 邮箱 */
  email?: string
  /** 用户角色 */
  role: UserRole
  /** 用户状态 */
  status: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 消息相关 ============

/** 消息视图对象 */
export interface MessageVO {
  /** 消息ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 用户ID */
  userId: string
  /** 用户昵称 */
  userNickname: string
  /** 平台标识 */
  platform: string
  /** 消息内容 */
  content: string
  /** 是否AI回复（0否1是） */
  isAiReply: number
  /** AI模型名称 */
  aiModel?: string
  /** 匹配的关键词 */
  keywordMatched?: string
  /** 回复类型 */
  replyType?: string
  /** 创建时间 */
  createdAt: string
}

/** 待处理消息视图对象 */
export interface PendingMessageVO {
  /** 待处理消息ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 原始消息ID */
  messageId: string
  /** 用户ID */
  userId: string
  /** 用户昵称 */
  userNickname: string
  /** 平台标识 */
  platform: string
  /** 触发关键词 */
  keyword: string
  /** 介入类型 */
  interventionType: string
  /** 处理状态 */
  status: PendingMessageStatus
  /** 处理截止时间 */
  deadline?: string
  /** 升级级别 */
  escalationLevel: number
  /** 分配的客服ID */
  assignedCsUserId?: string
  /** 分配的客服姓名 */
  assignedCsUserName?: string
  /** 提醒次数 */
  reminderCount: number
  /** 处理人 */
  handledBy?: string
  /** 处理时间 */
  handledAt?: string
  /** 备注 */
  remark?: string
  /** 上下文摘要 */
  contextSummary?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 待处理消息状态枚举 */
export type PendingMessageStatus = 'pending' | 'processing' | 'resolved'

// ============ 客户相关 ============

/** 客户视图对象 */
export interface CustomerVO {
  /** 客户ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 平台用户ID */
  platformUserId?: string
  /** 昵称 */
  nickname: string
  /** 平台标识 */
  platform: string
  /** 客户状态 */
  status: string
  /** 首次消息时间 */
  firstMessageAt?: string
  /** 最后消息时间 */
  lastMessageAt?: string
  /** 消息总数 */
  messageCount: number
  /** 标签 */
  tags?: string
  /** 生命周期阶段 */
  lifecycleStage?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 客户画像视图对象 */
export interface CustomerProfileVO {
  /** 画像ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 用户ID */
  userId: string
  /** 用户昵称 */
  userNickname: string
  /** 画像类型 */
  profileType: string
  /** 画像数据 */
  profileData: string
  /** 标签 */
  tags?: string
  /** 生命周期阶段 */
  lifecycleStage?: string
  /** 消息总数 */
  totalMessages: number
  /** 订单总数 */
  totalOrders: number
  /** 消费总额 */
  totalSpent: number
  /** 最后活跃时间 */
  lastActiveAt?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 订单相关 ============

/** 订单状态枚举 */
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

/** 订单视图对象 */
export interface OrderVO {
  /** 订单ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 用户ID */
  userId: string
  /** 用户昵称 */
  userNickname: string
  /** 陪玩师ID */
  companionId: string
  /** 陪玩师名称 */
  companionName: string
  /** 服务类型 */
  serviceType: string
  /** 订单状态 */
  orderStatus: OrderStatus
  /** 预约开始时间 */
  scheduledStart?: string
  /** 预约结束时间 */
  scheduledEnd?: string
  /** 实际开始时间 */
  actualStart?: string
  /** 实际结束时间 */
  actualEnd?: string
  /** 总金额 */
  totalAmount?: number
  /** 游戏类型 */
  gameType?: string
  /** 备注 */
  remark?: string
  /** 来源 */
  source?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 工单相关 ============

/** 工单状态枚举 */
export type WorkOrderStatus = 'OPEN' | 'IN_PROGRESS' | 'SUBMITTED' | 'CONFIRMED' | 'CLOSED' | 'CANCELLED'

/** 工单优先级枚举 */
export type WorkOrderPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

/** 工单类型枚举 */
export type WorkOrderType = 'CONSULT' | 'COMPLAINT' | 'REFUND' | 'TECHNICAL' | 'OTHER'

/** 工单视图对象 */
export interface WorkOrderVO {
  /** 工单ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 工单标题 */
  title: string
  /** 工单描述 */
  description?: string
  /** 工单类型 */
  orderType: WorkOrderType
  /** 工单类型描述 */
  orderTypeDesc?: string
  /** 优先级 */
  priority: WorkOrderPriority
  /** 优先级描述 */
  priorityDesc?: string
  /** 工单状态 */
  status: WorkOrderStatus
  /** 状态描述 */
  statusDesc?: string
  /** 平台标识 */
  platform?: string
  /** 用户ID */
  userId?: string
  /** 用户昵称 */
  userNickname?: string
  /** 处理人ID */
  assignedToId?: string
  /** 处理人姓名 */
  assignedToName?: string
  /** 关闭原因 */
  closeReason?: string
  /** 取消原因 */
  cancelReason?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 服务追踪相关 ============

/** 服务追踪状态枚举 */
export type ServiceTrackStatus = 'CONSULT' | 'BOOKED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

/** 服务追踪视图对象 */
export interface ServiceTrackVO {
  /** 服务追踪ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 用户ID */
  userId: string
  /** 用户昵称 */
  userNickname?: string
  /** 关联工单ID */
  workOrderId?: string
  /** 陪玩师ID */
  companionId?: string
  /** 陪玩师名称 */
  companionName?: string
  /** 追踪状态 */
  status: ServiceTrackStatus
  /** 状态描述 */
  statusDesc?: string
  /** 咨询内容 */
  consultContent?: string
  /** 预约开始时间 */
  bookedStartTime?: string
  /** 预约结束时间 */
  bookedEndTime?: string
  /** 实际开始时间 */
  actualStartTime?: string
  /** 实际结束时间 */
  actualEndTime?: string
  /** 服务类型 */
  serviceType?: string
  /** 服务时长（分钟） */
  durationMinutes?: number
  /** 客户评分 */
  customerRating?: number
  /** 客户反馈 */
  customerFeedback?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 陪玩师相关 ============

/** 陪玩师视图对象 */
export interface CompanionVO {
  /** 陪玩师ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 真实姓名 */
  realName: string
  /** 昵称 */
  nickname: string
  /** 手机号 */
  phone?: string
  /** 微信号 */
  wechat?: string
  /** 等级ID */
  levelId: string
  /** 等级名称 */
  levelName?: string
  /** 头像URL */
  avatar?: string
  /** 游戏类型 */
  gameType: string
  /** 个人简介 */
  description?: string
  /** 价格 */
  price: number
  /** 是否启用（0否1是） */
  enabled: number
  /** 服务标签 */
  serviceTags?: string
  /** 支持的游戏 */
  supportedGames?: string
  /** KDA比率 */
  kdRatio?: number
  /** 段位等级 */
  rankLevel?: string
  /** 语音样本URL */
  voiceSampleUrl?: string
  /** 平均评分 */
  ratingAvg?: number
  /** 订单数量 */
  orderCount?: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 陪玩师排班视图对象 */
export interface CompanionScheduleVO {
  /** 排班ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 陪玩师ID */
  companionId: string
  /** 陪玩师名称 */
  companionName?: string
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 排班状态 */
  status: string
  /** 备注 */
  note?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 陪玩师等级视图对象 */
export interface CompanionLevelVO {
  /** 等级ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 等级名称 */
  name: string
  /** 等级描述 */
  description?: string
  /** 最低价格 */
  minPrice: number
  /** 最高价格 */
  maxPrice: number
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 配置相关 ============

/** 关键词视图对象 */
export interface KeywordVO {
  /** 关键词ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 关键词内容 */
  keyword: string
  /** 分类 */
  category?: string
  /** 回复类型 */
  replyType?: string
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 自动回复视图对象 */
export interface ReplyVO {
  /** 回复ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 分类 */
  category?: string
  /** 回复内容 */
  content: string
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** FAQ知识库视图对象 */
export interface FaqItemVO {
  /** FAQ ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 问题 */
  question: string
  /** 答案 */
  answer: string
  /** 分类 */
  category?: string
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 游戏配置视图对象 */
export interface GameConfigVO {
  /** 游戏配置ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 游戏名称 */
  name: string
  /** 游戏类型 */
  gameType?: string
  /** 游戏描述 */
  description?: string
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 服务项目视图对象 */
export interface ServiceItemVO {
  /** 服务项目ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 项目名称 */
  name: string
  /** 项目描述 */
  description?: string
  /** 价格 */
  price: number
  /** 时长（分钟） */
  durationMinutes?: number
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/** 活动套餐视图对象 */
export interface ActivityPackageVO {
  /** 活动套餐ID */
  id: string
  /** 行号 */
  rowNum: number
  /** 套餐名称 */
  name: string
  /** 套餐描述 */
  description?: string
  /** 原价 */
  originalPrice: number
  /** 套餐价 */
  packagePrice: number
  /** 是否启用（0否1是） */
  enabled: number
  /** 排序序号 */
  sortOrder: number
  /** 活动开始时间 */
  startTime?: string
  /** 活动结束时间 */
  endTime?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

// ============ 统计相关 ============

/** 统计概览数据 */
export interface StatsOverview {
  /** 消息总数 */
  totalMessages: number
  /** 客户总数 */
  totalCustomers: number
  /** AI回复数量 */
  aiReplyCount: number
  /** 人工回复数量 */
  manualReplyCount: number
  /** 平均响应时间（秒） */
  avgResponseTime: number
  /** 待处理数量 */
  pendingCount: number
  /** 活跃客服数量 */
  activeCsCount: number
  /** 解决率 */
  resolutionRate: number
  /** 客户满意度 */
  customerSatisfaction: number
}

/** 趋势数据 */
export interface TrendData {
  /** 日期 */
  date: string
  /** 消息数量 */
  messageCount: number
}

/** 客服用户数据 */
export interface CsUserData {
  /** 客服用户ID */
  csUserId: string
  /** 客服用户名 */
  csUserName: string
  /** 消息数量 */
  messageCount: number
  /** 服务客户数 */
  customerCount: number
  /** 平均响应时间（秒） */
  avgResponseTime: number
  /** 解决率 */
  resolutionRate: number
}

/** 统计数据视图对象 */
export interface StatsVO {
  /** 概览数据 */
  overview: StatsOverview
  /** 趋势数据列表 */
  trendData: TrendData[]
  /** 客服用户数据列表 */
  csUserData: CsUserData[]
}

// ============ 通知相关 ============

/** 通知视图对象 */
export interface NotificationVO {
  /** 通知类型 */
  type: string
  /** 通知标题 */
  title: string
  /** 通知内容 */
  message: string
  /** 时间戳 */
  timestamp: string
  /** 附加数据 */
  data?: Record<string, unknown>
}
