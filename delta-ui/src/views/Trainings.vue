<!--
  陪玩师培训管理页面，管理培训课程和学习进度

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>培训管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>新增课程
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="companionNickname" label="陪玩师" width="120" />
        <el-table-column prop="courseType" label="培训类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ courseTypeLabel(row.courseType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="trainingStatus" label="培训状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="trainingStatusType(row.trainingStatus)" size="small">{{ trainingStatusLabel(row.trainingStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="examScore" label="考核得分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.examScore != null" :style="{ color: row.examScore >= 60 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.examScore }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startedAt) }}</template>
        </el-table-column>
        <el-table-column prop="completedAt" label="完成时间" width="170">
          <template #default="{ row }">{{ formatTime(row.completedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button v-if="row.trainingStatus === 'NOT_STARTED'" type="primary" size="small" link @click="startTraining(row)">开始学习</el-button>
            <el-button v-if="row.trainingStatus === 'IN_PROGRESS'" type="success" size="small" link @click="openComplete(row)">完成学习</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog :title="form.id ? '编辑课程' : '新增课程'" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form :model="form" label-width="120px">
        <el-form-item label="课程名称" required>
          <el-input id="training-course-name" name="courseName" v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="陪玩师ID">
          <el-input id="training-companion-id" name="companionId" v-model="form.companionId" placeholder="请输入陪玩师ID" />
        </el-form-item>
        <el-form-item label="培训类型">
          <el-select id="training-course-type" name="courseType" v-model="form.courseType" style="width: 100%">
            <el-option label="服务规范" value="SERVICE_STANDARD" />
            <el-option label="话术模板" value="SCRIPT_TEMPLATE" />
            <el-option label="合规培训" value="COMPLIANCE" />
            <el-option label="游戏技能" value="GAME_SKILL" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训内容">
          <el-input id="training-course-content" name="courseContent" v-model="form.courseContent" type="textarea" :rows="6" placeholder="培训内容（支持Markdown）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="完成学习" v-model="completeDialogVisible" width="400px">
      <el-form label-width="100px">
        <el-form-item label="考核得分">
          <el-input-number id="training-complete-score" name="completeScore" v-model="completeScore" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doComplete" :loading="saving">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { trainingApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const completeDialogVisible = ref(false)
const tableData = ref([])
const completeScore = ref(80)
const currentCompleteId = ref(null)

const form = reactive({
  id: null,
  companionId: '',
  courseName: '',
  courseType: 'SERVICE_STANDARD',
  courseContent: ''
})

function courseTypeLabel(type) {
  const map = { SERVICE_STANDARD: '服务规范', SCRIPT_TEMPLATE: '话术模板', COMPLIANCE: '合规培训', GAME_SKILL: '游戏技能' }
  return map[type] || type
}

function trainingStatusType(status) {
  const map = { NOT_STARTED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success' }
  return map[status] || 'info'
}

function trainingStatusLabel(status) {
  const map = { NOT_STARTED: '未开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }
  return map[status] || status
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

function resetForm() {
  Object.assign(form, { id: null, companionId: '', courseName: '', courseType: 'SERVICE_STANDARD', courseContent: '' })
}

async function loadData() {
  loading.value = true
  try {
    const res = await trainingApi.getPage({ page: page.value, size: size.value })
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载培训列表失败')
  } finally {
    loading.value = false
  }
}

function handleCreate() { resetForm(); dialogVisible.value = true }

function handleEdit(row) { Object.assign(form, row); dialogVisible.value = true }

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该培训课程吗？', '确认删除', { type: 'warning' })
    await trainingApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    if (form.id) {
      await trainingApi.update(form)
    } else {
      await trainingApi.create(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function startTraining(row) {
  try {
    await trainingApi.startTraining(row.id)
    ElMessage.success('开始学习')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function openComplete(row) {
  currentCompleteId.value = row.id
  completeScore.value = 80
  completeDialogVisible.value = true
}

async function doComplete() {
  saving.value = true
  try {
    await trainingApi.completeTraining(currentCompleteId.value, { examScore: completeScore.value })
    ElMessage.success('学习完成')
    completeDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
