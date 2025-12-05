<!--
T089 [P] [US2] 建立預約清單元件
顯示使用者的預約清單，支援篩選與操作
-->

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatTime, isUpcoming, formatDate } from '@/utils/date'
import type { Reservation, ReservationStatus } from '@/types/reservation'

interface Props {
  reservations: Reservation[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  edit: [reservation: Reservation]
  cancel: [reservation: Reservation]
  view: [reservation: Reservation]
}>()

// 篩選條件
const statusFilter = ref<ReservationStatus | 'ALL'>('ALL')

// 篩選後的預約清單
const filteredReservations = computed(() => {
  if (statusFilter.value === 'ALL') {
    return props.reservations
  }
  return props.reservations.filter((r) => r.status === statusFilter.value)
})

// 預約狀態樣式
function getStatusClass(status: ReservationStatus): string {
  const classes: Record<ReservationStatus, string> = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    CONFIRMED: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-red-100 text-red-800',
    COMPLETED: 'bg-gray-100 text-gray-800'
  }
  return classes[status]
}

// 預約狀態顯示文字
function getStatusText(status: ReservationStatus): string {
  const texts: Record<ReservationStatus, string> = {
    PENDING: '待確認',
    CONFIRMED: '已確認',
    CANCELLED: '已取消',
    COMPLETED: '已完成'
  }
  return texts[status]
}

// 檢查是否可以編輯
function canEdit(reservation: Reservation): boolean {
  return (
    (reservation.status === 'PENDING' || reservation.status === 'CONFIRMED') &&
    isUpcoming(reservation.startTime)
  )
}

// 檢查是否可以取消
function canCancel(reservation: Reservation): boolean {
  return (
    (reservation.status === 'PENDING' || reservation.status === 'CONFIRMED') &&
    isUpcoming(reservation.startTime)
  )
}
</script>

<template>
  <div class="reservation-list">
    <!-- 篩選器 -->
    <div class="mb-6 flex items-center gap-4">
      <label for="status-filter" class="text-sm font-medium text-gray-700">狀態篩選:</label>
      <select
        id="status-filter"
        v-model="statusFilter"
        class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
      >
        <option value="ALL">
          全部
        </option>
        <option value="PENDING">
          待確認
        </option>
        <option value="CONFIRMED">
          已確認
        </option>
        <option value="CANCELLED">
          已取消
        </option>
        <option value="COMPLETED">
          已完成
        </option>
      </select>
    </div>

    <!-- Loading 狀態 -->
    <div v-if="loading" class="text-center py-8">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
      <p class="mt-2 text-gray-600">
        載入中...
      </p>
    </div>

    <!-- 空狀態 -->
    <div v-else-if="filteredReservations.length === 0" class="text-center py-12">
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
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
        />
      </svg>
      <p class="mt-2 text-gray-600">
        尚無預約記錄
      </p>
    </div>

    <!-- 預約清單 -->
    <div v-else class="space-y-4">
      <div
        v-for="reservation in filteredReservations"
        :key="reservation.id"
        class="bg-white rounded-lg shadow border border-gray-200 p-6 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between">
          <!-- 左側資訊 -->
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-lg font-semibold text-gray-900">
                {{ reservation.purpose }}
              </h3>
              <span :class="['px-2 py-1 rounded-full text-xs font-medium', getStatusClass(reservation.status)]">
                {{ getStatusText(reservation.status) }}
              </span>
            </div>

            <div class="space-y-2 text-sm text-gray-600">
              <div class="flex items-center gap-2">
                <svg
                  class="h-4 w-4"
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
                <span>{{ reservation.roomName || `會議室 #${reservation.roomId}` }}</span>
              </div>

              <div class="flex items-center gap-2">
                <svg
                  class="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
                <span>{{ formatDate(reservation.startTime) }}</span>
              </div>

              <div class="flex items-center gap-2">
                <svg
                  class="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span>{{ formatTime(reservation.startTime) }} - {{ formatTime(reservation.endTime) }}</span>
              </div>

              <div v-if="reservation.participants.length > 0" class="flex items-center gap-2">
                <svg
                  class="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
                  />
                </svg>
                <span>{{ reservation.participants.length }} 位參與者</span>
              </div>
            </div>
          </div>

          <!-- 右側操作按鈕 -->
          <div class="flex gap-2 ml-4">
            <button
              v-if="canEdit(reservation)"
              class="px-3 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md hover:bg-blue-100 transition-colors"
              title="編輯預約"
              @click="emit('edit', reservation)"
            >
              編輯
            </button>
            <button
              v-if="canCancel(reservation)"
              class="px-3 py-2 text-sm font-medium text-red-600 bg-red-50 rounded-md hover:bg-red-100 transition-colors"
              title="取消預約"
              @click="emit('cancel', reservation)"
            >
              取消
            </button>
            <button
              class="px-3 py-2 text-sm font-medium text-gray-600 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors"
              title="查看詳情"
              @click="emit('view', reservation)"
            >
              詳情
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reservation-list {
  @apply w-full;
}
</style>
