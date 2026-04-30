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
          <el-button type="primary" @click="showTimeRangeDialog">设置可用时间</el-button>
          <el-button type="success" @click="showBatchRangeDialog">批量设置(多日)</el-button>
          <el-button @click="clearCurrentDay">清空当天</el-button>
        </div>
      </div>

      <div class="time-range-panel" v-if="selectedCompanionId">
        <div class="panel-header">
          <span class="panel-title">{{ currentDate }} 可用时段</span>
          <span class="panel-count">共 {{ schedules.length }} 条记录</span>
        </div>
        
        <el-table :data="schedules" stripe style="width: 100%" v-if="schedules.length > 0">
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
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="当日暂无排班，请点击「设置可用时间」添加" />
      </div>
      <el-empty v-else description="请先选择陪玩师" />
    </el-card>

    <el-dialog v-model="rangeDialogVisible" title="设置可用时间" width="500px">
      <el-form :model="rangeForm" label-width="100px">
        <el-form-item label="开始时间" required>
          <el-time-picker
            v-model="rangeForm.rangeStart"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-picker
            v-model="rangeForm.rangeEnd"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rangeForm.remark" type="textarea" :rows="2" placeholder="可选，如：晚饭休息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rangeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTimeRange" :loading="rangeLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchRangeDialogVisible" title="批量设置可用时间（多日）" width="550px">
      <el-form :model="batchRangeForm" label-width="100px">
        <el-form-item label="开始日期" required>
          <el-date-picker
            v-model="batchRangeForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" required>
          <el-date-picker
            v-model="batchRangeForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日开始" required>
          <el-time-picker
            v-model="batchRangeForm.dailyStart"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="如: 19:00"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日结束" required>
          <el-time-picker
            v-model="batchRangeForm.dailyEnd"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="如: 23:30"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchRangeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchRange" :loading="batchRangeLoading">确定</el-button>
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

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Download } from '@element-plus/icons-vue'
import { companionScheduleApi, companionApi, downloadExcel } from '@/api'
import type { Result, PageResult, CompanionVO, CompanionScheduleVO } from '@/types'

interface ScheduleRow extends CompanionScheduleVO {
  timeSlot: string
  exists: boolean
}

const props = defineProps<{
  companionId?: number | null
}>()

const emit = defineEmits<{ refresh: [] }>()

const companionList = ref<CompanionVO[]>([])
const selectedCompanionId = ref<string | number | null>(null)
const currentDate = ref<string>(new Date().toISOString().split('T')[0])
const schedules = ref<ScheduleRow[]>([])
const rangeDialogVisible = ref<boolean>(false)
const batchRangeDialogVisible = ref<boolean>(false)
const editDialogVisible = ref<boolean>(false)
const rangeLoading = ref<boolean>(false)
const batchRangeLoading = ref<boolean>(false)

const rangeForm = reactive<{
  rangeStart: string
  rangeEnd: string
  remark: string
}>({
  rangeStart: '',
  rangeEnd: '',
  remark: ''
})

const batchRangeForm = reactive<{
  startDate: string | null
  endDate: string | null
  dailyStart: string
  dailyEnd: string
}>({
  startDate: null,
  endDate: null,
  dailyStart: '',
  dailyEnd: ''
})

const editForm = reactive<{
  id: string | null
  status: string
  remark: string
}>({
  id: null,
  status: 'AVAILABLE',
  remark: ''
})

const activeCompanionId = computed<string | number | null>(() => props.companionId || selectedCompanionId.value)

