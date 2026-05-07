/**
 * 应用入口文件
 *
 * 负责创建Vue应用实例、注册全局插件和组件、挂载应用
 *
 * @author 刘建国
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import vLazy from './directives/vLazy'
import ErrorBoundary from './components/ErrorBoundary.vue'

import './styles/global.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.directive('lazy', vLazy)
app.component('ErrorBoundary', ErrorBoundary)

app.mount('#app')
