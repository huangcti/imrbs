<!--
T073 [US1] 建立會議室搜尋頁面
整合 RoomFilter, RoomCard, RoomDetail, ReservationForm 元件
-->

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomStore } from '@/stores/room'
import RoomFilter from '@/components/room/RoomFilter.vue'
import RoomCard from '@/components/room/RoomCard.vue'
import RoomDetail from '@/components/room/RoomDetail.vue'
import ReservationForm from '@/components/reservation/ReservationForm.vue'
import type { RoomSearchParams, Room } from '@/types/room'

const _router = useRouter()
const roomStore = useRoomStore()

// 視圖狀態
const currentView = ref<'search' | 'detail' | 'reserve'>('search')
const selectedRoom = ref<Room | null>(null)
const selectedDate = ref<string>('')
const selectedTimeSlot = ref<{ startTime: string; endTime: string } | null>(null)

// 搜尋狀態
const searching = ref(false)
const searchPerformed = ref(false)

// 計算屬性
const rooms = computed(() => roomStore.rooms)
const loading = computed(() => roomStore.loading)
const error = computed(() => roomStore.error)
const hasRooms = computed(() => roomStore.hasRooms)

// 執行搜尋
async function handleSearch(params: RoomSearchParams): Promise<void> {
  searching.value = true
  searchPerformed.value = false
  selectedDate.value = params.date

  try {
    await roomStore.searchRooms(params)
    searchPerformed.value = true
    currentView.value = 'search'
  } catch (error) {
    console.error('搜尋失敗:', error)
  } finally {
    searching.value = false
  }
}

// 查看會議室詳情
function handleViewDetail(roomId: number): void {
  const room = rooms.value.find((r) => r.id === roomId)
  if (room) {
    selectedRoom.value = room
    currentView.value = 'detail'
  }
}

// 選擇會議室進行預約
function handleSelectRoom(room: Room): void {
  selectedRoom.value = room
  currentView.value = 'detail'
}

// 開始預約流程
function handleStartReservation(
  room: Room,
  timeSlot: { startTime: string; endTime: string }
): void {
  selectedRoom.value = room
  selectedTimeSlot.value = timeSlot
  currentView.value = 'reserve'
}

// 預約成功
function handleReservationSuccess(reservationId: number): void {
  alert(`預約成功！預約編號: ${reservationId}`)
  // 導航到我的預約頁面 (未來實作)
  // router.push('/my-reservations')
  // 暫時返回搜尋頁面
  resetToSearch()
}

// 取消預約/返回
function handleCancel(): void {
  if (currentView.value === 'reserve') {
    currentView.value = 'detail'
  } else {
    resetToSearch()
  }
}

// 重置到搜尋狀態
function resetToSearch(): void {
  currentView.value = 'search'
  selectedRoom.value = null
  selectedTimeSlot.value = null
}
</script>

<template>
  <div class="room-search-page min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4">
      <!-- 麵包屑導航 -->
      <nav class="mb-6 text-sm text-gray-600">
        <button class="hover:text-blue-600 transition-colors" @click="resetToSearch">
          <i class="pi pi-home mr-1" />首頁
        </button>
        <span class="mx-2">/</span>
        <span v-if="currentView === 'search'" class="font-semibold text-gray-900">搜尋會議室</span>
        <template v-else>
          <button class="hover:text-blue-600 transition-colors" @click="resetToSearch">
            搜尋會議室
          </button>
          <span class="mx-2">/</span>
          <span v-if="currentView === 'detail'" class="font-semibold text-gray-900">
            {{ selectedRoom?.name }}
          </span>
          <span v-else-if="currentView === 'reserve'" class="font-semibold text-gray-900">
            預約確認
          </span>
        </template>
      </nav>

      <!-- 搜尋視圖 -->
      <div v-if="currentView === 'search'" class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- 篩選側欄 -->
        <div class="lg:col-span-1">
          <RoomFilter @search="handleSearch" />
        </div>

        <!-- 搜尋結果 -->
        <div class="lg:col-span-3">
          <!-- Loading 狀態 -->
          <div v-if="loading" class="text-center py-12">
            <i class="pi pi-spin pi-spinner text-4xl text-blue-600" />
            <p class="mt-4 text-gray-600">
              搜尋中...
            </p>
          </div>

          <!-- 錯誤狀態 -->
          <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            <i class="pi pi-exclamation-circle mr-2" />
            {{ error }}
          </div>

          <!-- 空狀態 - 未搜尋 -->
          <div v-else-if="!searchPerformed" class="text-center py-12">
            <i class="pi pi-search text-4xl text-gray-400" />
            <p class="mt-4 text-gray-600">
              請在左側輸入搜尋條件
            </p>
          </div>

          <!-- 空狀態 - 無結果 -->
          <div v-else-if="!hasRooms" class="text-center py-12">
            <i class="pi pi-inbox text-4xl text-gray-400" />
            <p class="mt-4 text-gray-600">
              找不到符合條件的會議室
            </p>
            <p class="text-sm text-gray-500">
              請嘗試調整搜尋條件
            </p>
          </div>

          <!-- 會議室列表 -->
          <div v-else>
            <div class="mb-4 text-gray-700">
              找到 <span class="font-semibold text-blue-600">{{ roomStore.totalRooms }}</span> 間會議室
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
              <RoomCard
                v-for="room in rooms"
                :key="room.id"
                :room="room"
                @view-detail="handleViewDetail"
                @select="handleSelectRoom"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 會議室詳情視圖 -->
      <div v-else-if="currentView === 'detail' && selectedRoom" class="max-w-4xl mx-auto">
        <RoomDetail
          :room-id="selectedRoom.id"
          :selected-date="selectedDate"
          @close="resetToSearch"
          @reserve="handleStartReservation"
        />
      </div>

      <!-- 預約表單視圖 -->
      <div v-else-if="currentView === 'reserve' && selectedRoom && selectedTimeSlot" class="max-w-2xl mx-auto">
        <ReservationForm
          :room="selectedRoom"
          :date="selectedDate"
          :time-slot="selectedTimeSlot"
          @success="handleReservationSuccess"
          @cancel="handleCancel"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 頁面樣式可根據需要調整 */
</style>
