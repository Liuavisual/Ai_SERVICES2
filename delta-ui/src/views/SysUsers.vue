<!--
  系统用户管理页面，CRUD后台账号和权限

  @author delta
-->
<template>
  <div class="sys-users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统用户管理</span>
          <el-button type="primary" @click="handleCreate" v-if="isAdmin">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 180px" :teleported="false">
            <el-option label="系统管理员" value="SYS_ADMIN" />
            <el-option label="客服负责人" value="CS_LEADER" />
            <el-option label="普通客服" value="CS_STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px" :teleported="false">
            <el-option label="待审核" value="PENDING" />
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ maskPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column prop="roleDesc" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ row.roleDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)" v-if="isAdmin">编辑</el-button>
            <el-button 
              v-if="row.status === 'PENDING' && isAdmin" 
              type="success" 
              size="small" 
              link 
              @click="handleAudit(row, 'ACTIVE')"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING' && isAdmin" 
              type="danger" 
              size="small" 
              link 
              @click="handleAudit(row, 'DISABLED')"
            >
              拒绝
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)" v-if="isAdmin">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { pageNum = 1; fetchData() }"
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input 
            id="sysUser-username"
            v-model="form.username" 
            :disabled="!!form.id" 
            placeholder="请输入用户名" 
            name="username"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input 
            id="sysUser-password-new"
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            name="password"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="password" v-if="form.id">
          <el-input 
            id="sysUser-password-edit"
            v-model="form.password" 
            type="password" 
            placeholder="不修改请留空" 
            show-password
            name="password"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            id="sysUser-realName"
            v-model="form.realName" 
            placeholder="请输入真实姓名" 
            name="realName"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            id="sysUser-phone"
            v-model="form.phone" 
            placeholder="请输入手机号" 
            name="phone"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            id="sysUser-email"
            v-model="form.email" 
            placeholder="请输入邮箱" 
            name="email"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select 
            id="sysUser-role"
            v-model="form.role" 
            placeholder="请选择角色" 
            style="width: 100%" 
            :teleported="false"
            name="role"
          >
            <el-option label="系统管理员" value="SYS_ADMIN" />
            <el-option label="客服负责人" value="CS_LEADER" />
            <el-option label="普通客服" value="CS_STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="form.id">
          <el-select 
            id="sysUser-status"
            v-model="form.status" 
            placeholder="请选择状态" 
            style="width: 100%" 
            :teleported="false"
            name="status"
          >
            <el-option label="待审核" value="PENDING" />
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
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
import { sysUserApi } from '@/api'
import type { Result, PageResult, SysUserVO, UserRole } from '@/types'

const isAdmin = computed<boolean>(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').role === 'SYS_ADMIN' } catch { return false }
})

const loading = ref<boolean>(false)
const submitLoading = ref<boolean>(false)
const dialogVisible = ref<boolean>(false)
const dialogTitle = ref<string>('')
const tableData = ref<SysUserVO[]>([])
const pageNum = ref<number>(1)
const pageSize = ref<number>(10)
const total = ref<number>(0)

const searchForm = reactive<{
  role: UserRole | null
  status: string | null
}>({
  role: null,
  status: null
})

const form = reactive<{
  id: number | null
  username: string
  password: string
  realName: string
  phone: string
  email: string
  role: string
  status: string
}>({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: '',
  status: ''
})

const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: form.id
    ? [{
        validator: (_rule: any, value: string, callback: (err?: Error) => void) => {
          if (value && value.length < 8) {
            callback(new Error('密码长度至少 8 个字符'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }]
    : [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 8, message: '密码长度至少 8 个字符', trigger: 'blur' }
      ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}))

const formRef = ref<FormInstance>()

const fetchData = async (): Promise<void> => {
  loading.value = true
  try {
    const res: Result<PageResult<SysUserVO>> = await sysUserApi.getPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      role: searchForm.role,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = (): void => {
  searchForm.role = null
  searchForm.status = null
  pageNum.value = 1
  fetchData()
}

const handleCreate = (): void => {
  dialogTitle.value = '添加用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: SysUserVO): void => {
  dialogTitle.value = '编辑用户'
  const { id, username, realName, phone, email, role, status } = row
  Object.assign(form, { id, username, realName, phone, email, role, status })
  dialogVisible.value = true
}

const handleAudit = async (row: SysUserVO, status: string): Promise<void> => {
  const action = status === 'ACTIVE' ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res: Result<null> = await sysUserApi.audit({ userId: row.id, status })
    if (res.code === 200) {
      ElMessage.success(`审核${action}成功`)
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
      console.error('审核失败', error)
    }
  }
}

const handleDelete = async (row: SysUserVO): Promise<void> => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res: Result<null> = await sysUserApi.delete(row.id)
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
      res = await sysUserApi.update(form)
    } else {
      res = await sysUserApi.create(form)
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
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.role = ''
  form.status = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const maskPhone = (phone: string): string => {
  if (!phone) return ''
  if (phone.length < 7) return phone
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

const getRoleTagType = (role: string): string => {
  const map: Record<string, string> = {
    'SYS_ADMIN': 'danger',
    'CS_LEADER': 'warning',
    'CS_STAFF': 'success'
  }
  return map[role] || 'info'
}

const getStatusTagType = (status: string): string => {
  const map: Record<string, string> = {
    'PENDING': 'info',
    'ACTIVE': 'success',
    'DISABLED': 'danger'
  }
  return map[status] || 'info'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>
