<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="max-w-md w-full space-y-8 p-10 bg-white rounded-xl shadow-lg text-center">
      <!-- 載入動畫 -->
      <div v-if="isLoading" class="space-y-4">
        <div class="mx-auto w-16 h-16">
          <svg
            class="animate-spin text-indigo-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              class="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="4"
            />
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
        </div>
        <h2 class="text-xl font-semibold text-gray-800">
          正在驗證您的身份...
        </h2>
        <p class="text-sm text-gray-500">
          請稍候,系統正在處理登入資訊
        </p>
      </div>

      <!-- 錯誤訊息 -->
      <div v-if="error" class="space-y-4">
        <div class="mx-auto w-16 h-16 bg-red-100 rounded-full flex items-center justify-center">
          <svg
            class="w-10 h-10 text-red-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </div>
        <h2 class="text-xl font-semibold text-gray-800">
          登入失敗
        </h2>
        <p class="text-sm text-red-600">
          {{ error }}
        </p>
        <button
          class="mt-4 px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
          @click="redirectToLogin"
        >
          返回登入頁面
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * AuthCallback.vue - OAuth 回調處理頁面
 * 處理從 Keycloak 返回的 Authorization Code 並完成登入
 */

import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { handleOAuthCallback, isLoading, error } = useAuth()

/**
 * 重定向到登入頁面
 */
function redirectToLogin(): void {
  router.push('/login')
}

/**
 * 組件掛載時處理 OAuth 回調
 */
onMounted(async () => {
  const success = await handleOAuthCallback()

  if (success) {
    // 登入成功,檢查是否有重定向目標
    const redirectPath = router.currentRoute.value.query.redirect as string
    await router.push(redirectPath || '/')
  }
})
</script>
