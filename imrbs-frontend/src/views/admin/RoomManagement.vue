<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 頁面標題 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900">
        會議室管理
      </h1>
      <p class="mt-2 text-gray-600">
        新增、編輯、刪除會議室及設定維護時段
      </p>
    </div>

    <!-- 操作按鈕 -->
    <div class="mb-6 flex justify-between items-center">
      <button
        data-testid="create-room-btn"
        class="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors font-medium"
        @click="openCreateModal"
      >
        + 新增會議室
      </button>

      <!-- 搜尋過濾 (未來擴展) -->
      <div class="flex gap-3">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜尋會議室..."
          data-testid="search-input"
          class="px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
      </div>
    </div>

    <!-- 會議室列表 -->
    <div v-if="loading" class="text-center py-12">
      <p class="text-gray-500">
        載入中...
      </p>
    </div>

    <div v-else-if="filteredRooms.length === 0" class="text-center py-12">
      <p class="text-gray-500">
        目前沒有會議室
      </p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="room in filteredRooms"
        :key="room.id"
        :data-testid="`room-card-${room.id}`"
        class="bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow"
      >
        <!-- 會議室照片 -->
        <div class="h-48 bg-gray-200 rounded-t-lg overflow-hidden">
          <img
            v-if="room.photos && room.photos.length > 0"
            :src="room.photos[0]"
            :alt="room.name"
            class="w-full h-full object-cover"
          >
          <div v-else class="flex items-center justify-center h-full text-gray-400">
            <svg
              class="w-16 h-16"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </div>
        </div>

        <!-- 會議室資訊 -->
        <div class="p-4">
          <div class="flex justify-between items-start mb-2">
            <h3 class="text-lg font-semibold text-gray-900" :data-testid="`room-name-${room.id}`">
              {{ room.name }}
            </h3>
            <span
              :class="{
                'px-2 py-1 text-xs rounded-full': true,
                'bg-green-100 text-green-800': room.status === 'AVAILABLE',
                'bg-yellow-100 text-yellow-800': room.status === 'MAINTENANCE',
                'bg-red-100 text-red-800': room.status === 'DISABLED'
              }"
            >
              {{ getStatusText(room.status) }}
            </span>
          </div>

          <p class="text-sm text-gray-600 mb-3">
            {{ room.building }} {{ room.floor }} | 容納 {{ room.capacity }} 人
          </p>

          <!-- 設備標籤 -->
          <div class="flex flex-wrap gap-1 mb-4">
            <span
              v-for="equipment in (room.equipment || []).slice(0, 3)"
              :key="equipment"
              class="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded"
            >
              {{ equipment }}
            </span>
            <span
              v-if="(room.equipment || []).length > 3"
              class="px-2 py-1 bg-gray-100 text-gray-500 text-xs rounded"
            >
              +{{ (room.equipment || []).length - 3 }}
            </span>
          </div>

          <!-- 操作按鈕 -->
          <div class="flex gap-2">
            <button
              :data-testid="`edit-btn-${room.id}`"
              class="flex-1 px-3 py-2 bg-white border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-sm font-medium"
              @click="openEditModal(room)"
            >
              編輯
            </button>
            <button
              :data-testid="`maintenance-btn-${room.id}`"
              class="flex-1 px-3 py-2 bg-white border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors text-sm font-medium"
              @click="openMaintenanceModal(room)"
            >
              維護
            </button>
            <button
              :data-testid="`delete-btn-${room.id}`"
              class="px-3 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors text-sm font-medium"
              @click="confirmDelete(room)"
            >
              刪除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 創建/編輯會議室 Modal -->
    <div
      v-if="showRoomModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeRoomModal"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-3xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h2 class="text-2xl font-bold text-gray-900 mb-6">
            {{ editingRoom ? '編輯會議室' : '新增會議室' }}
          </h2>
          
          <RoomForm
            :initial-data="editingRoom || {}"
            :is-submitting="submitting"
            @submit="handleRoomSubmit"
            @cancel="closeRoomModal"
          />

          <!-- 照片上傳 (編輯模式) -->
          <div v-if="editingRoom" class="mt-6 pt-6 border-t">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">
              照片管理
            </h3>
            <PhotoUpload
              :room-id="editingRoom.id!"
              :photos="editingRoom.photos || []"
              @update:photos="updateRoomPhotos"
              @upload-success="handlePhotoUploadSuccess"
              @upload-error="handlePhotoUploadError"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 維護時段 Modal -->
    <div
      v-if="showMaintenanceModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeMaintenanceModal"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4">
        <div class="p-6">
          <h2 class="text-2xl font-bold text-gray-900 mb-2">
            設定維護時段
          </h2>
          <p class="text-gray-600 mb-6">
            {{ selectedRoom?.name }}
          </p>
          
          <MaintenanceScheduleForm
            :is-submitting="submitting"
            @submit="handleMaintenanceSubmit"
            @cancel="closeMaintenanceModal"
          />
        </div>
      </div>
    </div>

    <!-- Toast 通知 -->
    <div
      v-if="toast.show"
      :class="{
        'fixed bottom-4 right-4 px-6 py-3 rounded-md shadow-lg text-white font-medium z-50': true,
        'bg-green-600': toast.type === 'success',
        'bg-red-600': toast.type === 'error',
        'bg-blue-600': toast.type === 'info'
      }"
      data-testid="toast-message"
    >
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RoomForm from '@/components/admin/RoomForm.vue'
import PhotoUpload from '@/components/admin/PhotoUpload.vue'
import MaintenanceScheduleForm from '@/components/admin/MaintenanceScheduleForm.vue'
import axios from 'axios'

