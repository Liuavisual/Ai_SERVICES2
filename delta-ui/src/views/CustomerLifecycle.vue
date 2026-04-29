<!--
  客户生命周期管理页面

  功能：流失风险客户/已流失客户列表查看、生命周期阶段展示、标签更新
  阶段标识：NEW/ACTIVE/LOYAL/AT_RISK/CHURNED

  @author 刘建国
-->
<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card stat-warning" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ atRiskCount }}</div>
          <div class="stat-label">流失风险客户</div>
        </div>
        <el-icon :size="32" class="stat-icon"><Warning /></el-icon>
      </el-card>
      <el-card class="stat-card stat-danger" shadow="hover">
        <div class="stat-content">
          <div class="stat-value">{{ churnedCount }}</div>
          <div class="stat-label">已流失客户</div>
        </div>
        <el-icon :size="32" class="stat-icon"><CircleClose /></el-icon>
      </el-card>
    </div>

    <!-- 客户列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" class="header-tabs">
            <el-tab-pane label="流失风险客户" name="atRisk" />
            <el-tab-pane label="已流失客户" name="churned" />
          </el-tabs>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleUpdateTags" :loading="updateLoading">
              <el-icon><Refresh /></el-icon>
              更新标签
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="rowNum" label="编号" width="70" />
        <el-table-column label="客户昵称" width="140">
          <template #default="{ row }">
            <span>{{ row.nickname || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="平台" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getPlatformType(row.platform)">{{ getPlatformText(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后消息时间" width="170">
          <template #default="{ row }">
            <span v-if="row.lastActiveAt">{{ formatDateTime(row.lastActiveAt) }}</span>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="消息总数" width="100">
          <template #default="{ row }">
            <span>{{ row.messageCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生命周期阶段" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="getStageType(row.lifecycleStage)" effect="light">
              {{ getStageText(row.lifecycleStage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 客户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="客户信息" width="520px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detailInfo">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="客户昵称">{{ detailInfo.nickname || '—' }}</el-descriptions-item>
            <el-descriptions-item label="平台">
              <el-tag :type="getPlatformType(detailInfo.platform)" size="small">{{ getPlatformText(detailInfo.platform) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="生命周期阶段">
              <el-tag :type="getStageType(detailInfo.lifecycleStage)" size="small">{{ getStageText(detailInfo.lifecycleStage) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="消息数">{{ detailInfo.messageCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="最后活跃">{{ detailInfo.lastActiveAt || '—' }}</el-descriptions-item>
            <el-descriptions-item label="AI状态">
              <el-tag :type="detailInfo.aiEnabled ? 'success' : 'info'" size="small">
                {{ detailInfo.aiEnabled ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="分配客服">{{ detailInfo.assignedCsUserName || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detailInfo.createdAt || '—' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <el-empty v-else description="未找到客户信息" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, CircleClose, Refresh } from '@element-plus/icons-vue'
import { lifecycleApi, customerApi } from '@/api'
import type { Result, CustomerVO } from '@/types'

const loading = ref<boolean>(false)
const updateLoading = ref<boolean>(false)
const detailLoading = ref<boolean>(false)
const activeTab = ref<'atRisk' | 'churned'>('atRisk')
const atRiskData = ref<CustomerVO[]>([])
const churnedData = ref<CustomerVO[]>([])
const detailVisible = ref<boolean>(false)
const detailInfo = ref<CustomerVO | null>(null)

const atRiskCount = computed<number>(() => atRiskData.value.length)
const churnedCount = computed<number>(() => churnedData.value.length)

const tableData = computed<CustomerVO[]>(() => {
  const data = activeTab.value === 'atRisk' ? atRiskData.value : churnedData.value
  return data.map((item, index) => ({ ...item, rowNum: index + 1 }))
})

const getPlatformType = (platform: string): string => ({ wechat: 'primary', kook: 'success', yy: 'warning' }[platform] || 'info')

const getPlatformText = (platform: string): string => ({ wechat: '微信', kook: 'KOOK', yy: 'YY' }[platform] || platform || '—')

const getStageType = (stage: string): string => ({
  NEW: 'info',
  ACTIVE: 'primary',
  LOYAL: 'success',
  AT_RISK: 'warning',
  CHURNED: 'danger'
}[stage] || 'info')

const getStageText = (stage: string): string => ({
  NEW: '新客户',
  ACTIVE: '活跃',
  LOYAL: '忠实',
  AT_RISK: '流失风险',
  CHURNED: '已流失'
}[stage] || stage || '—')

function formatDateTime(val: string): string {
  if (!val) return '—'
  const d = new Date(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchAtRiskData(): Promise<void> {
  try {
    const res: Result<CustomerVO[]> = await lifecycleApi.getAtRisk()
    if (res.code === 200) {
      atRiskData.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('获取流失风险客户失败')
    console.error('获取流失风险客户失败', e)
  }
}

async function fetchChurnedData(): Promise<void> {
  try {
    const res: Result<CustomerVO[]> = await lifecycleApi.getChurned()
    if (res.code === 200) {
      churnedData.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('获取已流失客户失败')
    console.error('获取已流失客户失败', e)
  }
}

async function loadAllData(): Promise<void> {
  loading.value = true
  try {
    await Promise.all([fetchAtRiskData(), fetchChurnedData()])
  } finally {
    loading.value = false
  }
}

async function handleUpdateTags(): Promise<void> {
  updateLoading.value = true
  try {
    const res: Result<null> = await lifecycleApi.updateTags()
    if (res.code === 200) {
      ElMessage.success('标签更新已触发')
      await loadAllData()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('更新标签失败')
    console.error('更新标签失败', e)
  } finally {
    updateLoading.value = false
  }
}

async function handleViewDetail(row: CustomerVO): Promise<void> {
  detailVisible.value = true
  detailLoading.value = true
  detailInfo.value = null
  try {
    const res: Result<CustomerVO> = await customerApi.getById(row.id)
    if (res.code === 200) {
      detailInfo.value = res.data
    }
  } catch (e) {
    ElMessage.error('获取客户信息失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

watch(activeTab, (newTab: 'atRisk' | 'churned') => {
  if (newTab === 'atRisk' && atRiskData.value.length === 0) {
    fetchAtRiskData()
  } else if (newTab === 'churned' && churnedData.value.length === 0) {
    fetchChurnedData()
  }
})

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.stats-row { display: flex; gap: 16px; }

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-radius: var(--gu-radius-lg, 8px);
}

.stat-content { display: flex; flex-direction: column; gap: 4px; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  font-family: var(--gu-font-heading, inherit);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--gu-text-secondary, #909399);
  font-weight: 500;
}

.stat-icon { opacity: 0.6; }

.stat-warning .stat-value { color: var(--gu-warning, #E6A23C); }
.stat-warning .stat-icon { color: var(--gu-warning, #E6A23C); }
.stat-danger .stat-value { color: var(--gu-danger, #F56C6C); }
.stat-danger .stat-icon { color: var(--gu-danger, #F56C6C); }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-tabs { flex: 1; }
.header-actions { display: flex; align-items: center; gap: 8px; }

.muted-text { color: var(--gu-text-muted, #C0C4CC); font-size: 12px; }

@media (max-width: 768px) {
  .stats-row { flex-direction: column; }
  .stat-card { padding: 14px; }
  .stat-value { font-size: 22px; }
}
</style>
