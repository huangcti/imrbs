<!--
T048 [P] 建立通用佈局元件 - Header
T173 [P] [US8] 整合語言切換至 Header 元件
頂部導航欄,包含 Logo、導航選單、語言切換、使用者資訊
-->

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue'

const router = useRouter()
const { t } = useI18n()

// 使用者資訊 (從 localStorage 或 auth store 取得)
const user = computed(() => {
  return {
    name: localStorage.getItem('user_name') || 'User',
    email: localStorage.getItem('user_email') || 'user@example.com'
  }
})

const showUserMenu = ref(false)

// 導航選單
const navItems = computed(() => [
  { label: t('nav.home'), path: '/', icon: 'pi pi-home', dataCy: 'nav-home' },
  { label: t('nav.rooms'), path: '/rooms', icon: 'pi pi-search', dataCy: 'nav-rooms' },
  { label: t('nav.myReservations'), path: '/reservations', icon: 'pi pi-calendar', dataCy: 'nav-reservations' }
])

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
            <i class="pi pi-building text-2xl text-blue-600" />
            <span data-cy="page-title" class="text-xl font-bold text-gray-800">{{ t('app.title') }}</span>
          </router-link>
        </div>

        <!-- 導航選單 -->
        <nav class="hidden md:flex space-x-1">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            :data-cy="item.dataCy"
            class="px-4 py-2 rounded-md text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors flex items-center space-x-2"
            active-class="bg-blue-100 text-blue-700"
          >
            <i :class="item.icon" />
            <span>{{ item.label }}</span>
          </router-link>
        </nav>

        <!-- 右側操作區 -->
        <div class="flex items-center space-x-4">
          <!-- 語言切換 -->
          <LanguageSwitcher />
          
          <!-- 使用者選單 -->
          <div class="relative">
            <button
              class="flex items-center space-x-2 px-3 py-2 rounded-md hover:bg-gray-100 transition-colors"
              @click="toggleUserMenu"
            >
              <i class="pi pi-user text-gray-600" />
              <span class="hidden md:inline text-sm text-gray-700">{{ user.name }}</span>
              <i class="pi pi-chevron-down text-xs text-gray-500" />
            </button>

            <!-- 下拉選單 -->
            <div
              v-if="showUserMenu"
              class="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-gray-200 py-2"
            >
              <div class="px-4 py-3 border-b border-gray-200">
                <p class="text-sm font-medium text-gray-900">
                  {{ user.name }}
                </p>
                <p class="text-xs text-gray-500">
                  {{ user.email }}
                </p>
              </div>
              <button
                class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                @click="navigateTo('/profile')"
              >
                <i class="pi pi-user" />
                <span>{{ t('nav.settings') }}</span>
              </button>
              <button
                class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                @click="navigateTo('/reservations')"
              >
                <i class="pi pi-calendar" />
                <span>{{ t('nav.myReservations') }}</span>
              </button>
              <hr class="my-2">
              <button
                class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                @click="logout"
              >
                <i class="pi pi-sign-out" />
                <span>{{ t('nav.logout') }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* 點擊外部關閉選單 */
</style>
