<!--
  陪玩师管理页面，CRUD陪玩师信息和状态

  @author delta
-->
<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="等级">
          <el-select v-model="queryParams.levelId" placeholder="请选择等级" clearable style="width: 180px" :teleported="false">
            <el-option v-for="level in levels" :key="level.id" :label="level.levelName" :value="level.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.enabled" placeholder="请选择状态" clearable style="width: 180px" :teleported="false">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>陪玩师管理</span>
          <el-button type="primary" @click="handleAdd" v-if="isAdmin">新增陪玩师</el-button>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="levelName" label="等级" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.levelName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span v-if="row.displayPrice !== null && row.displayPrice !== undefined">¥{{ row.displayPrice }}</span>
            <span v-else-if="row.levelBasePrice !== null && row.levelBasePrice !== undefined">¥{{ row.levelBasePrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="wechat" label="微信" width="130" />
        <el-table-column prop="gameType" label="擅长游戏" show-overflow-tooltip min-width="150" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleSchedule(row)">时间</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)" v-if="isAdmin">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="isAdmin">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑陪玩师' : '新增陪玩师'"
      width="600px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            id="companion-nickname"
            v-model="formData.nickname" 
            placeholder="请输入昵称/游戏名" 
            name="nickname"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            id="companion-realName"
            v-model="formData.realName" 
            placeholder="请输入真实姓名" 
            name="realName"
          />
        </el-form-item>
        <el-form-item label="等级" prop="levelId">
          <el-select 
            id="companion-levelId"
            v-model="formData.levelId" 
            placeholder="请选择等级" 
            style="width: 100%" 
            :teleported="false"
            name="levelId"
          >
            <el-option v-for="level in levels" :key="level.id" :label="level.levelName" :value="level.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number 
            id="companion-price"
            v-model="formData.price" 
            :min="0" 
            :precision="2" 
            placeholder="不填则使用等级基础价格" 
            style="width: 100%"
            name="price"
          />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input 
            id="companion-phone"
            v-model="formData.phone" 
            placeholder="请输入联系电话" 
            name="phone"
          />
        </el-form-item>
        <el-form-item label="微信" prop="wechat">
          <el-input 
            id="companion-wechat"
            v-model="formData.wechat" 
            placeholder="请输入微信号" 
            name="wechat"
          />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-input 
            id="companion-avatar"
            v-model="formData.avatar" 
            placeholder="请输入头像URL" 
            name="avatar"
          />
        </el-form-item>
        <el-form-item label="擅长游戏" prop="gameType">
          <el-input 
            id="companion-gameType"
            v-model="formData.gameType" 
            placeholder="请输入擅长游戏类型，逗号分隔" 
            name="gameType"
          />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input 
            id="companion-description"
            v-model="formData.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入个人简介" 
            name="description"
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch 
            id="companion-enabled"
            v-model="formData.enabled" 
            name="enabled"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="scheduleDialogVisible"
      :title="`${currentCompanion?.nickname || ''} - 时间管理`"
      width="900px"
    >
      <companion-schedule
        ref="scheduleRef"
        :companion-id="currentCompanion?.id"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineAsyncComponent } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { companionApi, companionLevelApi } from '@/api'
import type { Result, PageResult, CompanionVO, CompanionLevelVO } from '@/types'

const isAdmin = computed<boolean>(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').role === 'SYS_ADMIN' } catch { return false }
})

const CompanionSchedule = defineAsyncComponent(() => import('./CompanionSchedule.vue'))

const tableData = ref<CompanionVO[]>([])
const total = ref<number>(0)
const levels = ref<CompanionLevelVO[]>([])
const queryParams = reactive<{
  pageNum: number
  pageSize: number
  levelId: number | null
  nickname: string
  enabled: string | null
}>({
  pageNum: 1,
  pageSize: 10,
  levelId: null,
  nickname: '',
  enabled: null
})

const dialogVisible = ref<boolean>(false)
const scheduleDialogVisible = ref<boolean>(false)
const isEdit = ref<boolean>(false)
const formRef = ref<FormInstance>()
const scheduleRef = ref<any>(null)
const currentCompanion = ref<CompanionVO | null>(null)

const formData = reactive<{
  id: string | null
  realName: string
  nickname: string
  phone: string
  wechat: string
  levelId: number | null
  avatar: string
  gameType: string
  description: string
  price: number | null
  enabled: boolean
}>({
  id: null,
  realName: '',
  nickname: '',
  phone: '',
  wechat: '',
  levelId: null,
  avatar: '',
  gameType: '',
  description: '',
  price: null,
  enabled: true
})

const formRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const fetchData = async (): Promise<void> => {
  try {
    const params = {
      ...queryParams,
      enabled: queryParams.enabled === '1' ? 1 : queryParams.enabled === '0' ? 0 : null
    }
    const res: Result<PageResult<CompanionVO>> = await companionApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records.map(item => ({
        ...item,
        enabled: Boolean(item.enabled),
        price: item.price !== null && item.price !== undefined ? Number(item.price) : null,
        displayPrice: item.displayPrice !== null && item.displayPrice !== undefined ? Number(item.displayPrice) : null,
        levelBasePrice: item.levelBasePrice !== null && item.levelBasePrice !== undefined ? Number(item.levelBasePrice) : null
      }))
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
    console.error('查询失败', error)
  }
}

const fetchLevels = async (): Promise<void> => {
  try {
    const res: Result<CompanionLevelVO[]> = await companionLevelApi.getAll()
    if (res.code === 200) {
      levels.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取等级失败')
    console.error('获取等级失败', error)
  }
}

const handleQuery = (): void => {
  queryParams.pageNum = 1
  fetchData()
}

const handleReset = (): void => {
  queryParams.pageNum = 1
  queryParams.levelId = null
  queryParams.nickname = ''
  queryParams.enabled = null
  fetchData()
}

const handleAdd = (): void => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    realName: '',
    nickname: '',
    phone: '',
    wechat: '',
    levelId: null,
    avatar: '',
    gameType: '',
    description: '',
    price: null,
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = (row: CompanionVO): void => {
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    enabled: Boolean(row.enabled),
    price: row.price !== null && row.price !== undefined ? Number(row.price) : null,
    levelId: row.levelId !== null && row.levelId !== undefined ? Number(row.levelId) : null
  })
  dialogVisible.value = true
}

const handleSubmit = async (): Promise<void> => {
  try {
    if (!formRef.value) return
    await formRef.value.validate()
    if (isEdit.value) {
      await companionApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await companionApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  }
}

const handleDelete = (row: CompanionVO): void => {
  ElMessageBox.confirm('确定要删除该陪玩师吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await companionApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleSchedule = (row: CompanionVO): void => {
  currentCompanion.value = row
  scheduleDialogVisible.value = true
}

onMounted(() => {
  fetchData()
  fetchLevels()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 20px;
}
</style>
