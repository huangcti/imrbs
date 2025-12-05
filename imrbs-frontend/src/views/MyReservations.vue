<!--
T092 [US2] 建立我的預約頁面
整合預約清單、編輯表單、取消確認 Modal，提供完整的預約管理功能
-->

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useReservationStore } from '@/stores/reservation'
import ReservationList from '@/components/reservation/ReservationList.vue'
import ReservationEditForm from '@/components/reservation/ReservationEditForm.vue'
import CancelReservationModal from '@/components/reservation/CancelReservationModal.vue'
import type { Reservation } from '@/types/reservation'

const reservationStore = useReservationStore()

// UI 狀態
const editModalVisible = ref(false)
const cancelModalVisible = ref(false)
const selectedReservation = ref<Reservation | null>(null)
const successMessage = ref<string | null>(null)
const errorMessage = ref<string | null>(null)

// 載入預約清單
onMounted(async () => {
  await loadReservations()
})

async function loadReservations(): Promise<void> {
  try {
    await reservationStore.fetchMyReservations()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '載入預約清單失敗'
  }
}

// 編輯預約
function handleEdit(reservation: Reservation): void {
  selectedReservation.value = reservation
  editModalVisible.value = true
}

// 取消預約
function handleCancel(reservation: Reservation): void {
  selectedReservation.value = reservation
  cancelModalVisible.value = true
}

// 查看詳情 (暫時使用 alert)
function handleView(reservation: Reservation): void {
  // TODO: 實作詳情頁面或 Modal
  alert(`預約詳情:\n\n會議主題: ${reservation.purpose}\n會議室: ${reservation.roomName || `#${reservation.roomId}`}\n狀態: ${reservation.status}`)
}

// 編輯成功
async function handleEditSuccess(): Promise<void> {
  successMessage.value = '預約已成功更新'
  editModalVisible.value = false
  selectedReservation.value = null
  
  // 重新載入清單
  await loadReservations()
  
  // 3 秒後清除成功訊息
  setTimeout(() => {
    successMessage.value = null
  }, 3000)
}

// 取消成功
async function handleCancelSuccess(): Promise<void> {
  successMessage.value = '預約已成功取消'
  cancelModalVisible.value = false
  selectedReservation.value = null
  
  // 重新載入清單
  await loadReservations()
  
  // 3 秒後清除成功訊息
  setTimeout(() => {
    successMessage.value = null
  }, 3000)
}

// 關閉 Modal
function handleModalCancel(): void {
  editModalVisible.value = false
  cancelModalVisible.value = false
  selectedReservation.value = null
}

// 清除訊息
function clearMessage(): void {
  successMessage.value = null
  errorMessage.value = null
}
</script>

<template>
  <div class="my-reservations-page min-h-screen bg-gray-50 py-8">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 頁面標題 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">
          我的預約
        </h1>
        <p class="mt-2 text-sm text-gray-600">
          查看、編輯或取消您的會議室預約
        </p>
      </div>

      <!-- 成功訊息 -->
      <div
        v-if="successMessage"
        class="mb-6 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-md flex items-center justify-between"
        role="alert"
      >
        <div class="flex items-center gap-2">
          <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clip-rule="evenodd"
            />
          </svg>
          <span>{{ successMessage }}</span>
        </div>
        <button
          class="text-green-700 hover:text-green-900"
          aria-label="關閉"
          @click="clearMessage"
        >
          <svg
            class="h-5 w-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <!-- 錯誤訊息 -->
      <div
        v-if="errorMessage"
        class="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md flex items-center justify-between"
        role="alert"
      >
        <div class="flex items-center gap-2">
          <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clip-rule="evenodd"
            />
          </svg>
          <span>{{ errorMessage }}</span>
        </div>
        <button
          class="text-red-700 hover:text-red-900"
          aria-label="關閉"
          @click="clearMessage"
        >
          <svg
            class="h-5 w-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <!-- 統計資訊 -->
      <div class="mb-6 grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg shadow p-4">
          <div class="text-sm text-gray-600">
            全部預約
          </div>
          <div class="text-2xl font-bold text-gray-900">
            {{ reservationStore.reservations.length }}
          </div>
        </div>
        <div class="bg-white rounded-lg shadow p-4">
          <div class="text-sm text-gray-600">
            待確認
          </div>
          <div class="text-2xl font-bold text-yellow-600">
            {{ reservationStore.reservations.filter((r) => r.status === 'PENDING').length }}
          </div>
        </div>
        <div class="bg-white rounded-lg shadow p-4">
          <div class="text-sm text-gray-600">
            已確認
          </div>
          <div class="text-2xl font-bold text-green-600">
            {{ reservationStore.reservations.filter((r) => r.status === 'CONFIRMED').length }}
          </div>
        </div>
        <div class="bg-white rounded-lg shadow p-4">
          <div class="text-sm text-gray-600">
            已取消
          </div>
          <div class="text-2xl font-bold text-red-600">
            {{ reservationStore.reservations.filter((r) => r.status === 'CANCELLED').length }}
          </div>
        </div>
      </div>

      <!-- 預約清單 -->
      <div class="bg-white rounded-lg shadow">
        <div class="p-6">
          <ReservationList
            :reservations="reservationStore.reservations"
            :loading="reservationStore.loading"
            @edit="handleEdit"
            @cancel="handleCancel"
            @view="handleView"
          />
        </div>
      </div>

      <!-- 快速操作提示 -->
      <div class="mt-6 bg-blue-50 border border-blue-200 text-blue-800 px-4 py-3 rounded-md">
        <div class="flex items-start gap-2">
          <svg class="h-5 w-5 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
              clip-rule="evenodd"
            />
          </svg>
          <div class="text-sm">
            <p class="font-medium mb-1">
              提醒事項：
            </p>
            <ul class="list-disc list-inside space-y-1">
              <li>只能編輯或取消尚未開始的預約</li>
              <li>取消預約需在會議開始前 24 小時進行</li>
              <li>取消後將自動通知所有參與者</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 編輯表單 Modal -->
    <ReservationEditForm
      v-if="selectedReservation"
      :reservation="selectedReservation"
      :visible="editModalVisible"
      @success="handleEditSuccess"
      @cancel="handleModalCancel"
      @update:visible="editModalVisible = $event"
    />

    <!-- 取消確認 Modal -->
    <CancelReservationModal
      :reservation="selectedReservation"
      :visible="cancelModalVisible"
      @success="handleCancelSuccess"
      @cancel="handleModalCancel"
      @update:visible="cancelModalVisible = $event"
    />
  </div>
</template>

<style scoped>
.my-reservations-page {
  @apply min-h-screen;
}
</style>