const getStatusType = (status: string): string => {
  const map: Record<string, string> = { 'AVAILABLE': 'success', 'BOOKED': 'warning', 'UNAVAILABLE': 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status: string): string => {
  const map: Record<string, string> = { 'AVAILABLE': '可预约', 'BOOKED': '已预约', 'UNAVAILABLE': '不可用' }
  return map[status] || status
}

const fetchCompanions = async (): Promise<void> => {
  try {
    const res: Result<PageResult<CompanionVO>> = await companionApi.getPage({ pageNum: 1, pageSize: 200, enabled: 1 })
    if (res.code === 200) {
      companionList.value = res.data.records || []
    }
  } catch (e) {
    console.error('获取陪玩师列表失败', e)
  }
}

const fetchSchedules = async (): Promise<void> => {
  if (!activeCompanionId.value) return
  try {
    const res: Result<CompanionScheduleVO[]> = await companionScheduleApi.getByCompanionDate({
      companionId: activeCompanionId.value,
      scheduleDate: currentDate.value
    })
    if (res.code === 200) {
      schedules.value = (res.data || []).map(s => ({ ...s, exists: true }))
    }
  } catch (error) {
    ElMessage.error('获取排班失败')
    console.error('获取排班失败', error)
  }
}

defineExpose({ refresh: fetchSchedules })

const onCompanionChange = (): void => {
  schedules.value = []
  fetchSchedules()
}

const prevDay = (): void => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() - 1)
  currentDate.value = date.toISOString().split('T')[0]
}

const nextDay = (): void => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() + 1)
  currentDate.value = date.toISOString().split('T')[0]
}

const showTimeRangeDialog = (): void => {
  rangeForm.rangeStart = ''
  rangeForm.rangeEnd = ''
  rangeForm.remark = ''
  rangeDialogVisible.value = true
}

const submitTimeRange = async (): Promise<void> => {
  if (!rangeForm.rangeStart || !rangeForm.rangeEnd) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  rangeLoading.value = true
  try {
    await companionScheduleApi.createTimeRange({
      companionId: activeCompanionId.value,
      scheduleDate: currentDate.value,
      rangeStart: rangeForm.rangeStart,
      rangeEnd: rangeForm.rangeEnd
    })
    ElMessage.success('添加成功')
    rangeDialogVisible.value = false
    fetchSchedules()
  } catch (error: any) {
    console.error('添加失败', error)
    ElMessage.error(error?.response?.data?.message || '添加失败')
  } finally {
    rangeLoading.value = false
  }
}

const showBatchRangeDialog = (): void => {
  batchRangeForm.startDate = currentDate.value
  const endDate = new Date(currentDate.value)
  endDate.setDate(endDate.getDate() + 6)
  batchRangeForm.endDate = endDate.toISOString().split('T')[0]
  batchRangeForm.dailyStart = ''
  batchRangeForm.dailyEnd = ''
  batchRangeDialogVisible.value = true
}

const submitBatchRange = async (): Promise<void> => {
  if (!batchRangeForm.startDate || !batchRangeForm.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (!batchRangeForm.dailyStart || !batchRangeForm.dailyEnd) {
    ElMessage.warning('请选择每日时间范围')
    return
  }
  batchRangeLoading.value = true
  try {
    await companionScheduleApi.createTimeRangeBatch({
      companionId: activeCompanionId.value,
      startDate: batchRangeForm.startDate,
      endDate: batchRangeForm.endDate,
      dailyStart: batchRangeForm.dailyStart,
      dailyEnd: batchRangeForm.dailyEnd
    })
    ElMessage.success('批量创建成功')
    batchRangeDialogVisible.value = false
    fetchSchedules()
  } catch (error: any) {
    console.error('批量创建失败', error)
    ElMessage.error(error?.response?.data?.message || '批量创建失败')
  } finally {
    batchRangeLoading.value = false
  }
}

const editSchedule = (row: ScheduleRow): void => {
  editForm.id = row.id
  editForm.status = row.status
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

const submitEdit = async (): Promise<void> => {
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

const updateStatus = async (row: ScheduleRow, status: string): Promise<void> => {
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

const deleteSchedule = (row: ScheduleRow): void => {
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

const clearCurrentDay = (): void => {
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

const handleExport = (): void => {
  const params: Record<string, any> = {}
  if (activeCompanionId.value) params.companionId = activeCompanionId.value
  if (currentDate.value) params.scheduleDate = currentDate.value
  downloadExcel('/companion-schedules/export', params, '排班管理.xlsx')
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

.time-range-panel { margin-top: 10px; }

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.panel-title { font-size: 15px; font-weight: 600; color: #303133; }

.panel-count { font-size: 13px; color: #909399; }
</style>
