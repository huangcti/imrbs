<!--
T091 [P] [US2] 建立取消預約確認 Modal 元件
用於確認取消預約並要求填寫取消原因
-->

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useReservationStore } from '@/stores/reservation'
import { formatDateTime, getHoursUntil } from '@/utils/date'
import type { Reservation } from '@/types/reservation'

interface Props {
  reservation: Reservation | null
  visible: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  success: []
  cancel: []
  'update:visible': [value: boolean]
}>()

const reservationStore = useReservationStore()

// 取消原因
const cancellationReason = ref('')
const submitting = ref(false)
const errorMessage = ref<string | null>(null)

// 表單驗證
const isValid = computed(() => {
  return cancellationReason.value.trim().length >= 10 && cancellationReason.value.trim().length <= 500
})

// 計算距離會議開始還有多少小時
const hoursUntilMeeting = computed(() => {
  if (!props.reservation) return 0
  return getHoursUntil(props.reservation.startTime)
})

// 檢查是否符合 24 小時取消規則
const canCancelWithin24Hours = computed(() => {
  return hoursUntilMeeting.value >= 24
})

// 警告訊息
const warningMessage = computed(() => {
  if (!props.reservation) return null
  
  if (hoursUntilMeeting.value < 24) {
    return `注意：距離會議開始不足 24 小時（剩餘 ${Math.floor(hoursUntilMeeting.value)} 小時），無法取消此預約。`
  }
  
  if (hoursUntilMeeting.value < 48) {
    return `距離會議開始僅剩 ${Math.floor(hoursUntilMeeting.value)} 小時，請確認是否要取消預約。`
  }
  
  return null
})

// 提交取消
async function handleSubmit(): Promise<void> {
  if (!props.reservation) return
  
  if (!isValid.value) {
    errorMessage.value = '請填寫取消原因（10-500 字元）'
    return
  }

  if (!canCancelWithin24Hours.value) {
    errorMessage.value = '距離會議開始不足 24 小時，無法取消預約'
    return
  }

  submitting.value = true
  errorMessage.value = null

  try {
    await reservationStore.cancelReservation(
      props.reservation.id,
      cancellationReason.value.trim()
    )

    emit('success')
    emit('update:visible', false)
    
    // 重置表單
    cancellationReason.value = ''
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '取消預約失敗'
  } finally {
    submitting.value = false
  }
}

// 取消操作
function handleCancel(): void {
  cancellationReason.value = ''
  errorMessage.value = null
  emit('cancel')
  emit('update:visible', false)
}
</script>

<template>
  <div
    v-if="visible && reservation"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    @click.self="handleCancel"
  >
    <div class="bg-white rounded-lg shadow-xl w-full max-w-lg">
      <!-- Header -->
      <div class="flex items-center justify-between p-6 border-b border-gray-200">
        <h2 class="text-xl font-bold text-gray-900">取消預約</h2>
        <button
          @click="handleCancel"
          class="text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="關閉"
        >
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <!-- Body -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <!-- 預約資訊 -->
        <div class="bg-gray-50 rounded-md p-4 space-y-2">
          <h3 class="font-medium text-gray-900">{{ reservation.purpose }}</h3>
          <p class="text-sm text-gray-600">
            {{ reservation.roomName || `會議室 #${reservation.roomId}` }}
          </p>
          <p class="text-sm text-gray-600">{{ formatDateTime(reservation.startTime) }}</p>
        </div>

        <!-- 警告訊息 -->
        <div
          v-if="warningMessage"
          :class="[
            'px-4 py-3 rounded-md',
            canCancelWithin24Hours
              ? 'bg-yellow-50 border border-yellow-200 text-yellow-800'
              : 'bg-red-50 border border-red-200 text-red-700'
          ]"
          role="alert"
        >
          <div class="flex items-start gap-2">
            <svg class="h-5 w-5 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                clip-rule="evenodd"
              />
            </svg>
            <span class="text-sm">{{ warningMessage }}</span>
          </div>
        </div>

        <!-- 錯誤訊息 -->
        <div
          v-if="errorMessage"
          class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"
          role="alert"
        >
          {{ errorMessage }}
        </div>

        <!-- 取消原因 -->
        <div>
          <label for="cancellation-reason" class="block text-sm font-medium text-gray-700 mb-2">
            取消原因 <span class="text-red-500">*</span>
          </label>
          <textarea
            id="cancellation-reason"
            v-model="cancellationReason"
            rows="4"
            required
            maxlength="500"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-red-500 focus:border-transparent"
            placeholder="請說明取消此預約的原因（10-500 字元）"
            :disabled="!canCancelWithin24Hours"
          ></textarea>
          <p class="mt-1 text-sm text-gray-500">{{ cancellationReason.length }}/500</p>
        </div>

        <!-- 確認提示 -->
        <div class="bg-blue-50 border border-blue-200 text-blue-800 px-4 py-3 rounded-md">
          <p class="text-sm">
            <strong>提醒：</strong>取消預約後將無法復原，系統將發送通知信給所有參與者。
          </p>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-200">
          <button
            type="button"
            @click="handleCancel"
            class="px-6 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
            :disabled="submitting"
          >
            返回
          </button>
          <button
            type="submit"
            class="px-6 py-2 text-sm font-medium text-white bg-red-600 rounded-md hover:bg-red-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
            :disabled="!isValid || !canCancelWithin24Hours || submitting"
          >
            <span v-if="submitting">取消中...</span>
            <span v-else>確認取消</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
/* Modal 背景動畫 */
.fixed {
  animation: fadeIn 0.2s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
