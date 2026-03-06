<!--
T069 [P] [US1] 建立會議室篩選元件
用於搜尋會議室的篩選條件表單
-->

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getToday } from '@/utils/date'
import type { RoomSearchParams } from '@/types/room'

const emit = defineEmits<{
  search: [params: RoomSearchParams]
}>()

// 表單資料
const searchForm = reactive<{
  name: string
  date: string
  startTime: string
  endTime: string
  capacity: number | null
  equipment: string[]
  building: string
  floor: number | null
}>({
  name: '',
  date: getToday(),
  startTime: '09:00',
  endTime: '10:00',
  capacity: null,
  equipment: [],
  building: '',
  floor: null
})

// 可用設備選項
const equipmentOptions = ref([
  { label: '投影機', value: '投影機' },
  { label: '白板', value: '白板' },
  { label: '視訊會議設備', value: '視訊會議設備' },
  { label: '音響系統', value: '音響系統' },
  { label: '網路連線', value: '網路連線' }
])

// 執行搜尋
function handleSearch(): void {
  const params: RoomSearchParams = {
    date: searchForm.date,
    startTime: `${searchForm.date}T${searchForm.startTime}:00`,
    endTime: `${searchForm.date}T${searchForm.endTime}:00`
  }

  // 可選參數
  if (searchForm.name) params.name = searchForm.name
  if (searchForm.capacity) params.capacity = searchForm.capacity
  if (searchForm.equipment.length > 0) params.equipment = searchForm.equipment
  if (searchForm.building) params.building = searchForm.building
  if (searchForm.floor) params.floor = searchForm.floor

  emit('search', params)
}

// 重置表單
function handleReset(): void {
  searchForm.name = ''
  searchForm.date = getToday()
  searchForm.startTime = '09:00'
  searchForm.endTime = '10:00'
  searchForm.capacity = null
  searchForm.equipment = []
  searchForm.building = ''
  searchForm.floor = null
}
</script>

<template>
  <div class="room-filter p-4 bg-white rounded-lg shadow">
    <h2 class="text-xl font-bold mb-4">
      搜尋會議室
    </h2>

    <form class="space-y-4" @submit.prevent="handleSearch">
      <!-- 會議室名稱搜尋 -->
      <div>
        <label for="name" class="block text-sm font-medium mb-1">會議室名稱</label>
        <input
          id="name"
          v-model="searchForm.name"
          type="text"
          placeholder="輸入會議室名稱關鍵字"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
      </div>

      <!-- 日期選擇 -->
      <div>
        <label for="date" class="block text-sm font-medium mb-1">日期 *</label>
        <input
          id="date"
          v-model="searchForm.date"
          type="date"
          :min="getToday()"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
      </div>

      <!-- 時間範圍 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="start-time" class="block text-sm font-medium mb-1">開始時間 *</label>
          <input
            id="start-time"
            v-model="searchForm.startTime"
            type="time"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
        <div>
          <label for="end-time" class="block text-sm font-medium mb-1">結束時間 *</label>
          <input
            id="end-time"
            v-model="searchForm.endTime"
            type="time"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
      </div>

      <!-- 容量 -->
      <div>
        <label for="capacity" class="block text-sm font-medium mb-1">容量</label>
        <input
          id="capacity"
          v-model.number="searchForm.capacity"
          type="number"
          min="1"
          placeholder="最少容納人數"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
      </div>

      <!-- 設備選擇 -->
      <div>
        <label class="block text-sm font-medium mb-1">設備需求</label>
        <div class="space-y-2">
          <label
            v-for="option in equipmentOptions"
            :key="option.value"
            class="flex items-center"
          >
            <input
              v-model="searchForm.equipment"
              type="checkbox"
              :value="option.value"
              class="mr-2"
            >
            <span class="text-sm">{{ option.label }}</span>
          </label>
        </div>
      </div>

      <!-- 建築物與樓層 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="building" class="block text-sm font-medium mb-1">建築物</label>
          <input
            id="building"
            v-model="searchForm.building"
            type="text"
            placeholder="例: A棟"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
        <div>
          <label for="floor" class="block text-sm font-medium mb-1">樓層</label>
          <input
            id="floor"
            v-model.number="searchForm.floor"
            type="number"
            placeholder="例: 3"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
      </div>

      <!-- 按鈕 -->
      <div class="flex gap-2">
        <button
          type="submit"
          class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
        >
          搜尋
        </button>
        <button
          type="button"
          class="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors"
          @click="handleReset"
        >
          重置
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* 元件樣式可根據需要調整 */
</style>
