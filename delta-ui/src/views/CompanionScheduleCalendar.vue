<template>
  <div class="schedule-calendar-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">📅 排班日历视图</span>
          <div class="header-actions">
            <el-select v-model="selectedCompanionId" placeholder="全部陪玩师" filterable clearable style="width:200px" @change="loadSchedules">
              <el-option v-for="c in companions" :key="c.id" :label="c.nickname" :value="c.id" />
            </el-select>
            <el-button @click="goToday">今天</el-button>
          </div>
        </div>
      </template>

      <div class="calendar-nav">
        <el-button @click="prevMonth" circle><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="month-label">{{ currentYear }}年 {{ currentMonth }}月</span>
        <el-button @click="nextMonth" circle><el-icon><ArrowRight /></el-icon></el-button>
      </div>

      <div class="calendar-grid-wrapper">
        <div class="weekday-header">
          <span v-for="day in weekdays" :key="day" class="weekday-cell">{{ day }}</span>
        </div>

        <div class="calendar-grid">
          <div
            v-for="(cell, idx) in calendarCells"
            :key="idx"
            class="calendar-cell"
            :class="{
              'is-today': cell.isToday,
              'is-other-month': cell.isOtherMonth,
              'has-schedules': cell.scheduleCount > 0,
              'is-selected': selectedDate === cell.dateStr
            }"
            @click="selectDate(cell)"
          >
            <span class="cell-date">{{ cell.day }}</span>
            <span v-if="cell.scheduleCount > 0" class="cell-badge">{{ cell.scheduleCount }}条</span>
          </div>
        </div>
      </div>

      <div v-if="selectedDate" class="schedule-detail-panel">
        <el-divider />
        <div class="detail-header">
          <span class="detail-title">{{ selectedDate }} 排班详情</span>
          <el-tag v-if="computedSchedules.length > 0" type="success" size="small">
            {{ computedSchedules.length }}条可用时段
          </el-tag>
          <el-tag v-else type="info" size="small">当天无排班</el-tag>
        </div>

        <el-table v-if="computedSchedules.length > 0" :data="computedSchedules" stripe size="small" style="margin-top:12px">
          <el-table-column label="陪玩师" prop="companionNickname" width="140" />
          <el-table-column label="时间" width="180">
            <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'AVAILABLE' ? 'success' : (row.status === 'BOOKED' ? 'warning' : 'info')" size="small">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" v-if="selectedCompanionId">
            <template #default="{ row }">
              <el-button v-if="row.status === 'AVAILABLE'" type="danger" size="small" link @click="deleteSchedule(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { companionScheduleApi, companionApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CompanionScheduleVO } from '@/types'

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

interface CalendarCell {
  day: number
  dateStr: string
  isToday: boolean
  isOtherMonth: boolean
  scheduleCount: number
}

interface CompanionOption {
  id: number | string
  nickname: string
  realName?: string | null
}

const companions = ref<CompanionOption[]>([])
const selectedCompanionId = ref<number | string | null>(null)
const selectedDate = ref<string>('')

const now = new Date()
const currentYear = ref(now.getFullYear())
const currentMonth = ref(now.getMonth() + 1)

const scheduleMap = ref<Record<string, CompanionScheduleVO[]>>({})

