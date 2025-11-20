import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { createI18n } from 'vue-i18n'
import App from './App.vue'
import router from './router'

// PrimeVue styles
import 'primevue/resources/themes/lara-light-blue/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'

// Tailwind CSS
import './assets/main.css'

const app = createApp(App)

// Pinia state management
const pinia = createPinia()
app.use(pinia)

// Vue Router
app.use(router)

// PrimeVue UI library
app.use(PrimeVue)

// i18n multi-language
const i18n = createI18n({
  legacy: false,
  locale: 'zh-TW',
  fallbackLocale: 'en',
  messages: {}
})
app.use(i18n)

app.mount('#app')
