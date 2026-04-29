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
        <div class="data-section trend-section" v-if="statsData?.trendData?.length > 0">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>消息趋势</span>
                <el-tag type="info" size="small">{{ periodLabel }}</el-tag>
              </div>
            </template>
            <div class="trend-chart">
              <div
                v-for="(item, idx) in statsData.trendData"
                :key="idx"
                class="trend-bar-group"
              >
                <div class="trend-bar-wrapper">
                  <div
                    class="trend-bar"
                    :style="{ height: getBarHeight(item.messageCount) + '%' }"
                    :title="`${item.date}: ${item.messageCount}条`"
                  ></div>
                </div>
                <span class="trend-label">{{ item.date }}</span>
              </div>
            </div>
            <el-table :data="statsData.trendData" stripe size="small" style="margin-top:16px">
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

        <div class="data-section" v-if="statsData?.overview">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>运营指标</span>
                <el-tag type="warning" size="small">核心KPI</el-tag>
              </div>
            </template>
            <div class="kpi-list">
              <div class="kpi-item">
                <span class="kpi-label">解决率</span>
                <el-progress
                  :percentage="statsData.overview.resolutionRate || 0"
                  :color="getProgressColor(statsData.overview.resolutionRate)"
                  :stroke-width="12"
                  style="flex:1"
                />
                <span class="kpi-value">{{ statsData.overview.resolutionRate || 0 }}%</span>
              </div>
              <div class="kpi-item">
                <span class="kpi-label">满意度</span>
                <el-progress
                  :percentage="statsData.overview.customerSatisfaction || 0"
                  :color="getProgressColor(statsData.overview.customerSatisfaction)"
                  :stroke-width="12"
                  style="flex:1"
                />
                <span class="kpi-value">{{ statsData.overview.customerSatisfaction || 0 }}%</span>
              </div>
              <div class="kpi-item">
                <span class="kpi-label">AI占比</span>
                <el-progress
                  :percentage="aiRatio"
                  color="#8B5CF6"
                  :stroke-width="12"
                  style="flex:1"
                />
                <span class="kpi-value">{{ aiRatio }}%</span>
              </div>
              <div class="kpi-item">
                <span class="kpi-label">活跃客服</span>
                <span class="kpi-value kpi-value--large">{{ statsData.overview.activeCsCount || 0 }}</span>
              </div>
            </div>
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
import { useAuthStore } from '@/stores/auth'
import { ChatDotRound, User, Clock, Bell, ChatLineRound, Money, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'

const loading = ref(false)
const period = ref('DAILY')
const statsData = ref(null)
const authStore = useAuthStore()

const periodLabel = computed(() => {
  const map = { DAILY: '今日', WEEKLY: '本周', MONTHLY: '本月', QUARTERLY: '本季', YEARLY: '本年' }
  return map[period.value] || ''
})

const aiRatio = computed(() => {
  const o = statsData.value?.overview
  if (!o) return 0
  const total = (o.aiReplyCount || 0) + (o.manualReplyCount || 0)
  if (total === 0) return 0
  return Math.round((o.aiReplyCount || 0) / total * 100)
})

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

const getBarHeight = (count) => {
  if (!statsData.value?.trendData?.length) return 0
  const max = Math.max(...statsData.value.trendData.map(d => d.messageCount || 0), 1)
  return Math.max((count / max) * 100, 2)
}

const getProgressColor = (val) => {
  if (val >= 80) return '#10B981'
  if (val >= 50) return '#F59E0B'
  return '#EF4444'
}

const fetchData = async () => {
  loading.value = true
  try {
    let res
    const params = { period: period.value }
    const role = authStore.role
    if (role === 'CS_STAFF') {
      params.csUserId = authStore.userId
      res = await statsApi.getPersonal(params)
    } else if (role === 'CS_LEADER') {
      res = await statsApi.getTeam(params)
    } else {
      res = await statsApi.getGlobal(params)
    }
    if (res.code === 200) statsData.value = res.data
  } catch (e) { ElMessage.error('获取统计数据失败') }
  finally { loading.value = false }
}

onMounted(() => { fetchData() })
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

.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 160px;
  padding: 0 8px;
  overflow-x: auto;
}

.trend-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 28px;
  height: 100%;
}

.trend-bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.trend-bar {
  width: 70%;
  max-width: 32px;
  background: linear-gradient(180deg, #6366F1 0%, #818CF8 100%);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  cursor: pointer;
  min-height: 2px;
}

.trend-bar:hover {
  background: linear-gradient(180deg, #4F46E5 0%, #6366F1 100%);
}

.trend-label {
  font-size: 10px;
  color: var(--gu-text-muted);
  margin-top: 4px;
  white-space: nowrap;
}

.kpi-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 8px 0;
}

.kpi-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.kpi-label {
  font-size: 13px;
  color: var(--gu-text-muted);
  width: 60px;
  flex-shrink: 0;
  font-weight: 500;
}

.kpi-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--gu-text-primary);
  width: 50px;
  text-align: right;
  flex-shrink: 0;
}

.kpi-value--large {
  font-size: 24px;
  font-weight: 700;
  color: #10B981;
  width: auto;
}

@media (max-width: 768px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr) !important; }
  .data-grid { grid-template-columns: 1fr !important; }
  .dash-header { flex-direction: column; gap: 12px; }
}
</style>
