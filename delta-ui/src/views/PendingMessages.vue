<template>
  <div class="pending-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
          <div class="header-right">
            <el-button @click="handleExport" size="small" v-if="isAdminOrLeader">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-tag type="danger" effect="dark" size="small" v-if="overdueCount > 0">
              {{ overdueCount }} 条超时
            </el-tag>
            <el-tag type="warning" effect="dark" size="small" v-if="urgentCount > 0">
              {{ urgentCount }} 条紧急
            </el-tag>
          </div>
        </div>
      </template>

      <div class="filter-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="平台">
            <el-select v-model="queryParams.platform" placeholder="全部" clearable style="width:130px" :teleported="false">
              <el-option label="微信" value="wechat" />
              <el-option label="KOOK" value="kook" />
              <el-option label="YY" value="yy" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px" :teleported="false">
              <el-option label="待处理" value="pending" />
              <el-option label="处理中" value="processing" />
              <el-option label="已解决" value="resolved" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="queryParams.keyword" placeholder="搜索..." clearable style="width:140px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 虚拟滚动模式切换按钮 -->
      <div class="table-toolbar">
        <el-switch
          v-model="virtualMode"
          active-text="虚拟滚动"
          inactive-text="普通模式"
          :disabled="loading"
        />
        <el-alert
          v-if="total > 100 && !virtualMode"
          title="数据量较大，建议开启虚拟滚动模式以提升性能"
          type="info"
          :closable="false"
          show-icon
          class="virtual-hint"
        />
      </div>

      <!-- 普通表格模式 -->
      <el-table v-if="!virtualMode" :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column label="客户" width="140">
          <template #default="{ row }">
            <span class="customer-name">{{ row.userNickname }}</span>
            <el-tag size="small" :type="getPlatformType(row.platform)" class="platform-tag">{{ getPlatformText(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="介入类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getInterventionTypeTag(row.interventionType)">{{ row.interventionTypeDesc || row.interventionType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="messageContent" label="原始消息" show-overflow-tooltip min-width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusType(row.status)" effect="light">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="剩余时间" width="140">
          <template #default="{ row }">
            <template v-if="row.status === 'pending' || row.status === 'processing'">
              <span :class="getCountdownClass(row)">{{ formatCountdown(row) }}</span>
            </template>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="升级" width="80">
          <template #default="{ row }">
            <span v-if="row.escalationLevel >= 2" class="escalate-badge escalate-high">上报</span>
            <span v-else-if="row.escalationLevel >= 1" class="escalate-badge escalate-warn">警告</span>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="指派客服" width="100">
          <template #default="{ row }">
            <span>{{ row.assignedCsUserName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button link type="primary" size="small" @click="handleProcess(row, 'processing')">接手</el-button>
              <el-button link type="success" size="small" @click="handleProcess(row, 'resolved')">完成</el-button>
            </template>
            <template v-else-if="row.status === 'processing'">
              <el-button link type="success" size="small" @click="handleProcess(row, 'resolved')">完成</el-button>
            </template>
            <el-button link size="small" @click="handleViewCustomer(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 虚拟滚动表格模式 -->
      <div v-else class="virtual-table-wrapper" v-loading="loading">
        <el-auto-resizer>
          <template #default="{ height, width }">
            <el-table-v2
              :columns="v2Columns"
              :data="tableData"
              :width="width"
              :height="virtualTableHeight"
              :row-height="50"
              fixed
            />
          </template>
        </el-auto-resizer>
      </div>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { queryParams.pageNum = 1; handleQuery() }"
        @current-change="handleQuery"
      />
      <!-- 数据量较大时的提示信息 -->
      <el-alert
        v-if="total > 100"
        title="数据量较大，请使用筛选条件缩小范围"
        type="warning"
        :closable="false"
        show-icon
        class="data-hint"
      />
    </el-card>

    <el-dialog v-model="processDialogVisible" :title="processDialogTitle" width="560px">
      <el-form :model="processForm" label-width="90px">
        <el-form-item label="客户昵称" v-if="processForm.userNickname">
          <span>{{ processForm.userNickname }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(processForm.currentStatus)">{{ getStatusText(processForm.currentStatus) }}</el-tag>
          <span style="margin-left: 8px; color: var(--gu-text-muted);">→</span>
          <el-tag :type="getStatusType(processForm.status)" style="margin-left: 8px;">{{ getStatusText(processForm.status) }}</el-tag>
        </el-form-item>
        <el-form-item label="对话摘要" v-if="processForm.contextSummary">
          <div class="context-summary">{{ processForm.contextSummary }}</div>
        </el-form-item>
        <el-form-item :label="processForm.status === 'processing' ? '接手备注' : '处理结果'">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="3"
            :placeholder="processForm.status === 'processing' ? '记录接手情况，如：已联系客户...' : '请填写处理结果，如：问题已解决/已转交/需后续跟进...'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button
          :type="processForm.status === 'processing' ? 'primary' : 'success'"
          @click="submitProcess"
          :loading="submitLoading"
        >
          {{ processForm.status === 'processing' ? '确认接手' : '确认完成' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="customerDialogVisible" title="客户信息" width="520px">
      <div v-loading="customerLoading">
        <template v-if="customerInfo">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="客户昵称">{{ customerInfo.nickname || '—' }}</el-descriptions-item>
            <el-descriptions-item label="平台">
              <el-tag :type="getPlatformTagType(customerInfo.platform)" size="small">{{ getPlatformLabel(customerInfo.platform) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AI状态">
              <el-tag :type="customerInfo.aiEnabled ? 'success' : 'info'" size="small">
                {{ customerInfo.aiEnabled ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="分配客服">{{ customerInfo.assignedCsUserName || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="消息数">{{ customerInfo.messageCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="最后活跃">{{ customerInfo.lastActiveAt || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ customerInfo.createdAt || '—' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <el-empty v-else description="未找到客户信息" />
      </div>
      <template #footer>
        <el-button @click="customerDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, h, onMounted, onUnmounted, inject } from 'vue'
import { ElMessage, ElTag, ElButton } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { pendingMessageApi, customerApi, downloadExcel } from '@/api'

/** 加载状态 */
const loading = ref(false)
/** 提交加载状态 */
const submitLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
/** 数据总数 */
const total = ref(0)
/** 刷新待办数量方法（从父组件注入） */
const refreshPendingCount = inject('refreshPendingCount')
/** 当前时间戳（用于倒计时计算） */
const now = ref(Date.now())
/** 倒计时定时器 */
let countdownTimer = null
/** 是否开启虚拟滚动模式 */
const virtualMode = ref(false)
/** 虚拟表格高度 */
const virtualTableHeight = ref(500)

/** 用户信息（从localStorage读取） */
let userInfo = {}
try {
  userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
} catch (e) {}
/** 是否管理员或主管 */
const isAdminOrLeader = userInfo.role === 'SYS_ADMIN' || userInfo.role === 'CS_LEADER'

/** 查询参数 */
const queryParams = reactive({ pageNum: 1, pageSize: 20, status: null, platform: null, keyword: '' })
/** 处理对话框可见状态 */
const processDialogVisible = ref(false)
/** 处理表单数据 */
const processForm = reactive({ id: null, status: '', currentStatus: '', remark: '', contextSummary: '', userNickname: '' })

/** 客户信息对话框可见状态 */
const customerDialogVisible = ref(false)
/** 客户信息加载状态 */
const customerLoading = ref(false)
/** 客户信息数据 */
const customerInfo = ref(null)

/** 处理对话框标题 */
const processDialogTitle = computed(() => {
  if (processForm.status === 'processing') return '接手处理'
  if (processForm.status === 'resolved') return '完成处理'
  return '处理待办'
})

/**
 * 计算剩余秒数
 * @param {Object} row - 待办消息行数据
 * @returns {number} 剩余秒数
 */
const getRemaining = (row) => {
  if (!row.deadline) return 0
  const target = new Date(row.deadline).getTime()
  return Math.max(0, Math.floor((target - now.value) / 1000))
}

/** 超时数量 */
const overdueCount = computed(() => tableData.value.filter(r => r.status !== 'resolved' && getRemaining(r) <= 0).length)
/** 紧急数量 */
const urgentCount = computed(() => tableData.value.filter(r => r.status !== 'resolved' && getRemaining(r) > 0 && getRemaining(r) < 120).length)

/**
 * 获取状态标签类型
 * @param {string} s - 状态值
 * @returns {string} Tag类型
 */
const getStatusType = (s) => ({ pending: 'warning', processing: 'primary', resolved: 'success' }[s] || 'info')

/**
 * 获取状态显示文本
 * @param {string} s - 状态值
 * @returns {string} 状态文本
 */
const getStatusText = (s) => ({ pending: '待处理', processing: '处理中', resolved: '已解决' }[s] || s)

/**
 * 获取平台显示文本
 * @param {string} p - 平台标识
 * @returns {string} 平台文本
 */
const getPlatformText = (p) => ({ wechat: '微信', kook: 'KOOK', yy: 'YY' }[p] || p || '—')

/**
 * 获取平台标签类型（筛选栏用）
 * @param {string} p - 平台标识
 * @returns {string} Tag类型
 */
const getPlatformType = (p) => ({ wechat: 'primary', kook: 'success', yy: 'warning' }[p] || 'info')

/**
 * 获取介入类型标签类型
 * @param {string} t - 介入类型
 * @returns {string} Tag类型
 */
const getInterventionTypeTag = (t) => ({ ORDER: 'danger', SCHEDULE: 'warning', SPECIFIC_COMPANION: 'primary', COMPLAINT: 'danger', HUMAN_REQUEST: '' }[t] || 'info')

/**
 * 获取平台显示文本（客户信息对话框用）
 * @param {string} p - 平台标识
 * @returns {string} 平台文本
 */
const getPlatformLabel = (p) => ({ wechat: '微信', kook: 'KOOK', yy: 'YY' }[p] || p || '—')

/**
 * 获取平台标签类型（客户信息对话框用）
 * @param {string} p - 平台标识
 * @returns {string} Tag类型
 */
const getPlatformTagType = (p) => ({ wechat: 'primary', kook: 'success', yy: 'warning' }[p] || 'info')

/**
 * 格式化倒计时
 * @param {Object} row - 待办消息行数据
 * @returns {string} 倒计时文本
 */
const formatCountdown = (row) => {
  const s = getRemaining(row)
  if (s <= 0) return '已超时'
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? m + '分' + sec + '秒' : sec + '秒'
}

/**
 * 获取倒计时CSS类名
 * @param {Object} row - 待办消息行数据
 * @returns {string} CSS类名
 */
const getCountdownClass = (row) => {
  const s = getRemaining(row)
  if (s <= 0) return 'countdown overdue'
  if (s < 120) return 'countdown urgent'
  return 'countdown'
}

/**
 * el-table-v2 列定义（虚拟滚动模式使用）
 * 使用 cellRenderer 渲染自定义内容
 */
const v2Columns = computed(() => [
  { key: 'id', dataKey: 'id', title: '编号', width: 70 },
  {
    key: 'userNickname',
    dataKey: 'userNickname',
    title: '客户',
    width: 140,
    /** 客户列渲染：昵称+平台Tag */
    cellRenderer: ({ row }) => h('div', { style: 'display:flex;align-items:center;gap:4px' }, [
      h('span', { class: 'customer-name' }, row.userNickname),
      h(ElTag, { size: 'small', type: getPlatformType(row.platform), class: 'platform-tag' }, () => getPlatformText(row.platform))
    ])
  },
  {
    key: 'interventionType',
    dataKey: 'interventionType',
    title: '介入类型',
    width: 120,
    /** 介入类型列渲染 */
    cellRenderer: ({ row }) => h(ElTag, { size: 'small', type: getInterventionTypeTag(row.interventionType) }, () => row.interventionTypeDesc || row.interventionType)
  },
  { key: 'messageContent', dataKey: 'messageContent', title: '原始消息', width: 180 },
  {
    key: 'status',
    dataKey: 'status',
    title: '状态',
    width: 110,
    /** 状态列渲染 */
    cellRenderer: ({ cellData }) => h(ElTag, { size: 'small', type: getStatusType(cellData), effect: 'light' }, () => getStatusText(cellData))
  },
  {
    key: 'deadline',
    dataKey: 'deadline',
    title: '剩余时间',
    width: 140,
    /** 剩余时间列渲染 */
    cellRenderer: ({ row }) => {
      if (row.status === 'pending' || row.status === 'processing') {
        return h('span', { class: getCountdownClass(row) }, formatCountdown(row))
      }
      return h('span', { class: 'muted-text' }, '—')
    }
  },
  {
    key: 'escalationLevel',
    dataKey: 'escalationLevel',
    title: '升级',
    width: 80,
    /** 升级列渲染 */
    cellRenderer: ({ cellData }) => {
      if (cellData >= 2) return h('span', { class: 'escalate-badge escalate-high' }, '上报')
      if (cellData >= 1) return h('span', { class: 'escalate-badge escalate-warn' }, '警告')
      return h('span', { class: 'muted-text' }, '—')
    }
  },
  {
    key: 'assignedCsUserName',
    dataKey: 'assignedCsUserName',
    title: '指派客服',
    width: 100,
    /** 指派客服列渲染 */
    cellRenderer: ({ cellData }) => cellData || '—'
  },
  { key: 'createdAt', dataKey: 'createdAt', title: '创建时间', width: 160 },
  {
    key: 'actions',
    dataKey: 'id',
    title: '操作',
    width: 200,
    fixed: 'right',
    /** 操作列渲染：接手/完成/查看按钮 */
    cellRenderer: ({ row }) => {
      const buttons = []
      if (row.status === 'pending') {
        buttons.push(h(ElButton, { link: true, type: 'primary', size: 'small', onClick: () => handleProcess(row, 'processing') }, () => '接手'))
        buttons.push(h(ElButton, { link: true, type: 'success', size: 'small', onClick: () => handleProcess(row, 'resolved') }, () => '完成'))
      } else if (row.status === 'processing') {
        buttons.push(h(ElButton, { link: true, type: 'success', size: 'small', onClick: () => handleProcess(row, 'resolved') }, () => '完成'))
      }
      buttons.push(h(ElButton, { link: true, size: 'small', onClick: () => handleViewCustomer(row) }, () => '查看'))
      return h('div', buttons)
    }
  }
])

/**
 * 查询待办消息列表
 */
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await pendingMessageApi.getPage(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      /** 数据量超过100条时自动提示可切换虚拟滚动 */
      if (total.value > 100 && !virtualMode.value) {
        ElMessage.info('数据量较大，可开启虚拟滚动模式提升性能')
      }
    }
  } catch (e) {
    ElMessage.error('查询失败')
    console.error('查询失败', e)
  } finally {
    loading.value = false
  }
}

/**
 * 重置筛选条件
 */
const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.status = null
  queryParams.platform = null
  queryParams.keyword = ''
  handleQuery()
}

/**
 * 打开处理对话框
 * @param {Object} row - 待办消息行数据
 * @param {string} status - 目标状态
 */
const handleProcess = (row, status) => {
  processForm.id = row.id
  processForm.status = status
  processForm.currentStatus = row.status
  processForm.remark = row.remark || ''
  processForm.contextSummary = row.contextSummary || ''
  processForm.userNickname = row.userNickname || ''
  processDialogVisible.value = true
}

/**
 * 提交处理结果
 */
const submitProcess = async () => {
  if (processForm.status === 'resolved' && !processForm.remark.trim()) {
    ElMessage.warning('完成处理时请填写处理结果')
    return
  }
  submitLoading.value = true
  try {
    const res = await pendingMessageApi.handle({
      id: processForm.id,
      status: processForm.status,
      remark: processForm.remark
    })
    if (res.code === 200) {
      ElMessage.success(processForm.status === 'processing' ? '接手成功' : '处理完成')
      processDialogVisible.value = false
      handleQuery()
      if (refreshPendingCount) refreshPendingCount()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 查看客户信息
 * @param {Object} row - 待办消息行数据
 */
const handleViewCustomer = async (row) => {
  customerDialogVisible.value = true
  customerLoading.value = true
  customerInfo.value = null
  try {
    const res = await customerApi.getById(row.userId)
    if (res.code === 200) {
      customerInfo.value = res.data
    }
  } catch (e) {
    if (e.message && e.message.includes('无权')) {
      ElMessage.error('您无权查看此客户信息')
    } else {
      ElMessage.error('获取客户信息失败')
    }
    customerDialogVisible.value = false
  } finally {
    customerLoading.value = false
  }
}

/** 更新当前时间戳 */
const tickNow = () => { now.value = Date.now() }

/**
 * 导出待办事项Excel
 */
const handleExport = () => {
  const params = {}
  if (queryParams.status) params.status = queryParams.status
  if (queryParams.platform) params.platform = queryParams.platform
  if (queryParams.keyword) params.keyword = queryParams.keyword
  downloadExcel('/pending-messages/export', params, '待办事项.xlsx')
}

onMounted(() => {
  handleQuery()
  countdownTimer = setInterval(tickNow, 1000)
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped>
.pending-page { padding: 0; }
.header-right { display: flex; gap: 8px; }
.filter-bar { margin-bottom: 16px; padding: 14px; background: var(--gu-bg-secondary); border: 1px solid var(--gu-border); border-radius: var(--gu-radius-lg); }
.customer-name { font-weight: 500; color: var(--gu-text-primary); margin-right: 6px; }
.platform-tag { font-size: 11px; transform: scale(0.85); transform-origin: left center; }
.muted-text { color: var(--gu-text-muted); font-size: 12px; }
.countdown { font-family: "Courier New", monospace; font-size: 13px; font-weight: 600; color: var(--gu-success); letter-spacing: 1px; }
.countdown.urgent { color: var(--gu-warning); animation: pulse 1.5s ease-in-out infinite; }
.countdown.overdue { color: var(--gu-danger); font-weight: 700; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.escalate-badge { display: inline-block; padding: 2px 8px; border-radius: 3px; font-size: 11px; font-weight: 600; }
.escalate-warn { background: rgba(201,118,48,0.12); color: var(--gu-warning); border: 1px solid rgba(201,118,48,0.3); }
.escalate-high { background: rgba(166,61,64,0.12); color: var(--gu-danger); border: 1px solid rgba(166,61,64,0.3); animation: pulse 1.5s ease-in-out infinite; }
.context-summary { background: var(--gu-bg-secondary); border: 1px solid var(--gu-border); border-radius: var(--gu-radius-lg); padding: 10px 12px; font-size: 13px; line-height: 1.6; color: var(--gu-text-secondary); white-space: pre-line; max-height: 200px; overflow-y: auto; }
/* 数据量提示样式 */
.data-hint { margin-top: 12px; }

/* 表格工具栏样式 */
.table-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

/* 虚拟滚动提示样式 */
.virtual-hint {
  flex: 1;
  padding: 4px 8px;
}

/* 虚拟表格容器样式 */
.virtual-table-wrapper {
  height: 500px;
}
</style>
