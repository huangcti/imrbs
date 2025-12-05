<!--
T090 [P] [US2] 建立預約編輯表單元件
用於修改現有預約的表單元件
-->

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useReservationStore } from '@/stores/reservation'
import { validateParticipants } from '@/utils/validation'
import type { Reservation } from '@/types/reservation'

interface Props {
  reservation: Reservation
  visible: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  success: []
  cancel: []
  'update:visible': [value: boolean]
}>()

const reservationStore = useReservationStore()

// 表單資料
const form = reactive<{
  meetingTitle: string
  startTime: string
  endTime: string
  participantsText: string
}>({
  meetingTitle: '',
  startTime: '',
  endTime: '',
  participantsText: ''
})

const submitting = ref(false)
const errorMessage = ref<string | null>(null)

// 監聽 props 變化，初始化表單
watch(
  () => props.reservation,
  (reservation) => {
    if (reservation) {
      form.meetingTitle = reservation.purpose
      // 格式化為 datetime-local 所需的格式: YYYY-MM-DDTHH:mm
      form.startTime = formatForDatetimeLocal(reservation.startTime)
      form.endTime = formatForDatetimeLocal(reservation.endTime)
      form.participantsText = reservation.participants.join(', ')
    }
  },
  { immediate: true }
)

// 參與者清單 (從文字轉換為陣列)
const participants = computed(() => {
  if (!form.participantsText.trim()) return []
  return form.participantsText
    .split(/[,;\n]/)
    .map((email) => email.trim())
    .filter((email) => email.length > 0)
})

// 表單驗證
const isValid = computed(() => {
  if (form.meetingTitle.length < 5 || form.meetingTitle.length > 200) return false
  if (!form.startTime || !form.endTime) return false
  if (new Date(form.endTime) <= new Date(form.startTime)) return false

  if (participants.value.length > 0) {
    const validation = validateParticipants(participants.value)
    return validation.valid
  }
  return true
})

// 檢查是否有變更
const hasChanges = computed(() => {
  return (
    form.meetingTitle !== props.reservation.purpose ||
    formatForDatetimeLocal(props.reservation.startTime) !== form.startTime ||
    formatForDatetimeLocal(props.reservation.endTime) !== form.endTime ||
    form.participantsText !== props.reservation.participants.join(', ')
  )
})

// 格式化為 datetime-local 輸入格式
function formatForDatetimeLocal(isoString: string): string {
  const date = new Date(isoString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

// 轉換為 ISO 格式
function toISOString(datetimeLocal: string): string {
  return new Date(datetimeLocal).toISOString()
}

// 提交更新
async function handleSubmit(): Promise<void> {
  if (!isValid.value || !hasChanges.value) {
    errorMessage.value = '請檢查表單內容或確認有進行修改'
    return
  }

  submitting.value = true
  errorMessage.value = null

  try {
    await reservationStore.updateReservation(props.reservation.id, {
      meetingTitle: form.meetingTitle,
      startTime: toISOString(form.startTime),
      endTime: toISOString(form.endTime),
      participants: form.participantsText
    })

    emit('success')
    emit('update:visible', false)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '更新預約失敗'
  } finally {
    submitting.value = false
  }
}

// 取消
function handleCancel(): void {
  emit('cancel')
  emit('update:visible', false)
}
</script>

<template>
  <div
    v-if="visible"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    @click.self="handleCancel"
  >
    <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="flex items-center justify-between p-6 border-b border-gray-200">
        <h2 class="text-2xl font-bold text-gray-900">
          編輯預約
        </h2>
        <button
          class="text-gray-400 hover:text-gray-600 transition-colors"
          aria-label="關閉"
          @click="handleCancel"
        >
          <svg
            class="h-6 w-6"
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
        </button>
      </div>

      <!-- Body -->
      <form class="p-6 space-y-6" @submit.prevent="handleSubmit">
        <!-- 錯誤訊息 -->
        <div
          v-if="errorMessage"
          class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"
          role="alert"
        >
          {{ errorMessage }}
        </div>

        <!-- 會議主題 -->
        <div>
          <label for="meeting-title" class="block text-sm font-medium text-gray-700 mb-2">
            會議主題 <span class="text-red-500">*</span>
          </label>
          <input
            id="meeting-title"
            v-model="form.meetingTitle"
            type="text"
            required
            maxlength="200"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="請輸入會議主題（5-200 字元）"
          >
          <p class="mt-1 text-sm text-gray-500">
            {{ form.meetingTitle.length }}/200
          </p>
        </div>

        <!-- 開始時間 -->
        <div>
          <label for="start-time" class="block text-sm font-medium text-gray-700 mb-2">
            開始時間 <span class="text-red-500">*</span>
          </label>
          <input
            id="start-time"
            v-model="form.startTime"
            type="datetime-local"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
        </div>

        <!-- 結束時間 -->
        <div>
          <label for="end-time" class="block text-sm font-medium text-gray-700 mb-2">
            結束時間 <span class="text-red-500">*</span>
          </label>
          <input
            id="end-time"
            v-model="form.endTime"
            type="datetime-local"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
        </div>

        <!-- 參與者 -->
        <div>
          <label for="participants" class="block text-sm font-medium text-gray-700 mb-2">
            參與者 Email（選填）
          </label>
          <textarea
            id="participants"
            v-model="form.participantsText"
            rows="3"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="請輸入參與者 Email，以逗號、分號或換行分隔&#10;例如: user1@example.com, user2@example.com"
          />
          <p class="mt-1 text-sm text-gray-500">
            已識別 {{ participants.length }} 位參與者
          </p>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-200">
          <button
            type="button"
            class="px-6 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
            :disabled="submitting"
            @click="handleCancel"
          >
            取消
          </button>
          <button
            type="submit"
            class="px-6 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
            :disabled="!isValid || !hasChanges || submitting"
          >
            <span v-if="submitting">更新中...</span>
            <span v-else>確認更新</span>
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
