<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <!-- 維護原因 -->
    <div>
      <label for="reason" class="block text-sm font-medium text-gray-700 mb-2">
        維護原因 <span class="text-red-500">*</span>
      </label>
      <input
        id="reason"
        v-model="formData.reason"
        type="text"
        data-testid="maintenance-reason-input"
        required
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        :class="{ 'border-red-500': errors.reason }"
        placeholder="例如：空調維修"
      />
      <p v-if="errors.reason" class="mt-1 text-sm text-red-600">{{ errors.reason }}</p>
    </div>

    <!-- 開始時間 -->
    <div>
      <label for="startTime" class="block text-sm font-medium text-gray-700 mb-2">
        開始時間 <span class="text-red-500">*</span>
      </label>
      <input
        id="startTime"
        v-model="formData.startTime"
        type="datetime-local"
        data-testid="maintenance-start-time-input"
        required
        :min="minDateTime"
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        :class="{ 'border-red-500': errors.startTime }"
      />
      <p v-if="errors.startTime" class="mt-1 text-sm text-red-600">{{ errors.startTime }}</p>
    </div>

    <!-- 結束時間 -->
    <div>
      <label for="endTime" class="block text-sm font-medium text-gray-700 mb-2">
        結束時間 <span class="text-red-500">*</span>
      </label>
      <input
        id="endTime"
        v-model="formData.endTime"
        type="datetime-local"
        data-testid="maintenance-end-time-input"
        required
        :min="formData.startTime || minDateTime"
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        :class="{ 'border-red-500': errors.endTime }"
      />
      <p v-if="errors.endTime" class="mt-1 text-sm text-red-600">{{ errors.endTime }}</p>
    </div>

    <!-- 備註 -->
    <div>
      <label for="notes" class="block text-sm font-medium text-gray-700 mb-2">
        備註
      </label>
      <textarea
        id="notes"
        v-model="formData.notes"
        rows="3"
        data-testid="maintenance-notes-textarea"
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        placeholder="維護詳細說明..."
      ></textarea>
    </div>

    <!-- 預估維護時長 -->
    <div class="bg-blue-50 p-4 rounded-md" v-if="estimatedDuration">
      <p class="text-sm text-blue-700">
        <span class="font-medium">預估維護時長:</span> {{ estimatedDuration }}
      </p>
    </div>

    <!-- 表單按鈕 -->
    <div class="flex justify-end gap-3 pt-4 border-t">
      <button
        type="button"
        @click="$emit('cancel')"
        data-testid="cancel-btn"
        class="px-6 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
      >
        取消
      </button>
      <button
        type="submit"
        data-testid="submit-btn"
        :disabled="isSubmitting"
        class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
      >
        {{ isSubmitting ? '儲存中...' : '創建維護時段' }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

// T125 [P] [US4] 維護時段設定元件

interface MaintenanceScheduleFormData {
  reason: string
  startTime: string
  endTime: string
  notes: string
}

interface Props {
  initialData?: Partial<MaintenanceScheduleFormData>
  isSubmitting?: boolean
}

interface Emits {
  (event: 'submit', formData: MaintenanceScheduleFormData): void
  (event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
  isSubmitting: false
})

const emit = defineEmits<Emits>()

// 表單資料
const formData = ref<MaintenanceScheduleFormData>({
  reason: '',
  startTime: '',
  endTime: '',
  notes: '',
  ...props.initialData
})

// 驗證錯誤
const errors = ref<Record<string, string>>({})

// 最小日期時間 (當前時間)
const minDateTime = computed(() => {
  const now = new Date()
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
  return now.toISOString().slice(0, 16)
})

// 預估維護時長
const estimatedDuration = computed(() => {
  if (!formData.value.startTime || !formData.value.endTime) {
    return null
  }

  const start = new Date(formData.value.startTime)
  const end = new Date(formData.value.endTime)
  const diffMs = end.getTime() - start.getTime()

  if (diffMs <= 0) {
    return null
  }

  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

  if (hours > 0 && minutes > 0) {
    return `${hours} 小時 ${minutes} 分鐘`
  } else if (hours > 0) {
    return `${hours} 小時`
  } else {
    return `${minutes} 分鐘`
  }
})

// 監聽 initialData 變化
watch(() => props.initialData, (newData) => {
  if (newData) {
    formData.value = {
      ...formData.value,
      ...newData
    }
  }
}, { deep: true })

// 驗證表單
const validateForm = (): boolean => {
  errors.value = {}

  if (!formData.value.reason || formData.value.reason.trim() === '') {
    errors.value.reason = '維護原因不能為空'
  }

  if (!formData.value.startTime) {
    errors.value.startTime = '開始時間不能為空'
  } else {
    const startTime = new Date(formData.value.startTime)
    const now = new Date()
    
    if (startTime < now) {
      errors.value.startTime = '開始時間不能早於現在'
    }
  }

  if (!formData.value.endTime) {
    errors.value.endTime = '結束時間不能為空'
  } else if (formData.value.startTime) {
    const startTime = new Date(formData.value.startTime)
    const endTime = new Date(formData.value.endTime)
    
    if (endTime <= startTime) {
      errors.value.endTime = '結束時間必須晚於開始時間'
    }

    // 檢查維護時段長度 (不超過 7 天)
    const diffMs = endTime.getTime() - startTime.getTime()
    const diffDays = diffMs / (1000 * 60 * 60 * 24)
    
    if (diffDays > 7) {
      errors.value.endTime = '維護時段不能超過 7 天'
    }
  }

  return Object.keys(errors.value).length === 0
}

// 提交表單
const handleSubmit = () => {
  if (validateForm()) {
    emit('submit', formData.value)
  }
}
</script>
