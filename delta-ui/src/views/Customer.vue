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
        <el-table-column type="index" label="序号" width="80" />
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
        @size-change="onSizeChange"
        @current-change="onPageChange"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="客户详情" width="600px">
      <div v-if="currentCustomer" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="会员等级">{{ currentCustomer.memberLevel || '普通用户' }}</el-descriptions-item>
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { customerApi, sysUserApi } from '@/api'
import { authStorage } from '@/utils/storage'
import type { Result, PageResult, CustomerVO, SysUserVO } from '@/types'

interface CustomerRow extends CustomerVO {
  aiEnabled: boolean
  assignedCsUserId?: string
}

const userInfo = authStorage.getUserInfo()
const isAdminOrLeader: boolean = userInfo.role === 'SYS_ADMIN' || userInfo.role === 'CS_LEADER'

const loading = ref<boolean>(false)
const tableData = ref<CustomerRow[]>([])
const total = ref<number>(0)
const csList = ref<SysUserVO[]>([])

const queryForm = reactive<{
  pageNum: number
  pageSize: number
  platform: string | null
  aiEnabled: string | null
  csUserId: string | null
  keyword: string
}>({
  pageNum: 1,
  pageSize: 10,
  platform: null,
  aiEnabled: null,
  csUserId: null,
  keyword: ''
})

const detailDialogVisible = ref<boolean>(false)
const currentCustomer = ref<CustomerVO | null>(null)

const assignDialogVisible = ref<boolean>(false)
const assignForm = reactive<{
  csUserId: string | null
  assignType: string
  remark: string
}>({
  csUserId: null,
  assignType: 'MANUAL',
  remark: ''
})

const getPlatformLabel = (platform: string): string => {
  const map: Record<string, string> = {
    wechat: '微信',
    kook: 'KOOK',
    yy: 'YY'
  }
  return map[platform] || platform
}

const getPlatformTagType = (platform: string): string => {
  const map: Record<string, string> = {
    wechat: 'primary',
    kook: 'success',
    yy: 'warning'
  }
  return map[platform] || 'info'
}

const handleQuery = async (): Promise<void> => {
  queryForm.pageNum = 1
  loading.value = true
  try {
    const params = {
      page: queryForm.pageNum,
      size: queryForm.pageSize,
      platform: queryForm.platform || null,
      aiEnabled: queryForm.aiEnabled === '1' ? true : queryForm.aiEnabled === '0' ? false : null,
      csUserId: queryForm.csUserId || null,
      keyword: queryForm.keyword || null
    }
    const res: Result<PageResult<CustomerRow>> = await customerApi.getPage(params)
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

const handleReset = (): void => {
  queryForm.pageNum = 1
  queryForm.platform = null
  queryForm.aiEnabled = null
  queryForm.csUserId = null
  queryForm.keyword = ''
  handleQuery()
}

const buildPageParams = () => ({
  page: queryForm.pageNum,
  size: queryForm.pageSize
})

const onSizeChange = (): void => {
  queryForm.pageNum = 1
  loading.value = true
  customerApi.getPage(buildPageParams()).then((res: Result<any>) => {
    if (res.code === 200) {
      tableData.value = res.data.records.map((item: Record<string, unknown>) => ({ ...item, aiEnabled: Boolean(item.aiEnabled) }))
      total.value = res.data.total
    }
  }).finally(() => { loading.value = false })
}

const onPageChange = (): void => {
  loading.value = true
  customerApi.getPage(buildPageParams()).then((res: Result<any>) => {
    if (res.code === 200) {
      tableData.value = res.data.records.map((item: Record<string, unknown>) => ({ ...item, aiEnabled: Boolean(item.aiEnabled) }))
      total.value = res.data.total
    }
  }).finally(() => { loading.value = false })
}

const handleViewDetail = async (row: CustomerVO): Promise<void> => {
  try {
    const res: Result<CustomerVO> = await customerApi.getById(row.id)
    if (res.code === 200) {
      currentCustomer.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error: any) {
    if (error.message && error.message.includes('无权')) {
      ElMessage.error('您无权查看此客户信息')
    } else {
      ElMessage.error('获取详情失败')
    }
  }
}

const handleAssign = (row: CustomerRow): void => {
  currentCustomer.value = row
  assignForm.csUserId = row.assignedCsUserId || null
  assignForm.assignType = 'MANUAL'
  assignForm.remark = ''
  assignDialogVisible.value = true
}

const handleConfirmAssign = async (): Promise<void> => {
  if (!assignForm.csUserId) {
    ElMessage.warning('请选择客服')
    return
  }
  try {
    const res: Result<null> = await customerApi.assignCustomer(currentCustomer.value!.id, assignForm)
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const handleToggleAi = async (row: CustomerRow): Promise<void> => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.aiEnabled ? '禁用' : '启用'}该客户的AI吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const res: Result<null> = await customerApi.toggleAiEnabled(row.id, { aiEnabled: !row.aiEnabled })
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

const loadCsList = async (): Promise<void> => {
  if (!isAdminOrLeader) return
  try {
    const res: Result<PageResult<SysUserVO>> = await sysUserApi.getPage({ page: 1, size: 100, role: 'CS_STAFF' })
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
