<!--
  俱乐部配置页面，管理俱乐部信息和等级定价

  @author delta
-->
<template>
  <div class="club-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>俱乐部配置</span>
          <el-button type="primary" @click="handleSave" :loading="loading">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
        </div>
      </template>

      <el-form :model="configForm" label-width="150px" style="max-width: 900px">
        <el-divider content-position="left">基础信息</el-divider>
        
        <el-form-item label="俱乐部名称">
          <el-input v-model="configForm.clubName" placeholder="请输入俱乐部名称" />
        </el-form-item>

        <el-form-item label="俱乐部Logo">
          <el-input v-model="configForm.clubLogo" placeholder="请输入Logo URL" />
        </el-form-item>

        <el-form-item label="主营游戏">
          <el-input v-model="configForm.mainGames" placeholder="请输入主营游戏，多个用逗号分隔" />
        </el-form-item>

        <el-form-item label="服务口号">
          <el-input v-model="configForm.serviceSlogan" placeholder="请输入服务口号" />
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input v-model="configForm.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>

        <el-divider content-position="left">价格配置</el-divider>

        <el-alert
          title="价格配置说明"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <div>
              <p>• 此页面配置的是陪玩师等级的参考基准价格</p>
              <p>• 每个陪玩师的具体价格可在「陪玩师管理」页面进行个性化设置</p>
              <p>• 价格最小单位为0.01元，可直接手动输入精确价格</p>
              <p>• 价格配置会根据「陪玩师等级管理」中启用的等级自动生成</p>
            </div>
          </template>
        </el-alert>

        <el-row :gutter="20">
          <el-col :span="12" v-for="(priceItem, index) in configForm.levelPrices" :key="priceItem.levelId">
            <el-form-item :label="`${priceItem.levelName}基准价`">
              <el-input-number 
                v-model="priceItem.price" 
                :min="0" 
                :precision="2" 
                :step="0.01"
                controls-position="right"
                style="width: 100%" 
              />
              <span style="margin-left: 10px">元/小时</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">特色介绍</el-divider>

        <el-form-item label="俱乐部特色">
          <el-input
            v-model="configForm.clubFeatures"
            type="textarea"
            :rows="4"
            placeholder="请输入俱乐部特色介绍"
          />
        </el-form-item>

        <el-form-item label="自定义欢迎语">
          <el-input
            v-model="configForm.welcomeMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入自定义欢迎语（可选）"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { clubConfigApi } from '@/api'

const loading = ref(false)
const configForm = ref({
  clubName: '',
  clubLogo: '',
  mainGames: '',
  serviceSlogan: '',
  welcomeMessage: '',
  contactInfo: '',
  clubFeatures: '',
  levelPrices: []
})

const loadConfig = async () => {
  try {
    const res = await clubConfigApi.get()
    if (res.data) {
      configForm.value = {
        clubName: res.data.clubName || '',
        clubLogo: res.data.clubLogo || '',
        mainGames: res.data.mainGames || '',
        serviceSlogan: res.data.serviceSlogan || '',
        welcomeMessage: res.data.welcomeMessage || '',
        contactInfo: res.data.contactInfo || '',
        clubFeatures: res.data.clubFeatures || '',
        levelPrices: res.data.levelPrices || []
      }
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
    console.error('加载配置失败', error)
  }
}

const handleSave = async () => {
  loading.value = true
  try {
    await clubConfigApi.update(configForm.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error('保存失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
</style>
