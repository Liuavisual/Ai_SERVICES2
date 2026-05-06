<!--
  根组件

  使用 ErrorBoundary 错误边界包裹路由视图，防止单个页面崩溃导致整个应用白屏。

  @author 刘建国
-->
<template>
  <ErrorBoundary>
    <router-view />
  </ErrorBoundary>
</template>

<script setup>
import ErrorBoundary from '@/components/ErrorBoundary.vue'
</script>
