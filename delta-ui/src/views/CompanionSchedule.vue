<!--
   陪玩师日程管理页面 - 陪玩师自行设定每日可接单时间段

   @author 刘建国
-->

<template>
  <div class="companion-schedule-page">
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px">
      <h3 style="margin: 0">日程管理 - 设置可接单时间</h3>
      <el-button type="primary" @click="scheduleDialogVisible = true">
        <el-icon style="margin-right: 4px"><Plus /></el-icon>
        添加时间段
      </el-button>
    </div>

    <!-- 快速设置面板 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header>
        <span style="font-weight: 600">批量设置</span>
      </template>
      <el-form :model="batchForm" label-width="100px" inline>
        <el-form-item label="起止日期">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            :disabled-date="disabledPastDate"
          />
        </el-form-item>
        <el-form-item label="每日开始">
          <el-time-picker v-model="batchForm.dailyStart" format="HH:mm" value-format="HH:mm:00" placeholder="每天开始时间" />
        </el-form-item>
        <el-form-item label="每日结束">
          <el-time-picker v-model="batchForm.dailyEnd" format="HH:mm" value-format="HH:mm:00" placeholder="每天结束时间" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleBatchCreate" :loading="batchLoading">批量生成</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日程列表 -->
    <el-card shadow="never">
      <template #header>
        <span style="font-weight: 600">已有日程</span>
      </template>
      <el-table :data="scheduleList" border stripe v-loading="loading" max-height="500">
        <el-table-column prop="scheduleDate" label="日期" width="140" sortable />
        <el-table-column prop="startTime" label="开始时间" width="120" />
        <el-table-column prop="endTime" label="结束时间" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'AVAILABLE' ? 'success' : row.status === 'BOOKED' ? 'warning' : 'info'" size="small">
              {{ row.status === 'AVAILABLE' ? '可预约' : row.status === 'BOOKED' ? '已预约' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDelete(row.id)" :disabled="row.status === 'BOOKED'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加时间段弹窗 -->
    <el-dialog v-model="scheduleDialogVisible" title="添加可接单时间段" width="480px" destroy-on-close>
      <el-form :model="scheduleForm" label-width="90px">
        <el-form-item label="日期" required>
          <el-date-picker
            v-model="scheduleForm.scheduleDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledPastDate"
          />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-select
            v-model="scheduleForm.startTime"
            :max-time="scheduleForm.endTime"
            placeholder="选择开始时间"
            start="00:00"
            step="00:30"
            end="23:30"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-select
            v-model="scheduleForm.endTime"
            :min-time="scheduleForm.startTime"
            placeholder="选择结束时间"
            start="00:00"
            step="00:30"
            end="24:00"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.note" placeholder="可选备注" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSchedule" :loading="scheduleLoading">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { companionScheduleApi, companionApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { Result, CompanionScheduleVO } from '@/types'

const authStore = useAuthStore()

const companionId = ref<number | null>(null)
const loading = ref(false)
const scheduleList = ref<CompanionScheduleVO[]>([])

const scheduleDialogVisible = ref(false)
const scheduleLoading = ref(false)

const scheduleForm = reactive({
  scheduleDate: '',
  startTime: '',
  endTime: '',
  note: ''
})

const batchForm = reactive({
  dateRange: [] as string[],
  dailyStart: '',
  dailyEnd: ''
})
const batchLoading = ref(false)

const disabledPastDate = (time: Date): boolean => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

async function loadCompanionId(): Promise<void> {
  const userId = authStore.userInfo?.id
  if (!userId) return
  try {
    const res: Result<{ id: number }> = await companionApi.getByUserId(userId)
    if (res.code === 200 && res.data) {
      companionId.value = res.data.id
      fetchScheduleList()
    }
  } catch (e) {
    console.error('获取陪玩师信息失败', e)
  }
}

async function fetchScheduleList(): Promise<void> {
  if (!companionId.value) return
  loading.value = true
  try {
    const res: Result<CompanionScheduleVO[]> = await companionScheduleApi.getListByCompanionId(companionId.value)
    if (res.code === 200) {
      scheduleList.value = res.data || []
    }
  } catch (e) {
    console.error('获取日程列表失败', e)
  } finally {
    loading.value = false
  }
}

async function handleAddSchedule(): Promise<void> {
  if (!companionId.value || !scheduleForm.scheduleDate || !scheduleForm.startTime || !scheduleForm.endTime) {
    ElMessage.warning('请填写完整的时间信息')
    return
  }
  scheduleLoading.value = true
  try {
    const res: Result<null> = await companionScheduleApi.createTimeRange({
      companionId: String(companionId.value),
      scheduleDate: scheduleForm.scheduleDate,
      rangeStart: scheduleForm.startTime + ':00',
      rangeEnd: scheduleForm.endTime + ':00'
    })
    if (res.code === 200) {
      ElMessage.success('时间段添加成功')
      scheduleDialogVisible.value = false
      scheduleForm.scheduleDate = ''
      scheduleForm.startTime = ''
      scheduleForm.endTime = ''
      scheduleForm.note = ''
      fetchScheduleList()
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '添加失败')
  } finally {
    scheduleLoading.value = false
  }
}

async function handleBatchCreate(): Promise<void> {
  if (!companionId.value) return
  if (!batchForm.dateRange || batchForm.dateRange.length < 2 || !batchForm.dailyStart || !batchForm.dailyEnd) {
    ElMessage.warning('请填写完整的批量设置信息')
    return
  }
  batchLoading.value = true
  try {
    const res: Result<null> = await companionScheduleApi.createTimeRangeBatch({
      companionId: String(companionId.value),
      startDate: batchForm.dateRange[0],
      endDate: batchForm.dateRange[1],
      dailyStart: batchForm.dailyStart,
      dailyEnd: batchForm.dailyEnd
    })
    if (res.code === 200) {
      ElMessage.success('批量创建成功')
      fetchScheduleList()
    } else {
      ElMessage.error(res.message || '批量创建失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '批量创建失败')
  } finally {
    batchLoading.value = false
  }
}

async function handleDelete(id: string): Promise<void> {
  try {
    await ElMessageBox.confirm('确认删除该时间段？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res: Result<null> = await companionScheduleApi.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchScheduleList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadCompanionId()
})
</script>

<style scoped>
.companion-schedule-page {
  padding: 16px;
}
</style>