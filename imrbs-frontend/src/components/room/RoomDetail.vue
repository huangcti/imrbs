<!--
T071 [P] [US1] 建立會議室詳情元件
顯示會議室完整資訊與可用時段
-->

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoomStore } from '@/stores/room'
import { formatDateChinese } from '@/utils/date'
import type { Room, RoomAvailability } from '@/types/room'

interface Props {
  roomId: number
  selectedDate: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  reserve: [room: Room, timeSlot: { startTime: string; endTime: string }]
}>()

const roomStore = useRoomStore()
const room = ref<Room | null>(null)
const availability = ref<RoomAvailability | null>(null)
const loading = ref(false)
const selectedTimeSlot = ref<{ startTime: string; endTime: string } | null>(null)

// 格式化日期顯示
const formattedDate = computed(() => {
  return formatDateChinese(props.selectedDate)
})

// 載入會議室詳情與可用時段
async function loadRoomDetails(): Promise<void> {
  loading.value = true
  try {
    room.value = await roomStore.fetchRoomById(props.roomId)
    availability.value = await roomStore.fetchRoomAvailability(props.roomId, props.selectedDate)
  } catch (error) {
    console.error('載入會議室詳情失敗:', error)
  } finally {
    loading.value = false
  }
}

// 選擇時段
function selectTimeSlot(timeSlot: { startTime: string; endTime: string; available: boolean }): void {
  if (!timeSlot.available) return
  selectedTimeSlot.value = {
    startTime: timeSlot.startTime,
    endTime: timeSlot.endTime
  }
}

// 確認預約
function handleReserve(): void {
  if (!room.value || !selectedTimeSlot.value) return
  emit('reserve', room.value, selectedTimeSlot.value)
}

onMounted(() => {
  loadRoomDetails()
})
</script>

<template>
  <div class="room-detail bg-white rounded-lg shadow-lg">
    <!-- Loading 狀態 -->
    <div v-if="loading" class="p-8 text-center">
      <i class="pi pi-spin pi-spinner text-4xl text-blue-600" />
      <p class="mt-4 text-gray-600">
        載入中...
      </p>
    </div>

    <!-- 會議室詳情 -->
    <div v-else-if="room" class="p-6">
      <!-- 標題列 -->
      <div class="flex items-start justify-between mb-6">
        <div>
          <h2 class="text-2xl font-bold text-gray-900">
            {{ room.name }}
          </h2>
          <p class="text-gray-600 mt-1">
            <i class="pi pi-building mr-1" />
            {{ room.building }} {{ room.floor }}樓
          </p>
        </div>
        <button
          class="text-gray-400 hover:text-gray-600"
          aria-label="關閉"
          @click="emit('close')"
        >
          <i class="pi pi-times text-xl" />
        </button>
      </div>

      <!-- 會議室圖片 -->
      <div v-if="room.photos && room.photos.length > 0" class="mb-6">
        <div class="grid grid-cols-2 gap-2">
          <img
            v-for="(photo, index) in room.photos.slice(0, 4)"
            :key="index"
            :src="photo"
            :alt="`${room.name} 照片 ${index + 1}`"
            class="w-full h-48 object-cover rounded-lg"
          >
        </div>
      </div>

      <!-- 基本資訊 -->
      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="bg-gray-50 p-4 rounded-lg">
          <p class="text-sm text-gray-600 mb-1">
            容量
          </p>
          <p class="text-xl font-semibold text-gray-900">
            <i class="pi pi-users mr-2" />{{ room.capacity }} 人
          </p>
        </div>
        <div class="bg-gray-50 p-4 rounded-lg">
          <p class="text-sm text-gray-600 mb-1">
            狀態
          </p>
          <p class="text-xl font-semibold">
            <span
              :class="{
                'text-green-600': room.status === 'AVAILABLE',
                'text-red-600': room.status === 'UNAVAILABLE',
                'text-yellow-600': room.status === 'MAINTENANCE'
              }"
            >
              {{ room.status === 'AVAILABLE' ? '可用' : room.status === 'MAINTENANCE' ? '維護中' : '不可用' }}
            </span>
          </p>
        </div>
      </div>

      <!-- 設備清單 -->
      <div v-if="room.equipment && room.equipment.length > 0" class="mb-6">
        <h3 class="text-lg font-semibold mb-3">
          設備
        </h3>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="(item, index) in room.equipment"
            :key="index"
            class="bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-sm"
          >
            {{ item }}
          </span>
        </div>
      </div>

      <!-- 描述 -->
      <div v-if="room.description" class="mb-6">
        <h3 class="text-lg font-semibold mb-3">
          描述
        </h3>
        <p class="text-gray-700">
          {{ room.description }}
        </p>
      </div>

      <!-- 可用時段 -->
      <div v-if="availability" class="mb-6">
        <h3 class="text-lg font-semibold mb-3">
          {{ formattedDate }} 可用時段
        </h3>
        <div v-if="availability.availableSlots.length === 0" class="text-center py-8 text-gray-500">
          當天無可用時段
        </div>
        <div v-else class="grid grid-cols-3 gap-2">
          <button
            v-for="(slot, index) in availability.availableSlots"
            :key="index"
            :disabled="!slot.available"
            :class="{
              'p-3 rounded-lg border-2 text-sm transition-all': true,
              'border-blue-600 bg-blue-50 text-blue-700': selectedTimeSlot?.startTime === slot.startTime,
              'border-gray-300 hover:border-blue-400': slot.available && selectedTimeSlot?.startTime !== slot.startTime,
              'border-gray-200 bg-gray-100 text-gray-400 cursor-not-allowed': !slot.available
            }"
            @click="selectTimeSlot(slot)"
          >
            <div>{{ slot.startTime.split('T')[1]?.substring(0, 5) }}</div>
            <div class="text-xs">
              至
            </div>
            <div>{{ slot.endTime.split('T')[1]?.substring(0, 5) }}</div>
          </button>
        </div>
      </div>

      <!-- 預約按鈕 -->
      <div v-if="room.status === 'AVAILABLE'" class="flex gap-2">
        <button
          :disabled="!selectedTimeSlot"
          :class="{
            'flex-1 py-3 rounded-lg font-semibold transition-colors': true,
            'bg-blue-600 text-white hover:bg-blue-700': selectedTimeSlot,
            'bg-gray-300 text-gray-500 cursor-not-allowed': !selectedTimeSlot
          }"
          @click="handleReserve"
        >
          {{ selectedTimeSlot ? '確認預約' : '請選擇時段' }}
        </button>
      </div>
    </div>

    <!-- 錯誤狀態 -->
    <div v-else class="p-8 text-center text-red-600">
      <i class="pi pi-exclamation-triangle text-4xl" />
      <p class="mt-4">
        載入會議室詳情失敗
      </p>
    </div>
  </div>
</template>

<style scoped>
/* 元件樣式可根據需要調整 */
</style>
