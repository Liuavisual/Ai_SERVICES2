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
        <el-table-column prop="id" label="ID" width="80" />
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
        @size-change="fetchData"
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

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { replyApi } from '@/api'

const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  triggerType: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
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

const getTriggerTypeText = (type) => {
  const map = {
    'keyword': '关键词',
    'welcome': '欢迎语',
    'default': '默认回复'
  }
  return map[type] || type
}

const fetchData = async () => {
  const res = await replyApi.getPage(queryParams)
  if (res.code === 200) {
    tableData.value = res.data.records.map(item => ({
      ...item,
      enabled: Boolean(item.enabled)
    }))
    total.value = res.data.total
  }
}

const handleAdd = () => {
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

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    enabled: Boolean(row.enabled)
  })
  dialogVisible.value = true
}

const handleTriggerTypeChange = () => {
  if (formData.triggerType !== 'keyword') {
    formData.triggerKey = ''
  }
}

const handleSubmit = async () => {
  try {
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

const handleDelete = (row) => {
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
