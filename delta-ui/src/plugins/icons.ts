/**
 * Element Plus Icons 全局注册插件
 *
 * 由于项目中 MainLayout.vue 使用了动态图标引用：
 *   <component :is="menu.icon" />
 * 其中 menu.icon 是字符串名称（如 'DataLine'、'User'等），
 * 必须全量注册所有图标组件，否则动态引用无法解析。
 *
 * 将图标注册逻辑从 main.js 抽离到独立插件文件，
 * 便于后续维护和优化（如改为按需注册）。
 *
 * @author 刘建国
 */
import type { App } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

/**
 * 全局注册所有 Element Plus 图标组件
 *
 * 遍历 @element-plus/icons-vue 包中的所有图标组件，
 * 逐个注册为全局组件，使模板中可通过字符串名称动态引用。
 *
 * @param {App} app - Vue应用实例
 */
export function registerIcons(app: App) {
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }
}
