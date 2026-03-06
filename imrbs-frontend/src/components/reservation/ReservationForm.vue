<!--
T072 [P] [US1] 建立預約表單元件
用於提交預約的表單元件
-->

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import axios from 'axios'
import { useReservationStore } from '@/stores/reservation'
import { validateParticipants } from '@/utils/validation'
import type { Room } from '@/types/room'
import type { CreateReservationRequest } from '@/types/reservation'

interface Props {
  room: Room
  date: string
  timeSlot: { startTime: string; endTime: string }
}

const props = defineProps<Props>()

const emit = defineEmits<{
  success: [reservationId: number]
  cancel: []
}>()

const reservationStore = useReservationStore()

// 表單資料
const form = reactive<{
  purpose: string
  participantsText: string
}>({
  purpose: '',
  participantsText: ''
})

const submitting = ref(false)
const errorMessage = ref<string | null>(null)

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
  if (form.purpose.length < 5 || form.purpose.length > 200) return false
  if (participants.value.length > 0) {
    const validation = validateParticipants(participants.value)
    return validation.valid
  }
  return true
})

// 提交預約
async function handleSubmit(): Promise<void> {
  if (!isValid.value) {
    errorMessage.value = '請檢查表單內容'
    return
  }

  submitting.value = true
  errorMessage.value = null

  try {
    const request: CreateReservationRequest = {
      roomId: props.room.id,
      userId: 1,
      meetingTitle: form.purpose,
      startTime: props.timeSlot.startTime,
      endTime: props.timeSlot.endTime,
      participants: participants.value.join(',')
    }

    const reservationId = await reservationStore.createReservation(request)
    emit('success', reservationId)
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.data?.message) {
      errorMessage.value = error.response.data.message
    } else if (error instanceof Error) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = '預約失敗，請稍後再試'
    }
  } finally {
    submitting.value = false
  }
}

// 取消
function handleCancel(): void {
  emit('cancel')
}
</script>

<template>
  <div class="reservation-form bg-white rounded-lg shadow-lg p-6">
    <h2 class="text-2xl font-bold mb-6">
      預約會議室
    </h2>

    <!-- 會議室資訊摘要 -->
    <div class="bg-blue-50 p-4 rounded-lg mb-6">
      <h3 class="font-semibold text-blue-900 mb-2">
        {{ room.name }}
      </h3>
      <p class="text-sm text-blue-700">
        <i class="pi pi-calendar mr-1" />{{ date }}
      </p>
      <p class="text-sm text-blue-700">
        <i class="pi pi-clock mr-1" />
        {{ timeSlot.startTime.split('T')[1]?.substring(0, 5) }} -
        {{ timeSlot.endTime.split('T')[1]?.substring(0, 5) }}
      </p>
      <p class="text-sm text-blue-700">
        <i class="pi pi-users mr-1" />容納 {{ room.capacity }} 人
      </p>
    </div>

    <!-- 表單 -->
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <!-- 會議目的 -->
      <div>
        <label for="purpose" class="block text-sm font-medium mb-1">
          會議目的 * <span class="text-xs text-gray-500">(5-200 字元)</span>
        </label>
        <textarea
          id="purpose"
          v-model="form.purpose"
          required
          rows="3"
          minlength="5"
          maxlength="200"
          placeholder="請簡要說明會議目的..."
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <p class="text-xs text-gray-500 mt-1">
          {{ form.purpose.length }} / 200 字元
        </p>
      </div>

      <!-- 參與者 -->
      <div>
        <label for="participants" class="block text-sm font-medium mb-1">
          參與者 Email <span class="text-xs text-gray-500">(可選，每行一個或用逗號分隔)</span>
        </label>
        <textarea
          id="participants"
          v-model="form.participantsText"
          rows="4"
          placeholder="example1@company.com&#10;example2@company.com"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
        />
        <p class="text-xs text-gray-500 mt-1">
          已輸入 {{ participants.length }} 位參與者
          <span v-if="participants.length > 50" class="text-red-600">
            (超過上限 50 人)
          </span>
        </p>
      </div>

      <!-- 錯誤訊息 -->
      <div v-if="errorMessage" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        <i class="pi pi-exclamation-circle mr-2" />
        {{ errorMessage }}
      </div>

      <!-- 按鈕 -->
      <div class="flex gap-2 pt-4">
        <button
          type="submit"
          :disabled="!isValid || submitting"
          :class="{
            'flex-1 py-3 rounded-lg font-semibold transition-colors': true,
            'bg-blue-600 text-white hover:bg-blue-700': isValid && !submitting,
            'bg-gray-300 text-gray-500 cursor-not-allowed': !isValid || submitting
          }"
        >
          <span v-if="submitting">
            <i class="pi pi-spin pi-spinner mr-2" />提交中...
          </span>
          <span v-else>確認預約</span>
        </button>
        <button
          type="button"
          :disabled="submitting"
          class="px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
          @click="handleCancel"
        >
          取消
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* 元件樣式可根據需要調整 */
</style>
