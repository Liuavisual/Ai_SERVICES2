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

      <el-table :data="tableData" v-loading="loading" stripe>
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

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { queryParams.pageNum = 1; handleQuery() }"
        @current-change="handleQuery"
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
import { ref, reactive, computed, onMounted, onUnmounted, inject } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { pendingMessageApi, customerApi, downloadExcel } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const refreshPendingCount = inject('refreshPendingCount')
const now = ref(Date.now())
let countdownTimer = null

let userInfo = {}
try {
  userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
} catch (e) {}
const isAdminOrLeader = userInfo.role === 'SYS_ADMIN' || userInfo.role === 'CS_LEADER'

const queryParams = reactive({ pageNum: 1, pageSize: 20, status: null, platform: null, keyword: '' })
const processDialogVisible = ref(false)
const processForm = reactive({ id: null, status: '', currentStatus: '', remark: '', contextSummary: '', userNickname: '' })

const customerDialogVisible = ref(false)
const customerLoading = ref(false)
const customerInfo = ref(null)

const processDialogTitle = computed(() => {
  if (processForm.status === 'processing') return '接手处理'
  if (processForm.status === 'resolved') return '完成处理'
  return '处理待办'
})

const getRemaining = (row) => {
  if (!row.deadline) return 0
  const target = new Date(row.deadline).getTime()
  return Math.max(0, Math.floor((target - now.value) / 1000))
}

const overdueCount = computed(() => tableData.value.filter(r => r.status !== 'resolved' && getRemaining(r) <= 0).length)
const urgentCount = computed(() => tableData.value.filter(r => r.status !== 'resolved' && getRemaining(r) > 0 && getRemaining(r) < 120).length)

const getStatusType = (s) => ({ pending: 'warning', processing: 'primary', resolved: 'success' }[s] || 'info')
const getStatusText = (s) => ({ pending: '待处理', processing: '处理中', resolved: '已解决' }[s] || s)
const getPlatformText = (p) => ({ wechat: '微信', kook: 'KOOK', yy: 'YY' }[p] || p || '—')
const getPlatformType = (p) => ({ wechat: 'primary', kook: 'success', yy: 'warning' }[p] || 'info')
const getInterventionTypeTag = (t) => ({ ORDER: 'danger', SCHEDULE: 'warning', SPECIFIC_COMPANION: 'primary', COMPLAINT: 'danger', HUMAN_REQUEST: '' }[t] || 'info')
const getPlatformLabel = (p) => ({ wechat: '微信', kook: 'KOOK', yy: 'YY' }[p] || p || '—')
const getPlatformTagType = (p) => ({ wechat: 'primary', kook: 'success', yy: 'warning' }[p] || 'info')

const formatCountdown = (row) => {
  const s = getRemaining(row)
  if (s <= 0) return '已超时'
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? m + '分' + sec + '秒' : sec + '秒'
}

const getCountdownClass = (row) => {
  const s = getRemaining(row)
  if (s <= 0) return 'countdown overdue'
  if (s < 120) return 'countdown urgent'
  return 'countdown'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await pendingMessageApi.getPage(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('查询失败')
    console.error('查询失败', e)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.status = null
  queryParams.platform = null
  queryParams.keyword = ''
  handleQuery()
}

const handleProcess = (row, status) => {
  processForm.id = row.id
  processForm.status = status
  processForm.currentStatus = row.status
  processForm.remark = row.remark || ''
  processForm.contextSummary = row.contextSummary || ''
  processForm.userNickname = row.userNickname || ''
  processDialogVisible.value = true
}

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

const tickNow = () => { now.value = Date.now() }

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
</style>
