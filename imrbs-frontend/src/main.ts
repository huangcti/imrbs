import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setupI18n } from './i18n'

// Tailwind CSS
import './assets/main.css'

// 啟動應用程式
async function bootstrap() {
  const app = createApp(App)

  // Pinia state management
  const pinia = createPinia()
  app.use(pinia)

  // Vue Router
  app.use(router)

  // i18n multi-language (with browser language detection)
  const i18n = await setupI18n()
  app.use(i18n)

  app.mount('#app')
}

bootstrap().catch(console.error)

