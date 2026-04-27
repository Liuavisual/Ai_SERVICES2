/**
 * Vue应用入口文件
 *
 * 初始化配置：
 * - Pinia: 状态管理
 * - Vue Router: 路由
 * - Element Plus: UI组件库（中文语言包）
 * - Element Plus Icons: 全局注册所有图标组件
 * - 全局样式: 古风主题CSS
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import './styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局注册Element Plus图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
