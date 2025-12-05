<script setup lang="ts">
/**
 * VirtualRoomList - 會議室虛擬滾動列表
 * 
 * 封裝 VirtualList 組件，專門用於會議室列表的高效能渲染。
 * 適用於大量會議室數據的展示場景。
 * 
 * @example
 * <VirtualRoomList
 *   :rooms="availableRooms"
 *   :loading="isLoading"
 *   @select="handleRoomSelect"
 *   @load-more="loadMoreRooms"
 * />
 */

import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import VirtualList from './VirtualList.vue'
import type { Room } from '@/types/room'

interface Props {
  /** 會議室列表 */
  rooms: Room[]
  /** 是否正在載入 */
  loading?: boolean
  /** 是否有更多數據 */
  hasMore?: boolean
  /** 選中的會議室 ID */
  selectedRoomId?: number | null
  /** 容器高度 (px) */
  containerHeight?: number
  /** 是否顯示設備標籤 */
  showEquipment?: boolean
  /** 是否顯示樓層建築資訊 */
  showLocation?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  hasMore: false,
  selectedRoomId: null,
  containerHeight: 500,
  showEquipment: true,
  showLocation: true
})

const emit = defineEmits<{
  /** 選擇會議室 */
  (_e: 'select', _room: Room): void
  /** 載入更多 */
  (_e: 'load-more'): void
  /** 查看詳情 */
  (_e: 'view-details', _room: Room): void
}>()

const { t } = useI18n()

// VirtualList ref
const virtualListRef = ref<InstanceType<typeof VirtualList> | null>(null)

// 每個會議室卡片的高度
const ROOM_CARD_HEIGHT = 120

// 處理會議室選擇
const handleSelect = (room: Room) => {
  emit('select', room)
}

// 處理查看詳情
const handleViewDetails = (room: Room) => {
  emit('view-details', room)
}

// 處理滾動到底部
const handleScrollEnd = () => {
  if (props.hasMore && !props.loading) {
    emit('load-more')
  }
}

// 取得狀態顏色
const getStatusColor = (status: Room['status']) => {
  switch (status) {
    case 'AVAILABLE':
      return 'bg-green-100 text-green-800'
    case 'MAINTENANCE':
      return 'bg-yellow-100 text-yellow-800'
    case 'UNAVAILABLE':
    default:
      return 'bg-red-100 text-red-800'
  }
}

// 取得狀態文字
const getStatusText = (status: Room['status']) => {
  switch (status) {
    case 'AVAILABLE':
      return t('room.status.available')
    case 'MAINTENANCE':
      return t('room.status.maintenance')
    case 'UNAVAILABLE':
    default:
      return t('room.status.unavailable')
  }
}

// 滾動到指定會議室
const scrollToRoom = (roomId: number) => {
  const index = props.rooms.findIndex(r => r.id === roomId)
  if (index !== -1 && virtualListRef.value) {
    virtualListRef.value.scrollToIndex(index)
  }
}

// 滾動到頂部
const scrollToTop = () => {
  virtualListRef.value?.scrollToTop()
}

// 暴露方法
defineExpose({
  scrollToRoom,
  scrollToTop
})

// 監視選中的會議室，自動滾動到可視區域
watch(() => props.selectedRoomId, (newId) => {
  if (newId !== null) {
    scrollToRoom(newId)
  }
})
</script>

<template>
  <div class="virtual-room-list">
    <!-- 載入中覆蓋層 -->
    <div
      v-if="loading && rooms.length === 0"
      class="flex items-center justify-center py-12"
    >
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
      <span class="ml-3 text-gray-600">{{ t('common.loading') }}</span>
    </div>

    <!-- 虛擬列表 -->
    <VirtualList
      v-else
      ref="virtualListRef"
      :items="rooms"
      :item-height="ROOM_CARD_HEIGHT"
      :container-height="containerHeight"
      :buffer-size="3"
      container-class="room-list-container"
      @scroll-end="handleScrollEnd"
    >
      <!-- 會議室卡片 -->
      <template #default="{ item: room }">
        <div
          :class="[
            'room-card p-4 mx-2 my-1 bg-white rounded-lg border transition-all duration-200',
            selectedRoomId === room.id
              ? 'border-blue-500 ring-2 ring-blue-200'
              : 'border-gray-200 hover:border-blue-300 hover:shadow-md'
          ]"
          @click="handleSelect(room)"
        >
          <div class="flex justify-between items-start">
            <!-- 左側資訊 -->
            <div class="flex-1 min-w-0">
              <!-- 會議室名稱和狀態 -->
              <div class="flex items-center gap-2 mb-2">
                <h3 class="text-lg font-medium text-gray-900 truncate">
                  {{ room.name }}
                </h3>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs font-medium rounded-full',
                    getStatusColor(room.status)
                  ]"
                >
                  {{ getStatusText(room.status) }}
                </span>
              </div>

              <!-- 位置和容量 -->
              <div class="flex items-center gap-4 text-sm text-gray-600 mb-2">
                <span v-if="showLocation" class="flex items-center gap-1">
                  <svg
                    class="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                  {{ room.building }} {{ room.floor }}F
                </span>
                <span class="flex items-center gap-1">
                  <svg
                    class="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                    />
                  </svg>
                  {{ room.capacity }} {{ t('room.people') }}
                </span>
              </div>

              <!-- 設備標籤 -->
              <div v-if="showEquipment && room.equipment.length > 0" class="flex flex-wrap gap-1">
                <span
                  v-for="(equip, idx) in room.equipment.slice(0, 3)"
                  :key="idx"
                  class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded"
                >
                  {{ equip }}
                </span>
                <span
                  v-if="room.equipment.length > 3"
                  class="px-2 py-0.5 bg-gray-100 text-gray-500 text-xs rounded"
                >
                  +{{ room.equipment.length - 3 }}
                </span>
              </div>
            </div>

            <!-- 右側操作按鈕 -->
            <div class="ml-4 flex flex-col gap-2">
              <button
                type="button"
                class="px-3 py-1.5 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-1 disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="room.status !== 'AVAILABLE'"
                @click.stop="handleSelect(room)"
              >
                {{ t('room.book') }}
              </button>
              <button
                type="button"
                class="px-3 py-1.5 text-gray-600 text-sm border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-1"
                @click.stop="handleViewDetails(room)"
              >
                {{ t('room.details') }}
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- 空狀態 -->
      <template #empty>
        <div class="text-center py-12">
          <svg
            class="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
            />
          </svg>
          <h3 class="mt-2 text-sm font-medium text-gray-900">
            {{ t('room.noRoomsFound') }}
          </h3>
          <p class="mt-1 text-sm text-gray-500">
            {{ t('room.tryDifferentCriteria') }}
          </p>
        </div>
      </template>
    </VirtualList>

    <!-- 載入更多指示器 -->
    <div
      v-if="loading && rooms.length > 0"
      class="flex items-center justify-center py-4"
    >
      <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600" />
      <span class="ml-2 text-sm text-gray-500">{{ t('common.loadingMore') }}</span>
    </div>

    <!-- 已載入全部 -->
    <div
      v-if="!hasMore && rooms.length > 0 && !loading"
      class="text-center py-4 text-sm text-gray-400"
    >
      {{ t('common.allLoaded') }}
    </div>
  </div>
</template>

<style scoped>
.virtual-room-list {
  @apply relative;
}

.room-card {
  cursor: pointer;
  height: 112px; /* 略小於 ROOM_CARD_HEIGHT 以留出間距 */
}

.room-list-container {
  @apply bg-gray-50 rounded-lg;
}
</style>
