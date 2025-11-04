<template>
  <div class="my-reservations">
    <div class="header-section">
      <h2>📋 我的預約</h2>
      <div class="filter-controls">
        <input 
          type="email" 
          v-model="searchEmail" 
          placeholder="輸入您的 Email 查詢預約"
          @keyup.enter="loadReservations"
        />
        <button class="btn-search" @click="loadReservations">
          🔍 查詢
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">載入中...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else-if="reservations.length === 0" class="empty-state">
      <p>暫無預約記錄</p>
      <small>請輸入您的 Email 查詢預約，或前往「建立預約」頁面新增預約</small>
    </div>

    <div v-else class="reservations-list">
      <div 
        v-for="reservation in sortedReservations" 
        :key="reservation.id"
        :class="['reservation-card', reservation.status.toLowerCase()]"
      >
        <div class="card-header">
          <div class="status-badge" :class="reservation.status.toLowerCase()">
            {{ getStatusText(reservation.status) }}
          </div>
          <div class="reservation-id">
            ID: {{ reservation.id.substring(0, 8) }}
          </div>
        </div>

        <div class="card-body">
          <h3>{{ reservation.title }}</h3>
          
          <div class="reservation-details">
            <div class="detail-item">
              <span class="icon">🏢</span>
              <span class="label">會議室:</span>
              <span class="value">{{ getRoomName(reservation.roomId) }}</span>
            </div>

            <div class="detail-item">
              <span class="icon">📅</span>
              <span class="label">日期:</span>
              <span class="value">{{ formatDate(reservation.date) }}</span>
            </div>

            <div class="detail-item">
              <span class="icon">⏰</span>
              <span class="label">時間:</span>
              <span class="value">
                {{ reservation.startTime }} - {{ reservation.endTime }}
              </span>
            </div>

            <div class="detail-item">
              <span class="icon">📧</span>
              <span class="label">聯絡人:</span>
              <span class="value">{{ reservation.organizerEmail }}</span>
            </div>

            <div v-if="reservation.participants && reservation.participants.length > 0" class="detail-item">
              <span class="icon">👥</span>
              <span class="label">參與者:</span>
              <span class="value">{{ reservation.participants.join(', ') }}</span>
            </div>
          </div>
        </div>

        <div class="card-actions" v-if="reservation.status === 'CONFIRMED'">
          <button 
            class="btn-edit" 
            @click="startEdit(reservation)"
          >
            ✏️ 修改
          </button>
          <button 
            class="btn-cancel" 
            @click="cancelReservation(reservation.id)"
            :disabled="cancelling === reservation.id"
          >
            {{ cancelling === reservation.id ? '取消中...' : '🗑️ 取消預約' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="editingReservation" class="modal-overlay" @click="closeEdit">
      <div class="modal-content" @click.stop>
        <h3>修改預約</h3>
        
        <form @submit.prevent="updateReservation" class="edit-form">
          <div class="form-group">
            <label>會議室</label>
            <select v-model="editForm.roomId" required>
              <option v-for="room in rooms" :key="room.id" :value="room.id">
                {{ room.name }} - {{ room.location }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>日期</label>
            <input type="date" v-model="editForm.date" required />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>開始時間</label>
              <input type="time" v-model="editForm.startTime" required />
            </div>

            <div class="form-group">
              <label>結束時間</label>
              <input type="time" v-model="editForm.endTime" required />
            </div>
          </div>

          <div class="form-group">
            <label>會議主題</label>
            <input type="text" v-model="editForm.title" required />
          </div>

          <div class="form-group">
            <label>聯絡 Email</label>
            <input type="email" v-model="editForm.organizerEmail" required />
          </div>

          <div v-if="editError" class="error-message">
            ❌ {{ editError }}
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn-save" :disabled="updating">
              {{ updating ? '儲存中...' : '💾 儲存' }}
            </button>
            <button type="button" class="btn-close" @click="closeEdit">
              ❌ 取消
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'MyReservations',
  setup() {
    const searchEmail = ref('')
    const reservations = ref([])
    const rooms = ref([])
    const loading = ref(false)
    const error = ref(null)
    const cancelling = ref(null)
    const editingReservation = ref(null)
    const editForm = ref({})
    const editError = ref(null)
    const updating = ref(false)

    const sortedReservations = computed(() => {
      return [...reservations.value].sort((a, b) => {
        if (a.date !== b.date) {
          return new Date(b.date) - new Date(a.date)
        }
        return b.startTime.localeCompare(a.startTime)
      })
    })

    async function loadRooms() {
      try {
        const response = await axios.get('/api/rooms')
        rooms.value = response.data
      } catch (err) {
        console.error('載入會議室列表失敗:', err)
      }
    }

    async function loadReservations() {
      if (!searchEmail.value) {
        error.value = '請輸入 Email'
        return
      }

      loading.value = true
      error.value = null

      try {
        const response = await axios.get('/api/reservations')
        // Filter by email (client-side since API returns all)
        reservations.value = response.data.filter(r => 
          r.organizerEmail.toLowerCase() === searchEmail.value.toLowerCase()
        )
        
        if (reservations.value.length === 0) {
          error.value = '找不到此 Email 的預約記錄'
        }
      } catch (err) {
        error.value = '載入預約失敗: ' + (err.response?.data?.message || err.message)
      } finally {
        loading.value = false
      }
    }

    async function cancelReservation(id) {
      if (!confirm('確定要取消此預約嗎？')) {
        return
      }

      cancelling.value = id

      try {
        await axios.delete(`/api/reservations/${id}`)
        // Reload reservations
        await loadReservations()
      } catch (err) {
        alert('取消失敗: ' + (err.response?.data?.message || err.message))
      } finally {
        cancelling.value = null
      }
    }

    function startEdit(reservation) {
      editingReservation.value = reservation
      editForm.value = {
        roomId: reservation.roomId,
        date: reservation.date,
        startTime: reservation.startTime.substring(0, 5),
        endTime: reservation.endTime.substring(0, 5),
        title: reservation.title,
        organizerEmail: reservation.organizerEmail,
        participants: reservation.participants || []
      }
      editError.value = null
    }

    function closeEdit() {
      editingReservation.value = null
      editForm.value = {}
      editError.value = null
    }

    async function updateReservation() {
      updating.value = true
      editError.value = null

      try {
        const payload = {
          ...editForm.value,
          startTime: editForm.value.startTime + ':00',
          endTime: editForm.value.endTime + ':00'
        }

        await axios.put(`/api/reservations/${editingReservation.value.id}`, payload)
        closeEdit()
        await loadReservations()
      } catch (err) {
        if (err.response?.status === 409) {
          editError.value = '此時段已被預約，請選擇其他時間'
        } else {
          editError.value = '更新失敗: ' + (err.response?.data?.message || err.message)
        }
      } finally {
        updating.value = false
      }
    }

    function getRoomName(roomId) {
      const room = rooms.value.find(r => r.id === roomId)
      return room ? `${room.name} (${room.location})` : roomId
    }

    function getStatusText(status) {
      const statusMap = {
        'CONFIRMED': '✅ 已確認',
        'CANCELLED': '❌ 已取消',
        'PENDING': '⏳ 待確認'
      }
      return statusMap[status] || status
    }

    function formatDate(dateStr) {
      const date = new Date(dateStr)
      return date.toLocaleDateString('zh-TW', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        weekday: 'short'
      })
    }

    onMounted(() => {
      loadRooms()
    })

    return {
      searchEmail,
      reservations,
      rooms,
      loading,
      error,
      cancelling,
      editingReservation,
      editForm,
      editError,
      updating,
      sortedReservations,
      loadReservations,
      cancelReservation,
      startEdit,
      closeEdit,
      updateReservation,
      getRoomName,
      getStatusText,
      formatDate
    }
  }
}
</script>

<style scoped>
.my-reservations {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-section {
  margin-bottom: 2rem;
}

.header-section h2 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.filter-controls {
  display: flex;
  gap: 1rem;
}

.filter-controls input {
  flex: 1;
  padding: 0.8rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
}

.filter-controls input:focus {
  outline: none;
  border-color: #667eea;
}

.btn-search {
  padding: 0.8rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-search:hover {
  transform: translateY(-2px);
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 3rem;
}

.error {
  color: #e74c3c;
  background: #fee;
  border-radius: 8px;
}

.empty-state {
  color: #999;
}

.empty-state small {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.9rem;
}

.reservations-list {
  display: grid;
  gap: 1.5rem;
}

.reservation-card {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.reservation-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.reservation-card.cancelled {
  opacity: 0.6;
  border-color: #bdc3c7;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.status-badge {
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.status-badge.confirmed {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.reservation-id {
  color: #999;
  font-size: 0.85rem;
  font-family: monospace;
}

.card-body {
  padding: 1.5rem;
}

.card-body h3 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.2rem;
}

.reservation-details {
  display: grid;
  gap: 0.8rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
}

.detail-item .icon {
  font-size: 1.1rem;
}

.detail-item .label {
  color: #666;
  font-weight: 600;
  min-width: 80px;
}

.detail-item .value {
  color: #333;
}

.card-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
}

.btn-edit,
.btn-cancel {
  flex: 1;
  padding: 0.7rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-edit {
  background: #3498db;
  color: white;
}

.btn-edit:hover {
  background: #2980b9;
}

.btn-cancel {
  background: #e74c3c;
  color: white;
}

.btn-cancel:hover:not(:disabled) {
  background: #c0392b;
}

.btn-cancel:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h3 {
  color: #333;
  margin-bottom: 1.5rem;
}

.edit-form {
  display: grid;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #555;
  font-size: 0.9rem;
}

.form-group input,
.form-group select {
  padding: 0.7rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
}

.error-message {
  padding: 0.8rem;
  background: #fee;
  color: #c0392b;
  border-radius: 8px;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-save,
.btn-close {
  flex: 1;
  padding: 0.8rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.btn-save {
  background: #27ae60;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #229954;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-close {
  background: #ecf0f1;
  color: #555;
}

.btn-close:hover {
  background: #bdc3c7;
}
</style>
