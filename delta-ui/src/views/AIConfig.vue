<!--
  AI配置页面，管理DeepSeek模型参数和系统提示词

  @author delta
-->
<template>
  <div class="ai-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI配置</span>
          <el-button type="primary" @click="handleSave" :loading="loading">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
        </div>
      </template>

      <el-form :model="configForm" label-width="200px" style="max-width: 800px">
        <el-divider content-position="left">DeepSeek API 配置</el-divider>

        <el-form-item label="启用AI">
          <el-switch
            v-model="configForm['deepseek.enabled']"
            :active-value="'true'"
            :inactive-value="'false'"
          />
          <div class="form-tip">开启后AI将自动回复客户消息</div>
        </el-form-item>

        <el-form-item label="API Key">
          <el-input
            v-model="configForm['deepseek.api_key']"
            type="password"
            show-password
            placeholder="请输入 DeepSeek API Key"
          />
          <div class="form-tip">
            获取地址：<a href="https://platform.deepseek.com/" target="_blank">https://platform.deepseek.com/</a>
          </div>
        </el-form-item>

        <el-form-item label="API Base URL">
          <el-input 
            v-model="configForm['deepseek.base_url']" 
            placeholder="请输入 API Base URL" 
          />
        </el-form-item>

        <el-form-item label="模型名称">
          <el-select 
            v-model="configForm['deepseek.model']" 
            placeholder="请选择模型" 
            style="width: 100%" 
          >
            <el-option label="deepseek-chat" value="deepseek-chat" />
            <el-option label="deepseek-coder" value="deepseek-coder" />
          </el-select>
        </el-form-item>

        <el-form-item label="温度参数 (Temperature)">
          <el-slider
            v-model="temperatureValue"
            :min="0"
            :max="2"
            :step="0.1"
            :show-tooltip="true"
          />
          <div class="form-tip">值越小越确定，值越大越有创意，建议 0.7</div>
        </el-form-item>

        <el-form-item label="最大 Token 数">
          <el-input-number
            v-model="maxTokensValue"
            :min="100"
            :max="4000"
            :step="100"
          />
        </el-form-item>

        <el-form-item label="系统提示词">
          <el-input
            v-model="configForm['deepseek.system_prompt']"
            type="textarea"
            :rows="4"
            placeholder="请输入系统提示词"
          />
          <div class="form-tip">设置 AI 的角色和行为准则</div>
        </el-form-item>

        <el-divider content-position="left">使用统计</el-divider>

        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">0</div>
              <div class="stat-label">今日调用次数</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">0</div>
              <div class="stat-label">今日 Token 使用</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">0</div>
              <div class="stat-label">本月调用次数</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">0</div>
              <div class="stat-label">本月 Token 使用</div>
            </el-card>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { aiConfigApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import type { Result } from '@/types'

interface AiConfigItem {
  id: number
  configKey: string
  configValue: string
  description: string
}

const loading = ref<boolean>(false)
const originalConfigs = ref<AiConfigItem[]>([])
const configForm = ref<Record<string, string>>({})

const temperatureValue = computed<number>({
  get: (): number => {
    const val = configForm.value['deepseek.temperature']
    return val !== undefined && val !== null && val !== '' ? parseFloat(val) : 0.7
  },
  set: (val: number): void => {
    configForm.value['deepseek.temperature'] = String(val)
  }
})

const maxTokensValue = computed<number>({
  get: (): number => {
    const val = configForm.value['deepseek.max_tokens']
    return val !== undefined && val !== null && val !== '' ? parseInt(val) : 2000
  },
  set: (val: number): void => {
    configForm.value['deepseek.max_tokens'] = String(val)
  }
})

const loadConfigs = async (): Promise<void> => {
  try {
    const res: Result<AiConfigItem[]> = await aiConfigApi.getAll()
    if (res.code === 200) {
      originalConfigs.value = res.data
      const newConfigForm: Record<string, string> = {}
      res.data.forEach(config => {
        newConfigForm[config.configKey] = config.configValue
      })
      configForm.value = newConfigForm
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
    console.error('加载配置失败', error)
  }
}

const handleSave = async (): Promise<void> => {
  loading.value = true
  try {
    const updates = Object.keys(configForm.value)
      .filter(key => {
        const original = getOriginalValue(key)
        const current = configForm.value[key]
        return current !== original
      })
      .map(key => ({
        configKey: key,
        configValue: configForm.value[key]
      }))

    if (updates.length === 0) {
      ElMessage.info('没有需要保存的配置')
      return
    }

    const res: Result<null> = await aiConfigApi.update({ updates })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      loadConfigs()
    }
  } catch (error) {
    ElMessage.error('保存失败')
    console.error('保存失败', error)
  } finally {
    loading.value = false
  }
}

const getOriginalValue = (key: string): string | undefined => {
  const config = originalConfigs.value.find(c => c.configKey === key)
  return config ? config.configValue : undefined
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: var(--gu-text-muted);
  margin-top: 5px;
}

.form-tip a {
  color: var(--gu-accent);
}

.stat-card {
  text-align: center;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--gu-accent);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--gu-text-muted);
}
</style>
