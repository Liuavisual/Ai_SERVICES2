<template>
  <div class="game-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>游戏配置</span>
          <el-button type="primary" @click="handleAdd" v-if="isAdmin">
            <el-icon><Plus /></el-icon>
            新增游戏
          </el-button>
        </div>
      </template>

      <el-table :data="gameList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="gameName" label="游戏名称" min-width="130" />
        <el-table-column prop="gameCode" label="游戏编码" width="140" />
        <el-table-column prop="gameType" label="游戏类型" width="110">
          <template #default="{ row }">
            <el-tag :type="gameTypeTag(row.gameType)" size="small">{{ gameTypeLabel(row.gameType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="150" />
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="150" v-if="isAdmin">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="游戏名称" required>
          <el-input v-model="form.gameName" placeholder="如：三角洲行动" />
        </el-form-item>
        <el-form-item label="游戏编码" required>
          <el-input v-model="form.gameCode" placeholder="如：DELTA_FORCE" />
        </el-form-item>
        <el-form-item label="游戏类型">
          <el-select v-model="form.gameType" style="width: 100%">
            <el-option label="FPS射击" value="FPS" />
            <el-option label="MOBA竞技" value="MOBA" />
            <el-option label="大逃杀" value="BR" />
            <el-option label="RPG角色" value="RPG" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
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

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { gameConfigApi, clubConfigApi } from '@/api/index.js'
import { authStorage } from '@/utils/storage'
import type { Result, GameConfigVO } from '@/types'

const loading = ref<boolean>(false)
const gameList = ref<GameConfigVO[]>([])
const dialogVisible = ref<boolean>(false)
const dialogTitle = ref<string>('新增游戏')
const clubConfigId = ref<number>(1)
const form = ref<Record<string, any>>({ id: null, clubConfigId: 1, gameName: '', gameCode: '', gameType: 'FPS', description: '', sortOrder: 0, enabled: 1 })

const isAdmin = computed<boolean>(() => {
  return authStorage.getUserInfo().role === 'SYS_ADMIN'
})

const gameTypeLabel = (t: string): string => ({ FPS: 'FPS射击', MOBA: 'MOBA竞技', BR: '大逃杀', RPG: 'RPG角色' }[t] || t)
const gameTypeTag = (t: string): string => ({ FPS: 'danger', MOBA: 'warning', BR: 'success', RPG: 'info' }[t] || '')

const loadData = async (): Promise<void> => {
  loading.value = true
  try {
    const res: Result<any> = await clubConfigApi.get()
    if (res.code === 200 && res.data) clubConfigId.value = res.data.id || 1
    form.value.clubConfigId = clubConfigId.value
    const result: Result<GameConfigVO[]> = await gameConfigApi.getByClubId(String(clubConfigId.value))
    gameList.value = result.data || []
  } catch (e) { ElMessage.error('加载失败') }
  loading.value = false
}

const handleAdd = (): void => {
  dialogTitle.value = '新增游戏'
  form.value = { id: null, clubConfigId: clubConfigId.value, gameName: '', gameCode: '', gameType: 'FPS', description: '', sortOrder: 0, enabled: 1 }
  dialogVisible.value = true
}

const handleEdit = (row: GameConfigVO): void => {
  dialogTitle.value = '编辑游戏'
  const { id, clubConfigId, gameName, gameCode, gameType, description, sortOrder, enabled } = row
  Object.assign(form.value, { id, clubConfigId, gameName, gameCode, gameType, description, sortOrder, enabled })
  dialogVisible.value = true
}

const handleSubmit = async (): Promise<void> => {
  try {
    if (form.value.id) await gameConfigApi.update(form.value)
    else await gameConfigApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('保存失败') }
}

const handleDelete = async (row: GameConfigVO): Promise<void> => {
  try {
    await ElMessageBox.confirm('确定删除此游戏配置？', '提示', { type: 'warning' })
    await gameConfigApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.game-config-container {
  padding: 0;
}
</style>
