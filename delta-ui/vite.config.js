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
 * 优化策略：
 * - 手动代码分割：将vue/el-plus/工具库独立chunk，提高缓存命中率
 * - Terser压缩：移除console/debugger，减少包体积
 * - 预加载：自动注入modulepreload链接
 * - Element Plus按需导入：组件+API自动导入，减少CSS体积
 *
 * @author 刘建国
 */
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [
        ElementPlusResolver({
          locale: zhCn,
          importStyle: false
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
  },
  build: {
    target: 'es2020',
    cssMinify: 'esbuild',
    rollupOptions: {
      output: {
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'el-vendor': ['element-plus', '@element-plus/icons-vue'],
          'util-vendor': ['axios']
        }
      }
    },
    chunkSizeWarningLimit: 500,
    modulePreload: {
      polyfill: true
    }
  }
})
