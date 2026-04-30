<!--
  FAQ知识库页面，管理常见问答条目

  @author delta
-->
<template>
  <div class="faq-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>FAQ知识库管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增FAQ
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable style="width: 200px">
            <el-option label="价格" value="价格" />
            <el-option label="预约" value="预约" />
            <el-option label="陪玩师" value="陪玩师" />
            <el-option label="退款" value="退款" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="question" label="问题" show-overflow-tooltip />
        <el-table-column prop="answer" label="答案" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { pagination.page = 1; loadData() }"
        @current-change="loadData"
        style="justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="价格" value="价格" />
            <el-option label="预约" value="预约" />
            <el-option label="陪玩师" value="陪玩师" />
            <el-option label="退款" value="退款" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题" prop="question">
          <el-input v-model="form.question" placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="6"
            placeholder="请输入答案"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { faqItemApi } from '@/api'
import type { Result, PageResult, FaqItemVO } from '@/types'

const loading = ref<boolean>(false)
const submitLoading = ref<boolean>(false)
const dialogVisible = ref<boolean>(false)
const dialogTitle = ref<string>('')
const formRef = ref<FormInstance>()
const tableData = ref<FaqItemVO[]>([])

const searchForm = reactive<{ category: string }>({
  category: ''
})

const pagination = reactive<{
  page: number
  size: number
  total: number
}>({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive<{
  id: string | null
  category: string
  question: string
  answer: string
  sortOrder: number
  enabled: number
}>({
  id: null,
  category: '',
  question: '',
  answer: '',
  sortOrder: 0,
  enabled: 1
})

const rules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }]
}

const getCategoryType = (category: string): string => {
  const map: Record<string, string> = {
    '价格': 'success',
    '预约': 'primary',
    '陪玩师': 'warning',
    '退款': 'danger',
    '其他': 'info'
  }
  return map[category] || 'info'
}

const loadData = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...(searchForm.category ? { category: searchForm.category } : {})
    }
    const res: Result<PageResult<FaqItemVO>> = await faqItemApi.getPage(params)
    if (res.data) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = (): void => {
  pagination.page = 1
  loadData()
}

const handleReset = (): void => {
  searchForm.category = ''
  handleSearch()
}

const handleAdd = (): void => {
  dialogTitle.value = '新增FAQ'
  Object.assign(form, {
    id: null,
    category: '',
    question: '',
    answer: '',
    sortOrder: 0,
    enabled: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: FaqItemVO): void => {
  dialogTitle.value = '编辑FAQ'
  const { id, category, question, answer, sortOrder, enabled } = row
  Object.assign(form, { id, category, question, answer, sortOrder, enabled })
  dialogVisible.value = true
}

const handleDelete = async (row: FaqItemVO): Promise<void> => {
  try {
    await ElMessageBox.confirm('确定要删除这条FAQ吗？', '提示', {
      type: 'warning'
    })
    await faqItemApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
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
    if (form.id) {
      await faqItemApi.update(form)
    } else {
      await faqItemApi.create(form)
    }
    ElMessage.success(form.id ? '更新成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
  padding: 14px;
  background: var(--gu-bg-secondary);
  border-radius: var(--gu-radius-lg);
  border: 1px solid var(--gu-border);
}
</style>