// T126 [US4] 會議室管理頁面

interface Room {
  id?: number
  name: string
  capacity: number
  building: string
  floor: string
  locationDescription: string
  equipment: string[]
  features: string[]
  status: 'AVAILABLE' | 'MAINTENANCE' | 'DISABLED'
  photos: string[]
  bookingRule?: {
    maxHoursPerReservation: number
    maxAdvanceBookingDays: number
    allowRecurring: boolean
    requiresApproval: boolean
  }
}

interface MaintenanceSchedule {
  reason: string
  startTime: string
  endTime: string
  notes: string
}

// 狀態
const loading = ref(false)
const submitting = ref(false)
const rooms = ref<Room[]>([])
const searchQuery = ref('')

// Modal 狀態
const showRoomModal = ref(false)
const showMaintenanceModal = ref(false)
const editingRoom = ref<Room | null>(null)
const selectedRoom = ref<Room | null>(null)

// Toast 通知
const toast = ref({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error' | 'info'
})

// 過濾後的會議室列表
const filteredRooms = computed(() => {
  if (!searchQuery.value) {
    return rooms.value
  }
  const query = searchQuery.value.toLowerCase()
  return rooms.value.filter(room =>
    room.name.toLowerCase().includes(query) ||
    room.building?.toLowerCase().includes(query) ||
    room.floor?.toLowerCase().includes(query)
  )
})

// 取得狀態文字
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'AVAILABLE': '可用',
    'MAINTENANCE': '維護中',
    'DISABLED': '停用'
  }
  return statusMap[status] || status
}

// 載入會議室列表
const loadRooms = async () => {
  loading.value = true
  try {
    // TODO: 實作 API 呼叫 GET /api/v1/admin/rooms
    // const response = await axios.get('/api/v1/admin/rooms')
    // rooms.value = response.data
    
    // Mock data for development
    rooms.value = []
  } catch {
    showToast('載入會議室失敗', 'error')
  } finally {
    loading.value = false
  }
}

// 打開創建 Modal
const openCreateModal = () => {
  editingRoom.value = null
  showRoomModal.value = true
}

// 打開編輯 Modal
const openEditModal = (room: Room) => {
  editingRoom.value = { ...room }
  showRoomModal.value = true
}

// 關閉會議室 Modal
const closeRoomModal = () => {
  showRoomModal.value = false
  editingRoom.value = null
}

// 處理會議室提交
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

const handleRoomSubmit = async (formData: RoomFormData) => {
  submitting.value = true
  try {
    if (editingRoom.value?.id) {
      // 更新會議室
      await axios.put(`/api/v1/rooms/${editingRoom.value.id}`, formData)
      showToast('會議室更新成功', 'success')
    } else {
      // 創建會議室
      await axios.post('/api/v1/rooms', formData)
      showToast('會議室創建成功', 'success')
    }
    
    closeRoomModal()
    await loadRooms()
  } catch {
    showToast('操作失敗', 'error')
  } finally {
    submitting.value = false
  }
}

// 更新照片
const updateRoomPhotos = (photos: string[]) => {
  if (editingRoom.value) {
    editingRoom.value.photos = photos
  }
}

// 照片上傳成功
const handlePhotoUploadSuccess = (_photoUrl: string) => {
  showToast('照片上傳成功', 'success')
  loadRooms() // 重新載入會議室列表
}

// 照片上傳失敗
const handlePhotoUploadError = (error: string) => {
  showToast(`上傳失敗: ${error}`, 'error')
}

// 打開維護 Modal
const openMaintenanceModal = (room: Room) => {
  selectedRoom.value = room
  showMaintenanceModal.value = true
}

// 關閉維護 Modal
const closeMaintenanceModal = () => {
  showMaintenanceModal.value = false
  selectedRoom.value = null
}

// 處理維護提交
const handleMaintenanceSubmit = async (formData: MaintenanceSchedule) => {
  if (!selectedRoom.value?.id) return

  submitting.value = true
  try {
    await axios.post(`/api/v1/admin/rooms/${selectedRoom.value.id}/maintenance`, formData)
    showToast('維護時段設定成功', 'success')
    closeMaintenanceModal()
    await loadRooms()
  } catch {
    showToast('設定失敗', 'error')
  } finally {
    submitting.value = false
  }
}

// 確認刪除
const confirmDelete = async (room: Room) => {
  if (!room.id) return

  if (!confirm(`確定要刪除 "${room.name}" 嗎？此操作無法復原。`)) {
    return
  }

  try {
    await axios.delete(`/api/v1/rooms/${room.id}`)
    showToast('會議室已刪除', 'success')
    await loadRooms()
  } catch {
    showToast('刪除失敗', 'error')
  }
}

// 顯示 Toast
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// 初始化
onMounted(() => {
  loadRooms()
})
</script>
