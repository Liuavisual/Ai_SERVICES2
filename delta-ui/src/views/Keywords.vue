<!--
  关键词管理页面，CRUD关键词触发规则

  @author delta
-->
<template>
  <div class="keywords-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>关键词库</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索关键词..."
              prefix-icon="Search"
              clearable
              style="width: 200px; margin-right: 12px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button @click="importRef?.click()">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <input ref="importRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleImport" />
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="keyword" label="关键词" min-width="150" />
        <el-table-column prop="category" label="分类" width="140">
          <template #default="{ row }">
            <el-tag size="small" :type="getCategoryType(row.category)">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { currentPage = 1; fetchData() }"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="输入关键词" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%" :teleported="false">
            <el-option label="游戏术语" value="游戏术语" />
            <el-option label="服务相关" value="服务相关" />
            <el-option label="价格相关" value="价格相关" />
            <el-option label="敏感词" value="敏感词" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { keywordApi, downloadExcel, uploadExcel } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Upload } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { Result, PageResult, KeywordVO } from '@/types'

const loading = ref<boolean>(false)
const tableData = ref<KeywordVO[]>([])
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const total = ref<number>(0)
const importRef = ref<HTMLInputElement | null>(null)
const searchKeyword = ref<string>('')

const dialogVisible = ref<boolean>(false)
const dialogTitle = ref<string>('新增关键词')
const formRef = ref<FormInstance>()
const isEdit = ref<boolean>(false)

const form = ref<{
  id: string | null
  keyword: string
  category: string
  priority: number
  enabled: boolean
}>({
  id: null,
  keyword: '',
  category: '',
  priority: 10,
  enabled: true
})

const rules = {
  keyword: [{ required: true, message: '请输入关键词', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const getCategoryType = (category: string): string => {
  const map: Record<string, string> = { '游戏术语': '', '服务相关': 'success', '价格相关': 'warning', '敏感词': 'danger', '其他': 'info' }
  return map[category] || 'info'
}

const fetchData = async (): Promise<void> => {
  loading.value = true
  try {
    const res: Result<PageResult<KeywordVO>> = await keywordApi.getPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取关键词列表失败')
    console.error('获取关键词列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  fetchData()
}

const handleAdd = (): void => {
  isEdit.value = false
  dialogTitle.value = '新增关键词'
  form.value = { id: null, keyword: '', category: '', priority: 10, enabled: true }
  dialogVisible.value = true
}

const handleEdit = (row: KeywordVO): void => {
  isEdit.value = true
  dialogTitle.value = '编辑关键词'
  form.value = { id: row.id, keyword: row.keyword, category: row.category || '', priority: (row as any).priority || 10, enabled: Boolean(row.enabled) }
  dialogVisible.value = true
}

const handleSubmit = async (): Promise<void> => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const res: Result<null> = isEdit.value
      ? await keywordApi.update(form.value)
      : await keywordApi.create(form.value)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: KeywordVO): Promise<void> => {
  try {
    await ElMessageBox.confirm('确认删除该关键词？', '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: Result<null> = await keywordApi.delete(row.id)
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

onMounted(() => {
  fetchData()
})

const handleExport = (): void => {
  const params: Record<string, string> = {}
  if (searchKeyword.value) params.keyword = searchKeyword.value
  downloadExcel('/keywords/export', params, '关键词列表.xlsx')
}

const handleImport = async (event: Event): Promise<void> => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const res: Result<any> = await uploadExcel('/keywords/import', file)
    ElMessage.success(`导入完成：成功${res.data.success}条，失败${res.data.fail}条，共${res.data.total}条`)
    fetchData()
  } catch (e) {
    ElMessage.error('导入失败')
  } finally {
    (event.target as HTMLInputElement).value = ''
  }
}
</script>

<style scoped>
.keywords-container {
  padding: 0;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
