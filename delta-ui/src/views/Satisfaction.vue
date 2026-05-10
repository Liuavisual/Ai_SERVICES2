<!--
  客户满意度评价管理页面

  功能：统计卡片、筛选查询、分页列表、评分星级显示、标签展示
  权限：SYS_ADMIN、CS_LEADER

  @author 刘建国
-->
<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon rating-icon">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ avgRating }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </div>
      </el-card>

    <el-card class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span>陪玩师综合评分看板</span>
          <el-button link type="primary" size="small" @click="toggleDashboard">{{ dashboardExpanded ? '收起' : '展开' }}</el-button>
        </div>
      </template>
      <div v-show="dashboardExpanded">
        <el-table :data="ratingDashboard" stripe v-loading="dashboardLoading" max-height="400">
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="companionNickname" label="陪玩师" width="120" />
          <el-table-column label="综合评分" width="160">
            <template #default="{ row }">
              <div style="display:flex;align-items:center;gap:8px">
                <span style="font-size:20px;font-weight:700;color:#f56c6c">{{ row.avgRating != null ? Number(row.avgRating).toFixed(1) : '0.0' }}</span>
                <el-rate v-model="row.avgRatingNum" disabled :max="5" allow-half show-score size="small" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评分分布" min-width="280">
            <template #default="{ row }">
              <div class="rating-distribution">
                <div class="dist-item" v-for="star in 5" :key="star">
                  <span class="dist-label">{{ star }}星</span>
                  <el-progress
                    :percentage="row.totalReviews > 0 ? Math.round((row['rating' + star + 'Count'] || 0) / row.totalReviews * 100) : 0"
                    :stroke-width="8"
                    :show-text="false"
                    :color="starColor(star)"
                    style="flex:1;margin:0 4px"
                  />
                  <span class="dist-count">{{ row['rating' + star + 'Count'] || 0 }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalReviews" label="评价数" width="70" align="center" />
          <el-table-column label="最近评价" width="170">
            <template #default="{ row }">
              <span v-if="row.lastReviewAt">{{ formatSatisfactionDate(row.lastReviewAt) }}</span>
              <span v-else style="color:var(--gu-text-muted)">暂无</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon count-icon">
            <el-icon :size="24"><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalCount }}</div>
            <div class="stat-label">评价总数</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="陪玩师">
          <el-select
            v-model="queryParams.companionId"
            placeholder="全部陪玩师"
            clearable
            style="width: 160px"
            :teleported="false"
          >
            <el-option
              v-for="c in companionList"
              :key="c.id"
              :label="c.nickname"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分范围">
          <el-select v-model="queryParams.minRating" placeholder="最低" clearable style="width: 90px" :teleported="false">
            <el-option label="1分" :value="1" />
            <el-option label="2分" :value="2" />
            <el-option label="3分" :value="3" />
            <el-option label="4分" :value="4" />
            <el-option label="5分" :value="5" />
          </el-select>
          <span style="margin: 0 4px; color: var(--gu-text-muted)">-</span>
          <el-select v-model="queryParams.maxRating" placeholder="最高" clearable style="width: 90px" :teleported="false">
            <el-option label="1分" :value="1" />
            <el-option label="2分" :value="2" />
            <el-option label="3分" :value="3" />
            <el-option label="4分" :value="4" />
            <el-option label="5分" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>满意度评价</span>
          <el-tag type="info" size="small" effect="plain">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="rowNum" label="编号" width="70" />
        <el-table-column label="客户昵称" width="120">
          <template #default="{ row }">
            <span>{{ row.userNickname || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="陪玩师" width="120">
          <template #default="{ row }">
            <span>{{ row.companionName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="180">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled :colors="rateColors" show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="feedback" label="反馈内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <template v-if="row.tags">
              <el-tag
                v-for="tag in (row.tags ? row.tags.split(',').filter(Boolean) : [])"
                :key="tag"
                size="small"
                type="primary"
                effect="plain"
                style="margin: 2px 4px 2px 0"
              >
                {{ tag.trim() }}
              </el-tag>
            </template>
            <span v-else style="color: var(--gu-text-muted)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="服务类型" width="100">
          <template #default="{ row }">
            <span>{{ row.serviceType || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="匿名" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isAnonymous === 1 ? 'warning' : 'info'" effect="light">
              {{ row.isAnonymous === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { queryParams.pageNum = 1; fetchData() }"
        @current-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, Tickets } from '@element-plus/icons-vue'
import { satisfactionApi, companionApi } from '@/api'
import type { SatisfactionVO, CompanionVO } from '@/types'

/** 加载状态 */
const loading = ref<boolean>(false)
/** 表格数据 */
const tableData = ref<SatisfactionVO[]>([])
/** 数据总数 */
const total = ref<number>(0)
/** 平均评分 */
const avgRating = ref<string>('0.0')
/** 陪玩师列表（下拉选择用） */
const companionList = ref<CompanionVO[]>([])

/** 星级颜色配置 */
const rateColors: string[] = ['#99A9BF', '#F7BA2A', '#FF9900']

/** 查询参数 */
const queryParams = reactive<{
  pageNum: number
  pageSize: number
  companionId: string
  minRating: number | null
  maxRating: number | null
}>({
  pageNum: 1,
  pageSize: 10,
  companionId: '',
  minRating: null,
  maxRating: null
})

/**
 * 获取满意度评价分页数据
 */
async function fetchData(): Promise<void> {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.pageNum,
      size: queryParams.pageSize
    }
    if (queryParams.companionId) {
      params.companionId = queryParams.companionId
    }
    if (queryParams.minRating != null) {
      params.minRating = queryParams.minRating
    }
    if (queryParams.maxRating != null) {
      params.maxRating = queryParams.maxRating
    }

    const res = await satisfactionApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      total.value = res.data?.total || 0
      // 计算当前页的平均评分
      if (tableData.value.length > 0) {
        const sum = tableData.value.reduce((acc: number, item: SatisfactionVO) => acc + (item.rating || 0), 0)
        avgRating.value = (sum / tableData.value.length).toFixed(1)
      } else {
        avgRating.value = '0.0'
      }
    }
  } catch (e) {
    ElMessage.error('查询失败')
    console.error('查询满意度评价失败', e)
  } finally {
    loading.value = false
  }
}

/**
 * 加载陪玩师列表（用于筛选下拉）
 */
async function loadCompanions(): Promise<void> {
  try {
    const res = await companionApi.getAll()
    if (res.code === 200) {
      companionList.value = res.data || []
    }
  } catch (e) {
    console.error('加载陪玩师列表失败', e)
  }
}

/** 查询（重置页码） */
function handleQuery(): void {
  queryParams.pageNum = 1
  fetchData()
}

/** 重置筛选条件 */
function handleReset(): void {
  queryParams.pageNum = 1
  queryParams.companionId = ''
  queryParams.minRating = null
  queryParams.maxRating = null
  fetchData()
}

/** 评价总数（计算属性，直接用total） */
const totalCount = computed<number>(() => total.value)

const dashboardExpanded = ref<boolean>(false)
const dashboardLoading = ref<boolean>(false)
const ratingDashboard = ref<any[]>([])

function toggleDashboard(): void {
  dashboardExpanded.value = !dashboardExpanded.value
  if (dashboardExpanded.value && ratingDashboard.value.length === 0) {
    loadRatingDashboard()
  }
}

function starColor(star: number): string {
  const colors = ['#F56C6C', '#E6A23C', '#F7BA2A', '#67C23A', '#409EFF']
  return colors[star - 1] || '#909399'
}

function formatSatisfactionDate(val: string): string {
  if (!val) return '-'
  const d = new Date(val)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadRatingDashboard(): Promise<void> {
  dashboardLoading.value = true
  try {
    const res = await companionApi.getAllRatings()
    if (res.code === 200) {
      ratingDashboard.value = (res.data || []).map((item: any) => ({
        ...item,
        avgRatingNum: Number(item.avgRating || 0)
      }))
    }
  } catch (e) {
    console.error('加载评分看板失败', e)
  } finally {
    dashboardLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  loadCompanions()
})
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }

.stats-row {
  display: flex;
  gap: 16px;
}

.stat-card {
  flex: 1;
  min-width: 0;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rating-icon {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
}

.count-icon {
  background: rgba(22, 119, 255, 0.1);
  color: #1677ff;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--gu-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--gu-text-muted);
  margin-top: 2px;
}

.filter-card { flex-shrink: 0; }
.filter-form { display: flex; flex-wrap: wrap; gap: 0; }
.table-card { flex: 1; min-height: 400px; }
.card-header {
  display: flex; align-items: center;
  justify-content: space-between;
  font-weight: 600; font-size: 15px;
}

@media (max-width: 768px) {
  .stats-row { flex-direction: column; }
}

.dashboard-card {
  flex-shrink: 0;
}

.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dist-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.dist-label {
  width: 24px;
  color: var(--gu-text-muted);
  text-align: right;
}

.dist-count {
  width: 24px;
  color: var(--gu-text-secondary);
  text-align: left;
}
</style>
