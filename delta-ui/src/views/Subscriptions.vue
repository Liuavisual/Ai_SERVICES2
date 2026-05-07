<!--
  俱乐部订阅管理页面，管理系统中的订阅记录和试用管理

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订阅管理</span>
          <el-space>
            <el-button type="success" @click="handleTrial">
              <el-icon><Present /></el-icon>开通试用
            </el-button>
            <el-button type="primary" @click="handleSubscribe">
              <el-icon><Plus /></el-icon>开通订阅
            </el-button>
          </el-space>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterStatus" placeholder="订阅状态" clearable @change="loadData" style="width: 160px">
          <el-option label="全部" value="" />
          <el-option label="试用中" value="TRIAL" />
          <el-option label="生效中" value="ACTIVE" />
          <el-option label="已过期" value="EXPIRED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="clubName" label="俱乐部" min-width="140" />
        <el-table-column prop="planName" label="方案" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startAt" label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column prop="expireAt" label="到期时间" width="170">
          <template #default="{ row }">{{ formatTime(row.expireAt) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="实付金额" width="100" align="right">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.paidAmount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="autoRenew" label="自动续费" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.autoRenew ? 'success' : 'info'">{{ row.autoRenew ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ACTIVE'" type="primary" size="small" link @click="handleRenew(row)">续费</el-button>
            <el-button v-if="row.status === 'ACTIVE'" type="danger" size="small" link @click="handleCancel(row)">取消</el-button>
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

    <el-dialog title="开通订阅" v-model="subDialogVisible" width="500px">
      <el-form label-width="120px">
        <el-form-item label="选择俱乐部" required>
          <el-input v-model="subscribeForm.clubConfigId" placeholder="请输入俱乐部ID" />
        </el-form-item>
        <el-form-item label="选择方案" required>
          <el-input v-model="subscribeForm.planId" placeholder="请输入方案ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doSubscribe" :loading="saving">确认订阅</el-button>
      </template>
    </el-dialog>

    <el-dialog title="开通试用" v-model="trialDialogVisible" width="500px">
      <el-form label-width="120px">
        <el-form-item label="选择俱乐部" required>
          <el-input v-model="trialClubConfigId" placeholder="请输入俱乐部ID" />
        </el-form-item>
        <el-alert title="试用说明" type="info" :closable="false" style="margin-top: 10px">
          试用期15天，自动开通基础版功能，到期后可付费升级。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="trialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doTrial" :loading="saving">确认开通</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { subscriptionApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const filterStatus = ref('')
const tableData = ref([])
const subDialogVisible = ref(false)
const trialDialogVisible = ref(false)

const subscribeForm = reactive({ clubConfigId: '', planId: '' })
const trialClubConfigId = ref('')

function statusType(status) {
  const map = { TRIAL: 'warning', ACTIVE: 'success', EXPIRED: 'danger', CANCELLED: 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { TRIAL: '试用中', ACTIVE: '生效中', EXPIRED: '已过期', CANCELLED: '已取消' }
  return map[status] || status
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await subscriptionApi.getPage(params)
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载订阅列表失败')
  } finally {
    loading.value = false
  }
}

function handleSubscribe() { subDialogVisible.value = true }
function handleTrial() { trialDialogVisible.value = true }

async function doSubscribe() {
  saving.value = true
  try {
    await subscriptionApi.subscribe(subscribeForm)
    ElMessage.success('订阅开通成功')
    subDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('开通失败: ' + (e.message || ''))
  } finally {
    saving.value = false
  }
}

async function doTrial() {
  saving.value = true
  try {
    await subscriptionApi.trial({ clubConfigId: trialClubConfigId.value })
    ElMessage.success('试用开通成功')
    trialDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('开通失败: ' + (e.message || ''))
  } finally {
    saving.value = false
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消该订阅吗？', '确认取消', { type: 'warning' })
    await subscriptionApi.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleRenew(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入续费月数', '续费', {
      inputValue: '1',
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入有效月数'
    })
    await subscriptionApi.renew(row.id, { months: parseInt(value) })
    ElMessage.success('续费成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('续费失败')
  }
}

onMounted(() => loadData())
</script>
