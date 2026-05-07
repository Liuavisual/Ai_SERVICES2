<!--
  AI质检记录页面，展示AI全流程质检结果和违规记录

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>质检记录</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterRiskLevel" placeholder="风险等级" clearable @change="loadData" style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="安全" value="SAFE" />
          <el-option label="低风险" value="LOW" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="高风险" value="HIGH" />
          <el-option label="严重违规" value="CRITICAL" />
        </el-select>
        <el-select v-model="filterHandleStatus" placeholder="处理状态" clearable @change="loadData" style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="已审核" value="REVIEWED" />
          <el-option label="已处理" value="RESOLVED" />
          <el-option label="已忽略" value="IGNORED" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="companionNickname" label="陪玩师" width="120" />
        <el-table-column prop="userName" label="客户" width="120" />
        <el-table-column prop="checkType" label="检测类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ checkTypeLabel(row.checkType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="riskType(row.riskLevel)" size="small">{{ riskLabel(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.score >= 80 ? '#67c23a' : row.score >= 60 ? '#e6a23c' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="violationType" label="违规类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.violationType" type="danger" size="small">{{ row.violationType }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="violationSummary" label="违规摘要" min-width="180" show-overflow-tooltip />
        <el-table-column prop="checkTime" label="检测时间" width="170">
          <template #default="{ row }">{{ formatTime(row.checkTime) }}</template>
        </el-table-column>
        <el-table-column prop="handleStatus" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="handleStatusType(row.handleStatus)" size="small">{{ handleStatusLabel(row.handleStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.handleStatus === 'PENDING'" type="primary" size="small" link @click="handleAction(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog title="处理质检记录" v-model="dialogVisible" width="500px">
      <el-form label-width="120px">
        <el-form-item label="处理状态">
          <el-select v-model="handleForm.handleStatus" style="width: 100%">
            <el-option label="已审核" value="REVIEWED" />
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="处理人ID">
          <el-input v-model="handleForm.handlerId" placeholder="请输入处理人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doHandle" :loading="saving">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { qualityCheckApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const filterRiskLevel = ref('')
const filterHandleStatus = ref('')
const tableData = ref([])

const handleForm = reactive({
  id: null,
  handleStatus: 'REVIEWED',
  handleRemark: '',
  handlerId: '1'
})

function checkTypeLabel(type) {
  const map = { SERVICE: '服务质量', CONTENT: '内容合规', ATTITUDE: '服务态度', SPEED: '响应速度' }
  return map[type] || type
}

function riskType(level) {
  const map = { SAFE: 'success', LOW: 'warning', MEDIUM: '', HIGH: 'danger', CRITICAL: 'danger' }
  return map[level] || 'info'
}

function riskLabel(level) {
  const map = { SAFE: '安全', LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '严重违规' }
  return map[level] || level
}

function handleStatusType(status) {
  const map = { PENDING: 'warning', REVIEWED: '', RESOLVED: 'success', IGNORED: 'info' }
  return map[status] || 'info'
}

function handleStatusLabel(status) {
  const map = { PENDING: '待处理', REVIEWED: '已审核', RESOLVED: '已处理', IGNORED: '已忽略' }
  return map[status] || status
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterRiskLevel.value) params.riskLevel = filterRiskLevel.value
    if (filterHandleStatus.value) params.handleStatus = filterHandleStatus.value
    const res = await qualityCheckApi.getPage(params)
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载质检记录失败')
  } finally {
    loading.value = false
  }
}

function handleAction(row) {
  handleForm.id = row.id
  handleForm.handleStatus = 'REVIEWED'
  handleForm.handleRemark = ''
  dialogVisible.value = true
}

async function doHandle() {
  saving.value = true
  try {
    await qualityCheckApi.handle(handleForm.id, handleForm)
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('处理失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
