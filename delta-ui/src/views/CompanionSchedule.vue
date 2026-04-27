<template>
  <div class="schedule-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排班管理</span>
        </div>
      </template>

      <div class="schedule-toolbar">
        <div class="toolbar-left">
          <el-select
            v-model="selectedCompanionId"
            placeholder="选择陪玩师"
            filterable
            style="width: 220px"
            :teleported="false"
            @change="onCompanionChange"
          >
            <el-option
              v-for="c in companionList"
              :key="c.id"
              :label="c.nickname + (c.realName ? ' (' + c.realName + ')' : '')"
              :value="c.id"
            />
          </el-select>
          <el-button @click="prevDay"><el-icon><ArrowLeft /></el-icon></el-button>
          <el-date-picker
            v-model="currentDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
          <el-button @click="nextDay"><el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="toolbar-right" v-if="selectedCompanionId">
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button @click="importRef?.click()">
            <el-icon><Upload /></el-icon>
            导入
          </el-button>
          <input ref="importRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleImport" />
          <el-button type="primary" @click="showBatchCreateDialog">批量生成</el-button>
          <el-button @click="clearCurrentDay">清空当天</el-button>
        </div>
      </div>

      <div class="schedule-grid" v-if="selectedCompanionId">
        <el-table :data="timeSlots" stripe style="width: 100%">
          <el-table-column label="时间段" width="160">
            <template #default="{ row }">
              <strong>{{ row.timeSlot }}</strong>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small" v-if="row.exists">
                {{ getStatusText(row.status) }}
              </el-tag>
              <el-tag type="info" size="small" v-else>未设置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" show-overflow-tooltip min-width="200">
            <template #default="{ row }">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <template v-if="row.exists">
                <template v-if="row.status === 'AVAILABLE'">
                  <el-button link type="warning" size="small" @click="updateStatus(row, 'BOOKED')">已约</el-button>
                  <el-button link type="primary" size="small" @click="editSchedule(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="deleteSchedule(row)">删除</el-button>
                </template>
                <template v-else-if="row.status === 'BOOKED'">
                  <el-button link type="success" size="small" @click="updateStatus(row, 'AVAILABLE')">空闲</el-button>
                  <el-button link type="primary" size="small" @click="editSchedule(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="deleteSchedule(row)">删除</el-button>
                </template>
                <template v-else>
                  <el-button link type="primary" size="small" @click="editSchedule(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="deleteSchedule(row)">删除</el-button>
                </template>
              </template>
              <template v-else>
                <el-button type="primary" size="small" @click="addSchedule(row)">添加</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="请先选择陪玩师" />
    </el-card>

    <el-dialog v-model="batchDialogVisible" title="批量生成时间" width="600px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="batchForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="batchForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <el-checkbox-group v-model="batchForm.timeSlots">
            <el-checkbox v-for="slot in defaultTimeSlots" :key="slot" :value="slot">{{ slot }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchCreate" :loading="batchLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑时间" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%" :teleported="false">
            <el-option label="可预约" value="AVAILABLE" />
            <el-option label="已预约" value="BOOKED" />
            <el-option label="不可用" value="UNAVAILABLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Download, Upload } from '@element-plus/icons-vue'
import { companionScheduleApi, companionApi, downloadExcel, uploadExcel } from '@/api'

const props = defineProps({
  companionId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['refresh'])

const companionList = ref([])
const selectedCompanionId = ref(null)
const currentDate = ref(new Date().toISOString().split('T')[0])
const schedules = ref([])
const batchDialogVisible = ref(false)
const editDialogVisible = ref(false)
const batchLoading = ref(false)

const defaultTimeSlots = [
  '08:00-10:00', '10:00-12:00', '12:00-14:00',
  '14:00-16:00', '16:00-18:00', '18:00-20:00',
  '20:00-22:00', '22:00-24:00'
]

const batchForm = reactive({
  startDate: null,
  endDate: null,
  timeSlots: []
})

const editForm = reactive({
  id: null,
  status: 'AVAILABLE',
  remark: ''
})

const activeCompanionId = computed(() => props.companionId || selectedCompanionId.value)

const timeSlots = computed(() => {
  return defaultTimeSlots.map(slot => {
    const existing = schedules.value.find(s => s.timeSlot === slot)
    if (existing) {
      return { ...existing, exists: true }
    }
    const [start, end] = slot.split('-')
    return {
      timeSlot: slot,
      startTime: start + ':00',
      endTime: end + ':00',
      status: null,
      remark: '',
      exists: false
    }
  })
})

const getStatusType = (status) => {
  const map = { 'AVAILABLE': 'success', 'BOOKED': 'warning', 'UNAVAILABLE': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'AVAILABLE': '可预约', 'BOOKED': '已预约', 'UNAVAILABLE': '不可用' }
  return map[status] || status
}

const fetchCompanions = async () => {
  try {
    const res = await companionApi.getPage({ pageNum: 1, pageSize: 200, enabled: 1 })
    if (res.code === 200) {
      companionList.value = res.data.records || []
    }
  } catch (e) {
    console.error('获取陪玩师列表失败', e)
  }
}

const fetchSchedules = async () => {
  if (!activeCompanionId.value) return
  try {
    const res = await companionScheduleApi.getByCompanionDate({
      companionId: activeCompanionId.value,
      scheduleDate: currentDate.value
    })
    if (res.code === 200) {
      schedules.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取排班失败')
    console.error('获取排班失败', error)
  }
}

const onCompanionChange = () => {
  schedules.value = []
  fetchSchedules()
}

const prevDay = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() - 1)
  currentDate.value = date.toISOString().split('T')[0]
}

const nextDay = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() + 1)
  currentDate.value = date.toISOString().split('T')[0]
}

