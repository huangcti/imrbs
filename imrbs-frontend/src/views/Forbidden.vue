<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="max-w-md w-full space-y-6 p-10 bg-white rounded-xl shadow-lg text-center">
      <!-- 403 圖示 -->
      <div class="mx-auto w-20 h-20 bg-yellow-100 rounded-full flex items-center justify-center">
        <svg
          class="w-12 h-12 text-yellow-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
          />
        </svg>
      </div>

      <!-- 標題 -->
      <h1 class="text-3xl font-bold text-gray-900">
        403
      </h1>
      <h2 class="text-xl font-semibold text-gray-700">
        權限不足
      </h2>

      <!-- 說明 -->
      <p class="text-gray-600">
        抱歉,您沒有權限訪問此頁面。請聯繫系統管理員獲取相應權限。
      </p>

      <!-- 顯示來源路徑 -->
      <p v-if="fromPath" class="text-sm text-gray-500">
        嘗試訪問: <code class="bg-gray-100 px-2 py-1 rounded">{{ fromPath }}</code>
      </p>

      <!-- 操作按鈕 -->
      <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <button
          class="px-6 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
          @click="goBack"
        >
          返回上一頁
        </button>
        <button
          class="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
          @click="goHome"
        >
          返回首頁
        </button>
      </div>

      <!-- 角色說明 -->
      <div class="mt-8 pt-6 border-t border-gray-200 text-left">
        <h3 class="text-sm font-semibold text-gray-700 mb-3">
          您目前的角色:
        </h3>
        <ul class="space-y-1 text-sm text-gray-600">
          <li
            v-for="role in userRoles"
            :key="role"
            class="flex items-center"
          >
            <svg
              class="w-4 h-4 text-green-500 mr-2"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
              />
            </svg>
            {{ getRoleDisplayName(role) }}
          </li>
          <li v-if="userRoles.length === 0" class="text-gray-400">
            無角色資訊
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Forbidden.vue - 403 權限不足頁面
 */

import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import type { UserRole } from '@/types/auth'

const router = useRouter()
const route = useRoute()
const { userRoles } = useAuth()

const fromPath = computed(() => route.query.from as string | undefined)

/**
 * 返回上一頁
 */
function goBack(): void {
  router.back()
}

/**
 * 返回首頁
 */
function goHome(): void {
  router.push('/')
}

/**
 * 取得角色顯示名稱
 */
function getRoleDisplayName(role: UserRole): string {
  const roleNames: Record<UserRole, string> = {
    EMPLOYEE: '一般員工',
    ROOM_ADMIN: '會議室管理員',
    SYSTEM_ADMIN: '系統管理員'
  }
  return roleNames[role] || role
}
</script>
