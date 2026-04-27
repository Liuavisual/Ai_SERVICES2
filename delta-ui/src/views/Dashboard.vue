<!--
  数据看板页面，展示运营统计和待办概览

  @author delta
-->
<template>
  <div class="dash-page">
    <div class="dash-header">
      <h2 class="dash-title">数据总览</h2>
      <el-select v-model="period" style="width:130px" :teleported="false" @change="fetchData">
        <el-option label="今日" value="DAILY" />
        <el-option label="本周" value="WEEKLY" />
        <el-option label="本月" value="MONTHLY" />
        <el-option label="本季" value="QUARTERLY" />
        <el-option label="本年" value="YEARLY" />
      </el-select>
    </div>

    <div v-loading="loading">
      <div class="stat-grid">
        <div v-for="card in overviewCards" :key="card.title" class="stat-card">
          <div class="card-top">
            <span class="card-label">{{ card.title }}</span>
            <el-icon :size="20" :style="{ color: card.color }"><component :is="card.icon" /></el-icon>
          </div>
          <div class="card-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="card-line"></div>
        </div>
      </div>

      <div class="data-section" v-if="statsData?.trendData?.length > 0">
        <el-card>
          <template #header><span>消息趋势</span></template>
          <el-table :data="statsData.trendData" stripe size="small">
            <el-table-column prop="date" label="日期" width="140" />
            <el-table-column prop="messageCount" label="消息数" width="100" />
          </el-table>
        </el-card>
      </div>

      <div class="data-section" v-if="statsData?.csUserData?.length > 0">
        <el-card>
          <template #header><span>客服数据</span></template>
          <el-table :data="statsData.csUserData" stripe size="small">
            <el-table-column prop="csUserName" label="客服" width="120" />
            <el-table-column prop="messageCount" label="消息数" width="90" />
            <el-table-column prop="customerCount" label="服务客户" width="100" />
            <el-table-column prop="avgResponseTime" label="响应(秒)" width="110" />
            <el-table-column prop="resolutionRate" label="解决率" width="90">
              <template #default="{ row }">{{ row.resolutionRate }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
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
    { title: '消息总数', value: o.totalMessages || 0, icon: ChatDotRound, color: '#8B3A3A' },
    { title: '服务客户', value: o.totalCustomers || 0, icon: User, color: '#5a8a5a' }
  ]
  if (o.avgResponseTime !== undefined) cards.push({ title: '平均响应', value: o.avgResponseTime + 's', icon: Clock, color: '#b8860b' })
  if (o.pendingCount !== undefined) cards.push({ title: '待办事项', value: o.pendingCount, icon: Bell, color: '#c97630' })
  if (o.aiReplyCount !== undefined) cards.push({ title: 'AI回复', value: o.aiReplyCount, icon: ChatLineRound, color: '#6b7c85' })
  if (o.manualReplyCount !== undefined) cards.push({ title: '人工回复', value: o.manualReplyCount, icon: Money, color: '#8b6914' })
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
.dash-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.dash-title { font-size: 17px; font-weight: 600; color: var(--gu-text-primary); letter-spacing: 2px; }

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--gu-bg-card);
  border: 1px solid var(--gu-border);
  border-radius: var(--gu-radius-lg);
  padding: 18px 16px;
  position: relative;
  transition: all 0.2s ease;
}
.stat-card:hover { border-color: var(--gu-gold); box-shadow: var(--gu-shadow-lg); }

.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.card-label { font-size: 13px; color: var(--gu-text-muted); letter-spacing: 1px; }

.card-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
.card-line {
  position: absolute;
  bottom: 0; left: 16px; right: 16px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--gu-accent), transparent);
  opacity: 0.15;
}

.data-section { margin-top: 16px; }
</style>
