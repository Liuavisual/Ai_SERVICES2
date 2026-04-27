<template>
  <div class="service-item-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务项目管理</span>
          <el-button type="primary" @click="handleAdd" v-if="isAdmin">
            <el-icon><Plus /></el-icon>
            新增服务
          </el-button>
        </div>
      </template>

      <el-table :data="serviceList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="itemName" label="项目名称" width="120" />
        <el-table-column prop="itemCode" label="项目编码" width="120" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="categoryTag(row.category)" size="small">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gameName" label="关联游戏" width="100">
          <template #default="{ row }">{{ row.gameName || '通用' }}</template>
        </el-table-column>
        <el-table-column prop="basePrice" label="基础价格" width="110">
          <template #default="{ row }">
            <span v-if="row.basePrice" style="color: var(--gu-accent); font-weight: 600">
              ¥{{ row.basePrice }}/{{ row.priceUnit === 'HOUR' ? '时' : row.priceUnit === 'ORDER' ? '单' : '套' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="guaranteeText" label="服务承诺" show-overflow-tooltip min-width="130" />
        <el-table-column prop="enabled" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" v-if="isAdmin">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handlePriceRules(row)">定价</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.itemName" placeholder="如：护航服务" />
        </el-form-item>
        <el-form-item label="项目编码" required>
          <el-select v-model="form.itemCode" style="width: 100%" filterable allow-create>
            <el-option label="ESCORT-护航" value="ESCORT" />
            <el-option label="RUN_KNIFE-跑刀" value="RUN_KNIFE" />
            <el-option label="TEACHING-教学" value="TEACHING" />
            <el-option label="RANK_UP-冲分" value="RANK_UP" />
            <el-option label="PURE_PLAY-纯陪玩" value="PURE_PLAY" />
            <el-option label="ENTERTAINMENT-娱乐陪" value="ENTERTAINMENT" />
            <el-option label="ACTIVITY-活动" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="技术陪玩" value="TECHNICAL" />
            <el-option label="娱乐陪玩" value="ENTERTAINMENT" />
            <el-option label="纯陪玩" value="PURE_PLAY" />
            <el-option label="护航服务" value="ESCORT" />
            <el-option label="活动玩法" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联游戏">
          <el-select v-model="form.gameConfigId" clearable placeholder="通用服务" style="width: 100%">
            <el-option v-for="g in gameList" :key="g.id" :label="g.gameName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基础价格">
          <el-input-number v-model="form.basePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计价单位">
          <el-select v-model="form.priceUnit" style="width: 100%">
            <el-option label="按小时" value="HOUR" />
            <el-option label="按单" value="ORDER" />
            <el-option label="按套餐" value="PACKAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务承诺">
          <el-input v-model="form.guaranteeText" placeholder="如：保底588万物资" />
        </el-form-item>
        <el-form-item label="退款政策">
          <el-input v-model="form.refundPolicy" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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

    <el-dialog v-model="priceDialogVisible" title="定价规则" width="600px">
      <el-table :data="priceRules" border stripe style="width: 100%">
        <el-table-column prop="levelName" label="陪玩师等级" width="130">
          <template #default="{ row }">{{ row.levelName || '通用' }}</template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: var(--gu-accent); font-weight: 600">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span v-if="row.originalPrice" style="text-decoration: line-through; color: var(--gu-text-muted)">¥{{ row.originalPrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editPriceRule(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deletePriceRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; text-align: right">
        <el-button type="primary" @click="addPriceRule">新增定价</el-button>
      </div>

      <el-dialog v-model="priceFormVisible" :title="priceFormTitle" width="400px" append-to-body>
        <el-form :model="priceForm" label-width="100px">
          <el-form-item label="陪玩师等级">
            <el-select v-model="priceForm.companionLevelId" clearable placeholder="通用" style="width: 100%">
              <el-option v-for="l in levelList" :key="l.id" :label="l.levelName" :value="l.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="价格" required>
            <el-input-number v-model="priceForm.price" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="原价(划线)">
            <el-input-number v-model="priceForm.originalPrice" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="priceFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPriceRule">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { serviceItemApi, gameConfigApi, clubConfigApi, companionLevelApi } from '@/api/index.js'

const loading = ref(false)
const serviceList = ref([])
const gameList = ref([])
const levelList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增服务')
const priceDialogVisible = ref(false)
const priceFormVisible = ref(false)
const priceFormTitle = ref('新增定价')
const currentServiceItemId = ref(null)
const priceRules = ref([])
const clubConfigId = ref(1)

const form = ref({ id: null, clubConfigId: 1, itemName: '', itemCode: '', category: 'TECHNICAL', gameConfigId: null, description: '', basePrice: null, priceUnit: 'HOUR', minDuration: null, guaranteeText: '', refundPolicy: '', sortOrder: 0, enabled: 1 })
const priceForm = ref({ id: null, serviceItemId: null, companionLevelId: null, price: null, originalPrice: null, priceUnit: 'HOUR', enabled: 1 })

const isAdmin = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}').role === 'SYS_ADMIN' } catch { return false }
})

