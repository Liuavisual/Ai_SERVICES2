<!--
  回复话术管理页面，CRUD自动回复规则

  @author delta
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>回复话术管理</span>
          <div>
            <el-select
              v-model="queryParams.triggerType"
              placeholder="触发类型筛选"
              clearable
              style="width: 150px; margin-right: 10px;"
              :teleported="false"
              @change="fetchData"
            >
              <el-option label="关键词" value="keyword" />
              <el-option label="欢迎语" value="welcome" />
              <el-option label="默认回复" value="default" />
            </el-select>
            <el-button type="primary" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe>
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="triggerType" label="触发类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTriggerTypeText(row.triggerType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="triggerKey" label="触发关键词" width="150" />
        <el-table-column prop="content" label="回复内容" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { queryParams.pageNum = 1; fetchData() }"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑回复话术' : '新增回复话术'"
      width="600px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="触发类型" prop="triggerType">
          <el-select 
            id="reply-triggerType"
            v-model="formData.triggerType" 
            placeholder="请选择触发类型" 
            style="width: 100%" 
            :teleported="false" 
            @change="handleTriggerTypeChange"
            name="triggerType"
          >
            <el-option label="关键词" value="keyword" />
            <el-option label="欢迎语" value="welcome" />
            <el-option label="默认回复" value="default" />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="formData.triggerType === 'keyword'"
          label="触发关键词"
          prop="triggerKey"
        >
          <el-input 
            id="reply-triggerKey"
            v-model="formData.triggerKey" 
            placeholder="请输入触发关键词" 
            name="triggerKey"
          />
        </el-form-item>
        <el-form-item label="回复内容" prop="content">
          <el-input
            id="reply-content"
            v-model="formData.content"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容"
            name="content"
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch 
            id="reply-enabled"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { replyApi } from '@/api'
import type { Result, PageResult, ReplyVO } from '@/types'

const tableData = ref<ReplyVO[]>([])
const total = ref<number>(0)
const queryParams = reactive<{
  pageNum: number
  pageSize: number
  triggerType: string | null
}>({
  pageNum: 1,
  pageSize: 10,
  triggerType: null
})

const dialogVisible = ref<boolean>(false)
const isEdit = ref<boolean>(false)
const formRef = ref<FormInstance>()
const formData = reactive<{
  id: string | null
  triggerType: string
  triggerKey: string
  content: string
  enabled: boolean
}>({
  id: null,
  triggerType: '',
  triggerKey: '',
  content: '',
  enabled: true
})

const formRules = {
  triggerType: [{ required: true, message: '请选择触发类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const getTriggerTypeText = (type: string): string => {
  const map: Record<string, string> = {
    'keyword': '关键词',
    'welcome': '欢迎语',
    'default': '默认回复'
  }
  return map[type] || type
}

const fetchData = async (): Promise<void> => {
  const params = {
    page: queryParams.pageNum,
    size: queryParams.pageSize,
    triggerType: queryParams.triggerType || null
  }
  const res: Result<PageResult<ReplyVO>> = await replyApi.getPage(params)
  if (res.code === 200) {
    tableData.value = res.data.records.map(item => ({
      ...item,
      enabled: Number(item.enabled)
    }))
    total.value = res.data.total
  }
}

const handleAdd = (): void => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    triggerType: '',
    triggerKey: '',
    content: '',
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = (row: ReplyVO): void => {
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    enabled: Boolean(row.enabled)
  })
  dialogVisible.value = true
}

const handleTriggerTypeChange = (): void => {
  if (formData.triggerType !== 'keyword') {
    formData.triggerKey = ''
  }
}

const handleSubmit = async (): Promise<void> => {
  try {
    if (!formRef.value) return
    await formRef.value.validate()
    if (isEdit.value) {
      await replyApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await replyApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  }
}

const handleDelete = (row: ReplyVO): void => {
  ElMessageBox.confirm('确定要删除该回复话术吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await replyApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
</style>
