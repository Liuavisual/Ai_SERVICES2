<!--
  客服-客户分配页面，管理客服与客户的绑定关系

  @author delta
-->
<template>
  <div class="cs-user-customer-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>客服-客户分配管理</span>
          <el-button type="primary" @click="handleCreate" v-if="isAdmin">
            <el-icon><Plus /></el-icon>
            新建分配
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px" :teleported="false">
            <el-option label="有效" value="ACTIVE" />
            <el-option label="无效" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="csUserName" label="客服" width="150" />
        <el-table-column prop="customerUserName" label="客户" width="150" />
        <el-table-column prop="assignTypeDesc" label="分配方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.assignType === 'MANUAL' ? 'primary' : 'success'">
              {{ row.assignTypeDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignedAt" label="分配时间" width="180" />
        <el-table-column prop="assignedByName" label="分配人" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="justify-content: flex-end"
      />
    </el-card>
    
    <!-- 编辑/新增弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="客服用户" prop="csUserId">
          <el-select 
            id="csUserCustomer-csUserId"
            v-model="form.csUserId" 
            placeholder="请选择客服" 
            style="width: 100%" 
            filterable 
            :teleported="false"
            name="csUserId"
          >
            <el-option 
              v-for="user in csUsers" 
              :key="user.id" 
              :label="user.realName" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户用户" prop="customerUserId">
          <el-select 
            id="csUserCustomer-customerUserId"
            v-model="form.customerUserId" 
            placeholder="请选择客户" 
            style="width: 100%" 
            filterable 
            :teleported="false"
            name="customerUserId"
          >
            <el-option 
              v-for="customer in customers" 
              :key="customer.id" 
              :label="customer.nickname" 
              :value="customer.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分配方式" prop="assignType">
          <el-select 
            id="csUserCustomer-assignType"
            v-model="form.assignType" 
            placeholder="请选择分配方式" 
            style="width: 100%" 
            :teleported="false"
            name="assignType"
          >
            <el-option label="手动分配" value="MANUAL" />
            <el-option label="系统分配" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="form.id">
          <el-select 
            id="csUserCustomer-status"
            v-model="form.status" 
            placeholder="请选择状态" 
            style="width: 100%" 
            :teleported="false"
            name="status"
          >
            <el-option label="有效" value="ACTIVE" />
            <el-option label="无效" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { csUserCustomerApi, sysUserApi, customerApi } from '@/api'
import type { Result, PageResult, SysUserVO, CustomerVO } from '@/types'

interface CsUserCustomerRow {
  id: number
  csUserId: string
  csUserName: string
  customerUserId: string
  customerUserName: string
  assignType: string
  assignTypeDesc: string
  status: string
  statusDesc: string
  assignedAt: string
  assignedByName: string
  remark: string
}

const isAdmin = computed<boolean>(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').role === 'SYS_ADMIN' } catch { return false }
})

const loading = ref<boolean>(false)
const submitLoading = ref<boolean>(false)
const dialogVisible = ref<boolean>(false)
const dialogTitle = ref<string>('')
const tableData = ref<CsUserCustomerRow[]>([])
const pageNum = ref<number>(1)
const pageSize = ref<number>(10)
const total = ref<number>(0)
const csUsers = ref<SysUserVO[]>([])
const customers = ref<CustomerVO[]>([])

const searchForm = reactive<{ status: string }>({
  status: ''
})

const form = reactive<{
  id: number | null
  csUserId: string | null
  customerUserId: string | null
  assignType: string
  status: string
  remark: string
}>({
  id: null,
  csUserId: null,
  customerUserId: null,
  assignType: 'MANUAL',
  status: 'ACTIVE',
  remark: ''
})

const rules = {
  csUserId: [{ required: true, message: '请选择客服', trigger: 'change' }],
  customerUserId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  assignType: [{ required: true, message: '请选择分配方式', trigger: 'change' }]
}

const formRef = ref<FormInstance>()

const fetchData = async (): Promise<void> => {
  loading.value = true
  try {
    const res: Result<PageResult<CsUserCustomerRow>> = await csUserCustomerApi.getPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取分配列表失败')
    console.error('获取分配列表失败', error)
  } finally {
    loading.value = false
  }
}

const fetchCsUsers = async (): Promise<void> => {
  try {
    const res: Result<PageResult<SysUserVO>> = await sysUserApi.getPage({ pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      csUsers.value = res.data.records.filter((u: SysUserVO) => u.role === 'CS_STAFF' || u.role === 'CS_LEADER')
    }
  } catch (error) {
    ElMessage.error('获取客服列表失败')
    console.error('获取客服列表失败', error)
  }
}

const fetchCustomers = async (): Promise<void> => {
  try {
    const res: Result<PageResult<CustomerVO>> = await customerApi.getPage({ pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      customers.value = res.data.records
    }
  } catch (error) {
    ElMessage.error('获取客户列表失败')
    console.error('获取客户列表失败', error)
  }
}

const resetSearch = (): void => {
  searchForm.status = ''
  pageNum.value = 1
  fetchData()
}

const handleCreate = (): void => {
  dialogTitle.value = '新建分配'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: CsUserCustomerRow): void => {
  dialogTitle.value = '编辑分配'
  const { id, csUserId, customerUserId, assignType, status, remark } = row
  Object.assign(form, { id, csUserId, customerUserId, assignType, status, remark })
  dialogVisible.value = true
}

const handleDelete = async (row: CsUserCustomerRow): Promise<void> => {
  try {
    await ElMessageBox.confirm('确定要删除该分配关系吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res: Result<null> = await csUserCustomerApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除失败', error)
    }
  }
}

const handleSubmit = async (): Promise<void> => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    let res: Result<null>
    if (form.id) {
      res = await csUserCustomerApi.update(form)
    } else {
      res = await csUserCustomerApi.create(form)
    }
    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = (): void => {
  form.id = null
  form.csUserId = null
  form.customerUserId = null
  form.assignType = 'MANUAL'
  form.status = 'ACTIVE'
  form.remark = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

onMounted(() => {
  fetchData()
  fetchCsUsers()
  fetchCustomers()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>
