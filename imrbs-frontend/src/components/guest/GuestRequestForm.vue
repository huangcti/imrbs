<template>
  <div class="guest-request-form bg-white rounded-lg shadow-md p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">
      {{ $t('guest.requestForm.title') }}
    </h2>

    <form class="space-y-6" @submit.prevent="handleSubmit">
      <!-- 訪客資訊區塊 -->
      <fieldset class="border border-gray-200 rounded-lg p-4">
        <legend class="text-lg font-semibold text-gray-700 px-2">
          {{ $t('guest.requestForm.guestInfo') }}
        </legend>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <!-- 訪客姓名 -->
          <div>
            <label for="guestName" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.guestName') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="guestName"
              v-model="form.guestName"
              type="text"
              :placeholder="$t('guest.requestForm.guestNamePlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-500': errors.guestName }"
              required
            >
            <p v-if="errors.guestName" class="mt-1 text-sm text-red-500">
              {{ errors.guestName }}
            </p>
          </div>

          <!-- 訪客 Email -->
          <div>
            <label for="guestEmail" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.guestEmail') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="guestEmail"
              v-model="form.guestEmail"
              type="email"
              :placeholder="$t('guest.requestForm.guestEmailPlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-500': errors.guestEmail }"
              required
            >
            <p v-if="errors.guestEmail" class="mt-1 text-sm text-red-500">
              {{ errors.guestEmail }}
            </p>
          </div>

          <!-- 訪客電話 -->
          <div>
            <label for="guestPhone" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.guestPhone') }}
            </label>
            <input
              id="guestPhone"
              v-model="form.guestPhone"
              type="tel"
              :placeholder="$t('guest.requestForm.guestPhonePlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>

          <!-- 訪客公司 -->
          <div>
            <label for="guestCompany" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.guestCompany') }}
            </label>
            <input
              id="guestCompany"
              v-model="form.guestCompany"
              type="text"
              :placeholder="$t('guest.requestForm.guestCompanyPlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>
        </div>
      </fieldset>

      <!-- 會議資訊區塊 -->
      <fieldset class="border border-gray-200 rounded-lg p-4">
        <legend class="text-lg font-semibold text-gray-700 px-2">
          {{ $t('guest.requestForm.meetingInfo') }}
        </legend>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <!-- 會議室選擇 -->
          <div>
            <label for="roomId" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.room') }} <span class="text-red-500">*</span>
            </label>
            <select
              id="roomId"
              v-model="form.roomId"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-500': errors.roomId }"
              required
            >
              <option value="">
                {{ $t('guest.requestForm.selectRoom') }}
              </option>
              <option v-for="room in rooms" :key="room.id" :value="room.id">
                {{ room.name }} ({{ $t('guest.requestForm.capacity') }}: {{ room.capacity }})
              </option>
            </select>
            <p v-if="errors.roomId" class="mt-1 text-sm text-red-500">
              {{ errors.roomId }}
            </p>
          </div>

          <!-- 會議標題 -->
          <div>
            <label for="meetingTitle" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.meetingTitle') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="meetingTitle"
              v-model="form.meetingTitle"
              type="text"
              :placeholder="$t('guest.requestForm.meetingTitlePlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-500': errors.meetingTitle }"
              required
            >
            <p v-if="errors.meetingTitle" class="mt-1 text-sm text-red-500">
              {{ errors.meetingTitle }}
            </p>
          </div>

          <!-- 開始時間 -->
          <div>
            <label for="startTime" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.startTime') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="startTime"
              v-model="form.requestedStartTime"
              type="datetime-local"
              :min="minDateTime"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-500': errors.requestedStartTime }"
              required
            >
            <p v-if="errors.requestedStartTime" class="mt-1 text-sm text-red-500">
              {{ errors.requestedStartTime }}
            </p>
          </div>

          <!-- 結束時間 -->
          <div>
            <label for="endTime" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.endTime') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="endTime"
              v-model="form.requestedEndTime"
              type="datetime-local"
              :min="form.requestedStartTime || minDateTime"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              :class="{ 'border-red-500': errors.requestedEndTime }"
              required
            >
            <p v-if="errors.requestedEndTime" class="mt-1 text-sm text-red-500">
              {{ errors.requestedEndTime }}
            </p>
          </div>

          <!-- 參與人數 -->
          <div>
            <label for="attendeeCount" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.attendeeCount') }}
            </label>
            <input
              id="attendeeCount"
              v-model.number="form.attendeeCount"
              type="number"
              min="1"
              :max="selectedRoom?.capacity || 100"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
          </div>

          <!-- 會議目的 -->
          <div class="md:col-span-2">
            <label for="purpose" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('guest.requestForm.purpose') }}
            </label>
            <textarea
              id="purpose"
              v-model="form.purpose"
              rows="3"
              :placeholder="$t('guest.requestForm.purposePlaceholder')"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>
      </fieldset>

      <!-- 提交按鈕 -->
      <div class="flex justify-end space-x-4">
        <button
          type="button"
          class="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
          @click="resetForm"
        >
          {{ $t('common.reset') }}
        </button>
        <button
          type="submit"
          :disabled="isSubmitting"
          class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          <span v-if="isSubmitting" class="flex items-center">
            <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              />
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            {{ $t('common.submitting') }}
          </span>
          <span v-else>{{ $t('guest.requestForm.submit') }}</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { guestService, type GuestRequestData } from '@/services/guest.service'
