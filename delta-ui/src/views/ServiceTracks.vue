<!--
  服务追踪页面，管理服务全流程追踪

  功能：按用户/订单查询、预约/开始/结束/评价操作、详情查看
  状态流转：CONSULT → BOOKED → IN_PROGRESS → COMPLETED / CANCELLED

  @author 刘建国
-->
<template>
  <div class="page-container">
    <!-- 查询栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="查询方式">
          <el-select v-model="queryParams.queryType" style="width: 130px" :teleported="false">
            <el-option label="按用户ID" value="user" />
            <el-option label="按订单ID" value="order" />
          </el-select>
        </el-form-item>
        <el-form-item label="ID">
          <el-input
            v-model="queryParams.queryId"
            :placeholder="queryParams.queryType === 'user' ? '输入用户ID' : '输入订单ID'"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 服务追踪列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>服务追踪</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新建咨询
            </el-button>
            <el-tag type="info" size="small" effect="plain">共 {{ total }} 条</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="rowNum" label="编号" width="70" />
        <el-table-column label="客户" width="110">
          <template #default="{ row }">
            <span>{{ row.userNickname || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="陪玩师" width="110">
          <template #default="{ row }">
            <span>{{ row.companionName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagMap[row.status] || 'info'">
              {{ row.statusDesc || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="serviceType" label="服务类型" width="100" />
        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">
            <span v-if="row.bookedStartTime">{{ formatDateTime(row.bookedStartTime) }}</span>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="实际时长" width="90">
          <template #default="{ row }">
            <span v-if="row.durationMinutes">{{ row.durationMinutes }}分钟</span>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <span v-if="row.customerRating" class="rating-text">{{ row.customerRating }}分</span>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 'CONSULT'"
              link type="warning" size="small"
              @click="handleBook(row)"
            >预约</el-button>
            <el-button
              v-if="row.status === 'BOOKED'"
              link type="primary" size="small"
              @click="handleStart(row)"
            >开始</el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              link type="success" size="small"
              @click="handleEnd(row)"
            >结束</el-button>
            <el-button
              v-if="row.status === 'COMPLETED' && !row.customerRating"
              link type="warning" size="small"
              @click="handleRating(row)"
            >评价</el-button>
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
    <el-dialog v-model="detailVisible" title="服务追踪详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentTrack">
        <el-descriptions-item label="客户">{{ currentTrack.userNickname || '—' }}</el-descriptions-item>
        <el-descriptions-item label="陪玩师">{{ currentTrack.companionName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagMap[currentTrack.status] || 'info'">
            {{ currentTrack.statusDesc || currentTrack.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ currentTrack.serviceType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="咨询内容" :span="2">{{ currentTrack.consultContent || '—' }}</el-descriptions-item>
        <el-descriptions-item label="预约开始">
          {{ currentTrack.bookedStartTime ? formatDateTime(currentTrack.bookedStartTime) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="预约结束">
          {{ currentTrack.bookedEndTime ? formatDateTime(currentTrack.bookedEndTime) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际开始">
          {{ currentTrack.actualStartTime ? formatDateTime(currentTrack.actualStartTime) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际结束">
          {{ currentTrack.actualEndTime ? formatDateTime(currentTrack.actualEndTime) : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务时长(分钟)">{{ currentTrack.durationMinutes || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户评分">
          <span v-if="currentTrack.customerRating" class="rating-text">{{ currentTrack.customerRating }}分</span>
          <span v-else>—</span>
        </el-descriptions-item>
        <el-descriptions-item label="客户反馈" :span="2">{{ currentTrack.customerFeedback || '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTrack.createdAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentTrack.updatedAt || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新建咨询弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建咨询" width="500px" destroy-on-close>
      <el-form :model="createForm" label-width="80px" :rules="createRules" ref="createFormRef">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="createForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="工单ID" prop="workOrderId">
          <el-input v-model="createForm.workOrderId" placeholder="关联工单ID（可选）" />
        </el-form-item>
        <el-form-item label="咨询内容" prop="consultContent">
          <el-input
            v-model="createForm.consultContent"
            type="textarea"
            :rows="4"
            placeholder="请描述咨询内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreate" :loading="actionLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 预约弹窗 -->
    <el-dialog v-model="bookDialogVisible" title="预约服务" width="500px" destroy-on-close>
      <el-form :model="bookForm" label-width="90px" :rules="bookRules" ref="bookFormRef">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="bookForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="预约开始" prop="bookedStartTime">
          <el-date-picker
            v-model="bookForm.bookedStartTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="预约结束" prop="bookedEndTime">
          <el-date-picker
            v-model="bookForm.bookedEndTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="服务类型">
          <el-input v-model="bookForm.serviceType" placeholder="服务类型（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmBook" :loading="actionLoading">确认预约</el-button>
      </template>
    </el-dialog>

    <!-- 开始服务弹窗 -->
    <el-dialog v-model="startDialogVisible" title="开始服务" width="500px" destroy-on-close>
      <el-form :model="startForm" label-width="80px" :rules="startRules" ref="startFormRef">
        <el-form-item label="陪玩师ID" prop="companionId">
          <el-input v-model="startForm.companionId" placeholder="请输入陪玩师ID" />
        </el-form-item>
        <el-form-item label="陪玩师名" prop="companionName">
          <el-input v-model="startForm.companionName" placeholder="请输入陪玩师名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStart" :loading="actionLoading">确认开始</el-button>
      </template>
    </el-dialog>

    <!-- 结束服务弹窗 -->
    <el-dialog v-model="endDialogVisible" title="结束服务" width="500px" destroy-on-close>
      <el-form :model="endForm" label-width="80px">
        <el-form-item label="服务备注">
          <el-input
            v-model="endForm.remark"
            type="textarea"
            :rows="3"
            placeholder="服务结束备注（可选）..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="endDialogVisible = false">取消</el-button>
        <el-button type="success" @click="confirmEnd" :loading="actionLoading">确认结束</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="ratingDialogVisible" title="提交评价" width="500px" destroy-on-close>
      <el-form :model="ratingForm" label-width="80px" :rules="ratingRules" ref="ratingFormRef">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="ratingForm.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text />
        </el-form-item>
        <el-form-item label="反馈">
          <el-input
            v-model="ratingForm.feedback"
            type="textarea"
            :rows="3"
            placeholder="客户反馈内容（可选）..."
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ratingDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmRating" :loading="actionLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { serviceTrackApi } from '@/api'

/** 加载状态 */
const loading = ref(false)
/** 操作加载状态 */
const actionLoading = ref(false)
/** 表格数据 */
const tableData = ref([])
/** 数据总数 */
const total = ref(0)

/** 查询参数 */
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  queryType: 'user',
  queryId: ''
})

/** 详情弹窗 */
const detailVisible = ref(false)
/** 当前查看的服务追踪 */
const currentTrack = ref(null)

/** 新建咨询弹窗 */
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  userId: '',
  workOrderId: '',
  consultContent: ''
})
/** 新建表单校验规则 */
const createRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  consultContent: [{ required: true, message: '请输入咨询内容', trigger: 'blur' }]
}

/** 预约弹窗 */
const bookDialogVisible = ref(false)
const bookFormRef = ref(null)
const bookForm = reactive({
  id: null,
  userId: '',
  bookedStartTime: '',
  bookedEndTime: '',
  serviceType: ''
})
/** 预约表单校验规则 */
const bookRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  bookedStartTime: [{ required: true, message: '请选择预约开始时间', trigger: 'change' }],
  bookedEndTime: [{ required: true, message: '请选择预约结束时间', trigger: 'change' }]
}

/** 开始服务弹窗 */
const startDialogVisible = ref(false)
const startFormRef = ref(null)
const startForm = reactive({
  id: null,
  companionId: '',
  companionName: ''
})
/** 开始服务表单校验规则 */
const startRules = {
  companionId: [{ required: true, message: '请输入陪玩师ID', trigger: 'blur' }],
  companionName: [{ required: true, message: '请输入陪玩师名称', trigger: 'blur' }]
}

/** 结束服务弹窗 */
const endDialogVisible = ref(false)
const endForm = reactive({ id: null, remark: '' })

/** 评价弹窗 */
const ratingDialogVisible = ref(false)
const ratingFormRef = ref(null)
const ratingForm = reactive({
  id: null,
  rating: 0,
  feedback: ''
})
/** 评价表单校验规则 */
const ratingRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change', type: 'number', min: 1 }]
}

/** 状态标签类型映射 */
const statusTagMap = {
  CONSULT: 'warning',
  BOOKED: '',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'danger'
}

/**
 * 格式化日期时间
 * @param {string} val - 日期时间字符串
 * @returns {string} 格式化后的日期时间
 */
function formatDateTime(val) {
  if (!val) return '—'
  const d = new Date(val)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * 获取服务追踪列表数据
 */
async function fetchData() {
  if (!queryParams.queryId.trim()) {
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    let res
    if (queryParams.queryType === 'user') {
      res = await serviceTrackApi.listByUser(queryParams.queryId.trim())
    } else {
      res = await serviceTrackApi.listByOrder(queryParams.queryId.trim())
    }
    if (res.code === 200) {
      const data = res.data || []
      // 非分页接口返回数组，手动分页
      total.value = data.length
      const start = (queryParams.pageNum - 1) * queryParams.pageSize
      const end = start + queryParams.pageSize
      tableData.value = data.slice(start, end)
    }
  } catch (e) {
    ElMessage.error('查询失败')
    console.error('查询服务追踪失败', e)
  } finally {
    loading.value = false
  }
}

/** 查询（重置页码） */
function handleQuery() {
  if (!queryParams.queryId.trim()) {
    ElMessage.warning('请输入查询ID')
    return
  }
  queryParams.pageNum = 1
  fetchData()
}

/** 重置查询条件 */
function handleReset() {
  queryParams.pageNum = 1
  queryParams.queryType = 'user'
  queryParams.queryId = ''
  tableData.value = []
  total.value = 0
}

/** 查看详情 */
function handleView(row) {
  currentTrack.value = row
  detailVisible.value = true
}

/** 打开新建咨询弹窗 */
function handleCreate() {
  createForm.userId = ''
  createForm.workOrderId = ''
  createForm.consultContent = ''
  createDialogVisible.value = true
}

/** 确认创建咨询 */
async function confirmCreate() {
  if (!createFormRef.value) return
  try {
    await createFormRef.value.validate()
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const params = {
      userId: createForm.userId,
      workOrderId: createForm.workOrderId || undefined,
      consultContent: createForm.consultContent
    }
    const res = await serviceTrackApi.create(params)
    if (res.code === 200) {
      ElMessage.success('咨询创建成功')
      createDialogVisible.value = false
      // 如果当前查询条件匹配，刷新列表
      if (queryParams.queryId.trim()) {
        fetchData()
      }
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

/** 打开预约弹窗 */
function handleBook(row) {
  bookForm.id = row.id
  bookForm.userId = row.userId || ''
  bookForm.bookedStartTime = ''
  bookForm.bookedEndTime = ''
  bookForm.serviceType = ''
  bookDialogVisible.value = true
}

/** 确认预约 */
async function confirmBook() {
  if (!bookFormRef.value) return
  try {
    await bookFormRef.value.validate()
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const data = {
      bookedStartTime: bookForm.bookedStartTime,
      bookedEndTime: bookForm.bookedEndTime,
      serviceType: bookForm.serviceType || undefined
    }
    const res = await serviceTrackApi.book(bookForm.id, bookForm.userId, data)
    if (res.code === 200) {
      ElMessage.success('预约成功')
      bookDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '预约失败')
    }
  } catch (e) {
    ElMessage.error('预约失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开开始服务弹窗 */
function handleStart(row) {
  startForm.id = row.id
  startForm.companionId = row.companionId || ''
  startForm.companionName = row.companionName || ''
  startDialogVisible.value = true
}

/** 确认开始服务 */
async function confirmStart() {
  if (!startFormRef.value) return
  try {
    await startFormRef.value.validate()
  } catch (e) {
    return
  }
  actionLoading.value = true
  try {
    const res = await serviceTrackApi.start(startForm.id, startForm.companionId, startForm.companionName)
    if (res.code === 200) {
      ElMessage.success('服务已开始')
      startDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开结束服务弹窗 */
function handleEnd(row) {
  endForm.id = row.id
  endForm.remark = ''
  endDialogVisible.value = true
}

/** 确认结束服务 */
async function confirmEnd() {
  actionLoading.value = true
  try {
    const res = await serviceTrackApi.end(endForm.id, { remark: endForm.remark })
    if (res.code === 200) {
      ElMessage.success('服务已结束')
      endDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

/** 打开评价弹窗 */
function handleRating(row) {
  ratingForm.id = row.id
  ratingForm.rating = 0
  ratingForm.feedback = ''
  ratingDialogVisible.value = true
}

/** 确认提交评价 */
async function confirmRating() {
  if (!ratingFormRef.value) return
  try {
    await ratingFormRef.value.validate()
  } catch (e) {
    return
  }
  if (ratingForm.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  actionLoading.value = true
  try {
    const res = await serviceTrackApi.rating(ratingForm.id, ratingForm.rating, ratingForm.feedback)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      ratingDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error('评价失败')
    console.error(e)
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  // 初始不自动查询，等待用户输入查询条件
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
.muted-text { color: var(--gu-text-muted); font-size: 12px; }
.rating-text { font-weight: 600; color: #F7BA2A; }
</style>
