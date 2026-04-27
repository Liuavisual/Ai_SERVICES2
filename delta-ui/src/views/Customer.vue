<template>
  <div class="customer-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="平台">
          <el-select v-model="queryForm.platform" placeholder="请选择平台" clearable style="width: 180px" :teleported="false">
            <el-option label="微信" value="wechat" />
            <el-option label="KOOK" value="kook" />
            <el-option label="YY" value="yy" />
          </el-select>
        </el-form-item>
        <el-form-item label="AI状态" v-if="isAdminOrLeader">
          <el-select v-model="queryForm.aiEnabled" placeholder="请选择AI状态" clearable style="width: 180px" :teleported="false">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配客服" v-if="isAdminOrLeader">
          <el-select v-model="queryForm.csUserId" placeholder="请选择客服" clearable filterable style="width: 180px" :teleported="false">
            <el-option v-for="cs in csList" :key="cs.id" :label="cs.realName" :value="cs.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="queryForm.keyword" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="平台" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlatformTagType(row.platform)">{{ getPlatformLabel(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.aiEnabled ? 'success' : 'info'">
              {{ row.aiEnabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分配客服" width="120">
          <template #default="{ row }">
            {{ row.assignedCsUserName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="messageCount" label="消息数" width="100" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isAdminOrLeader ? '200' : '100'" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
            <template v-if="isAdminOrLeader">
              <el-button link type="warning" size="small" @click="handleAssign(row)">分配</el-button>
              <el-button link :type="row.aiEnabled ? 'warning' : 'success'" size="small" @click="handleToggleAi(row)">
                {{ row.aiEnabled ? '禁用AI' : '启用AI' }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="客户详情" width="600px">
      <div v-if="currentCustomer" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ currentCustomer.id }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentCustomer.nickname }}</el-descriptions-item>
          <el-descriptions-item label="平台">{{ getPlatformLabel(currentCustomer.platform) }}</el-descriptions-item>
          <el-descriptions-item label="AI状态">
            <el-tag :type="currentCustomer.aiEnabled ? 'success' : 'info'">
              {{ currentCustomer.aiEnabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分配客服">{{ currentCustomer.assignedCsUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="消息数">{{ currentCustomer.messageCount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentCustomer.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配客服" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="选择客服">
          <el-select
            id="customer-assignCsUserId"
            v-model="assignForm.csUserId"
            placeholder="请选择客服"
            filterable
            style="width: 100%"
            :teleported="false"
            name="csUserId"
          >
            <el-option v-for="cs in csList" :key="cs.id" :label="cs.realName" :value="cs.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配方式">
          <el-select
            id="customer-assignType"
            v-model="assignForm.assignType"
            placeholder="请选择分配方式"
            style="width: 100%"
            :teleported="false"
            name="assignType"
          >
            <el-option label="手动分配" value="MANUAL" />
            <el-option label="系统分配" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            id="customer-remark"
            v-model="assignForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            name="remark"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { customerApi, sysUserApi } from '@/api'

let userInfo = {}
try {
  userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
} catch (e) {}
const isAdminOrLeader = userInfo.role === 'SYS_ADMIN' || userInfo.role === 'CS_LEADER'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const csList = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  platform: null,
  aiEnabled: null,
  csUserId: null,
  keyword: ''
})

const detailDialogVisible = ref(false)
const currentCustomer = ref(null)

const assignDialogVisible = ref(false)
const assignForm = reactive({
  csUserId: null,
  assignType: 'MANUAL',
  remark: ''
})

const getPlatformLabel = (platform) => {
  const map = {
    wechat: '微信',
    kook: 'KOOK',
    yy: 'YY'
  }
  return map[platform] || platform
}

const getPlatformTagType = (platform) => {
  const map = {
    wechat: 'primary',
    kook: 'success',
    yy: 'warning'
  }
  return map[platform] || 'info'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      aiEnabled: queryForm.aiEnabled === '1' ? true : queryForm.aiEnabled === '0' ? false : null
    }
    const res = await customerApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records.map(item => ({
        ...item,
        aiEnabled: Boolean(item.aiEnabled)
      }))
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.pageNum = 1
  queryForm.platform = null
  queryForm.aiEnabled = null
  queryForm.csUserId = null
  queryForm.keyword = ''
  handleQuery()
}

const handleViewDetail = async (row) => {
  try {
    const res = await customerApi.getById(row.id)
    if (res.code === 200) {
      currentCustomer.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    if (error.message && error.message.includes('无权')) {
      ElMessage.error('您无权查看此客户信息')
    } else {
      ElMessage.error('获取详情失败')
    }
  }
}

const handleAssign = (row) => {
  currentCustomer.value = row
  assignForm.csUserId = row.assignedCsUserId
  assignForm.assignType = 'MANUAL'
  assignForm.remark = ''
  assignDialogVisible.value = true
}

const handleConfirmAssign = async () => {
  if (!assignForm.csUserId) {
    ElMessage.warning('请选择客服')
    return
  }
  try {
    const res = await customerApi.assignCustomer(currentCustomer.value.id, assignForm)
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const handleToggleAi = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.aiEnabled ? '禁用' : '启用'}该客户的AI吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await customerApi.toggleAiEnabled(row.id, { aiEnabled: !row.aiEnabled })
    if (res.code === 200) {
      ElMessage.success('操作成功')
      handleQuery()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const loadCsList = async () => {
  if (!isAdminOrLeader) return
  try {
    const res = await sysUserApi.getPage({ pageNum: 1, pageSize: 100, role: 'CS_STAFF' })
    if (res.code === 200) {
      csList.value = res.data.records
    }
  } catch (error) {
    ElMessage.error('加载客服列表失败')
    console.error('加载客服列表失败', error)
  }
}

onMounted(() => {
  handleQuery()
  loadCsList()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 20px;
}

.detail-content {
  padding: 10px 0;
}
</style>
