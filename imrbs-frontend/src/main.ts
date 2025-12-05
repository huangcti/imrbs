import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setupI18n } from './i18n'
import { useAuthStore } from './stores/auth'

// Tailwind CSS
import './assets/main.css'

// 啟動應用程式
async function bootstrap() {
  const app = createApp(App)

  // Pinia state management
  const pinia = createPinia()
  app.use(pinia)

  // 初始化認證狀態 (必須在 router 之前)
  const authStore = useAuthStore()
  authStore.restoreAuth()
  
  console.log('🔐 Auth initialized:', {
    isAuthenticated: authStore.isAuthenticated,
    user: authStore.user?.fullName,
    roles: authStore.userRoles
  })

  // Vue Router
  app.use(router)

  // i18n multi-language (with browser language detection)
  const i18n = await setupI18n()
  app.use(i18n)

  app.mount('#app')
}

bootstrap().catch(console.error)

