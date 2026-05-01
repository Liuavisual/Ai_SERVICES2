/**
 * Vue应用入口文件
 *
 * 初始化配置：
 * - Pinia: 状态管理
 * - Vue Router: 路由
 * - Element Plus: UI组件库（按需引入，由unplugin-auto-import和unplugin-vue-components自动处理）
 * - Element Plus Icons: 通过插件全局注册（模板中动态引用需要）
 * - Element Plus CSS: 全量导入（按需引入CSS有时不够完整）
 * - 全局样式: 古风主题CSS
 *
 * @author 刘建国
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'

import './styles/global.css'

import App from './App.vue'
import router from './router'
import { registerIcons } from './plugins/icons'

const passiveEvents = ['wheel', 'mousewheel', 'touchstart', 'touchmove']
const origAdd = EventTarget.prototype.addEventListener
EventTarget.prototype.addEventListener = function (type, listener, options) {
  if (passiveEvents.includes(type) && typeof listener === 'function') {
    if (typeof options === 'boolean') {
      options = { capture: options, passive: true }
    } else if (options && typeof options === 'object') {
      options = { ...options, passive: options.passive !== false }
    } else {
      options = { passive: true }
    }
  }
  return origAdd.call(this, type, listener, options)
}

const app = createApp(App)

// 通过插件注册Element Plus图标组件（模板中通过动态名称引用图标，需要全量注册）
registerIcons(app)

app.use(createPinia())
app.use(router)

app.mount('#app')
