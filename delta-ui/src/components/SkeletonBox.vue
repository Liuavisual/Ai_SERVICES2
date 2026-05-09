<!--
  骨架屏占位组件

  支持三种骨架形状：
  - text：文本行骨架
  - card：卡片骨架
  - table：表格骨架
  - form：表单骨架

  页面切换时作为路由组件的fallback内容展示，避免白屏

  @author 刘建国
-->
<template>
  <div
    class="skeleton-box"
    :class="[`skeleton-${type}`, { 'skeleton-animated': animated }]"
  >
    <!-- 文本骨架 -->
    <template v-if="type === 'text'">
      <div
        v-for="i in rows"
        :key="i"
        class="skeleton-line"
        :class="{ 'skeleton-line--short': lastRowShorter && i === rows }"
      />
    </template>

    <!-- 卡片骨架 -->
    <template v-if="type === 'card'">
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-card"
      >
        <div class="skeleton-card-image" />
        <div class="skeleton-card-body">
          <div class="skeleton-line skeleton-line--title" />
          <div class="skeleton-line" />
          <div class="skeleton-line skeleton-line--short" />
        </div>
      </div>
    </template>

    <!-- 表格骨架 -->
    <template v-if="type === 'table'">
      <div class="skeleton-table-header">
        <div
          v-for="i in columns"
          :key="i"
          class="skeleton-line skeleton-cell"
        />
      </div>
      <div
        v-for="i in rows"
        :key="i"
        class="skeleton-table-row"
      >
        <div
          v-for="j in columns"
          :key="j"
          class="skeleton-line skeleton-cell"
          :style="getCellStyle(j)"
        />
      </div>
    </template>

    <!-- 表单骨架 -->
    <template v-if="type === 'form'">
      <div
        v-for="i in rows"
        :key="i"
        class="skeleton-form-group"
      >
        <div class="skeleton-line skeleton-label" />
        <div class="skeleton-line skeleton-input" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'text', validator: (v: string) => ['text', 'card', 'table', 'form'].includes(v) },
  rows: { type: Number, default: 5 },
  count: { type: Number, default: 3 },
  columns: { type: Number, default: 5 },
  animated: { type: Boolean, default: true },
  lastRowShorter: { type: Boolean, default: true }
})

/**
 * 预生成表格列的随机宽度，避免模板中 Math.random() 导致每次渲染都重新计算
 * Math.random() 在模板中会在每次组件更新时触发，导致不必要的重渲染
 */
const cellWidths = computed(() => {
  const widths: Record<number, string> = {}
  for (let i = 1; i <= props.columns; i++) {
    widths[i] = `${70 + Math.random() * 30}%`
  }
  return widths
})

const getCellStyle = (index: number) => {
  return { width: cellWidths.value[index] || '100%' }
}
</script>

<style scoped>
.skeleton-box {
  padding: 16px;
  width: 100%;
}

.skeleton-line {
  height: 14px;
  background: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-line--short {
  width: 60%;
}

.skeleton-line--title {
  width: 40%;
  height: 18px;
}

/* 卡片骨架 */
.skeleton-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.skeleton-card-image {
  height: 160px;
  background: #f5f5f5;
}

.skeleton-card-body {
  padding: 12px 16px;
}

/* 表格骨架 */
.skeleton-table-header,
.skeleton-table-row {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.skeleton-table-header .skeleton-cell {
  height: 16px;
  background: #e4e7ed;
  flex: 1;
}

.skeleton-table-row .skeleton-cell {
  height: 14px;
  background: #f5f5f5;
  flex: 1;
  min-width: 80px;
}

/* 表单骨架 */
.skeleton-form-group {
  margin-bottom: 20px;
}

.skeleton-label {
  width: 80px;
  height: 14px;
  margin-bottom: 8px;
  background: #e4e7ed;
}

.skeleton-input {
  height: 36px;
  background: #f5f5f5;
  border-radius: 4px;
  max-width: 400px;
}

/* 动画 */
.skeleton-animated .skeleton-line,
.skeleton-animated .skeleton-card-image,
.skeleton-animated .skeleton-cell,
.skeleton-animated .skeleton-input {
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 37%, #f0f0f0 63%);
  background-size: 400% 100%;
}

@keyframes skeleton-shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}
</style>
