<template>
  <div class="guest-request-page min-h-screen bg-gray-50">
    <!-- 頁面標頭 -->
    <header class="bg-white shadow-sm">
      <div class="max-w-4xl mx-auto px-4 py-6">
        <h1 class="text-3xl font-bold text-gray-900">{{ $t('guest.page.title') }}</h1>
        <p class="mt-2 text-gray-600">{{ $t('guest.page.subtitle') }}</p>
      </div>
    </header>

    <main class="max-w-4xl mx-auto px-4 py-8">
      <!-- 提交成功訊息 -->
      <div
        v-if="submitResult"
        class="mb-8 p-6 bg-green-50 border border-green-200 rounded-lg"
      >
        <div class="flex items-start">
          <svg class="h-6 w-6 text-green-500 mr-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h3 class="text-lg font-semibold text-green-800">
              {{ $t('guest.page.submitSuccess') }}
            </h3>
            <p class="mt-2 text-green-700">
              {{ $t('guest.page.requestId') }}: <strong>#{{ submitResult.id }}</strong>
            </p>
            <p class="mt-1 text-green-700">
              {{ $t('guest.page.statusPending') }}
            </p>
            <div class="mt-4">
              <button
                @click="submitResult = null"
                class="text-green-600 hover:text-green-800 font-medium"
              >
                {{ $t('guest.page.submitAnother') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 預約表單 -->
      <GuestRequestForm
        v-if="!submitResult"
        :rooms="rooms"
        @submit-success="handleSubmitSuccess"
        @submit-error="handleSubmitError"
      />

      <!-- 查詢申請狀態區塊 -->
      <div class="mt-8 bg-white rounded-lg shadow-md p-6">
        <h2 class="text-xl font-semibold text-gray-800 mb-4">
          {{ $t('guest.page.checkStatus') }}
        </h2>
        <div class="flex flex-col sm:flex-row gap-4">
          <div class="flex-1">
            <label for="checkRequestId" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.page.requestIdLabel') }}
            </label>
            <input
              id="checkRequestId"
              v-model="checkRequestId"
              type="number"
              min="1"
              :placeholder="$t('guest.page.requestIdPlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="flex-1">
            <label for="checkEmail" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.page.emailLabel') }}
            </label>
            <input
              id="checkEmail"
              v-model="checkEmail"
              type="email"
              :placeholder="$t('guest.page.emailPlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div class="flex items-end">
            <button
              @click="checkStatus"
              :disabled="isCheckingStatus || !checkRequestId || !checkEmail"
              class="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
            >
              <span v-if="isCheckingStatus">{{ $t('common.loading') }}</span>
              <span v-else>{{ $t('guest.page.checkButton') }}</span>
            </button>
          </div>
        </div>

        <!-- 狀態查詢結果 -->
        <div v-if="statusResult" class="mt-6 p-4 border rounded-lg" :class="statusResultClass">
          <h3 class="font-semibold mb-2">{{ $t('guest.page.requestDetails') }}</h3>
          <dl class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm">
            <div>
              <dt class="text-gray-500">{{ $t('guest.page.requestId') }}</dt>
              <dd class="font-medium">#{{ statusResult.id }}</dd>
            </div>
            <div>
              <dt class="text-gray-500">{{ $t('guest.page.status') }}</dt>
              <dd class="font-medium">
                <span :class="statusBadgeClass">{{ statusLabel }}</span>
              </dd>
            </div>
            <div>
              <dt class="text-gray-500">{{ $t('guest.page.meetingTitle') }}</dt>
              <dd class="font-medium">{{ statusResult.meetingTitle }}</dd>
            </div>
            <div>
              <dt class="text-gray-500">{{ $t('guest.page.requestTime') }}</dt>
              <dd class="font-medium">{{ formatDateTime(statusResult.requestedStartTime) }}</dd>
            </div>
            <div v-if="statusResult.status === 'REJECTED'" class="sm:col-span-2">
              <dt class="text-gray-500">{{ $t('guest.page.rejectionReason') }}</dt>
              <dd class="font-medium text-red-600">{{ statusResult.rejectionReason }}</dd>
            </div>
          </dl>
        </div>
      </div>
    </main>

    <!-- 頁尾 -->
    <footer class="bg-white border-t mt-12">
      <div class="max-w-4xl mx-auto px-4 py-6 text-center text-gray-500 text-sm">
        {{ $t('guest.page.footer') }}
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import GuestRequestForm from '@/components/guest/GuestRequestForm.vue'
import { guestService, type GuestRequestResponse } from '@/services/guest.service'
import { useToast } from '@/composables/useToast'

const { t } = useI18n()
const { showToast } = useToast()

// 可用會議室列表 (實際應從 API 獲取)
const rooms = ref([
  { id: 1, name: '會議室 A', capacity: 10 },
  { id: 2, name: '會議室 B', capacity: 20 },
  { id: 3, name: '大型會議室', capacity: 50 }
])

// 提交結果
const submitResult = ref<{ id: number; status: string } | null>(null)

// 狀態查詢
const checkRequestId = ref<number | null>(null)
const checkEmail = ref('')
const isCheckingStatus = ref(false)
const statusResult = ref<GuestRequestResponse | null>(null)

// 狀態結果樣式
const statusResultClass = computed(() => {
  if (!statusResult.value) return ''
  switch (statusResult.value.status) {
    case 'APPROVED':
      return 'bg-green-50 border-green-200'
    case 'REJECTED':
      return 'bg-red-50 border-red-200'
    default:
      return 'bg-yellow-50 border-yellow-200'
  }
})

// 狀態標籤樣式
const statusBadgeClass = computed(() => {
  if (!statusResult.value) return ''
  switch (statusResult.value.status) {
    case 'APPROVED':
      return 'px-2 py-1 rounded-full bg-green-100 text-green-800'
    case 'REJECTED':
      return 'px-2 py-1 rounded-full bg-red-100 text-red-800'
    default:
      return 'px-2 py-1 rounded-full bg-yellow-100 text-yellow-800'
  }
})

// 狀態標籤文字
const statusLabel = computed(() => {
  if (!statusResult.value) return ''
  return t(`guest.status.${statusResult.value.status.toLowerCase()}`)
})

// 處理提交成功
function handleSubmitSuccess(result: { id: number; status: string }) {
  submitResult.value = result
}

// 處理提交錯誤
function handleSubmitError(error: Error) {
  console.error('Submit error:', error)
}

// 查詢狀態
async function checkStatus() {
  if (!checkRequestId.value || !checkEmail.value) return

  isCheckingStatus.value = true
  statusResult.value = null

  try {
    const result = await guestService.getGuestRequestStatus(
      checkRequestId.value,
      checkEmail.value
    )
    statusResult.value = result
  } catch (error: unknown) {
    const err = error as Error & { response?: { status: number } }
    
    if (err.response?.status === 404) {
      showToast({ type: 'error', message: t('guest.page.requestNotFound') })
    } else if (err.response?.status === 403) {
      showToast({ type: 'error', message: t('guest.page.emailNotMatch') })
    } else {
      showToast({ type: 'error', message: t('guest.page.checkError') })
    }
  } finally {
    isCheckingStatus.value = false
  }
}

// 格式化日期時間
function formatDateTime(dateTimeStr: string): string {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleString()
}

// 載入會議室列表
onMounted(async () => {
  // TODO: 從 API 獲取可用會議室列表
  // const response = await roomService.getAvailableRooms()
  // rooms.value = response
})
</script>
