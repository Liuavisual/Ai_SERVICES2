<!--
  异步组件加载包装器

  用于非路由场景下的异步组件加载，提供骨架屏fallback
  基于Vue3内置的 <Suspense> 实现

  @author 刘建国
-->
<template>
  <Suspense>
    <template #default>
      <slot name="default" />
    </template>
    <template #fallback>
      <slot name="fallback">
        <SkeletonBox :type="skeletonType" :rows="rows" />
      </slot>
    </template>
  </Suspense>
</template>

<script setup>
import SkeletonBox from './SkeletonBox.vue'

defineProps({
  skeletonType: { type: String, default: 'text' },
  rows: { type: Number, default: 5 }
})
</script>