const categoryLabel = (c) => ({ TECHNICAL: '技术陪玩', ENTERTAINMENT: '娱乐陪玩', PURE_PLAY: '纯陪玩', ESCORT: '护航服务', ACTIVITY: '活动玩法' }[c] || c)
const categoryTag = (c) => ({ TECHNICAL: 'danger', ENTERTAINMENT: 'warning', PURE_PLAY: 'success', ESCORT: 'info', ACTIVITY: 'primary' }[c] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await clubConfigApi.get()
    if (res.code === 200 && res.data) clubConfigId.value = res.data.id || 1
    form.value.clubConfigId = clubConfigId.value
    const [svcRes, gameRes, lvlRes] = await Promise.all([
      serviceItemApi.getByClubId(clubConfigId.value),
      gameConfigApi.getByClubId(clubConfigId.value),
      companionLevelApi.getAll()
    ])
    serviceList.value = svcRes.data || []
    gameList.value = gameRes.data || []
    levelList.value = lvlRes.data || []
  } catch (e) { ElMessage.error('加载失败') }
  loading.value = false
}

const handleAdd = () => {
  dialogTitle.value = '新增服务'
  form.value = { id: null, clubConfigId: clubConfigId.value, itemName: '', itemCode: '', category: 'TECHNICAL', gameConfigId: null, description: '', basePrice: null, priceUnit: 'HOUR', minDuration: null, guaranteeText: '', refundPolicy: '', sortOrder: 0, enabled: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑服务'
  const { id, clubConfigId, itemName, itemCode, category, gameConfigId, description, basePrice, priceUnit, minDuration, guaranteeText, refundPolicy, sortOrder, enabled } = row
  Object.assign(form.value, { id, clubConfigId, itemName, itemCode, category, gameConfigId, description, basePrice, priceUnit, minDuration, guaranteeText, refundPolicy, sortOrder, enabled })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.value.id) await serviceItemApi.update(form.value)
    else await serviceItemApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('保存失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此服务项目？关联的定价规则也会被删除', '提示', { type: 'warning' })
    await serviceItemApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error('删除失败')
  }
}

const handlePriceRules = async (row) => {
  currentServiceItemId.value = row.id
  try {
    const res = await serviceItemApi.getPriceRules(row.id)
    priceRules.value = res.data || []
  } catch { priceRules.value = [] }
  priceDialogVisible.value = true
}

const addPriceRule = () => {
  priceFormTitle.value = '新增定价'
  priceForm.value = { id: null, serviceItemId: currentServiceItemId.value, companionLevelId: null, price: null, originalPrice: null, priceUnit: 'HOUR', enabled: 1 }
  priceFormVisible.value = true
}

const editPriceRule = (row) => {
  priceFormTitle.value = '编辑定价'
  const { id, serviceItemId, companionLevelId, price, originalPrice, priceUnit, enabled } = row
  Object.assign(priceForm.value, { id, serviceItemId, companionLevelId, price, originalPrice, priceUnit, enabled })
  priceFormVisible.value = true
}

const submitPriceRule = async () => {
  try {
    await serviceItemApi.savePriceRule(priceForm.value)
    ElMessage.success('保存成功')
    priceFormVisible.value = false
    const res = await serviceItemApi.getPriceRules(currentServiceItemId.value)
    priceRules.value = res.data || []
  } catch (e) { ElMessage.error('保存失败') }
}

const deletePriceRule = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此定价规则？', '提示', { type: 'warning' })
    await serviceItemApi.deletePriceRule(row.id)
    const res = await serviceItemApi.getPriceRules(currentServiceItemId.value)
    priceRules.value = res.data || []
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.service-item-container {
  padding: 0;
}
</style>
