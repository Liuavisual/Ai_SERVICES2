<!--
  页面加载进度条组件

  在页面切换时于顶部显示细长的渐变色进度条，替代静止的 spinner
  通过 inject/provide 与 MainLayout 中的路由导航事件联动

  @author 刘建国
-->
<template>
  <div class="page-progress" :class="{ 'is-active': active }">
    <div class="page-progress-bar" :style="{ width: percent + '%', transitionDuration: duration + 'ms' }" />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const active = ref(false)
const percent = ref(0)
const duration = ref(800)

let timer = null

/**
 * 启动进度条动画
 * 模拟真实加载进度：快启动 → 缓慢增长 → 等待完成
 */
function start() {
  clearInterval(timer)
  active.value = true
  percent.value = 0
  duration.value = 800

  let step = 0
  timer = setInterval(() => {
    step += 1
    if (step <= 10) {
      percent.value = step * 5
    } else if (step <= 30) {
      percent.value = 50 + ((step - 10) * 1.5)
    } else {
      percent.value = 80 + ((step - 30) * 0.5)
    }

    if (percent.value >= 92) {
      clearInterval(timer)
      timer = null
    }
  }, 100)
}

/**
 * 完成进度条，快速拉到100%后隐藏
 */
function done() {
  clearInterval(timer)
  timer = null
  percent.value = 100
  duration.value = 200
  setTimeout(() => {
    active.value = false
    percent.value = 0
  }, 250)
}

defineExpose({ start, done })
</script>

<style scoped>
.page-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  height: 3px;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.page-progress.is-active {
  opacity: 1;
}

.page-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a, #e6a23c);
  border-radius: 0 2px 2px 0;
  transition: width linear;
  box-shadow: 0 1px 3px rgba(64, 158, 255, 0.3);
}
</style>
