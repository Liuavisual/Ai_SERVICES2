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
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
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
</style>
