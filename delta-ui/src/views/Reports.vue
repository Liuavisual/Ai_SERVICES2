<!--
  营收报表页面，展示数据分析/BI日报和周报

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>营收报表</span>
          <el-button type="primary" @click="handleGenerate">
            <el-icon><RefreshRight /></el-icon>生成日报
          </el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="loadData"
          style="width: 280px"
        />
        <el-select v-model="filterGameType" placeholder="游戏类型" clearable @change="loadData" style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="三角洲行动" value="三角洲行动" />
          <el-option label="王者荣耀" value="王者荣耀" />
          <el-option label="和平精英" value="和平精英" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="clubName" label="俱乐部" min-width="140" />
        <el-table-column prop="reportDate" label="日期" width="120" />
        <el-table-column prop="gameType" label="游戏类型" width="110">
          <template #default="{ row }">{{ row.gameType || '全部' }}</template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="订单总数" width="90" align="center" />
        <el-table-column prop="completedOrders" label="完成数" width="80" align="center" />
        <el-table-column prop="refundOrders" label="退款数" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.refundOrders > 0 ? '#f56c6c' : '' }">{{ row.refundOrders }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalRevenue" label="总收入(元)" width="120" align="right">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.totalRevenue }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="platformIncome" label="平台收入(元)" width="120" align="right">
          <template #default="{ row }">
            <el-tag type="success">{{ row.platformIncome }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiHandleRate" label="AI处理率" width="100" align="center">
          <template #default="{ row }">{{ row.aiHandleRate }}%</template>
        </el-table-column>
        <el-table-column prop="avgSatisfaction" label="客户满意度" width="110" align="center">
          <template #default="{ row }">
            <el-rate :model-value="Number(row.avgSatisfaction || 0)" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="newCustomers" label="新客户" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.newCustomers }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repeatCustomers" label="复购客户" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.repeatCustomers }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="activeCompanions" label="活跃陪玩师" width="100" align="center" />
      </el-table>

      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog title="生成日报" v-model="dialogVisible" width="400px">
      <el-form label-width="100px">
        <el-form-item label="俱乐部ID" required>
          <el-input v-model="generateForm.clubConfigId" placeholder="请输入俱乐部ID" />
        </el-form-item>
        <el-form-item label="报表日期" required>
          <el-date-picker v-model="generateForm.reportDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doGenerate" :loading="saving">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dateRange = ref(null)
const filterGameType = ref('')
const tableData = ref([])

const generateForm = reactive({
  clubConfigId: '',
  reportDate: new Date().toISOString().substring(0, 10)
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (filterGameType.value) params.gameType = filterGameType.value
    const res = await reportApi.getPage(params)
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载报表失败')
  } finally {
    loading.value = false
  }
}

function handleGenerate() { dialogVisible.value = true }

async function doGenerate() {
  saving.value = true
  try {
    await reportApi.generate(generateForm)
    ElMessage.success('日报生成成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
