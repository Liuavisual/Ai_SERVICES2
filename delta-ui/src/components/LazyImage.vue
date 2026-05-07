<!--
  图片懒加载指令 v-lazy

  使用 IntersectionObserver 实现图片进入视口时才加载
  支持占位图和加载失败时的降级图

  用法：
  <img v-lazy="'https://example.com/image.jpg'" />
  <img v-lazy="{ src: 'url', placeholder: 'loading.gif', fallback: 'error.png' }" />

  @author 刘建国
-->
<template>
  <img
    v-if="loaded && !error"
    :src="actualSrc"
    :alt="alt"
    :class="imgClass"
    :style="imgStyle"
  />
  <img
    v-else-if="placeholderSrc && !error"
    :src="placeholderSrc"
    :alt="alt"
    class="v-lazy-placeholder"
    :style="imgStyle"
  />
  <img
    v-else-if="error && fallbackSrc"
    :src="fallbackSrc"
    :alt="alt"
    class="v-lazy-fallback"
    :style="imgStyle"
  />
  <div
    v-else
    class="v-lazy-skeleton"
    :class="{ 'v-lazy-skeleton--error': error }"
    :style="imgStyle"
  />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  src: { type: [String, Object], required: true },
  alt: { type: String, default: '' },
  imgClass: { type: [String, Object, Array], default: '' },
  imgStyle: { type: Object, default: () => ({}) }
})

const loaded = ref(false)
const error = ref(false)
const actualSrc = ref('')
const placeholderSrc = ref('')
const fallbackSrc = ref('')

let observer = null

onMounted(() => {
  const config = typeof props.src === 'object' ? props.src : { src: props.src }
  actualSrc.value = config.src
  placeholderSrc.value = config.placeholder || ''
  fallbackSrc.value = config.fallback || ''

  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        loadImage()
        observer.disconnect()
      }
    },
    { rootMargin: '100px', threshold: 0.01 }
  )

  observer.observe(document.querySelector('.v-lazy-skeleton, .v-lazy-placeholder') || document.body)
  setTimeout(() => loadImage(), 50)
})

onUnmounted(() => {
  observer?.disconnect()
})

function loadImage() {
  const img = new Image()
  img.onload = () => {
    loaded.value = true
  }
  img.onerror = () => {
    error.value = true
  }
  img.src = actualSrc.value
}
</script>

<style scoped>
.v-lazy-placeholder,
.v-lazy-fallback {
  object-fit: cover;
  opacity: 0.7;
}

.v-lazy-skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 37%, #f0f0f0 63%);
  background-size: 400% 100%;
  animation: v-lazy-shimmer 1.5s ease-in-out infinite;
  min-height: 40px;
  border-radius: 4px;
}

.v-lazy-skeleton--error {
  background: #fef0f0;
  animation: none;
}

@keyframes v-lazy-shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}
</style>
