<template>
  <div class="activity-package-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动套餐管理</span>
          <div>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button @click="importRef?.click()" v-if="isAdmin">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <input ref="importRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleImport" />
            <el-button type="primary" @click="handleAdd" v-if="isAdmin">
              <el-icon><Plus /></el-icon>
              新增活动
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="packageList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="活动标题" min-width="160" />
        <el-table-column prop="activityType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.activityType)" size="small">{{ typeLabel(row.activityType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gameName" label="关联游戏" width="100">
          <template #default="{ row }">{{ row.gameName || '通用' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="140">
          <template #default="{ row }">
            <span style="color: var(--gu-accent); font-weight: 600">¥{{ row.packagePrice }}</span>
            <span v-if="row.originalPrice" style="text-decoration: line-through; color: var(--gu-text-muted); margin-left: 6px; font-size: 12px">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="serviceItemNames" label="包含服务" show-overflow-tooltip min-width="130" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="180">
          <template #default="{ row }">
            <template v-if="row.startTime && row.endTime">
              {{ row.startTime?.substring(0, 10) }} ~ {{ row.endTime?.substring(0, 10) }}
            </template>
            <template v-else>永久有效</template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" v-if="isAdmin">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="活动类型" required>
          <el-select v-model="form.activityType" style="width: 100%">
            <el-option label="赛季活动" value="SEASON" />
            <el-option label="节日特惠" value="FESTIVAL" />
            <el-option label="限时优惠" value="LIMITED" />
            <el-option label="促销活动" value="PROMOTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联游戏">
          <el-select v-model="form.gameConfigId" clearable placeholder="通用" style="width: 100%">
            <el-option v-for="g in gameList" :key="g.id" :label="g.gameName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐价格" required>
          <el-input-number v-model="form.packagePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价(划线)">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="包含服务">
          <el-select v-model="selectedServiceIds" multiple placeholder="选择包含的服务项目" style="width: 100%">
            <el-option v-for="s in serviceList" :key="s.id" :label="s.itemName" :value="s.id" />
          </el-select>
          <div style="color: var(--gu-text-muted); font-size: 12px; margin-top: 6px">
            注：活动套餐为独立定价，价格不随陪玩师等级变化，陪玩师自愿接单
          </div>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="使用条款">
          <el-input v-model="form.terms" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Upload } from '@element-plus/icons-vue'
import { activityPackageApi, gameConfigApi, serviceItemApi, clubConfigApi, downloadExcel, uploadExcel } from '@/api/index.js'

const loading = ref(false)
const packageList = ref([])
const gameList = ref([])
const serviceList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增活动')
const clubConfigId = ref(1)
const selectedServiceIds = ref([])

const form = ref({ id: null, clubConfigId: 1, title: '', description: '', activityType: 'SEASON', gameConfigId: null, serviceItemIds: '', packagePrice: null, originalPrice: null, startTime: null, endTime: null, bannerUrl: '', terms: '', sortOrder: 0, enabled: 1 })

const isAdmin = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').role === 'SYS_ADMIN' } catch { return false }
})

const typeLabel = (t) => ({ SEASON: '赛季活动', FESTIVAL: '节日特惠', LIMITED: '限时优惠', PROMOTION: '促销活动' }[t] || t)
const typeTag = (t) => ({ SEASON: 'danger', FESTIVAL: 'warning', LIMITED: 'success', PROMOTION: 'info' }[t] || 'info')
const statusTag = (s) => ({ '进行中': 'success', '未开始': 'warning', '已结束': 'info', '已禁用': 'danger', '永久有效': 'info' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await clubConfigApi.get()
    if (res.code === 200 && res.data) clubConfigId.value = res.data.id || 1
    form.value.clubConfigId = clubConfigId.value
    const [pkgRes, gameRes, svcRes] = await Promise.all([
      activityPackageApi.getByClubId(clubConfigId.value),
      gameConfigApi.getByClubId(clubConfigId.value),
      serviceItemApi.getByClubId(clubConfigId.value)
    ])
    packageList.value = pkgRes.data || []
    gameList.value = gameRes.data || []
    serviceList.value = svcRes.data || []
  } catch (e) { ElMessage.error('加载失败') }
  loading.value = false
}

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  form.value = { id: null, clubConfigId: clubConfigId.value, title: '', description: '', activityType: 'SEASON', gameConfigId: null, serviceItemIds: '', packagePrice: null, originalPrice: null, startTime: null, endTime: null, bannerUrl: '', terms: '', sortOrder: 0, enabled: 1 }
  selectedServiceIds.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑活动'
  const { id, clubConfigId, title, description, activityType, gameConfigId, serviceItemIds, packagePrice, originalPrice, startTime, endTime, bannerUrl, terms, sortOrder, enabled } = row
  Object.assign(form.value, { id, clubConfigId, title, description, activityType, gameConfigId, serviceItemIds, packagePrice, originalPrice, startTime, endTime, bannerUrl, terms, sortOrder, enabled })
  selectedServiceIds.value = serviceItemIds ? serviceItemIds.split(',').map(Number) : []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    form.value.serviceItemIds = selectedServiceIds.value.join(',')
    if (form.value.id) await activityPackageApi.update(form.value)
    else await activityPackageApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('保存失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此活动套餐？', '提示', { type: 'warning' })
    await activityPackageApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error('删除失败')
  }
}

const importRef = ref(null)

const handleExport = () => {
  downloadExcel('/activity-packages/export', { clubConfigId: clubConfigId.value }, '活动套餐.xlsx')
}

const handleImport = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadExcel('/activity-packages/import?clubConfigId=' + clubConfigId.value, file)
    ElMessage.success(`导入完成：成功${res.data.success}条，失败${res.data.fail}条，共${res.data.total}条`)
    loadData()
  } catch (e) {
    ElMessage.error('导入失败')
  } finally {
    event.target.value = ''
  }
}

onMounted(loadData)
</script>

<style scoped>
.activity-package-container {
  padding: 0;
}
</style>