import { useToast } from '@/composables/useToast'

const { t } = useI18n()
const { showToast } = useToast()

// Props
interface Props {
  rooms?: Array<{ id: number; name: string; capacity: number }>
}

const props = withDefaults(defineProps<Props>(), {
  rooms: () => []
})

// Emits
const emit = defineEmits<{
  (_e: 'submit-success', _response: { id: number; status: string }): void
  (_e: 'submit-error', _error: Error): void
}>()

// 表單資料
const form = reactive<GuestRequestData>({
  guestName: '',
  guestEmail: '',
  guestPhone: '',
  guestCompany: '',
  roomId: 0,
  meetingTitle: '',
  requestedStartTime: '',
  requestedEndTime: '',
  attendeeCount: undefined,
  purpose: ''
})

// 錯誤訊息
const errors = reactive<Record<string, string>>({})

// 提交狀態
const isSubmitting = ref(false)

// 計算最小日期時間 (現在)
const minDateTime = computed(() => {
  const now = new Date()
  return now.toISOString().slice(0, 16)
})

// 計算選中的會議室
const selectedRoom = computed(() => {
  return props.rooms.find((room) => room.id === form.roomId)
})

// 驗證表單
function validateForm(): boolean {
  // 清除舊錯誤
  Object.keys(errors).forEach((key) => delete errors[key])

  let isValid = true

  if (!form.guestName.trim()) {
    errors.guestName = t('guest.requestForm.errors.guestNameRequired')
    isValid = false
  }

  if (!form.guestEmail.trim()) {
    errors.guestEmail = t('guest.requestForm.errors.guestEmailRequired')
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.guestEmail)) {
    errors.guestEmail = t('guest.requestForm.errors.guestEmailInvalid')
    isValid = false
  }

  if (!form.roomId) {
    errors.roomId = t('guest.requestForm.errors.roomRequired')
    isValid = false
  }

  if (!form.meetingTitle.trim()) {
    errors.meetingTitle = t('guest.requestForm.errors.meetingTitleRequired')
    isValid = false
  }

  if (!form.requestedStartTime) {
    errors.requestedStartTime = t('guest.requestForm.errors.startTimeRequired')
    isValid = false
  }

  if (!form.requestedEndTime) {
    errors.requestedEndTime = t('guest.requestForm.errors.endTimeRequired')
    isValid = false
  } else if (form.requestedStartTime && form.requestedEndTime <= form.requestedStartTime) {
    errors.requestedEndTime = t('guest.requestForm.errors.endTimeInvalid')
    isValid = false
  }

  return isValid
}

// 提交表單
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const response = await guestService.submitGuestRequest({
      ...form,
      roomId: Number(form.roomId)
    })

    showToast({
      type: 'success',
      message: t('guest.requestForm.submitSuccess')
    })

    emit('submit-success', { id: response.id, status: response.status })
    resetForm()
  } catch (error: unknown) {
    const err = error as Error & { response?: { status: number; data?: { message?: string } } }
    let errorMessage = t('guest.requestForm.submitError')

    if (err.response?.status === 409) {
      errorMessage = t('guest.requestForm.errors.timeConflict')
    } else if (err.response?.data?.message) {
      errorMessage = err.response.data.message
    }

    showToast({
      type: 'error',
      message: errorMessage
    })

    emit('submit-error', err)
  } finally {
    isSubmitting.value = false
  }
}

// 重置表單
function resetForm() {
  form.guestName = ''
  form.guestEmail = ''
  form.guestPhone = ''
  form.guestCompany = ''
  form.roomId = 0
  form.meetingTitle = ''
  form.requestedStartTime = ''
  form.requestedEndTime = ''
  form.attendeeCount = undefined
  form.purpose = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}
</script>
