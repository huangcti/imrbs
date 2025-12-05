<!--
T070 [P] [US1] 建立會議室卡片元件
用於展示會議室基本資訊的卡片
-->

<script setup lang="ts">
import type { Room } from '@/types/room'

interface Props {
  room: Room
}

defineProps<Props>()

const emit = defineEmits<{
  viewDetail: [roomId: number]
  select: [room: Room]
}>()
</script>

<template>
  <div
    class="room-card bg-white rounded-lg shadow hover:shadow-lg transition-shadow cursor-pointer border border-gray-200"
    @click="emit('viewDetail', room.id)"
  >
    <!-- 會議室圖片 -->
    <div class="room-image h-48 bg-gray-200 rounded-t-lg overflow-hidden">
      <img
        v-if="room.photos && room.photos.length > 0"
        :src="room.photos[0]"
        :alt="room.name"
        class="w-full h-full object-cover"
      >
      <div v-else class="flex items-center justify-center h-full text-gray-400">
        <i class="pi pi-image text-4xl" />
      </div>
    </div>

    <!-- 會議室資訊 -->
    <div class="p-4">
      <div class="flex items-start justify-between mb-2">
        <h3 class="text-lg font-semibold text-gray-900">
          {{ room.name }}
        </h3>
        <span
          :class="{
            'px-2 py-1 text-xs rounded-full': true,
            'bg-green-100 text-green-800': room.status === 'AVAILABLE',
            'bg-red-100 text-red-800': room.status === 'UNAVAILABLE',
            'bg-yellow-100 text-yellow-800': room.status === 'MAINTENANCE'
          }"
        >
          {{ room.status === 'AVAILABLE' ? '可用' : room.status === 'MAINTENANCE' ? '維護中' : '不可用' }}
        </span>
      </div>

      <!-- 建築物與樓層 -->
      <p class="text-sm text-gray-600 mb-2">
        <i class="pi pi-building mr-1" />
        {{ room.building }} {{ room.floor }}樓
      </p>

      <!-- 容量 -->
      <p class="text-sm text-gray-600 mb-3">
        <i class="pi pi-users mr-1" />
        容納 {{ room.capacity }} 人
      </p>

      <!-- 設備 -->
      <div v-if="room.equipment && room.equipment.length > 0" class="mb-3">
        <div class="flex flex-wrap gap-1">
          <span
            v-for="(item, index) in room.equipment.slice(0, 3)"
            :key="index"
            class="text-xs bg-blue-50 text-blue-700 px-2 py-1 rounded"
          >
            {{ item }}
          </span>
          <span
            v-if="room.equipment.length > 3"
            class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded"
          >
            +{{ room.equipment.length - 3 }}
          </span>
        </div>
      </div>

      <!-- 描述 -->
      <p v-if="room.description" class="text-sm text-gray-500 line-clamp-2 mb-3">
        {{ room.description }}
      </p>

      <!-- 操作按鈕 -->
      <div class="flex gap-2">
        <button
          class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors text-sm"
          @click.stop="emit('viewDetail', room.id)"
        >
          查看詳情
        </button>
        <button
          v-if="room.status === 'AVAILABLE'"
          class="px-4 py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 transition-colors text-sm"
          @click.stop="emit('select', room)"
        >
          預約
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
