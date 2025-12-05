<template>
  <div class="relative" ref="dropdownRef">
    <!-- 語言切換按鈕 -->
    <button
      data-cy="language-switcher"
      @click="toggleDropdown"
      class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary-500"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"
        />
      </svg>
      <span>{{ currentLanguageLabel }}</span>
      <svg
        class="w-4 h-4 transition-transform duration-200"
        :class="{ 'rotate-180': isOpen }"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <!-- 下拉選單 -->
    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        class="absolute right-0 z-50 mt-2 w-40 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"
      >
        <div class="py-1">
          <button
            v-for="lang in languages"
            :key="lang.code"
            :data-cy="`lang-option-${lang.code}`"
            @click="switchLanguage(lang.code)"
            class="flex items-center w-full px-4 py-2 text-sm text-left hover:bg-gray-100"
            :class="{ 'bg-primary-50 text-primary-700': locale === lang.code }"
          >
            <span class="mr-2">{{ lang.flag }}</span>
            <span>{{ lang.label }}</span>
            <svg
              v-if="locale === lang.code"
              class="w-4 h-4 ml-auto text-primary-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'

// i18n
const { locale, t } = useI18n()
const { showSuccess, showError } = useToast()
const authStore = useAuthStore()

// 狀態
const isOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

// 支援的語言清單
const languages = [
  { code: 'zh-TW', label: '繁體中文', flag: '🇹🇼' },
  { code: 'en', label: 'English', flag: '🇺🇸' }
]

// 計算當前語言標籤
const currentLanguageLabel = computed(() => {
  const currentLang = languages.find((l) => l.code === locale.value)
  return currentLang?.label || '繁體中文'
})

// 切換下拉選單
const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

// 切換語言
const switchLanguage = async (langCode: string) => {
  if (locale.value === langCode) {
    isOpen.value = false
    return
  }

  try {
    // 更新本地語言設定
    locale.value = langCode
    localStorage.setItem('locale', langCode)

    // 如果已登入，同步至後端
    if (authStore.isAuthenticated) {
      await syncLanguageToBackend(langCode)
    }

    // 顯示成功訊息
    showSuccess(t('language.switchSuccess'))
  } catch (error) {
    console.error('語言切換失敗:', error)
    showError(t('language.switchFailed'))
  } finally {
    isOpen.value = false
  }
}

// 同步語言偏好至後端
const syncLanguageToBackend = async (langCode: string) => {
  try {
    const apiClient = (await import('@/services/api')).default
    await apiClient.put('/users/me/language', { language: langCode })
  } catch (error) {
    // 後端同步失敗不影響前端切換
    console.warn('語言偏好同步至後端失敗:', error)
  }
}

// 點擊外部關閉下拉選單
const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

// 初始化語言設定
const initializeLanguage = () => {
  // 優先使用 localStorage 中的設定
  const savedLocale = localStorage.getItem('locale')
  if (savedLocale && languages.some((l) => l.code === savedLocale)) {
    locale.value = savedLocale
    return
  }

  // 其次使用瀏覽器語言設定
  const browserLang = navigator.language
  if (browserLang.startsWith('zh')) {
    locale.value = 'zh-TW'
  } else if (browserLang.startsWith('en')) {
    locale.value = 'en'
  } else {
    // 預設繁體中文
    locale.value = 'zh-TW'
  }
}

// 生命週期
onMounted(() => {
  initializeLanguage()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
/* 確保下拉選單在其他元素上方 */
.relative {
  z-index: 40;
}
</style>
