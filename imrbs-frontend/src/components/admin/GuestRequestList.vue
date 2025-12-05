<template>
  <div class="guest-request-list">
    <!-- 篩選器 -->
    <div class="flex flex-wrap gap-4 mb-6">
      <div class="flex items-center space-x-2">
        <label for="statusFilter" class="text-sm font-medium text-gray-700">
          {{ $t('admin.guestRequests.filter.status') }}:
        </label>
        <select
          id="statusFilter"
          v-model="selectedStatus"
          @change="handleFilterChange"
          class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
        >
          <option value="">{{ $t('admin.guestRequests.filter.all') }}</option>
          <option value="PENDING">{{ $t('guest.status.pending') }}</option>
          <option value="APPROVED">{{ $t('guest.status.approved') }}</option>
          <option value="REJECTED">{{ $t('guest.status.rejected') }}</option>
        </select>
      </div>

      <button
        @click="refreshList"
        :disabled="isLoading"
        class="flex items-center px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
      >
        <svg
          class="w-4 h-4 mr-2"
          :class="{ 'animate-spin': isLoading }"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
          />
        </svg>
        {{ $t('common.refresh') }}
      </button>
    </div>

    <!-- 載入中狀態 -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
    </div>

    <!-- 空狀態 -->
    <div
      v-else-if="requests.length === 0"
      class="text-center py-12 bg-white rounded-lg shadow-sm"
    >
      <svg
        class="mx-auto h-12 w-12 text-gray-400"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
        />
      </svg>
      <p class="mt-4 text-gray-500">{{ $t('admin.guestRequests.empty') }}</p>
    </div>

    <!-- 申請清單 -->
    <div v-else class="space-y-4">
      <div
        v-for="request in requests"
        :key="request.id"
        class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow"
      >
        <div class="flex flex-wrap justify-between items-start gap-4">
          <!-- 申請資訊 -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center space-x-3 mb-2">
              <h3 class="text-lg font-semibold text-gray-900 truncate">
                {{ request.meetingTitle }}
              </h3>
              <span
                :class="getStatusBadgeClass(request.status)"
                class="px-2 py-1 text-xs font-medium rounded-full"
              >
                {{ $t(`guest.status.${request.status.toLowerCase()}`) }}
              </span>
            </div>

            <dl class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-2 text-sm">
              <div>
                <dt class="text-gray-500">{{ $t('admin.guestRequests.guestName') }}</dt>
                <dd class="font-medium text-gray-900">{{ request.guestName }}</dd>
              </div>
              <div>
                <dt class="text-gray-500">{{ $t('admin.guestRequests.guestEmail') }}</dt>
                <dd class="font-medium text-gray-900">{{ request.guestEmail }}</dd>
              </div>
              <div>
                <dt class="text-gray-500">{{ $t('admin.guestRequests.company') }}</dt>
                <dd class="font-medium text-gray-900">
                  {{ request.guestCompany || '-' }}
                </dd>
              </div>
              <div>
                <dt class="text-gray-500">{{ $t('admin.guestRequests.requestTime') }}</dt>
                <dd class="font-medium text-gray-900">
                  {{ formatDateTime(request.requestedStartTime) }} -
                  {{ formatTime(request.requestedEndTime) }}
                </dd>
              </div>
              <div>
                <dt class="text-gray-500">{{ $t('admin.guestRequests.submittedAt') }}</dt>
                <dd class="font-medium text-gray-900">
                  {{ formatDateTime(request.createdAt) }}
                </dd>
              </div>
            </dl>

            <!-- 拒絕原因 (如果已拒絕) -->
            <div
              v-if="request.status === 'REJECTED' && request.rejectionReason"
              class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg"
            >
              <p class="text-sm text-red-700">
                <strong>{{ $t('admin.guestRequests.rejectionReason') }}:</strong>
                {{ request.rejectionReason }}
              </p>
            </div>
          </div>

          <!-- 操作按鈕 -->
          <div
            v-if="request.status === 'PENDING'"
            class="flex flex-col sm:flex-row gap-2"
          >
            <button
              @click="handleApprove(request)"
              :disabled="processingId === request.id"
              class="px-4 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 disabled:bg-gray-400 transition-colors"
            >
              {{ $t('admin.guestRequests.approve') }}
            </button>
            <button
              @click="handleRejectClick(request)"
              :disabled="processingId === request.id"
              class="px-4 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 disabled:bg-gray-400 transition-colors"
            >
              {{ $t('admin.guestRequests.reject') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒絕對話框 -->
    <Teleport to="body">
      <div
        v-if="showRejectDialog"
        class="fixed inset-0 z-50 flex items-center justify-center"
      >
        <!-- 遮罩 -->
        <div
          class="absolute inset-0 bg-black bg-opacity-50"
          @click="closeRejectDialog"
        ></div>

        <!-- 對話框 -->
        <div class="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">
            {{ $t('admin.guestRequests.rejectDialog.title') }}
          </h3>

          <div class="mb-4">
            <label for="rejectReason" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('admin.guestRequests.rejectDialog.reasonLabel') }}
              <span class="text-red-500">*</span>
            </label>
            <textarea
              id="rejectReason"
              v-model="rejectReason"
              rows="3"
              :placeholder="$t('admin.guestRequests.rejectDialog.reasonPlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500"
              :class="{ 'border-red-500': rejectError }"
            ></textarea>
            <p v-if="rejectError" class="mt-1 text-sm text-red-500">{{ rejectError }}</p>
          </div>

          <div class="flex justify-end space-x-3">
            <button
              @click="closeRejectDialog"
              class="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              {{ $t('common.cancel') }}
            </button>
            <button
              @click="confirmReject"
              :disabled="isRejecting"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400"
            >
              {{ isRejecting ? $t('common.processing') : $t('admin.guestRequests.reject') }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { guestService, type GuestRequestResponse } from '@/services/guest.service'
import { useToast } from '@/composables/useToast'

const { t } = useI18n()
const { showToast } = useToast()

// Props
interface Props {
  initialStatus?: string
}

const props = withDefaults(defineProps<Props>(), {
  initialStatus: ''
})

// Emits
const emit = defineEmits<{
  (e: 'request-approved', request: GuestRequestResponse): void
  (e: 'request-rejected', request: GuestRequestResponse): void
}>()

// 狀態
const requests = ref<GuestRequestResponse[]>([])
const isLoading = ref(false)
const selectedStatus = ref(props.initialStatus)
const processingId = ref<number | null>(null)

// 拒絕對話框狀態
const showRejectDialog = ref(false)
const rejectingRequest = ref<GuestRequestResponse | null>(null)
const rejectReason = ref('')
const rejectError = ref('')
const isRejecting = ref(false)

// 載入申請清單
async function loadRequests() {
  isLoading.value = true
  try {
    const status = selectedStatus.value as 'PENDING' | 'APPROVED' | 'REJECTED' | undefined
    requests.value = await guestService.getGuestRequests(status || undefined)
  } catch (error) {
    showToast({ type: 'error', message: t('admin.guestRequests.loadError') })
    console.error('Failed to load guest requests:', error)
  } finally {
    isLoading.value = false
  }
}

// 處理篩選變更
function handleFilterChange() {
  loadRequests()
}

// 刷新清單
function refreshList() {
  loadRequests()
}

// 處理批准
async function handleApprove(request: GuestRequestResponse) {
  if (processingId.value) return

  processingId.value = request.id

  try {
    const approved = await guestService.approveGuestRequest(request.id)
    showToast({ type: 'success', message: t('admin.guestRequests.approveSuccess') })
    emit('request-approved', approved)
    loadRequests()
  } catch (error: unknown) {
    const err = error as Error & { response?: { data?: { message?: string } } }
    showToast({
      type: 'error',
      message: err.response?.data?.message || t('admin.guestRequests.approveError')
    })
  } finally {
    processingId.value = null
  }
}

// 處理拒絕點擊
function handleRejectClick(request: GuestRequestResponse) {
  rejectingRequest.value = request
  rejectReason.value = ''
  rejectError.value = ''
  showRejectDialog.value = true
}

// 關閉拒絕對話框
function closeRejectDialog() {
  showRejectDialog.value = false
  rejectingRequest.value = null
  rejectReason.value = ''
  rejectError.value = ''
}

// 確認拒絕
async function confirmReject() {
  if (!rejectingRequest.value) return

  if (!rejectReason.value.trim()) {
    rejectError.value = t('admin.guestRequests.rejectDialog.reasonRequired')
    return
  }

  isRejecting.value = true

  try {
    const rejected = await guestService.rejectGuestRequest(rejectingRequest.value.id, {
      reason: rejectReason.value
    })
    showToast({ type: 'success', message: t('admin.guestRequests.rejectSuccess') })
    emit('request-rejected', rejected)
    closeRejectDialog()
    loadRequests()
  } catch (error: unknown) {
    const err = error as Error & { response?: { data?: { message?: string } } }
    showToast({
      type: 'error',
      message: err.response?.data?.message || t('admin.guestRequests.rejectError')
    })
  } finally {
    isRejecting.value = false
  }
}

// 取得狀態標籤樣式
function getStatusBadgeClass(status: string): string {
  switch (status) {
    case 'APPROVED':
      return 'bg-green-100 text-green-800'
    case 'REJECTED':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-yellow-100 text-yellow-800'
  }
}

// 格式化日期時間
function formatDateTime(dateTimeStr: string): string {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 格式化時間
function formatTime(dateTimeStr: string): string {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 初始載入
onMounted(() => {
  loadRequests()
})
</script>
