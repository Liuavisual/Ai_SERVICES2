<!--
  陪玩师等级管理页面，CRUD等级体系和基础价格

  @author delta
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>陪玩师等级管理</span>
          <div>
            <el-input
              v-model="queryParams.levelName"
              placeholder="搜索等级名称"
              clearable
              style="width: 200px; margin-right: 10px;"
              @keyup.enter="fetchData"
            />
            <el-button type="primary" @click="handleAdd">新增等级</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="levelName" label="等级名称" min-width="120" />
        <el-table-column prop="levelCode" label="等级代码" min-width="130" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="basePrice" label="基础价格" width="120">
          <template #default="{ row }">
            <span v-if="row.basePrice !== null && row.basePrice !== undefined">¥{{ row.basePrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="180" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
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
      :title="isEdit ? '编辑等级' : '新增等级'"
      width="500px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input 
            id="level-levelName"
            v-model="formData.levelName" 
            placeholder="请输入等级名称，如：二品、一品、顶尖、明星" 
            name="levelName"
          />
        </el-form-item>
        <el-form-item label="等级代码" prop="levelCode">
          <el-input 
            id="level-levelCode"
            v-model="formData.levelCode" 
            placeholder="请输入等级代码，如：LEVEL_TWO" 
            name="levelCode"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number 
            id="level-sortOrder"
            v-model="formData.sortOrder" 
            :min="0" 
            placeholder="数字越小越靠前" 
            name="sortOrder"
          />
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number 
            id="level-basePrice"
            v-model="formData.basePrice" 
            :min="0" 
            :precision="2" 
            placeholder="元/小时" 
            name="basePrice"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            id="level-description"
            v-model="formData.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入等级描述" 
            name="description"
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch 
            id="level-enabled"
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
import { companionLevelApi } from '@/api'

const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  levelName: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  levelName: '',
  levelCode: '',
  sortOrder: 0,
  basePrice: 0,
  description: '',
  enabled: true
})

const formRules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  levelCode: [{ required: true, message: '请输入等级代码', trigger: 'blur' }]
}

const fetchData = async () => {
  try {
    const res = await companionLevelApi.getPage(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records.map(item => ({
        ...item,
        enabled: Boolean(item.enabled),
        basePrice: item.basePrice !== null && item.basePrice !== undefined ? Number(item.basePrice) : 0,
        sortOrder: item.sortOrder !== null && item.sortOrder !== undefined ? Number(item.sortOrder) : 0
      }))
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
    console.error('查询失败', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    levelName: '',
    levelCode: '',
    sortOrder: 0,
    basePrice: 0,
    description: '',
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    ...row,
    enabled: Boolean(row.enabled),
    basePrice: row.basePrice !== null && row.basePrice !== undefined ? Number(row.basePrice) : 0
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await companionLevelApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await companionLevelApi.create(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e !== false) ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该等级吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await companionLevelApi.delete(row.id)
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
