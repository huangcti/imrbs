<!--
T048 [P] 建立通用佈局元件 - Header
頂部導航欄,包含 Logo、導航選單、使用者資訊
-->

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 使用者資訊 (從 localStorage 或 auth store 取得)
const user = computed(() => {
  return {
    name: localStorage.getItem('user_name') || 'User',
    email: localStorage.getItem('user_email') || 'user@example.com'
  }
})

const showUserMenu = ref(false)

// 導航選單
const navItems = [
  { label: '首頁', path: '/', icon: 'pi pi-home' },
  { label: '查詢會議室', path: '/rooms', icon: 'pi pi-search' },
  { label: '我的預約', path: '/reservations', icon: 'pi pi-calendar' }
]

function navigateTo(path: string) {
  router.push(path)
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<template>
  <header class="bg-white shadow-md sticky top-0 z-50">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <div class="flex items-center space-x-4">
          <router-link to="/" class="flex items-center space-x-2">
            <i class="pi pi-building text-2xl text-blue-600"></i>
            <span class="text-xl font-bold text-gray-800">IMRBS</span>
          </router-link>
        </div>

        <!-- 導航選單 -->
        <nav class="hidden md:flex space-x-1">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="px-4 py-2 rounded-md text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors flex items-center space-x-2"
            active-class="bg-blue-100 text-blue-700"
          >
            <i :class="item.icon"></i>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>

        <!-- 使用者選單 -->
        <div class="relative">
          <button
            @click="toggleUserMenu"
            class="flex items-center space-x-2 px-3 py-2 rounded-md hover:bg-gray-100 transition-colors"
          >
            <i class="pi pi-user text-gray-600"></i>
            <span class="hidden md:inline text-sm text-gray-700">{{ user.name }}</span>
            <i class="pi pi-chevron-down text-xs text-gray-500"></i>
          </button>

          <!-- 下拉選單 -->
          <div
            v-if="showUserMenu"
            class="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-gray-200 py-2"
          >
            <div class="px-4 py-3 border-b border-gray-200">
              <p class="text-sm font-medium text-gray-900">{{ user.name }}</p>
              <p class="text-xs text-gray-500">{{ user.email }}</p>
            </div>
            <button
              @click="navigateTo('/profile')"
              class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
            >
              <i class="pi pi-user"></i>
              <span>個人設定</span>
            </button>
            <button
              @click="navigateTo('/reservations')"
              class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
            >
              <i class="pi pi-calendar"></i>
              <span>我的預約</span>
            </button>
            <hr class="my-2" />
            <button
              @click="logout"
              class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
            >
              <i class="pi pi-sign-out"></i>
              <span>登出</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* 點擊外部關閉選單 */
</style>