const calendarCells = computed<CalendarCell[]>((): CalendarCell[] => {
  const cells: CalendarCell[] = []
  const today = new Date()
  const todayStr = today.toISOString().split('T')[0]

  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  const startDay = firstDay.getDay()

  const lastDay = new Date(currentYear.value, currentMonth.value, 0)
  const totalDays = lastDay.getDate()

  const prevLastDay = new Date(currentYear.value, currentMonth.value - 1, 0)
  const prevTotalDays = prevLastDay.getDate()

  for (let i = startDay - 1; i >= 0; i--) {
    const day = prevTotalDays - i
    const m = currentMonth.value === 1 ? 12 : currentMonth.value - 1
    const y = currentMonth.value === 1 ? currentYear.value - 1 : currentYear.value
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateStr,
      isToday: dateStr === todayStr,
      isOtherMonth: true,
      scheduleCount: (scheduleMap.value[dateStr] || []).length
    })
  }

  for (let day = 1; day <= totalDays; day++) {
    const dateStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateStr,
      isToday: dateStr === todayStr,
      isOtherMonth: false,
      scheduleCount: (scheduleMap.value[dateStr] || []).length
    })
  }

  const remaining = 42 - cells.length
  for (let day = 1; day <= remaining; day++) {
    const m = currentMonth.value === 12 ? 1 : currentMonth.value + 1
    const y = currentMonth.value === 12 ? currentYear.value + 1 : currentYear.value
    const dateStr = `${y}-${String(m).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateStr,
      isToday: dateStr === todayStr,
      isOtherMonth: true,
      scheduleCount: (scheduleMap.value[dateStr] || []).length
    })
  }

  return cells
})

const computedSchedules = computed<CompanionScheduleVO[]>(() => {
  return scheduleMap.value[selectedDate.value] || []
})

const statusLabel = (status: string): string => {
  const map: Record<string, string> = {
    AVAILABLE: '可用',
    BOOKED: '已预约',
    UNAVAILABLE: '不可用',
    PENDING: '待确认'
  }
  return map[status] || status
}

const loadCompanions = async (): Promise<void> => {
  try {
    const res = await companionApi.getList()
    if (res.code === 200 && res.data) {
      companions.value = Array.isArray(res.data)
        ? res.data.map((c: any) => ({ id: c.id, nickname: c.nickname || c.realName || `ID:${c.id}`, realName: c.realName }))
        : []
    }
  } catch (error) {
    console.error('加载陪玩师列表失败', error)
  }
}

const loadSchedules = async (): Promise<void> => {
  const startDate = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-01`
  const lastDay = new Date(currentYear.value, currentMonth.value, 0).getDate()
  const endDate = `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`

  try {
    const params: Record<string, unknown> = { startDate, endDate }
    if (selectedCompanionId.value) {
      params.companionId = selectedCompanionId.value
    }
    const res = await companionScheduleApi.getByDate(params)
    if (res.code === 200 && res.data) {
      const list: CompanionScheduleVO[] = Array.isArray(res.data) ? res.data : []
      const map: Record<string, CompanionScheduleVO[]> = {}
      list.forEach((s: CompanionScheduleVO) => {
        if (s.scheduleDate) {
          if (!map[s.scheduleDate]) map[s.scheduleDate] = []
          map[s.scheduleDate].push(s)
        }
      })
      scheduleMap.value = map
    }
  } catch (error) {
    console.error('加载排班数据失败', error)
  }
}

const selectDate = (cell: CalendarCell): void => {
  selectedDate.value = cell.dateStr
}

const deleteSchedule = (row: CompanionScheduleVO): void => {
  ElMessageBox.confirm('确定要删除该时段吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await companionScheduleApi.delete(row.id as string)
      ElMessage.success('删除成功')
      loadSchedules()
    } catch (error) {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const prevMonth = (): void => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  selectedDate.value = ''
  loadSchedules()
}

const nextMonth = (): void => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  selectedDate.value = ''
  loadSchedules()
}

const goToday = (): void => {
  const today = new Date()
  currentYear.value = today.getFullYear()
  currentMonth.value = today.getMonth() + 1
  selectedDate.value = today.toISOString().split('T')[0]
  loadSchedules()
}

onMounted(() => {
  loadCompanions()
  loadSchedules()
})
</script>

<style scoped>
.schedule-calendar-page { padding: 0; }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 18px; font-weight: 600; }
.header-actions { display: flex; gap: 10px; align-items: center; }

.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 16px;
}
.month-label { font-size: 18px; font-weight: 600; min-width: 140px; text-align: center; }

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-weight: 600;
  color: #606266;
  padding: 8px 0;
  border-bottom: 2px solid #ebeef5;
  margin-bottom: 4px;
}
.weekday-cell { padding: 4px; }

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}
.calendar-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 72px;
  padding: 6px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.calendar-cell:hover { border-color: #409eff; background: #ecf5ff; }
.calendar-cell.is-today { border-color: #409eff; background: #ecf5ff; }
.calendar-cell.is-today .cell-date { color: #409eff; font-weight: 700; }
.calendar-cell.is-other-month { opacity: 0.35; }
.calendar-cell.is-selected { border-color: #409eff; background: #d9ecff; box-shadow: 0 0 0 2px rgba(64,158,255,0.2); }
.calendar-cell.has-schedules { border-color: #67c23a; }
.cell-date { font-size: 15px; }

.cell-badge {
  font-size: 11px;
  color: #fff;
  background: #67c23a;
  padding: 1px 6px;
  border-radius: 8px;
  margin-top: 3px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.detail-title { font-size: 16px; font-weight: 600; }
</style>