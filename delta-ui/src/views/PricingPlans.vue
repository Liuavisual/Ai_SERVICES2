<!--
  定价方案管理页面，管理SaaS三层定价方案（基础版/专业版/企业版）

  @author 刘建国
-->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>定价方案管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>新增方案
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="planCode" label="方案编码" width="120" />
        <el-table-column prop="planName" label="方案名称" min-width="120" />
        <el-table-column prop="monthlyPrice" label="月费(元)" width="110" align="right">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.monthlyPrice }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="yearlyPrice" label="年费(元)" width="110" align="right">
          <template #default="{ row }">
            <el-tag type="success">{{ row.yearlyPrice }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxCompanions" label="陪玩师上限" width="100" align="center" />
        <el-table-column prop="maxMonthlyMessages" label="月消息上限" width="100" align="center">
          <template #default="{ row }">{{ row.maxMonthlyMessages === 0 ? '无限制' : row.maxMonthlyMessages }}</template>
        </el-table-column>
        <el-table-column label="核心功能" width="280">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag size="small" v-if="row.includeSmartDispatch" type="success">智能派单</el-tag>
              <el-tag size="small" v-if="row.includeFullQualityCheck" type="success">全流程质检</el-tag>
              <el-tag size="small" v-if="row.includeAnalytics" type="success">数据分析</el-tag>
              <el-tag size="small" v-if="row.includeBrandCustom" type="warning">品牌定制</el-tag>
              <el-tag size="small" v-if="row.includeApiAccess" type="warning">API接入</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      :title="form.id ? '编辑定价方案' : '新增定价方案'"
      v-model="dialogVisible"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" label-width="140px">
        <el-form-item label="方案编码" required>
          <el-select v-model="form.planCode" placeholder="请选择方案编码" style="width: 100%">
            <el-option label="BASIC - 基础版" value="BASIC" />
            <el-option label="PRO - 专业版" value="PRO" />
            <el-option label="ENTERPRISE - 企业版" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="方案名称" required>
          <el-input v-model="form.planName" placeholder="请输入方案名称" />
        </el-form-item>
        <el-form-item label="月费(元)">
          <el-input-number v-model="form.monthlyPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="年费(元)">
          <el-input-number v-model="form.yearlyPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="陪玩师数量上限">
          <el-input-number v-model="form.maxCompanions" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="月消息量上限">
          <el-input-number v-model="form.maxMonthlyMessages" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="AI人格模板上限">
          <el-input-number v-model="form.maxPersonalityTemplates" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="情绪智能等级">
          <el-select v-model="form.emotionIntelligenceLevel" style="width: 100%">
            <el-option label="BASIC" value="BASIC" />
            <el-option label="ADVANCED" value="ADVANCED" />
            <el-option label="PREMIUM" value="PREMIUM" />
          </el-select>
        </el-form-item>
        <el-form-item label="核心功能">
          <el-checkbox v-model="form.includeSmartDispatch">智能派单</el-checkbox>
          <el-checkbox v-model="form.includeFullQualityCheck">全流程质检</el-checkbox>
          <el-checkbox v-model="form.includeAnalytics">数据分析</el-checkbox>
          <el-checkbox v-model="form.includeBrandCustom">品牌定制</el-checkbox>
          <el-checkbox v-model="form.includeApiAccess">API接入</el-checkbox>
        </el-form-item>
        <el-form-item label="功能描述">
          <el-input v-model="form.features" type="textarea" :rows="4" placeholder="功能描述（支持Markdown）" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pricingPlanApi } from '@/api'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])

const form = reactive({
  id: null,
  planCode: 'BASIC',
  planName: '',
  monthlyPrice: 99,
  yearlyPrice: 990,
  maxCompanions: 5,
  maxMonthlyMessages: 0,
  maxPersonalityTemplates: 2,
  emotionIntelligenceLevel: 'BASIC',
  includeSmartDispatch: false,
  includeFullQualityCheck: false,
  includeAnalytics: false,
  includeBrandCustom: false,
  includeApiAccess: false,
  features: '',
  sortOrder: 0,
  status: 1
})

function resetForm() {
  Object.assign(form, {
    id: null,
    planCode: 'BASIC',
    planName: '',
    monthlyPrice: 99,
    yearlyPrice: 990,
    maxCompanions: 5,
    maxMonthlyMessages: 0,
    maxPersonalityTemplates: 2,
    emotionIntelligenceLevel: 'BASIC',
    includeSmartDispatch: false,
    includeFullQualityCheck: false,
    includeAnalytics: false,
    includeBrandCustom: false,
    includeApiAccess: false,
    features: '',
    sortOrder: 0,
    status: 1
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await pricingPlanApi.getPage({ page: page.value, size: size.value })
    const data = res.data?.data || res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载定价方案失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该定价方案吗？', '确认删除', { type: 'warning' })
    await pricingPlanApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + (e.message || '未知错误'))
  }
}

async function handleSave() {
  if (!form.planName) {
    ElMessage.warning('请输入方案名称')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await pricingPlanApi.update(form)
    } else {
      await pricingPlanApi.create(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
