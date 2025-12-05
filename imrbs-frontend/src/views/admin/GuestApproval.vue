<template>
  <div class="guest-approval-page">
    <!-- 頁面標頭 -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-gray-900">
        {{ $t('admin.guestApproval.title') }}
      </h1>
      <p class="mt-2 text-gray-600">
        {{ $t('admin.guestApproval.subtitle') }}
      </p>
    </div>

    <!-- 統計卡片 -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
      <div class="bg-white rounded-lg shadow-sm p-6 border-l-4 border-yellow-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">
              {{ $t('admin.guestApproval.stats.pending') }}
            </p>
            <p class="text-2xl font-bold text-gray-900">
              {{ stats.pending }}
            </p>
          </div>
          <div class="p-3 bg-yellow-100 rounded-full">
            <svg
              class="w-6 h-6 text-yellow-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6 border-l-4 border-green-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">
              {{ $t('admin.guestApproval.stats.approved') }}
            </p>
            <p class="text-2xl font-bold text-gray-900">
              {{ stats.approved }}
            </p>
          </div>
          <div class="p-3 bg-green-100 rounded-full">
            <svg
              class="w-6 h-6 text-green-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6 border-l-4 border-red-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">
              {{ $t('admin.guestApproval.stats.rejected') }}
            </p>
            <p class="text-2xl font-bold text-gray-900">
              {{ stats.rejected }}
            </p>
          </div>
          <div class="p-3 bg-red-100 rounded-full">
            <svg
              class="w-6 h-6 text-red-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 標籤頁切換 -->
    <div class="mb-6 border-b border-gray-200">
      <nav class="-mb-px flex space-x-8">
        <button
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
            activeTab === 'pending'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
          @click="activeTab = 'pending'"
        >
          {{ $t('admin.guestApproval.tabs.pending') }}
          <span
            v-if="stats.pending > 0"
            class="ml-2 px-2 py-0.5 text-xs rounded-full bg-yellow-100 text-yellow-800"
          >
            {{ stats.pending }}
          </span>
        </button>
        <button
          :class="[
            'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
            activeTab === 'all'
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          ]"
          @click="activeTab = 'all'"
        >
          {{ $t('admin.guestApproval.tabs.all') }}
        </button>
      </nav>
    </div>

    <!-- 申請清單 -->
    <GuestRequestList
      :key="activeTab"
      :initial-status="activeTab === 'pending' ? 'PENDING' : ''"
      @request-approved="handleRequestApproved"
      @request-rejected="handleRequestRejected"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import GuestRequestList from '@/components/admin/GuestRequestList.vue'
import { guestService, type GuestRequestResponse } from '@/services/guest.service'

const { t: _t } = useI18n()

// 當前標籤
const activeTab = ref<'pending' | 'all'>('pending')

// 統計數據
const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0
})

// 載入統計數據
async function loadStats() {
  try {
    const allRequests = await guestService.getGuestRequests()
    stats.pending = allRequests.filter((r) => r.status === 'PENDING').length
    stats.approved = allRequests.filter((r) => r.status === 'APPROVED').length
    stats.rejected = allRequests.filter((r) => r.status === 'REJECTED').length
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

// 處理申請批准
function handleRequestApproved(_request: GuestRequestResponse) {
  loadStats()
}

// 處理申請拒絕
function handleRequestRejected(_request: GuestRequestResponse) {
  loadStats()
}

// 監聽標籤變化
watch(activeTab, () => {
  loadStats()
})

// 初始載入
onMounted(() => {
  loadStats()
})
</script>
