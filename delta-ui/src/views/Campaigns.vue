<!--
  营销活动管理页面，管理试用推广、裂变拉新、节日营销等活动

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>营销活动</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>新建活动
          </el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterCampaignType" placeholder="活动类型" clearable @change="loadData" style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="试用推广" value="TRIAL" />
          <el-option label="裂变拉新" value="REFERRAL" />
          <el-option label="节日营销" value="HOLIDAY" />
          <el-option label="复购唤醒" value="RECALL" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="活动状态" clearable @change="loadData" style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="进行中" value="ACTIVE" />
          <el-option label="已暂停" value="PAUSED" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="campaignName" label="活动名称" min-width="160" />
        <el-table-column prop="clubName" label="俱乐部" width="140" />
        <el-table-column prop="campaignType" label="活动类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ campaignTypeLabel(row.campaignType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetNewUsers" label="目标/实际拉新" width="130" align="center">
          <template #default="{ row }">{{ row.actualNewUsers || 0 }} / {{ row.targetNewUsers || 0 }}</template>
        </el-table-column>
        <el-table-column prop="budget" label="预算(元)" width="100" align="right">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.budget }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startAt" label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column prop="endAt" label="结束时间" width="170">
          <template #default="{ row }">{{ formatTime(row.endAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'DRAFT'" type="success" size="small" link @click="start(row)">启动</el-button>
            <el-button v-if="row.status === 'ACTIVE'" type="warning" size="small" link @click="pause(row)">暂停</el-button>
            <el-button v-if="row.status === 'ACTIVE' || row.status === 'PAUSED'" type="danger" size="small" link @click="end(row)">结束</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog :title="form.id ? '编辑活动' : '新建活动'" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form :model="form" label-width="120px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.campaignName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="俱乐部ID">
          <el-input v-model="form.clubConfigId" placeholder="请输入俱乐部ID" />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="form.campaignType" style="width: 100%">
            <el-option label="试用推广" value="TRIAL" />
            <el-option label="裂变拉新" value="REFERRAL" />
            <el-option label="节日营销" value="HOLIDAY" />
            <el-option label="复购唤醒" value="RECALL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="form.dateRange" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目标拉新人数">
          <el-input-number v-model="form.targetNewUsers" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动预算(元)">
          <el-input-number v-model="form.budget" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="奖励方案">
          <el-input v-model="form.rewardRules" type="textarea" :rows="3" placeholder="描述奖励规则" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { campaignApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const filterCampaignType = ref('')
const filterStatus = ref('')
const tableData = ref([])

const defaultForm = {
  id: null,
  clubConfigId: '',
  campaignName: '',
  campaignType: 'TRIAL',
  description: '',
  dateRange: null,
  targetNewUsers: 0,
  budget: 0,
  rewardRules: ''
}

const form = reactive({ ...defaultForm })

function campaignTypeLabel(type) {
  const map = { TRIAL: '试用推广', REFERRAL: '裂变拉新', HOLIDAY: '节日营销', RECALL: '复购唤醒', OTHER: '其他' }
  return map[type] || type
}

function statusType(status) {
  const map = { DRAFT: 'info', ACTIVE: 'success', PAUSED: 'warning', ENDED: 'danger', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { DRAFT: '草稿', ACTIVE: '进行中', PAUSED: '已暂停', ENDED: '已结束', CANCELLED: '已取消' }
  return map[status] || status
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

function resetForm() { Object.assign(form, defaultForm) }

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterCampaignType.value) params.campaignType = filterCampaignType.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await campaignApi.getPage(params)
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

function handleCreate() { resetForm(); dialogVisible.value = true }

function handleEdit(row) {
  Object.assign(form, row)
  if (row.startAt && row.endAt) form.dateRange = [row.startAt, row.endAt]
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？', '确认删除', { type: 'warning' })
    await campaignApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form }
    if (payload.dateRange?.length === 2) {
      payload.startAt = payload.dateRange[0]
      payload.endAt = payload.dateRange[1]
    }
    delete payload.dateRange
    if (payload.id) {
      await campaignApi.update(payload)
    } else {
      await campaignApi.create(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function start(row) { try { await campaignApi.start(row.id); ElMessage.success('活动已启动'); loadData() } catch (e) { ElMessage.error('启动失败') } }
async function pause(row) { try { await campaignApi.pause(row.id); ElMessage.success('活动已暂停'); loadData() } catch (e) { ElMessage.error('暂停失败') } }
async function end(row) { try { await campaignApi.end(row.id); ElMessage.success('活动已结束'); loadData() } catch (e) { ElMessage.error('结束失败') } }

onMounted(() => loadData())
</script>
