<!--
  裂变推荐管理页面，管理"老带新"推荐记录和奖励发放

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>裂变推荐</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-select v-model="filterConversionStatus" placeholder="转化状态" clearable @change="loadData" style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="待注册" value="PENDING" />
          <el-option label="已注册" value="REGISTERED" />
          <el-option label="试用中" value="TRIALING" />
          <el-option label="已付费" value="SUBSCRIBED" />
        </el-select>
        <el-select v-model="filterRewardStatus" placeholder="奖励状态" clearable @change="loadData" style="width: 140px; margin-left: 10px">
          <el-option label="全部" value="" />
          <el-option label="待发放" value="PENDING" />
          <el-option label="已发放" value="ISSUED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="campaignName" label="所属活动" min-width="140" />
        <el-table-column prop="referrerUserName" label="推荐人" width="120" />
        <el-table-column prop="refereeUserName" label="被推荐人" width="120" />
        <el-table-column prop="referralCode" label="推荐码" width="120" />
        <el-table-column prop="referralTime" label="推荐时间" width="170">
          <template #default="{ row }">{{ formatTime(row.referralTime) }}</template>
        </el-table-column>
        <el-table-column prop="conversionStatus" label="转化状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="conversionStatusType(row.conversionStatus)" size="small">{{ conversionStatusLabel(row.conversionStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rewardType" label="奖励类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ rewardTypeLabel(row.rewardType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rewardAmount" label="奖励金额(元)" width="110" align="right" />
        <el-table-column prop="rewardStatus" label="奖励状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="rewardStatusType(row.rewardStatus)" size="small">{{ rewardStatusLabel(row.rewardStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button v-if="row.rewardStatus === 'PENDING'" type="success" size="small" link @click="issue(row)">发放奖励</el-button>
            <el-button v-if="row.rewardStatus === 'PENDING'" type="danger" size="small" link @click="cancel(row)">取消奖励</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { referralApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const filterConversionStatus = ref('')
const filterRewardStatus = ref('')
const tableData = ref([])

function conversionStatusType(status) {
  const map = { PENDING: 'info', REGISTERED: 'warning', TRIALING: '', SUBSCRIBED: 'success' }
  return map[status] || 'info'
}

function conversionStatusLabel(status) {
  const map = { PENDING: '待注册', REGISTERED: '已注册', TRIALING: '试用中', SUBSCRIBED: '已付费' }
  return map[status] || status
}

function rewardTypeLabel(type) {
  const map = { MONTH_FREE: '赠月会员', CASH: '现金奖励', POINTS: '积分奖励' }
  return map[type] || type
}

function rewardStatusType(status) {
  const map = { PENDING: 'warning', ISSUED: 'success', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function rewardStatusLabel(status) {
  const map = { PENDING: '待发放', ISSUED: '已发放', CANCELLED: '已取消' }
  return map[status] || status
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterConversionStatus.value) params.conversionStatus = filterConversionStatus.value
    if (filterRewardStatus.value) params.rewardStatus = filterRewardStatus.value
    const res = await referralApi.getPage(params)
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载推荐列表失败')
  } finally {
    loading.value = false
  }
}

async function issue(row) { try { await referralApi.issueReward(row.id); ElMessage.success('奖励已发放'); loadData() } catch (e) { ElMessage.error('发放失败') } }
async function cancel(row) { try { await referralApi.cancelReward(row.id); ElMessage.success('奖励已取消'); loadData() } catch (e) { ElMessage.error('取消失败') } }

onMounted(() => loadData())
</script>
