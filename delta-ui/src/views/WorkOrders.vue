<!--
  工单管理页面，管理客服工单的全生命周期

  功能：筛选查询、分页列表、接手/提交/确认/关闭/取消操作、详情查看
  状态流转：OPEN → IN_PROGRESS → SUBMITTED → CONFIRMED / CLOSED / CANCELLED

  @author 刘建国
-->
<template>
  <div class="page-container">
    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px" :teleported="false">
            <el-option label="待处理" value="OPEN" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.orderType" placeholder="全部类型" clearable style="width: 120px" :teleported="false">
            <el-option label="咨询" value="CONSULT" />
            <el-option label="投诉" value="COMPLAINT" />
            <el-option label="退款" value="REFUND" />
            <el-option label="技术" value="TECHNICAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryParams.priority" placeholder="全部" clearable style="width: 110px" :teleported="false">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="平台">
          <el-select v-model="queryParams.platform" placeholder="全部" clearable style="width: 120px" :teleported="false">
            <el-option label="微信" value="wechat" />
            <el-option label="KOOK" value="kook" />
            <el-option label="YY" value="yy" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索标题/描述..." clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工单列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>工单管理</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleCreate" v-if="isAdminOrLeader">
              <el-icon><Plus /></el-icon>
              新建工单
            </el-button>
            <el-tag type="info" size="small" effect="plain">共 {{ total }} 条</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="rowNum" label="编号" width="70" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="orderTypeTagMap[row.orderType] || 'info'">
              {{ row.orderTypeDesc || row.orderType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="priorityTagMap[row.priority] || 'info'" effect="light">
              {{ row.priorityDesc || row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagMap[row.status] || 'info'">
              {{ row.statusDesc || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="指派人" width="100">
          <template #default="{ row }">
            <span>{{ row.assignedToName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 'OPEN'"
              link type="warning" size="small"
              @click="handleAccept(row)"
            >接手</el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              link type="primary" size="small"
              @click="handleSubmit(row)"
            >提交</el-button>
            <el-button
              v-if="row.status === 'SUBMITTED'"
              link type="success" size="small"
              @click="handleConfirm(row)"
            >确认</el-button>
            <el-button
              v-if="['OPEN', 'IN_PROGRESS', 'SUBMITTED'].includes(row.status)"
              link type="info" size="small"
              @click="handleClose(row)"
            >关闭</el-button>
            <el-button
              v-if="['OPEN', 'IN_PROGRESS'].includes(row.status)"
              link type="danger" size="small"
              @click="handleCancel(row)"
            >取消</el-button>
            <el-button
              v-if="row.status === 'CANCELLED' && isAdmin"
              link type="warning" size="small"
              @click="handleReopen(row)"
            >重开</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="标题" :span="2">{{ currentOrder.title || '—' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentOrder.description || '—' }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag size="small" :type="orderTypeTagMap[currentOrder.orderType] || 'info'">
            {{ currentOrder.orderTypeDesc || currentOrder.orderType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag size="small" :type="priorityTagMap[currentOrder.priority] || 'info'" effect="light">
            {{ currentOrder.priorityDesc || currentOrder.priority }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagMap[currentOrder.status] || 'info'">
            {{ currentOrder.statusDesc || currentOrder.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="平台">{{ currentOrder.platform || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentOrder.userNickname || '—' }}</el-descriptions-item>
        <el-descriptions-item label="指派人">{{ currentOrder.assignedToName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="关闭原因" :span="2" v-if="currentOrder.closeReason">{{ currentOrder.closeReason }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" :span="2" v-if="currentOrder.cancelReason">{{ currentOrder.cancelReason }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createdAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentOrder.updatedAt || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 提交处理弹窗 -->
    <el-dialog v-model="submitDialogVisible" title="提交处理" width="500px" destroy-on-close>
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="工单标题">
          <span>{{ submitForm.title }}</span>
        </el-form-item>
        <el-form-item label="处理结果" required>
          <el-input
            v-model="submitForm.result"
            type="textarea"
            :rows="4"
            placeholder="请填写处理结果..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubmit" :loading="actionLoading">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 确认完成弹窗 -->
    <el-dialog v-model="confirmDialogVisible" title="确认完成" width="500px" destroy-on-close>
      <el-form :model="confirmForm" label-width="80px">
        <el-form-item label="工单标题">
          <span>{{ confirmForm.title }}</span>
        </el-form-item>
        <el-form-item label="确认备注">
          <el-input
            v-model="confirmForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请填写确认备注（可选）..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="success" @click="confirmComplete" :loading="actionLoading">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 关闭工单弹窗 -->
    <el-dialog v-model="closeDialogVisible" title="关闭工单" width="500px" destroy-on-close>
      <el-form :model="closeForm" label-width="80px">
        <el-form-item label="关闭原因" required>
          <el-input
            v-model="closeForm.closeReason"
            type="textarea"
            :rows="3"
            placeholder="请输入关闭原因..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="info" @click="confirmClose" :loading="actionLoading">确认关闭</el-button>
      </template>
    </el-dialog>

    <!-- 取消工单弹窗 -->
    <el-dialog v-model="cancelDialogVisible" title="取消工单" width="500px" destroy-on-close>
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因" required>
          <el-input
            v-model="cancelForm.cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCancel" :loading="actionLoading">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 新建工单弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建工单" width="560px" destroy-on-close>
      <el-form :model="createForm" label-width="80px" :rules="createRules" ref="createFormRef">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入工单标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            placeholder="请描述问题..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型" prop="orderType">
          <el-select v-model="createForm.orderType" placeholder="请选择" style="width: 100%" :teleported="false">
            <el-option label="咨询" value="CONSULT" />
            <el-option label="投诉" value="COMPLAINT" />
            <el-option label="退款" value="REFUND" />
            <el-option label="技术" value="TECHNICAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="createForm.priority" placeholder="请选择" style="width: 100%" :teleported="false">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="平台" prop="platform">
          <el-select v-model="createForm.platform" placeholder="请选择" style="width: 100%" :teleported="false">
            <el-option label="微信" value="wechat" />
            <el-option label="KOOK" value="kook" />
            <el-option label="YY" value="yy" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate" :loading="actionLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 重新打开弹窗 -->
    <el-dialog v-model="reopenDialogVisible" title="重新打开工单" width="500px" destroy-on-close>
      <el-form :model="reopenForm" label-width="80px">
        <el-form-item label="重开原因" required>
          <el-input
            v-model="reopenForm.reopenReason"
            type="textarea"
            :rows="3"
            placeholder="请输入重新打开原因..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reopenDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmReopen" :loading="actionLoading">确认重开</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { workOrderApi } from '@/api'

/** 加载状态 */
const loading = ref(false)
/** 操作加载状态 */
const actionLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
/** 数据总数 */
const total = ref(0)

/** 当前用户信息 */
let userInfo = {}
try {
  userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
} catch (e) {}
/** 是否管理员或主管 */
const isAdminOrLeader = userInfo.role === 'SYS_ADMIN' || userInfo.role === 'CS_LEADER'
/** 是否管理员 */
const isAdmin = userInfo.role === 'SYS_ADMIN'

/** 查询参数 */
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  orderType: '',
  priority: '',
  platform: '',
  keyword: ''
})

/** 详情弹窗 */
const detailVisible = ref(false)
/** 当前查看的工单 */
const currentOrder = ref(null)

/** 提交处理弹窗 */
const submitDialogVisible = ref(false)
const submitForm = reactive({ id: null, title: '', result: '' })

/** 确认完成弹窗 */
const confirmDialogVisible = ref(false)
const confirmForm = reactive({ id: null, title: '', remark: '' })

/** 关闭工单弹窗 */
const closeDialogVisible = ref(false)
const closeForm = reactive({ id: null, closeReason: '' })

/** 取消工单弹窗 */
const cancelDialogVisible = ref(false)
const cancelForm = reactive({ id: null, cancelReason: '' })

/** 新建工单弹窗 */
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  title: '',
  description: '',
  orderType: '',
  priority: 'MEDIUM',
  platform: ''
})
/** 新建表单校验规则 */
const createRules = {
  title: [{ required: true, message: '请输入工单标题', trigger: 'blur' }],
  orderType: [{ required: true, message: '请选择工单类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

/** 重新打开弹窗 */
const reopenDialogVisible = ref(false)
const reopenForm = reactive({ id: null, reopenReason: '' })

/** 状态标签类型映射 */
const statusTagMap = {
  OPEN: 'warning',
  IN_PROGRESS: 'primary',
  SUBMITTED: '',
  CONFIRMED: 'success',
  CLOSED: 'info',
  CANCELLED: 'danger'
}

/** 优先级标签类型映射 */
const priorityTagMap = {
  LOW: 'info',
  MEDIUM: '',
  HIGH: 'warning',
  URGENT: 'danger'
}

/** 工单类型标签类型映射 */
const orderTypeTagMap = {
  CONSULT: '',
  COMPLAINT: 'danger',
  REFUND: 'warning',
  TECHNICAL: 'primary',
  OTHER: 'info'
}

/**
 * 获取工单分页数据
 */
async function fetchData() {
  loading.value = true
  try {
    const res = await workOrderApi.getPage(queryParams)
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    ElMessage.error('查询失败')
    console.error('查询工单失败', e)
  } finally {
    loading.value = false
  }
}

/** 查询（重置页码） */
function handleQuery() {
  queryParams.pageNum = 1
  fetchData()
}

/** 重置筛选条件 */
function handleReset() {
  queryParams.pageNum = 1
  queryParams.status = ''
  queryParams.orderType = ''
  queryParams.priority = ''
  queryParams.platform = ''
  queryParams.keyword = ''
  fetchData()
}

/** 查看工单详情 */
function handleView(row) {
  currentOrder.value = row
  detailVisible.value = true
}

/** 接手工单 */
async function handleAccept(row) {
  try {
    await ElMessageBox.confirm(`确认接手工单「${row.title}」？`, '接手确认', { type: 'info' })
    const res = await workOrderApi.accept(row.id)
    if (res.code === 200) {
      ElMessage.success('接手成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

/** 打开提交处理弹窗 */
function handleSubmit(row) {
  submitForm.id = row.id
  submitForm.title = row.title
  submitForm.result = ''
  submitDialogVisible.value = true
}

/** 确认提交处理 */
async function confirmSubmit() {
  if (!submitForm.result.trim()) {
    ElMessage.warning('请填写处理结果')
    return
  }
  actionLoading.value = true
  try {
    const res = await workOrderApi.submit(submitForm.id, { result: submitForm.result })
    if (res.code === 200) {
      ElMessage.success('提交成功')
      submitDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('提交失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开确认完成弹窗 */
function handleConfirm(row) {
  confirmForm.id = row.id
  confirmForm.title = row.title
  confirmForm.remark = ''
  confirmDialogVisible.value = true
}

/** 确认完成 */
async function confirmComplete() {
  actionLoading.value = true
  try {
    const res = await workOrderApi.confirm(confirmForm.id, { remark: confirmForm.remark })
    if (res.code === 200) {
      ElMessage.success('已确认完成')
      confirmDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('确认失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开关闭工单弹窗 */
function handleClose(row) {
  closeForm.id = row.id
  closeForm.closeReason = ''
  closeDialogVisible.value = true
}

/** 确认关闭 */
async function confirmClose() {
  if (!closeForm.closeReason.trim()) {
    ElMessage.warning('请输入关闭原因')
    return
  }
  actionLoading.value = true
  try {
    const res = await workOrderApi.close(closeForm.id, closeForm.closeReason)
    if (res.code === 200) {
      ElMessage.success('工单已关闭')
      closeDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('关闭失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开取消工单弹窗 */
function handleCancel(row) {
  cancelForm.id = row.id
  cancelForm.cancelReason = ''
  cancelDialogVisible.value = true
}

/** 确认取消 */
async function confirmCancel() {
  if (!cancelForm.cancelReason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  actionLoading.value = true
  try {
    const res = await workOrderApi.cancel(cancelForm.id, cancelForm.cancelReason)
    if (res.code === 200) {
      ElMessage.success('工单已取消')
      cancelDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('取消失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开新建工单弹窗 */
function handleCreate() {
  createForm.title = ''
  createForm.description = ''
  createForm.orderType = ''
  createForm.priority = 'MEDIUM'
  createForm.platform = ''
  createDialogVisible.value = true
}

/** 确认创建工单 */
async function confirmCreate() {
  if (!createFormRef.value) return
  try {
    await createFormRef.value.validate()
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const res = await workOrderApi.create(createForm)
    if (res.code === 200) {
      ElMessage.success('工单创建成功')
      createDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error('创建失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开重新打开弹窗 */
function handleReopen(row) {
  reopenForm.id = row.id
  reopenForm.reopenReason = ''
  reopenDialogVisible.value = true
}

/** 确认重新打开 */
async function confirmReopen() {
  if (!reopenForm.reopenReason.trim()) {
    ElMessage.warning('请输入重新打开原因')
    return
  }
  actionLoading.value = true
  try {
    const res = await workOrderApi.reopen(reopenForm.id, reopenForm.reopenReason)
    if (res.code === 200) {
      ElMessage.success('工单已重新打开')
      reopenDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('重开失败')
    console.error(e)
  } finally {
    actionLoading.value = false
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
</style>
