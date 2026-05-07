<!--
  陪玩师结算管理页面，管理陪玩师收益结算、确认和申诉

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>结算管理</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterSettlementStatus" placeholder="结算状态" clearable @change="loadData" style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="待结算" value="PENDING" />
          <el-option label="结算中" value="PROCESSING" />
          <el-option label="已结算" value="COMPLETED" />
        </el-select>
        <el-select v-model="filterConfirmStatus" placeholder="确认状态" clearable @change="loadData" style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="未确认" value="UNCONFIRMED" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="有异议" value="DISPUTED" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="companionNickname" label="陪玩师" width="120" />
        <el-table-column prop="settlementPeriod" label="结算周期" min-width="200" />
        <el-table-column prop="totalOrders" label="接单数" width="80" align="center" />
        <el-table-column prop="totalRevenue" label="总收入(元)" width="110" align="right">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.totalRevenue }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="platformFee" label="平台分成(元)" width="110" align="right">
          <template #default="{ row }">
            <el-tag type="info">{{ row.platformFee }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="companionIncome" label="实得金额(元)" width="120" align="right">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.companionIncome }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deductionAmount" label="扣款(元)" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.deductionAmount > 0" style="color: #f56c6c">{{ row.deductionAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="settlementStatus" label="结算状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="settleType(row.settlementStatus)" size="small">{{ settleLabel(row.settlementStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confirmStatus" label="确认状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="confirmType(row.confirmStatus)" size="small">{{ confirmLabel(row.confirmStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button v-if="row.confirmStatus === 'UNCONFIRMED'" type="success" size="small" link @click="doConfirm(row)">确认</el-button>
            <el-button v-if="row.confirmStatus === 'UNCONFIRMED'" type="warning" size="small" link @click="openDispute(row)">申诉</el-button>
            <el-button v-if="row.confirmStatus === 'CONFIRMED' && row.settlementStatus !== 'COMPLETED'" type="primary" size="small" link @click="doSettle(row)">执行结算</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page" v-model:page-size="size" :total="total"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog title="结算申诉" v-model="disputeDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="陪玩师ID">
          <el-input v-model="disputeForm.companionId" placeholder="请输入陪玩师ID" />
        </el-form-item>
        <el-form-item label="申诉内容">
          <el-input v-model="disputeForm.disputeContent" type="textarea" :rows="4" placeholder="请描述申诉原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disputeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doDispute" :loading="saving">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { settlementApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const disputeDialogVisible = ref(false)
const filterSettlementStatus = ref('')
const filterConfirmStatus = ref('')
const tableData = ref([])

const disputeForm = reactive({
  id: null,
  companionId: '',
  disputeContent: ''
})

function settleType(s) {
  const map = { PENDING: 'warning', PROCESSING: '', COMPLETED: 'success' }
  return map[s] || 'info'
}

function settleLabel(s) {
  const map = { PENDING: '待结算', PROCESSING: '结算中', COMPLETED: '已结算' }
  return map[s] || s
}

function confirmType(s) {
  const map = { UNCONFIRMED: 'warning', CONFIRMED: 'success', DISPUTED: 'danger' }
  return map[s] || 'info'
}

function confirmLabel(s) {
  const map = { UNCONFIRMED: '未确认', CONFIRMED: '已确认', DISPUTED: '有异议' }
  return map[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterSettlementStatus.value) params.settlementStatus = filterSettlementStatus.value
    if (filterConfirmStatus.value) params.confirmStatus = filterConfirmStatus.value
    const res = await settlementApi.getPage(params)
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载结算列表失败')
  } finally {
    loading.value = false
  }
}

async function doConfirm(row) {
  try {
    await settlementApi.confirm(row.id, { companionId: row.companionId || '1' })
    ElMessage.success('确认成功')
    loadData()
  } catch (e) {
    ElMessage.error('确认失败')
  }
}

function openDispute(row) {
  disputeForm.id = row.id
  disputeForm.companionId = row.companionId || ''
  disputeForm.disputeContent = ''
  disputeDialogVisible.value = true
}

async function doDispute() {
  saving.value = true
  try {
    await settlementApi.dispute(disputeForm.id, disputeForm)
    ElMessage.success('申诉已提交')
    disputeDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('申诉失败')
  } finally {
    saving.value = false
  }
}

async function doSettle(row) {
  try {
    await settlementApi.settle(row.id)
    ElMessage.success('结算完成')
    loadData()
  } catch (e) {
    ElMessage.error('结算失败')
  }
}

onMounted(() => loadData())
</script>
