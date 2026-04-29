<!--
  平台配置页面，管理各平台接入参数

  @author delta
-->
<template>
  <div class="page-container">
    <el-card class="table-card">
      <template #header>
        <span>平台配置</span>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="platform" label="平台" width="120">
          <template #default="{ row }">
            <el-tag :type="getPlatformTagType(row.platform)">{{ getPlatformText(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="config" label="配置" show-overflow-tooltip min-width="200">
          <template #default="{ row }">
            <el-input
              :model-value="formatConfig(row.config)"
              type="textarea"
              :rows="2"
              readonly
              style="font-size: 12px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑平台配置" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="平台">
          <el-tag :type="getPlatformTagType(formData.platform)">{{ getPlatformText(formData.platform) }}</el-tag>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch 
            id="platformConfig-enabled"
            v-model="formData.enabled" 
            name="enabled"
          />
        </el-form-item>
        <el-form-item label="配置JSON">
          <el-input
            id="platformConfig-config"
            v-model="configJson"
            type="textarea"
            :rows="12"
            placeholder='请输入JSON配置，例如: {"appId": "xxx", "secret": "xxx"}'
            name="config"
          />
          <div class="config-hint" v-if="platformHints[formData.platform]">
            <div class="hint-title">配置示例：</div>
            <div class="hint-content">{{ platformHints[formData.platform] }}</div>
          </div>
          <div v-if="jsonError" class="json-error">{{ jsonError }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { platformConfigApi } from '@/api'
import type { Result } from '@/types'

interface PlatformConfigRow {
  id: number
  platform: string
  enabled: boolean
  config: Record<string, any>
  updatedAt: string
}

const loading = ref<boolean>(false)
const submitLoading = ref<boolean>(false)
const tableData = ref<PlatformConfigRow[]>([])
const dialogVisible = ref<boolean>(false)
const jsonError = ref<string>('')

const platformHints: Record<string, string> = {
  wechat: `{"appId": "微信AppID", "appSecret": "微信AppSecret", "token": "Token", "aesKey": "AES密钥"}`,
  kook: `{"botToken": "KOOK机器人Token"}`,
  yy: `{"username": "YY账号", "password": "YY密码"}`
}

const formData = ref<{
  id: number | null
  platform: string
  enabled: boolean
  config: Record<string, any>
}>({
  id: null,
  platform: '',
  enabled: true,
  config: {}
})

const configJson = computed({
  get: (): string => {
    try {
      return JSON.stringify(formData.value.config, null, 2)
    } catch (e) {
      return ''
    }
  },
  set: (val: string): void => {
    jsonError.value = ''
    if (!val || val.trim() === '') {
      formData.value.config = {}
      return
    }
    try {
      formData.value.config = JSON.parse(val)
    } catch (e) {
      jsonError.value = 'JSON格式错误，请检查'
    }
  }
})

const getPlatformText = (platform: string): string => {
  const map: Record<string, string> = {
    'wechat': '微信',
    'kook': 'KOOK',
    'yy': 'YY'
  }
  return map[platform] || platform
}

const getPlatformTagType = (platform: string): string => {
  const map: Record<string, string> = {
    'wechat': 'primary',
    'kook': 'success',
    'yy': 'warning'
  }
  return map[platform] || 'info'
}

const formatConfig = (config: Record<string, any>): string => {
  if (!config) return ''
  try {
    return JSON.stringify(config, null, 2)
  } catch (e) {
    return ''
  }
}

const fetchData = async (): Promise<void> => {
  loading.value = true
  try {
    const res: Result<PlatformConfigRow[]> = await platformConfigApi.getAll()
    if (res.code === 200) {
      tableData.value = res.data
    }
  } catch (error) {
    console.error('获取平台配置失败', error)
    ElMessage.error('获取平台配置失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (row: PlatformConfigRow): void => {
  formData.value = { ...row }
  jsonError.value = ''
  dialogVisible.value = true
}

const handleSubmit = async (): Promise<void> => {
  if (jsonError.value) {
    ElMessage.error('请先修复JSON格式错误')
    return
  }
  submitLoading.value = true
  try {
    const res: Result<null> = await platformConfigApi.update(formData.value)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchData()
    }
  } catch (error) {
    console.error('更新失败', error)
    ElMessage.error('更新失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.table-card {
  margin-bottom: 16px;
}

.config-hint {
  margin-top: 10px;
  padding: 10px;
  background-color: var(--gu-bg-stripe);
  border-radius: var(--gu-radius);
}

.hint-title {
  font-weight: bold;
  color: var(--gu-text-muted);
  margin-bottom: 5px;
}

.hint-content {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: var(--gu-text-secondary);
  word-break: break-all;
}

.json-error {
  margin-top: 5px;
  color: var(--gu-danger);
  font-size: 12px;
}
</style>
