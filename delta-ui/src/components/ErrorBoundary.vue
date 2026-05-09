<!--
  全局错误边界组件

  捕获子组件渲染和生命周期中的JavaScript错误，
  防止单个页面崩溃导致整个应用白屏。
  生产环境下记录错误日志，展示友好的错误提示页面。

  @author 刘建国
-->
<template>
  <slot v-if="!hasError" />
  <div v-else class="error-boundary">
    <div class="error-boundary-content">
      <el-icon :size="64" color="#909399">
        <WarningFilled />
      </el-icon>
      <h2>页面加载异常</h2>
      <p>{{ errorMessage }}</p>
      <el-button type="primary" @click="handleRetry">重新加载</el-button>
      <el-button @click="handleGoHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('')

onErrorCaptured((err, instance, info) => {
  hasError.value = true
  errorMessage.value = err?.message || '未知错误'

  console.error('[ErrorBoundary] 捕获到渲染错误:', {
    error: err?.message,
    stack: err?.stack,
    component: instance?.type?.name || '未知组件',
    info
  })

  return false
})

function handleRetry() {
  hasError.value = false
  errorMessage.value = ''
}

function handleGoHome() {
  hasError.value = false
  errorMessage.value = ''
  router.push('/dashboard')
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px;
}

.error-boundary-content {
  text-align: center;
  max-width: 400px;
}

.error-boundary-content h2 {
  margin: 16px 0 8px;
  font-size: 20px;
  color: #303133;
}

.error-boundary-content p {
  margin-bottom: 24px;
  color: #909399;
  font-size: 14px;
  word-break: break-all;
}
</style>
