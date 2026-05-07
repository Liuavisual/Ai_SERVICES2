<!--
  权限管理页面
  允许超级管理员配置角色、权限和用户角色分配
  包含三个Tab：权限定义、角色管理、用户角色分配
  
  @author 刘建国
-->
<template>
  <div class="permission-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 权限定义列表 -->
      <el-tab-pane label="权限定义" name="permissions">
        <div class="toolbar">
          <el-button type="primary" @click="handleInitPermissions" :loading="initLoading" v-if="isAdmin">
            初始化系统权限
          </el-button>
          <el-select v-model="permGroupFilter" placeholder="按分组筛选" clearable style="width:180px;margin-left:12px">
            <el-option v-for="g in permGroups" :key="g.value" :label="g.label" :value="g.value" />
          </el-select>
        </div>
        <el-table :data="filteredPermissions" border stripe v-loading="permLoading" max-height="500">
          <el-table-column prop="permCode" label="权限编码" width="200" />
          <el-table-column prop="permName" label="权限名称" width="160" />
          <el-table-column prop="permGroup" label="分组" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ groupLabel(row.permGroup) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="actionType" label="操作类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="actionTypeColor(row.actionType)">{{ actionTypeLabel(row.actionType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        </el-table>
      </el-tab-pane>

      <!-- 角色管理 -->
      <el-tab-pane label="角色管理" name="roles">
        <div style="display:flex; gap:16px; height:500px">
          <div style="width:320px; flex-shrink:0">
            <div class="toolbar">
              <el-button type="primary" size="small" @click="showRoleDialog(null)">新增角色</el-button>
            </div>
            <el-table :data="roles" border stripe highlight-current-row @current-change="onRoleSelect"
              v-loading="roleLoading" max-height="450" style="width:100%">
              <el-table-column prop="roleName" label="角色名称" />
              <el-table-column prop="status" label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-switch :model-value="row.status === 1" @change="toggleRoleStatus(row)" :disabled="row.isSystem === 1" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="showRoleDialog(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="deleteRole(row)"
                    v-if="row.isSystem !== 1">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div style="flex:1; border-left:1px solid #ebeef5; padding-left:16px">
            <div v-if="selectedRole" class="perm-assign-panel">
              <h4>为角色「{{ selectedRole.roleName }}」分配权限</h4>
              <p class="desc">{{ selectedRole.description || '暂无描述' }}</p>
              <el-checkbox-group v-model="selectedPermIds" class="perm-checkbox-group">
                <div v-for="g in permGroups" :key="g.value" class="perm-group-block">
                  <div class="perm-group-title">{{ g.label }}</div>
                  <el-checkbox v-for="p in getGroupPermissions(g.value)" :key="p.id" :label="p.id" :value="p.id">
                    {{ p.permName }}
                  </el-checkbox>
                </div>
              </el-checkbox-group>
              <el-button type="primary" @click="saveRolePermissions" :loading="savePermLoading" style="margin-top:16px">
                保存权限配置
              </el-button>
            </div>
            <el-empty v-else description="请从左侧选择一个角色" />
          </div>
        </div>
      </el-tab-pane>

      <!-- 用户角色分配 -->
      <el-tab-pane label="用户角色分配" name="userRoles">
        <div style="display:flex; gap:16px">
          <div style="width:320px; flex-shrink:0">
            <el-input v-model="userSearch" placeholder="搜索用户" clearable size="small" style="margin-bottom:8px" />
            <el-table :data="filteredUsers" border stripe highlight-current-row @current-change="onUserSelect"
              v-loading="userLoading" max-height="450" style="width:100%">
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="role" label="内置角色" width="120">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.role === 'SYS_ADMIN' ? 'danger' : 'info'">
                    {{ roleLabel(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div style="flex:1; border-left:1px solid #ebeef5; padding-left:16px">
            <div v-if="selectedUser" class="user-role-panel">
              <h4>为用户「{{ selectedUser.username }}」分配自定义角色</h4>
              <p class="desc">内置角色：{{ roleLabel(selectedUser.role) }}</p>
              <el-checkbox-group v-model="selectedUserRoleIds" class="perm-checkbox-group">
                <el-checkbox v-for="r in roles" :key="r.id" :label="r.id" :value="r.id">
                  {{ r.roleName }}
                  <span style="color:#909399;font-size:12px;margin-left:4px">{{ r.description }}</span>
                </el-checkbox>
              </el-checkbox-group>
              <el-button type="primary" @click="saveUserRoles" :loading="saveUserRoleLoading" style="margin-top:16px">
                保存用户角色
              </el-button>
            </div>
            <el-empty v-else description="请从左侧选择一个用户" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 角色编辑弹窗 -->
    <el-dialog :title="roleForm.id ? '编辑角色' : '新增角色'" v-model="roleDialogVisible" width="480px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.roleCode" placeholder="如 CS_MANAGER" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" placeholder="如 客服主管" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" placeholder="角色描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="roleForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="saveRoleLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('permissions')
const permLoading = ref(false)
const roleLoading = ref(false)
const userLoading = ref(false)
const initLoading = ref(false)
const savePermLoading = ref(false)
const saveRoleLoading = ref(false)
const saveUserRoleLoading = ref(false)

// 数据
const permissions = ref([])
const roles = ref([])
const users = ref([])
const selectedRole = ref(null)
const selectedPermIds = ref([])
const selectedUser = ref(null)
const selectedUserRoleIds = ref([])
const permGroupFilter = ref('')
const userSearch = ref('')
const roleDialogVisible = ref(false)
const roleForm = ref({})

const isAdmin = computed(() => {
  const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return info.role === 'SYS_ADMIN'
})

const permGroups = [
  { value: 'dashboard', label: '工作台' },
  { value: 'customer', label: '客户管理' },
  { value: 'companion', label: '陪玩管理' },
  { value: 'order', label: '订单/工单' },
  { value: 'service', label: '服务管理' },
  { value: 'message', label: '消息处理' },
  { value: 'config', label: '系统配置' },
  { value: 'system', label: '系统管理' },
  { value: 'tool', label: '开发工具' }
]

const filteredPermissions = computed(() => {
  if (!permGroupFilter.value) return permissions.value
  return permissions.value.filter(p => p.permGroup === permGroupFilter.value)
})

const filteredUsers = computed(() => {
  if (!userSearch.value) return users.value
  const kw = userSearch.value.toLowerCase()
  return users.value.filter(u => u.username.toLowerCase().includes(kw))
})

const groupLabel = (g) => permGroups.find(pg => pg.value === g)?.label || g
const actionTypeLabel = (t) => ({ view: '查看', create: '新增', edit: '编辑', delete: '删除', export: '导出', manage: '管理' })[t] || t
const actionTypeColor = (t) => ({ view: '', create: 'success', edit: 'warning', delete: 'danger', export: 'primary', manage: 'danger' })[t] || ''
const roleLabel = (r) => ({ SYS_ADMIN: '超级管理员', CS_LEADER: '客服主管', CS_STAFF: '客服人员' })[r] || r

const getGroupPermissions = (group) => permissions.value.filter(p => p.permGroup === group)

// ============ 数据加载 ============

async function loadPermissions() {
  permLoading.value = true
  try {
    const res = await request.get('/api/v1/permission/list')
    permissions.value = res.data || []
  } finally {
    permLoading.value = false
  }
}

async function loadRoles() {
  roleLoading.value = true
  try {
    const res = await request.get('/api/v1/permission/roles')
    roles.value = res.data || []
  } finally {
    roleLoading.value = false
  }
}

async function loadUsers() {
  userLoading.value = true
  try {
    const res = await request.get('/api/v1/sys-users', { params: { page: 1, size: 200 } })
    users.value = (res.data?.records || []).filter(u => u.status === 'ACTIVE')
  } finally {
    userLoading.value = false
  }
}

async function loadUserRoles(userId) {
  try {
    const res = await request.get(`/api/v1/permission/users/${userId}/roles`)
    selectedUserRoleIds.value = (res.data || []).map(r => r.id)
  } catch { selectedUserRoleIds.value = [] }
}

// ============ 权限初始化 ============

async function handleInitPermissions() {
  try {
    await ElMessageBox.confirm('将初始化系统默认权限定义，已有数据不会重复创建。确认继续？', '提示', { type: 'info' })
    initLoading.value = true
    await request.post('/api/v1/permission/init')
    await loadPermissions()
    ElMessage.success('权限初始化成功')
  } catch { /* 取消 */ }
  finally { initLoading.value = false }
}

// ============ 角色管理 ============

function onRoleSelect(row) {
  selectedRole.value = row
  if (row) {
    selectedPermIds.value = (row.permissions || []).map(p => p.id)
  }
}

function showRoleDialog(row) {
  roleForm.value = row ? { ...row } : { roleCode: '', roleName: '', description: '', sortOrder: 99, status: 1 }
  roleDialogVisible.value = true
}

async function saveRole() {
  saveRoleLoading.value = true
  try {
    if (roleForm.value.id) {
      await request.put(`/api/v1/permission/roles/${roleForm.value.id}`, roleForm.value)
    } else {
      await request.post('/api/v1/permission/roles', roleForm.value)
    }
    roleDialogVisible.value = false
    await loadRoles()
    ElMessage.success('保存成功')
  } catch { /* handled by interceptor */ }
  finally { saveRoleLoading.value = false }
}

async function deleteRole(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
    await request.delete(`/api/v1/permission/roles/${row.id}`)
    if (selectedRole.value?.id === row.id) selectedRole.value = null
    await loadRoles()
    ElMessage.success('删除成功')
  } catch { /* 取消 */ }
}

async function toggleRoleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/api/v1/permission/roles/${row.id}`, { ...row, status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch { /* handled */ }
}

async function saveRolePermissions() {
  savePermLoading.value = true
  try {
    await request.put(`/api/v1/permission/roles/${selectedRole.value.id}/permissions`, { permIds: selectedPermIds.value })
    await loadRoles()
    ElMessage.success('权限配置已保存')
  } finally {
    savePermLoading.value = false
  }
}

// ============ 用户角色分配 ============

function onUserSelect(row) {
  selectedUser.value = row
  if (row) {
    loadUserRoles(row.id)
  }
}

async function saveUserRoles() {
  saveUserRoleLoading.value = true
  try {
    await request.put(`/api/v1/permission/users/${selectedUser.value.id}/roles`, { roleIds: selectedUserRoleIds.value })
    ElMessage.success('用户角色已保存')
  } finally {
    saveUserRoleLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadPermissions(), loadRoles(), loadUsers()])
})
</script>

<style scoped>
.permission-container { padding: 0; }
.toolbar { margin-bottom: 12px; display: flex; align-items: center; }
.perm-assign-panel h4 { margin: 0 0 4px 0; }
.perm-assign-panel .desc { color: #909399; font-size: 13px; margin-bottom: 16px; }
.perm-checkbox-group { max-height: 380px; overflow-y: auto; }
.perm-group-block { margin-bottom: 12px; padding: 8px 12px; background: #fafafa; border-radius: 6px; }
.perm-group-title { font-weight: 600; margin-bottom: 6px; color: #303133; font-size: 14px; }
.perm-group-block .el-checkbox { margin-right: 16px; margin-bottom: 4px; }
.user-role-panel h4 { margin: 0 0 4px 0; }
.user-role-panel .desc { color: #909399; font-size: 13px; margin-bottom: 16px; }
</style>
