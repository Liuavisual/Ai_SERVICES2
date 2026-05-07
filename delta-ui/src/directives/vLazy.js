/**
 * v-lazy 图片懒加载指令
 *
 * 基于 IntersectionObserver，图片进入视口时才加载真实src
 * 用法：<img v-lazy="'https://xxx.jpg'" />
 *
 * @author 刘建国
 */

const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const el = entry.target
        const realSrc = el.dataset.realSrc
        if (realSrc) {
          el.src = realSrc
          el.classList.add('v-lazy-loaded')
          el.removeAttribute('data-real-src')
        }
        observer.unobserve(el)
      }
    })
  },
  { rootMargin: '200px', threshold: 0.01 }
)

export default {
  mounted(el, binding) {
    const src = binding.value
    if (!src) return

    el.dataset.realSrc = src
    el.classList.add('v-lazy')

    const placeholder = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22%3E%3Crect fill=%22%23f0f0f0%22 width=%22100%22 height=%22100%22/%3E%3C/svg%3E'
    if (!el.src || el.src === window.location.href) {
      el.src = placeholder
    }

    observer.observe(el)
  },
  unmounted(el) {
    observer.unobserve(el)
  }
}