const addSchedule = async (slot) => {
  try {
    const [start, end] = slot.timeSlot.split('-')
    await companionScheduleApi.create({
      companionId: activeCompanionId.value,
      scheduleDate: currentDate.value,
      timeSlot: slot.timeSlot,
      startTime: start + ':00',
      endTime: end + ':00',
      status: 'AVAILABLE',
      remark: ''
    })
    ElMessage.success('添加成功')
    fetchSchedules()
  } catch (error) {
    console.error('添加失败', error)
    ElMessage.error('添加失败')
  }
}

const editSchedule = (row) => {
  editForm.id = row.id
  editForm.status = row.status
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await companionScheduleApi.update({
      id: editForm.id,
      status: editForm.status,
      remark: editForm.remark
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    fetchSchedules()
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error('更新失败')
  }
}

const updateStatus = async (row, status) => {
  try {
    await companionScheduleApi.updateStatus({
      id: row.id,
      status: status
    })
    ElMessage.success('更新成功')
    fetchSchedules()
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error('更新失败')
  }
}

const deleteSchedule = (row) => {
  ElMessageBox.confirm('确定要删除该时间段吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await companionScheduleApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchSchedules()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const showBatchCreateDialog = () => {
  batchForm.startDate = currentDate.value
  const endDate = new Date(currentDate.value)
  endDate.setDate(endDate.getDate() + 6)
  batchForm.endDate = endDate.toISOString().split('T')[0]
  batchForm.timeSlots = [...defaultTimeSlots]
  batchDialogVisible.value = true
}

const submitBatchCreate = async () => {
  if (!batchForm.startDate || !batchForm.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (batchForm.timeSlots.length === 0) {
    ElMessage.warning('请选择时间段')
    return
  }

  batchLoading.value = true
  try {
    const params = {
      companionId: activeCompanionId.value,
      startDate: batchForm.startDate,
      endDate: batchForm.endDate
    }
    await companionScheduleApi.createBatch(params, batchForm.timeSlots)
    ElMessage.success('批量生成成功')
    batchDialogVisible.value = false
    fetchSchedules()
  } catch (error) {
    console.error('批量生成失败', error)
    ElMessage.error('批量生成失败')
  } finally {
    batchLoading.value = false
  }
}

const clearCurrentDay = () => {
  ElMessageBox.confirm('确定要清空当天所有时间吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await companionScheduleApi.deleteByCompanionDate({
        companionId: activeCompanionId.value,
        scheduleDate: currentDate.value
      })
      ElMessage.success('清空成功')
      fetchSchedules()
    } catch (error) {
      console.error('清空失败', error)
      ElMessage.error('清空失败')
    }
  }).catch(() => {})
}

const importRef = ref(null)

const handleExport = () => {
  const params = {}
  if (activeCompanionId.value) params.companionId = activeCompanionId.value
  if (currentDate.value) params.scheduleDate = currentDate.value
  downloadExcel('/companion-schedules/export', params, '排班管理.xlsx')
}

const handleImport = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadExcel('/companion-schedules/import', file)
    ElMessage.success(`导入完成：成功${res.data.success}条，失败${res.data.fail}条，共${res.data.total}条`)
    fetchSchedules()
  } catch (e) {
    ElMessage.error('导入失败')
  } finally {
    event.target.value = ''
  }
}

watch(() => props.companionId, (val) => {
  if (val) {
    selectedCompanionId.value = val
    fetchSchedules()
  }
})

watch(() => currentDate.value, () => {
  fetchSchedules()
})

onMounted(() => {
  fetchCompanions()
  if (props.companionId) {
    selectedCompanionId.value = props.companionId
    fetchSchedules()
  }
})

defineExpose({ refresh: fetchSchedules })
</script>

<style scoped>
.schedule-page { padding: 0; }

.card-header { display: flex; justify-content: space-between; align-items: center; }

.schedule-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.schedule-grid { margin-top: 10px; }
</style>
