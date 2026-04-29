import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

/**
 * Vite构建配置
 *
 * 插件配置：
 * - vue: Vue3单文件组件支持
 * - AutoImport: Element Plus API自动按需导入
 * - Components: Element Plus组件自动按需导入（含中文语言包）
 *
 * @author 刘建国
 */
export default defineConfig({
  plugins: [
    vue(),
    // Element Plus API自动导入（如ElMessage, ElMessageBox等）
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    // Element Plus组件自动按需导入，配置中文语言包
    Components({
      resolvers: [
        ElementPlusResolver({
          locale: zhCn,
        }),
      ],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
