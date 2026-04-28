<template>
  <div class="dash-page">
    <div class="dash-header">
      <div class="dash-title-area">
        <h2 class="dash-title">数据总览</h2>
        <p class="dash-subtitle">实时运营数据监控</p>
      </div>
      <el-select v-model="period" style="width:140px" :teleported="false" @change="fetchData">
        <el-option label="今日" value="DAILY" />
        <el-option label="本周" value="WEEKLY" />
        <el-option label="本月" value="MONTHLY" />
        <el-option label="本季" value="QUARTERLY" />
        <el-option label="本年" value="YEARLY" />
      </el-select>
    </div>

    <div v-loading="loading">
      <div class="stat-grid">
        <div
          v-for="(card, idx) in overviewCards"
          :key="card.title"
          :class="['stat-card', `stat-card--${card.type}`, 'animate-fade-in-up', `animate-delay-${idx + 1}`]"
        >
          <div class="card-icon-wrap" :style="{ background: card.iconBg }">
            <el-icon :size="22" :style="{ color: card.color }"><component :is="card.icon" /></el-icon>
          </div>
          <div class="card-content">
            <span class="card-label">{{ card.title }}</span>
            <span class="card-value" :style="{ color: card.color }">{{ card.value }}</span>
          </div>
        </div>
      </div>

      <div class="data-grid">
        <div class="data-section" v-if="statsData?.trendData?.length > 0">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>消息趋势</span>
                <el-tag type="info" size="small">{{ period === 'DAILY' ? '今日' : period === 'WEEKLY' ? '本周' : period === 'MONTHLY' ? '本月' : period === 'QUARTERLY' ? '本季' : '本年' }}</el-tag>
              </div>
            </template>
            <el-table :data="statsData.trendData" stripe size="small">
              <el-table-column prop="date" label="日期" width="140" />
              <el-table-column prop="messageCount" label="消息数" width="100" />
            </el-table>
          </el-card>
        </div>

        <div class="data-section" v-if="statsData?.csUserData?.length > 0">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>客服数据</span>
                <el-tag type="success" size="small">{{ statsData.csUserData.length }} 位客服</el-tag>
              </div>
            </template>
            <el-table :data="statsData.csUserData" stripe size="small">
              <el-table-column prop="csUserName" label="客服" width="120" />
              <el-table-column prop="messageCount" label="消息数" width="90" />
              <el-table-column prop="customerCount" label="服务客户" width="100" />
              <el-table-column prop="avgResponseTime" label="响应(秒)" width="110" />
              <el-table-column prop="resolutionRate" label="解决率" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.resolutionRate >= 80 ? 'success' : row.resolutionRate >= 50 ? 'warning' : 'danger'" size="small">
                    {{ row.resolutionRate }}%
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { statsApi } from '@/api'
import { ChatDotRound, User, Clock, Bell, ChatLineRound, Money } from '@element-plus/icons-vue'

const loading = ref(false)
const period = ref('DAILY')
const statsData = ref(null)
const userInfo = ref(null)

const overviewCards = computed(() => {
  if (!statsData.value?.overview) return []
  const o = statsData.value.overview
  const cards = [
    { title: '消息总数', value: o.totalMessages || 0, icon: ChatDotRound, color: '#6366F1', iconBg: 'rgba(99,102,241,0.08)', type: 'primary' },
    { title: '服务客户', value: o.totalCustomers || 0, icon: User, color: '#10B981', iconBg: 'rgba(16,185,129,0.08)', type: 'success' }
  ]
  if (o.avgResponseTime !== undefined) cards.push({ title: '平均响应', value: o.avgResponseTime + 's', icon: Clock, color: '#F59E0B', iconBg: 'rgba(245,158,11,0.08)', type: 'warning' })
  if (o.pendingCount !== undefined) cards.push({ title: '待办事项', value: o.pendingCount, icon: Bell, color: '#EF4444', iconBg: 'rgba(239,68,68,0.08)', type: 'danger' })
  if (o.aiReplyCount !== undefined) cards.push({ title: 'AI回复', value: o.aiReplyCount, icon: ChatLineRound, color: '#8B5CF6', iconBg: 'rgba(139,92,246,0.08)', type: 'secondary' })
  if (o.manualReplyCount !== undefined) cards.push({ title: '人工回复', value: o.manualReplyCount, icon: Money, color: '#F97316', iconBg: 'rgba(249,115,22,0.08)', type: 'accent' })
  return cards
})

const fetchData = async () => {
  loading.value = true
  try {
    let res
    const params = { period: period.value }
    if (userInfo.value) {
      if (userInfo.value.role === 'CS_STAFF') { params.csUserId = userInfo.value.id; res = await statsApi.getPersonal(params) }
      else if (userInfo.value.role === 'CS_LEADER') res = await statsApi.getTeam(params)
      else res = await statsApi.getGlobal(params)
      if (res.code === 200) statsData.value = res.data
    } else {
      res = await statsApi.getGlobal(params)
      if (res.code === 200) statsData.value = res.data
    }
  } catch (e) { ElMessage.error('获取统计数据失败') }
  finally { loading.value = false }
}

onMounted(() => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    try { userInfo.value = JSON.parse(info) } catch { userInfo.value = null }
  }
  fetchData()
})
watch(() => period.value, () => fetchData())
</script>

<style scoped>
.dash-page { padding: 0; }

.dash-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.dash-title-area { display: flex; flex-direction: column; gap: 4px; }

.dash-title {
  font-family: var(--gu-font-heading);
  font-size: 22px;
  font-weight: 700;
  color: var(--gu-text-primary);
  letter-spacing: -0.02em;
}

.dash-subtitle {
  font-size: 13px;
  color: var(--gu-text-muted);
  font-weight: 400;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--gu-bg-card);
  border: 1px solid var(--gu-border);
  border-radius: var(--gu-radius-xl);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all var(--gu-transition);
  cursor: default;
}

.stat-card:hover {
  box-shadow: var(--gu-shadow-md);
  transform: translateY(-2px);
}

.card-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--gu-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-label {
  font-size: 12px;
  color: var(--gu-text-muted);
  font-weight: 500;
  letter-spacing: 0.02em;
}

.card-value {
  font-family: var(--gu-font-heading);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
}

.data-section { min-width: 0; }

@media (max-width: 768px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr) !important; }
  .data-grid { grid-template-columns: 1fr !important; }
  .dash-header { flex-direction: column; gap: 12px; }
}
</style>
