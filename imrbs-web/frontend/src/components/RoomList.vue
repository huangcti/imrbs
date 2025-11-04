<template>
  <div class="room-list">
    <div class="filter-section">
      <h2>📅 查詢會議室狀態</h2>
      
      <div class="filters">
        <div class="filter-group">
          <label>地點</label>
          <select v-model="selectedLocation">
            <option value="">全部</option>
            <option value="板橋">板橋</option>
            <option value="民生">民生</option>
          </select>
        </div>

        <div class="filter-group">
          <label>日期</label>
          <input type="date" v-model="selectedDate" />
        </div>

        <button class="btn-primary" @click="loadRoomStatus">
          🔍 查詢
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">載入中...</div>
    
    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="roomStatus" class="status-results">
      <div class="status-header">
        <h3>{{ roomStatus.location || '全部地點' }} - {{ roomStatus.date }}</h3>
        <p class="room-count">共 {{ roomStatus.rooms?.length || 0 }} 間會議室</p>
      </div>

      <div class="rooms-grid">
        <div 
          v-for="room in roomStatus.rooms" 
          :key="room.roomId"
          class="room-card"
        >
          <div class="room-header">
            <h4>{{ room.roomName }}</h4>
            <span class="room-floor">{{ room.floor }}</span>
          </div>

          <div class="room-info">
            <div class="info-item">
              <span class="label">容量:</span>
              <span class="value">{{ room.capacity }} 人</span>
            </div>
            <div class="info-item">
              <span class="label">地點:</span>
              <span class="value">{{ room.location }}</span>
            </div>
          </div>

          <div class="time-slots">
            <div v-if="!room.timeSlots || room.timeSlots.length === 0" class="slot-empty">
              ✅ 全天可用
            </div>
            <div 
              v-else 
              v-for="(slot, idx) in room.timeSlots" 
              :key="idx"
              :class="['time-slot', slot.status.toLowerCase()]"
            >
              <div class="slot-time">
                {{ slot.startTime }} - {{ slot.endTime }}
              </div>
              <div class="slot-info">
                <span class="slot-title">{{ slot.title }}</span>
                <span :class="['slot-status', slot.status.toLowerCase()]">
                  {{ getStatusText(slot.status) }}
                </span>
              </div>
            </div>
          </div>

          <button 
            class="btn-reserve"
            @click="$emit('reserveRoom', room.roomId)"
            :disabled="hasActiveReservation(room)"
          >
            {{ hasActiveReservation(room) ? '已被預約' : '立即預約' }}
          </button>
        </div>
      </div>
    </div>

    <div v-else-if="allRooms.length > 0" class="all-rooms">
      <h3>所有會議室</h3>
      <div class="rooms-grid">
        <div 
          v-for="room in allRooms" 
          :key="room.id"
          class="room-card simple"
        >
          <h4>{{ room.name }}</h4>
          <div class="room-details">
            <span>📍 {{ room.location }} - {{ room.floor }}</span>
            <span>👥 容量: {{ room.capacity }} 人</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'RoomList',
  emits: ['reserveRoom'],
  setup() {
    const selectedLocation = ref('')
    const selectedDate = ref(getTodayDate())
    const roomStatus = ref(null)
    const allRooms = ref([])
    const loading = ref(false)
    const error = ref(null)

    function getTodayDate() {
      return new Date().toISOString().split('T')[0]
    }

    async function loadRoomStatus() {
      loading.value = true
      error.value = null
      
      try {
        const params = {}
        if (selectedLocation.value) params.location = selectedLocation.value
        if (selectedDate.value) params.date = selectedDate.value

        const response = await axios.get('/api/rooms/status', { params })
        roomStatus.value = response.data
        allRooms.value = []
      } catch (err) {
        error.value = '載入會議室狀態失敗: ' + (err.response?.data?.message || err.message)
      } finally {
        loading.value = false
      }
    }

    async function loadAllRooms() {
      try {
        const response = await axios.get('/api/rooms')
        allRooms.value = response.data
      } catch (err) {
        console.error('載入會議室列表失敗:', err)
      }
    }

    function getStatusText(status) {
      const statusMap = {
        'CONFIRMED': '已確認',
        'CANCELLED': '已取消',
        'PENDING': '待確認'
      }
      return statusMap[status] || status
    }

    function hasActiveReservation(room) {
      return room.timeSlots?.some(slot => slot.status === 'CONFIRMED')
    }

    onMounted(() => {
      loadAllRooms()
    })

    return {
      selectedLocation,
      selectedDate,
      roomStatus,
      allRooms,
      loading,
      error,
      loadRoomStatus,
      getStatusText,
      hasActiveReservation
    }
  }
}
</script>

<style scoped>
.room-list {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.filter-section h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.filters {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: flex-end;
  margin-bottom: 2rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 200px;
}

.filter-group label {
  font-weight: 600;
  color: #555;
  font-size: 0.9rem;
}

.filter-group select,
.filter-group input {
  padding: 0.7rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.filter-group select:focus,
.filter-group input:focus {
  outline: none;
  border-color: #667eea;
}

.btn-primary {
  padding: 0.7rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.loading, .error {
  text-align: center;
  padding: 2rem;
  font-size: 1.1rem;
}

.error {
  color: #e74c3c;
  background: #fee;
  border-radius: 8px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.status-header h3 {
  color: #333;
  font-size: 1.3rem;
}

.room-count {
  color: #666;
  font-size: 0.95rem;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.room-card {
  background: #fafafa;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s;
}

.room-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.room-header h4 {
  color: #333;
  font-size: 1.2rem;
}

.room-floor {
  background: #667eea;
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.room-info {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

.info-item {
  display: flex;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.info-item .label {
  color: #666;
  font-weight: 600;
}

.info-item .value {
  color: #333;
}

.time-slots {
  margin: 1rem 0;
  min-height: 60px;
}

.slot-empty {
  text-align: center;
  padding: 1rem;
  background: #d4edda;
  color: #155724;
  border-radius: 8px;
  font-weight: 600;
}

.time-slot {
  background: white;
  border-left: 4px solid #667eea;
  padding: 0.8rem;
  margin-bottom: 0.5rem;
  border-radius: 6px;
}

.time-slot.cancelled {
  border-left-color: #95a5a6;
  opacity: 0.7;
}

.time-slot.confirmed {
  border-left-color: #e74c3c;
}

.slot-time {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.3rem;
}

.slot-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.slot-title {
  color: #666;
}

.slot-status {
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.slot-status.confirmed {
  background: #fee;
  color: #e74c3c;
}

.slot-status.cancelled {
  background: #f0f0f0;
  color: #95a5a6;
}

.btn-reserve {
  width: 100%;
  padding: 0.8rem;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 1rem;
}

.btn-reserve:hover:not(:disabled) {
  background: #229954;
}

.btn-reserve:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.all-rooms h3 {
  color: #333;
  margin-bottom: 1.5rem;
}

.room-card.simple {
  padding: 1.2rem;
}

.room-card.simple h4 {
  margin-bottom: 0.8rem;
  color: #667eea;
}

.room-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}
</style>
