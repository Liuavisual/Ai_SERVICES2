<!--
  可复用加载遮罩组件

  提供统一的加载状态展示，支持：
  - 全屏遮罩模式 / 内嵌模式
  - 自定义加载文本
  - 透明/非透明背景

  @author 刘建国
-->
<template>
  <div v-if="visible" class="loading-overlay" :class="{ 'is-fullscreen': fullscreen, 'is-transparent': transparent }">
    <div class="loading-overlay-inner">
      <el-icon class="loading-icon is-loading" :size="iconSize">
        <Loading />
      </el-icon>
      <span v-if="text" class="loading-text">{{ text }}</span>
    </div>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'

defineProps({
  visible: { type: Boolean, default: false },
  fullscreen: { type: Boolean, default: true },
  transparent: { type: Boolean, default: false },
  text: { type: String, default: '加载中...' },
  iconSize: { type: Number, default: 40 }
})
</script>

<style scoped>
.loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.85);
  z-index: 2000;
}

.loading-overlay.is-fullscreen {
  position: fixed;
  inset: 0;
}

.loading-overlay.is-transparent {
  background-color: rgba(255, 255, 255, 0.3);
}

.loading-overlay:not(.is-fullscreen) {
  position: absolute;
  inset: 0;
  border-radius: inherit;
}

.loading-overlay-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-icon {
  color: #409eff;
}

.loading-text {
  color: #909399;
  font-size: 14px;
}
</style>
