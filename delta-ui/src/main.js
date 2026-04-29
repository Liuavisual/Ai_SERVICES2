/**
 * Vue应用入口文件
 *
 * 初始化配置：
 * - Pinia: 状态管理
 * - Vue Router: 路由
 * - Element Plus: UI组件库（按需引入，由unplugin-auto-import和unplugin-vue-components自动处理）
 * - Element Plus Icons: 全局注册所有图标组件（模板中动态引用需要）
 * - Element Plus CSS: 全量导入（按需引入CSS有时不够完整）
 * - 全局样式: 古风主题CSS
 *
 * @author 刘建国
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import './styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局注册Element Plus图标组件（模板中通过动态名称引用图标，需要全量注册）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

app.mount('#app')
