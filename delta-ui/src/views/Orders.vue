<!--
  订单管理页面，集中管理陪玩服务订单的全生命周期

  @author delta
-->
<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.orderStatus" placeholder="全部状态" clearable style="width: 160px" :teleported="false">
            <el-option label="待确认" value="PENDING" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="待评价" value="PENDING_REVIEW" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="queryParams.paymentStatus" placeholder="全部" clearable style="width: 120px" :teleported="false">
            <el-option label="未支付" value="UNPAID" />
            <el-option label="部分支付" value="PARTIAL" />
            <el-option label="已支付" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-tag type="info" size="small" effect="plain">共 {{ total }} 条</el-tag>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="orderNo" label="订单号" min-width="180">
          <template #default="{ row }">
            <span style="font-family: monospace; font-size: 12px;">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="陪玩师" width="120">
          <template #default="{ row }">
            {{ row.companionName || '-' }}
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
            <el-tag v-if="row.paymentStatus === 'UNPAID'" type="danger" size="small" effect="plain" style="margin-left:4px">未付</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)" size="small">
              {{ row.orderStatusText || row.orderStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.orderStatus === 'PENDING'"
              link type="success" size="small"
              @click="handleConfirm(row)"
            >确认</el-button>
            <el-button
              v-if="row.orderStatus === 'CONFIRMED'"
              link type="warning" size="small"
              @click="handleStart(row)"
            >开始</el-button>
            <el-button
              v-if="row.orderStatus === 'IN_PROGRESS'"
              link type="success" size="small"
              @click="handleComplete(row)"
            >完成</el-button>
            <el-button
              v-if="['PENDING','CONFIRMED'].includes(row.orderStatus)"
              link type="danger" size="small"
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button
                v-if="row.orderStatus === 'COMPLETED'"
                link type="warning" size="small"
                @click="handleReview(row)"
              >评价</el-button>
              <el-button link type="info" size="small" @click="handleStatusHistory(row)">状态历史</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号" :span="2">
          <span style="font-family: monospace">{{ currentOrder.orderNo }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="陪玩师">{{ currentOrder.companionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ currentOrder.serviceType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预约开始">
          {{ currentOrder.scheduledStart ? formatDateTime(currentOrder.scheduledStart) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="预约结束">
          {{ currentOrder.scheduledEnd ? formatDateTime(currentOrder.scheduledEnd) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际开始">
          {{ currentOrder.actualStart ? formatDateTime(currentOrder.actualStart) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际结束">
          {{ currentOrder.actualEnd ? formatDateTime(currentOrder.actualEnd) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="时长(分钟)">{{ currentOrder.durationMinutes || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="amount-text">¥{{ currentOrder.totalAmount != null ? currentOrder.totalAmount : '0' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已付金额">
          ¥{{ currentOrder.paidAmount != null ? currentOrder.paidAmount : '0' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="currentOrder.paymentStatus === 'PAID' ? 'success' : 'danger'" size="small">
            {{ currentOrder.paymentStatusText || currentOrder.paymentStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态" :span="2">
          <el-tag :type="statusTagType(currentOrder.orderStatus)" size="small">
            {{ currentOrder.orderStatusText || currentOrder.orderStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentOrder.createdAt ? formatDateTime(currentOrder.createdAt) : '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="订单评价" width="480px" destroy-on-close>
      <div v-if="currentOrder" style="margin-bottom:16px">
        <div style="margin-bottom:6px">订单号：<b>{{ currentOrder.orderNo }}</b></div>
        <div style="margin-bottom:6px">陪玩师：{{ currentOrder.companionName || '-' }}</div>
        <div>服务类型：{{ currentOrder.serviceType || '-' }}</div>
      </div>
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分" required>
          <el-rate v-model="reviewForm.rating" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.feedback" type="textarea" :rows="3" placeholder="请输入评价内容（可选）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="reviewForm.tags" placeholder="多个标签用逗号分隔，如：技术好,耐心,声音好听" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReview" :loading="reviewLoading">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消订单" width="420px" destroy-on-close>
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="原因">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCancel" :loading="cancelLoading">确认取消</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { orderApi, satisfactionApi } from '@/api'
import type { Result, PageResult, OrderVO, OrderStatus } from '@/types'

const loading = ref<boolean>(false)
const tableData = ref<OrderVO[]>([])
const total = ref<number>(0)
const detailVisible = ref<boolean>(false)
const cancelVisible = ref<boolean>(false)
const cancelLoading = ref<boolean>(false)
const currentOrder = ref<OrderVO | null>(null)

const queryParams = reactive<{
  pageNum: number
  pageSize: number
  orderStatus: OrderStatus | ''
  paymentStatus: string
  orderNo: string
  userId: string
  companionId: string
}>({
  pageNum: 1,
  pageSize: 10,
  orderStatus: '',
  paymentStatus: '',
  orderNo: '',
  userId: '',
  companionId: ''
})

const cancelForm = reactive<{ reason: string }>({ reason: '' })

const reviewVisible = ref<boolean>(false)
const reviewLoading = ref<boolean>(false)
const reviewForm = reactive<{ rating: number; feedback: string; tags: string }>({
  rating: 0,
  feedback: '',
  tags: ''
})

const statusHistoryVisible = ref<boolean>(false)
const statusHistoryList = ref<StatusHistoryItem[]>([])

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

function formatDateTime(val: string): string {
  if (!val) return '-'
  const d = new Date(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
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

async function handleStatusHistory(row: OrderVO): Promise<void> {
  currentOrder.value = row
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

async function fetchData(): Promise<void> {
  loading.value = true
  try {
    const params = {
      page: queryParams.pageNum,
      size: queryParams.pageSize,
      orderNo: queryParams.orderNo || null,
      orderStatus: queryParams.orderStatus || null,
      userId: queryParams.userId || null,
      companionId: queryParams.companionId || null
    }
    const res: Result<PageResult<OrderVO>> = await orderApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('查询订单失败', e)
  } finally {
    loading.value = false
  }
}

function handleQuery(): void {
  queryParams.pageNum = 1
  fetchData()
}

function handleSizeChange(): void {
  queryParams.pageNum = 1
  fetchData()
}

function handlePageChange(page: number): void {
  queryParams.pageNum = page
  fetchData()
}

function handleReset(): void {
  queryParams.orderStatus = ''
  queryParams.paymentStatus = ''
  queryParams.orderNo = ''
  queryParams.pageNum = 1
  fetchData()
}

function handleView(row: OrderVO): void {
  currentOrder.value = row
  detailVisible.value = true
}

async function handleConfirm(row: OrderVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认订单 ${row.orderNo}？`, '确认操作', { type: 'info' })
    const res: Result<null> = await orderApi.confirm(row.id)
    if (res.code === 200) {
      ElMessage.success('订单已确认')
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleStart(row: OrderVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`开始服务：订单 ${row.orderNo}？`, '确认操作', { type: 'warning' })
    const res: Result<null> = await orderApi.startService(row.id)
    if (res.code === 200) {
      ElMessage.success('服务已开始')
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleComplete(row: OrderVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`完成服务：订单 ${row.orderNo}？将自动进入待评价状态。`, '确认操作', { type: 'success' })
    const res: Result<null> = await orderApi.completeOrder(row.id)
    if (res.code === 200) {
      ElMessage.success('服务已完成，等待客户评价')
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function handleCancel(row: OrderVO): void {
  currentOrder.value = row
  cancelForm.reason = ''
  cancelVisible.value = true
}

async function confirmCancel(): Promise<void> {
  if (!cancelForm.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  cancelLoading.value = true
  try {
    const res: Result<null> = await orderApi.cancelOrder(currentOrder.value!.id, cancelForm.reason)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      cancelVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    console.error('取消订单失败', e)
  } finally {
    cancelLoading.value = false
  }
}

function handleReview(row: OrderVO): void {
  currentOrder.value = row
  reviewForm.rating = 0
  reviewForm.feedback = ''
  reviewForm.tags = ''
  reviewVisible.value = true
}

async function confirmReview(): Promise<void> {
  if (!reviewForm.rating || reviewForm.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  reviewLoading.value = true
  try {
    const res: Result<null> = await orderApi.submitReview(currentOrder.value!.id, {
      rating: reviewForm.rating,
      reviewContent: reviewForm.feedback,
      reviewerId: 1
    })
    if (res.code === 200) {
      await satisfactionApi.submitOrderReview({
        userId: currentOrder.value!.userId || 1,
        orderId: Number(currentOrder.value!.id),
        companionId: Number(currentOrder.value!.companionId || 0),
        rating: reviewForm.rating,
        feedback: reviewForm.feedback,
        tags: reviewForm.tags,
        isAnonymous: 0
      })
      ElMessage.success('评价提交成功，评分已同步至满意度系统')
      reviewVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '评价提交失败')
    }
  } catch (e: any) {
    console.error('评价失败', e)
    ElMessage.error(e?.response?.data?.message || '评价提交失败')
  } finally {
    reviewLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.filter-card { flex-shrink: 0; }

.filter-form { display: flex; flex-wrap: wrap; gap: 0; }

.table-card { flex: 1; min-height: 400px; }

.card-header {
  display: flex; align-items: center;
  justify-content: space-between;
  font-weight: 600; font-size: 15px;
}

.header-actions { display: flex; align-items: center; gap: 8px; }

.amount-text {
  font-weight: 600; color: var(--el-color-danger);
  font-family: 'DIN Alternate', 'Helvetica Neue', sans-serif;
}
</style>
