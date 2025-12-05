<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="max-w-md w-full space-y-8 p-10 bg-white rounded-xl shadow-2xl">
      <!-- Logo 和標題 -->
      <div class="text-center">
        <h1 class="text-3xl font-bold text-gray-900">
          IMRBS
        </h1>
        <p class="mt-2 text-sm text-gray-600">
          會議室預約系統
        </p>
        <h2 class="mt-6 text-2xl font-semibold text-gray-800">
          員工登入
        </h2>
        <p class="mt-2 text-sm text-gray-500">
          使用公司帳號登入系統
        </p>
      </div>

      <!-- 錯誤訊息 -->
      <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
        <strong class="font-bold">登入失敗!</strong>
        <span class="block sm:inline">{{ error }}</span>
      </div>

      <!-- 登入按鈕 -->
      <div class="space-y-4">
        <button
          :disabled="isLoading"
          class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200"
          @click="handleLogin"
        >
          <span v-if="isLoading" class="absolute left-0 inset-y-0 flex items-center pl-3">
            <svg
              class="animate-spin h-5 w-5 text-white"
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
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          </span>
          <span v-if="!isLoading">
            <svg
              class="w-5 h-5 inline-block mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"
              />
            </svg>
            使用 SSO 登入
          </span>
          <span v-else>登入中...</span>
        </button>

        <!-- 說明文字 -->
        <p class="text-xs text-center text-gray-500">
          點擊登入將跳轉至公司認證系統 (Keycloak)
        </p>
      </div>

      <!-- 功能說明 -->
      <div class="mt-8 pt-6 border-t border-gray-200">
        <h3 class="text-sm font-semibold text-gray-700 mb-3">
          系統功能
        </h3>
        <ul class="space-y-2 text-sm text-gray-600">
          <li class="flex items-start">
            <svg class="w-4 h-4 text-green-500 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            查詢與預約會議室
          </li>
          <li class="flex items-start">
            <svg class="w-4 h-4 text-green-500 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            管理我的預約
          </li>
          <li class="flex items-start">
            <svg class="w-4 h-4 text-green-500 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            Email 通知與提醒
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * T108 [P] [US3] 建立登入頁面
 * OAuth 2.0 / SSO 登入頁面
 */

import { useAuth } from '@/composables/useAuth'

const { login, isLoading, error } = useAuth()

/**
 * 處理登入按鈕點擊
 * 發起 OAuth 2.0 授權流程
 */
function handleLogin(): void {
  login()
}
</script>

<style scoped>
/* 可選: 添加額外的過渡動畫 */
.bg-gradient-to-br {
  animation: gradient 15s ease infinite;
  background-size: 200% 200%;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
