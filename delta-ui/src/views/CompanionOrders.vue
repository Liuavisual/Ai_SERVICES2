<!--
  陪玩师订单处理页面，管理陪玩师视角的订单（接单/拒单/订单查看）

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="陪玩师" required>
          <el-select v-model="selectedCompanionId" placeholder="请选择陪玩师" clearable filterable style="width: 240px" :teleported="false" @change="onCompanionChange">
            <el-option v-if="isAdmin" label="全部陪玩师" :value="0" />
            <el-option v-for="c in companionList" :key="c.id" :label="c.nickname + ' (' + c.gameType + ')'" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :disabled="!selectedCompanionId">查询</el-button>
          <el-button @click="handleRefresh">刷新</el-button>
        </el-form-item>
        <el-form-item>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" style="margin-left: 16px">
            <el-tag type="warning" effect="plain">待处理: {{ pendingCount }} 单</el-tag>
          </el-badge>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="待处理订单" name="pending">
          <template #label>
            <span>待处理订单 <el-badge :value="pendingCount" :hidden="pendingCount === 0" /></span>
          </template>
                <el-table :data="pendingTableData" stripe style="width: 100%" v-loading="pendingLoading">
                  <el-table-column type="index" label="序号" width="60" />
                  <el-table-column prop="orderNo" label="订单号" min-width="180">
                    <template #default="{ row }">
                      <span style="font-family: monospace; font-size: 12px;">{{ row.orderNo }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="serviceType" label="服务类型" width="100" />
                  <el-table-column label="预约时间" width="170">
                    <template #default="{ row }">
                      <span v-if="row.scheduledStart">{{ formatDateTime(row.scheduledStart) }}</span>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="时长" width="80">
                    <template #default="{ row }">
                      <span v-if="row.durationMinutes">{{ row.durationMinutes }}分钟</span>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="金额" width="100">
                    <template #default="{ row }">
                      <span class="amount-text">¥{{ row.totalAmount != null ? row.totalAmount : '0' }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="时间来源" width="100">
                    <template #default="{ row }">
                      <el-tag v-if="row.timeSource === 'SYSTEM'" type="success" size="small">预设时段</el-tag>
                      <el-tag v-else-if="row.timeSource === 'CUSTOM'" type="warning" size="small">自定义</el-tag>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="创建时间" width="170">
                    <template #default="{ row }">
                      {{ row.createdAt ? formatDateTime(row.createdAt) : '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="160" fixed="right">
                    <template #default="{ row }">
                      <el-button link type="success" size="small" @click="handleAccept(row)">接单</el-button>
                      <el-button link type="danger" size="small" @click="handleShowReject(row)">拒单</el-button>
                      <el-button link type="info" size="small" @click="handleStatusHistory(row)">历史</el-button>
                    </template>
                  </el-table-column>
                </el-table>
        </el-tab-pane>
        <el-tab-pane label="全部订单" name="all">
          <el-table :data="allTableData" stripe style="width: 100%" v-loading="allLoading">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="orderNo" label="订单号" min-width="180">
              <template #default="{ row }">
                <span style="font-family: monospace; font-size: 12px;">{{ row.orderNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="serviceType" label="服务类型" width="100" />
            <el-table-column label="预约时间" width="170">
              <template #default="{ row }">
                <span v-if="row.scheduledStart">{{ formatDateTime(row.scheduledStart) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="100">
              <template #default="{ row }">
                <span class="amount-text">¥{{ row.totalAmount != null ? row.totalAmount : '0' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="订单状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.orderStatus)" size="small">
                  {{ row.orderStatusText || row.orderStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间来源" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.timeSource === 'SYSTEM'" type="success" size="small">预设</el-tag>
                <el-tag v-else-if="row.timeSource === 'CUSTOM'" type="warning" size="small">自定义</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.remark || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="170">
              <template #default="{ row }">
                {{ row.createdAt ? formatDateTime(row.createdAt) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleStatusHistory(row)">历史</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="rejectVisible" title="拒单确认" width="480px" destroy-on-close>
      <div v-if="rejectTarget" style="margin-bottom: 16px">
        <div style="margin-bottom: 6px">订单号：<b>{{ rejectTarget.orderNo }}</b></div>
        <div style="margin-bottom: 6px">服务类型：{{ rejectTarget.serviceType || '-' }}</div>
      </div>
      <el-form label-width="80px">
        <el-form-item label="拒单原因">
          <el-radio-group id="reject-reason-template" name="reasonTemplate" v-model="rejectForm.reasonTemplate" @change="onReasonTemplateChange">
            <el-radio value="TIME_CONFLICT">时间冲突</el-radio>
            <el-radio value="PERSONAL">个人原因</el-radio>
            <el-radio value="SKILL_MISMATCH">技能不匹配</el-radio>
            <el-radio value="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            id="reject-custom-reason"
            name="customReason"
            v-model="rejectForm.customReason"
            type="textarea"
            :rows="3"
            placeholder="请输入补充说明（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejectLoading">确认拒单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusHistoryVisible" title="状态变更历史" width="640px" destroy-on-close>
      <el-timeline v-if="statusHistoryList.length > 0">
        <el-timeline-item
          v-for="item in statusHistoryList"
          :key="item.id"
          :timestamp="formatDateTime(item.createdAt)"
          placement="top"
          :color="statusTimelineColor(item.fromStatus)"
        >
          <div class="timeline-content">
            <el-tag :type="statusTagType(item.fromStatus)" size="small" effect="plain">{{ item.fromStatus }}</el-tag>
            <el-icon style="margin: 0 4px"><ArrowRight /></el-icon>
            <el-tag :type="statusTagType(item.toStatus)" size="small" effect="plain">{{ item.toStatus }}</el-tag>
            <span style="margin-left: 8px; color: #606266">{{ item.operatorName || '-' }}</span>
            <span v-if="item.operatorRole" style="margin-left: 4px; color: #909399; font-size: 12px">({{ item.operatorRole }})</span>
            <div v-if="item.reason" style="margin-top: 4px; color: #909399; font-size: 13px">原因：{{ item.reason }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无状态变更记录" />
      <template #footer>
        <el-button @click="statusHistoryVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { orderApi, companionApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { Result, OrderVO, PageResult } from '@/types'

const authStore = useAuthStore()
const isAdmin = authStore.isAdmin

const companionList = ref<Array<{ id: number; nickname: string; gameType: string }>>([])
const selectedCompanionId = ref<number | null>(null)
const activeTab = ref<string>('pending')

const pendingLoading = ref<boolean>(false)
const pendingTableData = ref<OrderVO[]>([])
const pendingCount = ref<number>(0)

const allLoading = ref<boolean>(false)
const allTableData = ref<OrderVO[]>([])

const rejectVisible = ref<boolean>(false)
const rejectLoading = ref<boolean>(false)
const rejectTarget = ref<OrderVO | null>(null)
const rejectForm = reactive<{ reasonTemplate: string; customReason: string }>({
  reasonTemplate: 'TIME_CONFLICT',
  customReason: ''
})

const statusHistoryVisible = ref<boolean>(false)
const statusHistoryList = ref<StatusHistoryItem[]>([])

const reasonTemplateLabels: Record<string, string> = {
  TIME_CONFLICT: '时间冲突，无法接单',
  PERSONAL: '个人原因，暂时无法接单',
  SKILL_MISMATCH: '技能不匹配，无法胜任',
  OTHER: '其他原因'
}

interface StatusHistoryItem {
  id: number
  orderId: number
  fromStatus: string
  toStatus: string
  operatorId: number
  operatorName: string
  operatorRole: string
  reason: string
  createdAt: string
}

const statusTagMap: Record<string, string> = {
  PENDING: 'info',
  CONFIRMED: '',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  PENDING_REVIEW: '',
  CANCELLED: 'danger',
  REFUNDED: 'info',
  ABNORMAL: 'danger',
  ARCHIVED: 'info'
}

function statusTagType(status: string): string {
  return statusTagMap[status] || 'info'
}

function statusTimelineColor(status: string): string {
  const colorMap: Record<string, string> = {
    PENDING: '#909399',
    CONFIRMED: '#409eff',
    IN_PROGRESS: '#e6a23c',
    COMPLETED: '#67c23a',
    PENDING_REVIEW: '#409eff',
    CANCELLED: '#f56c6c',
    REFUNDED: '#909399',
    ABNORMAL: '#f56c6c',
    ARCHIVED: '#909399'
  }
  return colorMap[status] || '#909399'
}

function formatDateTime(val: string): string {
  if (!val) return '-'
  const d = new Date(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function onReasonTemplateChange(): void {
  rejectForm.customReason = ''
}

async function onCompanionChange(): Promise<void> {
  handleQuery()
}

function onTabChange(): void {
  if (activeTab.value === 'pending') {
    fetchPendingOrders()
  } else {
    fetchAllOrders()
  }
}

async function fetchPendingOrders(): Promise<void> {
  if (!selectedCompanionId.value && selectedCompanionId.value !== 0) return
  pendingLoading.value = true
  try {
    if (isAdmin && selectedCompanionId.value === 0) {
      const res: Result<PageResult<OrderVO>> = await orderApi.getPage({ page: 1, size: 200, orderStatus: 'PENDING' })
      pendingTableData.value = (res.data as any)?.records || []
      pendingCount.value = (res.data as any)?.total || pendingTableData.value.length
    } else {
      const res: Result<OrderVO[]> = await orderApi.getPendingByCompanion(selectedCompanionId.value!)
      if (res.code === 200) {
        pendingTableData.value = res.data || []
        pendingCount.value = (res.data || []).length
      }
    }
  } catch (e) {
    console.error('获取待处理订单失败', e)
  } finally {
    pendingLoading.value = false
  }
}

async function fetchAllOrders(): Promise<void> {
  if (!selectedCompanionId.value && selectedCompanionId.value !== 0) return
  allLoading.value = true
  try {
    if (isAdmin && selectedCompanionId.value === 0) {
      const res: Result<PageResult<OrderVO>> = await orderApi.getPage({ page: 1, size: 200 })
      allTableData.value = (res.data as any)?.records || []
    } else {
      const res: Result<OrderVO[]> = await orderApi.getByCompanion(String(selectedCompanionId.value!))
      if (res.code === 200) {
        allTableData.value = res.data || []
      }
    }
  } catch (e) {
    console.error('获取全部订单失败', e)
  } finally {
    allLoading.value = false
  }
}

function handleQuery(): void {
  if (activeTab.value === 'pending') {
    fetchPendingOrders()
  } else {
    fetchAllOrders()
  }
}

async function handleRefresh(): Promise<void> {
  await loadCompanions()
  handleQuery()
}

async function handleAccept(row: OrderVO): Promise<void> {
  if (!selectedCompanionId.value) return
  try {
    await ElMessageBox.confirm(
      `确认接单：${row.orderNo}？\n接单后将更新订单状态为"已确认"。`,
      '接单确认',
      { confirmButtonText: '确认接单', cancelButtonText: '取消', type: 'info' }
    )
    const res: Result<null> = await orderApi.accept(row.id, selectedCompanionId.value)
    if (res.code === 200) {
      ElMessage.success('接单成功，订单状态已更新')
      fetchPendingOrders()
    } else {
      ElMessage.error(res.message || '接单失败')
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('接单失败', e)
      ElMessage.error(e?.response?.data?.message || '接单失败')
    }
  }
}

function handleShowReject(row: OrderVO): void {
  rejectTarget.value = row
  rejectForm.reasonTemplate = 'TIME_CONFLICT'
  rejectForm.customReason = ''
  rejectVisible.value = true
}

async function confirmReject(): Promise<void> {
  if (!rejectTarget.value || !selectedCompanionId.value) return
  rejectLoading.value = true
  try {
    const templateLabel = reasonTemplateLabels[rejectForm.reasonTemplate] || ''
    const finalReason = rejectForm.customReason
      ? `${templateLabel}：${rejectForm.customReason}`
      : templateLabel
    const res: Result<null> = await orderApi.reject(
      rejectTarget.value.id,
      selectedCompanionId.value,
      finalReason
    )
    if (res.code === 200) {
      ElMessage.success('已拒单')
      rejectVisible.value = false
      fetchPendingOrders()
    } else {
      ElMessage.error(res.message || '拒单失败')
    }
  } catch (e: any) {
    console.error('拒单失败', e)
    ElMessage.error(e?.response?.data?.message || '拒单失败')
  } finally {
    rejectLoading.value = false
  }
}

async function handleStatusHistory(row: OrderVO): Promise<void> {
  statusHistoryList.value = []
  statusHistoryVisible.value = true
  try {
    const res: Result<StatusHistoryItem[]> = await orderApi.getStatusHistory(row.id)
    if (res.code === 200) {
      statusHistoryList.value = res.data || []
    }
  } catch (e) {
    console.error('获取状态历史失败', e)
  }
}

async function loadCompanions(): Promise<void> {
  try {
    const res: Result<Array<{ id: number; nickname: string; gameType: string }>> = await companionApi.getAll()
    if (res.code === 200) {
      companionList.value = res.data || []
    }
  } catch (e) {
    console.error('加载陪玩师列表失败', e)
  }
}

async function loadCompanionByUser(): Promise<void> {
  const userId = authStore.userInfo?.id
  if (!userId) return
  try {
    const res: Result<{ id: number; nickname: string; gameType: string }> = await companionApi.getByUserId(userId)
    if (res.code === 200 && res.data) {
      selectedCompanionId.value = res.data.id
      fetchPendingOrders()
      fetchAllOrders()
    }
  } catch (e) {
    console.error('自动加载陪玩师信息失败', e)
  }
}

onMounted(() => {
  if (isAdmin) {
    selectedCompanionId.value = 0
    fetchPendingOrders()
    fetchAllOrders()
  } else if (authStore.isCompanion) {
    loadCompanionByUser()
  }
  loadCompanions()
})

onActivated(() => {
  if (activeTab.value === 'pending') {
    fetchPendingOrders()
  } else {
    fetchAllOrders()
  }
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.filter-card { flex-shrink: 0; }

.filter-form { display: flex; flex-wrap: wrap; gap: 0; align-items: center; }

.table-card { flex: 1; min-height: 400px; }

.amount-text {
  font-weight: 600; color: var(--el-color-danger);
  font-family: 'DIN Alternate', 'Helvetica Neue', sans-serif;
}

.timeline-content {
  display: flex; align-items: center; flex-wrap: wrap;
}
</style>