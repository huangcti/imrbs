<!--
T048 [P] 建立通用佈局元件 - Sidebar
側邊欄 (可選),用於管理功能或進階篩選
-->

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

interface MenuItem {
  label: string
  path: string
  icon: string
  badge?: number
  children?: MenuItem[]
}

const menuItems = ref<MenuItem[]>([
  {
    label: '首頁',
    path: '/',
    icon: 'pi pi-home'
  },
  {
    label: '會議室管理',
    path: '/rooms',
    icon: 'pi pi-building',
    children: [
      { label: '查詢會議室', path: '/rooms/search', icon: 'pi pi-search' },
      { label: '會議室列表', path: '/rooms/list', icon: 'pi pi-list' }
    ]
  },
  {
    label: '預約管理',
    path: '/reservations',
    icon: 'pi pi-calendar',
    children: [
      { label: '我的預約', path: '/reservations/my', icon: 'pi pi-user' },
      { label: '所有預約', path: '/reservations/all', icon: 'pi pi-list' }
    ]
  },
  {
    label: '系統管理',
    path: '/admin',
    icon: 'pi pi-cog',
    children: [
      { label: '使用者管理', path: '/admin/users', icon: 'pi pi-users' },
      { label: '維護排程', path: '/admin/maintenance', icon: 'pi pi-wrench' },
      { label: '系統設定', path: '/admin/settings', icon: 'pi pi-sliders-h' }
    ]
  }
])

const expandedItems = ref<Set<string>>(new Set())

function toggleExpand(path: string) {
  if (expandedItems.value.has(path)) {
    expandedItems.value.delete(path)
  } else {
    expandedItems.value.add(path)
  }
}

function isExpanded(path: string): boolean {
  return expandedItems.value.has(path)
}

function _navigateTo(path: string) {
  router.push(path)
}
</script>

<template>
  <aside class="w-64 bg-white shadow-lg h-screen sticky top-0 overflow-y-auto">
    <div class="p-4">
      <!-- Logo -->
      <div class="flex items-center space-x-2 mb-8">
        <i class="pi pi-building text-2xl text-blue-600" />
        <span class="text-xl font-bold text-gray-800">IMRBS</span>
      </div>

      <!-- 選單 -->
      <nav>
        <ul class="space-y-1">
          <li v-for="item in menuItems" :key="item.path">
            <!-- 一級選單 -->
            <div>
              <button
                v-if="item.children"
                class="w-full flex items-center justify-between px-4 py-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                @click="toggleExpand(item.path)"
              >
                <div class="flex items-center space-x-3">
                  <i :class="item.icon" />
                  <span class="font-medium">{{ item.label }}</span>
                </div>
                <i
                  :class="[
                    'pi',
                    isExpanded(item.path) ? 'pi-chevron-down' : 'pi-chevron-right',
                    'text-xs'
                  ]"
                />
              </button>
              <router-link
                v-else
                :to="item.path"
                class="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                active-class="bg-blue-100 text-blue-700 font-medium"
              >
                <i :class="item.icon" />
                <span>{{ item.label }}</span>
                <span
                  v-if="item.badge"
                  class="ml-auto bg-red-500 text-white text-xs px-2 py-1 rounded-full"
                >
                  {{ item.badge }}
                </span>
              </router-link>
            </div>

            <!-- 二級選單 -->
            <ul v-if="item.children && isExpanded(item.path)" class="mt-1 ml-4 space-y-1">
              <li v-for="child in item.children" :key="child.path">
                <router-link
                  :to="child.path"
                  class="flex items-center space-x-3 px-4 py-2 rounded-lg text-sm text-gray-600 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                  active-class="bg-blue-50 text-blue-700 font-medium"
                >
                  <i :class="child.icon" />
                  <span>{{ child.label }}</span>
                </router-link>
              </li>
            </ul>
          </li>
        </ul>
      </nav>
    </div>
  </aside>
</template>
