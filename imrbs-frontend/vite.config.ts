import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    // 代碼分割優化
    rollupOptions: {
      output: {
        // 手動分割 chunks
        manualChunks: {
          // Vue 核心庫
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // i18n 支援
          'i18n': ['vue-i18n'],
          // 日期處理
          'date-utils': ['date-fns'],
          // 圖標庫 (若有使用)
          // 'icons': ['@heroicons/vue']
        },
        // 優化 chunk 檔案命名
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          // CSS 檔案
          if (assetInfo.name && assetInfo.name.endsWith('.css')) {
            return 'assets/css/[name]-[hash][extname]'
          }
          // 字體檔案
          if (assetInfo.name && /\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name)) {
            return 'assets/fonts/[name]-[hash][extname]'
          }
          // 圖片檔案
          if (assetInfo.name && /\.(png|jpe?g|gif|svg|webp|ico)$/.test(assetInfo.name)) {
            return 'assets/images/[name]-[hash][extname]'
          }
          return 'assets/[name]-[hash][extname]'
        }
      }
    },
    // 設定 chunk 大小警告閾值
    chunkSizeWarningLimit: 500,
    // 啟用 CSS 代碼分割
    cssCodeSplit: true,
    // 啟用 sourcemap 用於生產環境除錯 (可依需求關閉)
    sourcemap: false,
    // 最小化設定
    minify: 'terser',
    terserOptions: {
      compress: {
        // 移除 console.log (生產環境)
        drop_console: true,
        drop_debugger: true
      }
    }
  },
  // 預載入優化
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'vue-i18n']
  }
})
