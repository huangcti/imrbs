<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <!-- 會議室名稱 -->
    <div>
      <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
        會議室名稱 <span class="text-red-500">*</span>
      </label>
      <input
        id="name"
        v-model="formData.name"
        type="text"
        data-testid="room-name-input"
        required
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        :class="{ 'border-red-500': errors.name }"
        placeholder="例如：A01 會議室"
      />
      <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
    </div>

    <!-- 容納人數 -->
    <div>
      <label for="capacity" class="block text-sm font-medium text-gray-700 mb-2">
        容納人數 <span class="text-red-500">*</span>
      </label>
      <input
        id="capacity"
        v-model.number="formData.capacity"
        type="number"
        data-testid="room-capacity-input"
        required
        min="1"
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        :class="{ 'border-red-500': errors.capacity }"
        placeholder="例如：10"
      />
      <p v-if="errors.capacity" class="mt-1 text-sm text-red-600">{{ errors.capacity }}</p>
    </div>

    <!-- 建築與樓層 -->
    <div class="grid grid-cols-2 gap-4">
      <div>
        <label for="building" class="block text-sm font-medium text-gray-700 mb-2">
          建築
        </label>
        <input
          id="building"
          v-model="formData.building"
          type="text"
          data-testid="room-building-input"
          class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          placeholder="例如：總部大樓"
        />
      </div>

      <div>
        <label for="floor" class="block text-sm font-medium text-gray-700 mb-2">
          樓層 <span class="text-red-500">*</span>
        </label>
        <input
          id="floor"
          v-model="formData.floor"
          type="text"
          data-testid="room-floor-input"
          required
          class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          :class="{ 'border-red-500': errors.floor }"
          placeholder="例如：3F"
        />
        <p v-if="errors.floor" class="mt-1 text-sm text-red-600">{{ errors.floor }}</p>
      </div>
    </div>

    <!-- 位置描述 -->
    <div>
      <label for="locationDescription" class="block text-sm font-medium text-gray-700 mb-2">
        位置描述
      </label>
      <input
        id="locationDescription"
        v-model="formData.locationDescription"
        type="text"
        data-testid="room-location-input"
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        placeholder="例如：電梯旁，左轉第三間"
      />
    </div>

    <!-- 設備清單 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">
        設備
      </label>
      <div class="space-y-2">
        <div v-for="(equipment, index) in formData.equipment" :key="index" class="flex gap-2">
          <input
            v-model="formData.equipment[index]"
            type="text"
            :data-testid="`equipment-input-${index}`"
            class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="例如：投影機"
          />
          <button
            type="button"
            @click="removeEquipment(index)"
            class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
          >
            移除
          </button>
        </div>
        <button
          type="button"
          @click="addEquipment"
          data-testid="add-equipment-btn"
          class="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
        >
          + 新增設備
        </button>
      </div>
    </div>

    <!-- 特色標籤 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">
        特色標籤
      </label>
      <div class="flex flex-wrap gap-2 mb-2">
        <label
          v-for="feature in availableFeatures"
          :key="feature"
          class="flex items-center gap-2 px-3 py-2 bg-gray-50 border border-gray-300 rounded-md cursor-pointer hover:bg-gray-100 transition-colors"
        >
          <input
            v-model="formData.features"
            type="checkbox"
            :value="feature"
            :data-testid="`feature-${feature}`"
            class="rounded text-blue-600 focus:ring-2 focus:ring-blue-500"
          />
          <span class="text-sm">{{ feature }}</span>
        </label>
      </div>
    </div>

    <!-- 會議室狀態 -->
    <div>
      <label for="status" class="block text-sm font-medium text-gray-700 mb-2">
        狀態 <span class="text-red-500">*</span>
      </label>
      <select
        id="status"
        v-model="formData.status"
        data-testid="room-status-select"
        required
        class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      >
        <option value="AVAILABLE">可用</option>
        <option value="MAINTENANCE">維護中</option>
        <option value="DISABLED">停用</option>
      </select>
    </div>

    <!-- 預約規則 -->
    <div class="bg-gray-50 p-4 rounded-md space-y-4">
      <h3 class="text-sm font-medium text-gray-700">預約規則</h3>
      
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="maxHours" class="block text-xs text-gray-600 mb-1">
            最長預約時數
          </label>
          <input
            id="maxHours"
            v-model.number="formData.bookingRule.maxHoursPerReservation"
            type="number"
            data-testid="max-hours-input"
            min="1"
            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
            placeholder="4"
          />
        </div>

        <div>
          <label for="maxAdvanceDays" class="block text-xs text-gray-600 mb-1">
            最早預約天數
          </label>
          <input
            id="maxAdvanceDays"
            v-model.number="formData.bookingRule.maxAdvanceBookingDays"
            type="number"
            data-testid="max-advance-days-input"
            min="1"
            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
            placeholder="30"
          />
        </div>
      </div>

      <div class="flex items-center gap-4">
        <label class="flex items-center gap-2 text-sm text-gray-700">
          <input
            v-model="formData.bookingRule.allowRecurring"
            type="checkbox"
            data-testid="allow-recurring-checkbox"
            class="rounded text-blue-600 focus:ring-2 focus:ring-blue-500"
          />
          允許週期性預約
        </label>

        <label class="flex items-center gap-2 text-sm text-gray-700">
          <input
            v-model="formData.bookingRule.requiresApproval"
            type="checkbox"
            data-testid="requires-approval-checkbox"
            class="rounded text-blue-600 focus:ring-2 focus:ring-blue-500"
          />
          需要審核
        </label>
      </div>
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
        {{ isSubmitting ? '儲存中...' : (formData.id ? '更新' : '創建') }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'

// T123 [P] [US4] 會議室表單元件

interface RoomFormData {
  id?: number
  name: string
  capacity: number | null
  building: string
  floor: string
  locationDescription: string
  equipment: string[]
  features: string[]
  status: 'AVAILABLE' | 'MAINTENANCE' | 'DISABLED'
  bookingRule: {
    maxHoursPerReservation: number | null
    maxAdvanceBookingDays: number | null
    allowRecurring: boolean
    requiresApproval: boolean
  }
  photos: string[]
}

interface Props {
  initialData?: Partial<RoomFormData>
  isSubmitting?: boolean
}

interface Emits {
  (event: 'submit', formData: RoomFormData): void
  (event: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  initialData: () => ({}),
  isSubmitting: false
})

const emit = defineEmits<Emits>()

// 表單資料
const formData = ref<RoomFormData>({
  name: '',
  capacity: null,
  building: '',
  floor: '',
  locationDescription: '',
  equipment: [],
  features: [],
  status: 'AVAILABLE',
  bookingRule: {
    maxHoursPerReservation: 4,
    maxAdvanceBookingDays: 30,
    allowRecurring: false,
    requiresApproval: false
  },
  photos: [],
  ...props.initialData
})

// 驗證錯誤
const errors = ref<Record<string, string>>({})

// 可用特色標籤
const availableFeatures = [
  '視訊會議設備',
  '白板',
  '自然採光',
  '站立辦公桌',
  '無線投影',
  '咖啡機'
]

// 監聽 initialData 變化
watch(() => props.initialData, (newData) => {
  if (newData) {
    formData.value = {
      ...formData.value,
      ...newData
    }
  }
}, { deep: true })

// 新增設備
const addEquipment = () => {
  formData.value.equipment.push('')
}

// 移除設備
const removeEquipment = (index: number) => {
  formData.value.equipment.splice(index, 1)
}

// 驗證表單
const validateForm = (): boolean => {
  errors.value = {}

  if (!formData.value.name || formData.value.name.trim() === '') {
    errors.value.name = '會議室名稱不能為空'
  }

  if (!formData.value.capacity || formData.value.capacity <= 0) {
    errors.value.capacity = '容納人數必須大於 0'
  }

  if (!formData.value.floor || formData.value.floor.trim() === '') {
    errors.value.floor = '樓層不能為空'
  }

  return Object.keys(errors.value).length === 0
}

// 提交表單
const handleSubmit = () => {
  if (validateForm()) {
    // 過濾空設備
    const cleanedData = {
      ...formData.value,
      equipment: formData.value.equipment.filter(eq => eq.trim() !== '')
    }
    emit('submit', cleanedData)
  }
}
</script>
