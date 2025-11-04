<template>
  <div class="admin-panel">
    <h2>⚙️ 管理會議室</h2>

    <div class="admin-actions">
      <button class="btn-add" @click="showAddForm = !showAddForm">
        {{ showAddForm ? '❌ 取消新增' : '➕ 新增會議室' }}
      </button>
    </div>

    <!-- Add Room Form -->
    <div v-if="showAddForm" class="add-form">
      <h3>新增會議室</h3>
      <form @submit.prevent="addRoom" class="room-form">
        <div class="form-row">
          <div class="form-group">
            <label>會議室 ID *</label>
            <input 
              type="text" 
              v-model="newRoom.id" 
              placeholder="例如: room-007"
              required 
            />
          </div>

          <div class="form-group">
            <label>會議室名稱 *</label>
            <input 
              type="text" 
              v-model="newRoom.name" 
              placeholder="例如: 901會議室"
              required 
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>地點 *</label>
            <select v-model="newRoom.location" required>
              <option value="">請選擇</option>
              <option value="板橋">板橋</option>
              <option value="民生">民生</option>
            </select>
          </div>

          <div class="form-group">
            <label>樓層 *</label>
            <input 
              type="text" 
              v-model="newRoom.floor" 
              placeholder="例如: 9樓"
              required 
            />
          </div>
        </div>

        <div class="form-group">
          <label>容量 *</label>
          <input 
            type="number" 
            v-model.number="newRoom.capacity" 
            min="1"
            max="100"
            required 
          />
        </div>

        <div v-if="addError" class="error-message">
          ❌ {{ addError }}
        </div>

        <button type="submit" class="btn-submit" :disabled="adding">
          {{ adding ? '新增中...' : '✅ 確認新增' }}
        </button>
      </form>
    </div>

    <!-- Rooms List -->
    <div v-if="loading" class="loading">載入中...</div>

    <div v-else-if="error" class="error">{{ error }}</div>

    <div v-else class="rooms-table">
      <div class="table-header">
        <div class="col-id">ID</div>
        <div class="col-name">名稱</div>
        <div class="col-location">地點</div>
        <div class="col-floor">樓層</div>
        <div class="col-capacity">容量</div>
        <div class="col-actions">操作</div>
      </div>

      <div 
        v-for="room in sortedRooms" 
        :key="room.id"
        :class="['table-row', { editing: editingId === room.id }]"
      >
        <template v-if="editingId === room.id">
          <!-- Edit Mode -->
          <div class="col-id">{{ room.id }}</div>
          <div class="col-name">
            <input v-model="editForm.name" required />
          </div>
          <div class="col-location">
            <select v-model="editForm.location" required>
              <option value="板橋">板橋</option>
              <option value="民生">民生</option>
            </select>
          </div>
          <div class="col-floor">
            <input v-model="editForm.floor" required />
          </div>
          <div class="col-capacity">
            <input type="number" v-model.number="editForm.capacity" min="1" required />
          </div>
          <div class="col-actions">
            <button class="btn-save-small" @click="saveRoom(room.id)" :disabled="updating">
              💾 儲存
            </button>
            <button class="btn-cancel-small" @click="cancelEdit">
              ❌ 取消
            </button>
          </div>
        </template>

        <template v-else>
          <!-- View Mode -->
          <div class="col-id">
            <code>{{ room.id }}</code>
          </div>
          <div class="col-name">
            <strong>{{ room.name }}</strong>
          </div>
          <div class="col-location">
            <span class="location-badge">{{ room.location }}</span>
          </div>
          <div class="col-floor">
            {{ room.floor }}
          </div>
          <div class="col-capacity">
            <span class="capacity-badge">{{ room.capacity }} 人</span>
          </div>
          <div class="col-actions">
            <button class="btn-edit-small" @click="startEdit(room)">
              ✏️ 編輯
            </button>
            <button 
              class="btn-delete-small" 
              @click="deleteRoom(room.id)"
              :disabled="deleting === room.id"
            >
              {{ deleting === room.id ? '刪除中...' : '🗑️ 刪除' }}
            </button>
          </div>
        </template>
      </div>
    </div>

    <div v-if="!loading && sortedRooms.length === 0" class="empty-state">
      目前沒有會議室資料
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'AdminPanel',
  setup() {
    const rooms = ref([])
    const loading = ref(false)
    const error = ref(null)
    const showAddForm = ref(false)
    const adding = ref(false)
    const addError = ref(null)
    const editingId = ref(null)
    const editForm = ref({})
    const updating = ref(false)
    const deleting = ref(null)

    const newRoom = ref({
      id: '',
      name: '',
      location: '',
      floor: '',
      capacity: 10,
      metadata: '{}'
    })

    const sortedRooms = computed(() => {
      return [...rooms.value].sort((a, b) => {
        if (a.location !== b.location) {
          return a.location.localeCompare(b.location)
        }
        return a.id.localeCompare(b.id)
      })
    })

    async function loadRooms() {
      loading.value = true
      error.value = null

      try {
        const response = await axios.get('/api/rooms')
        rooms.value = response.data
      } catch (err) {
        error.value = '載入會議室列表失敗: ' + (err.response?.data?.message || err.message)
      } finally {
        loading.value = false
      }
    }

    async function addRoom() {
      adding.value = true
      addError.value = null

      try {
        await axios.post('/api/admin/rooms', newRoom.value)
        
        // Reset form and reload
        newRoom.value = {
          id: '',
          name: '',
          location: '',
          floor: '',
          capacity: 10,
          metadata: '{}'
        }
        showAddForm.value = false
        await loadRooms()
      } catch (err) {
        if (err.response?.status === 400) {
          const details = err.response.data.details
          if (details) {
            addError.value = Object.values(details).join('; ')
          } else {
            addError.value = err.response.data.error || '輸入資料有誤'
          }
        } else {
          addError.value = '新增失敗: ' + (err.response?.data?.message || err.message)
        }
      } finally {
        adding.value = false
      }
    }

    function startEdit(room) {
      editingId.value = room.id
      editForm.value = {
        name: room.name,
        location: room.location,
        floor: room.floor,
        capacity: room.capacity,
        metadata: room.metadata || '{}'
      }
    }

    function cancelEdit() {
      editingId.value = null
      editForm.value = {}
    }

    async function saveRoom(roomId) {
      updating.value = true

      try {
        await axios.put(`/api/admin/rooms/${roomId}`, {
          id: roomId,
          ...editForm.value
        })
        
        editingId.value = null
        editForm.value = {}
        await loadRooms()
      } catch (err) {
        alert('更新失敗: ' + (err.response?.data?.message || err.message))
      } finally {
        updating.value = false
      }
    }

    async function deleteRoom(roomId) {
      if (!confirm(`確定要刪除會議室 ${roomId} 嗎？`)) {
        return
      }

      deleting.value = roomId

      try {
        await axios.delete(`/api/admin/rooms/${roomId}`)
        await loadRooms()
      } catch (err) {
        alert('刪除失敗: ' + (err.response?.data?.message || err.message))
      } finally {
        deleting.value = null
      }
    }

    onMounted(() => {
      loadRooms()
    })

    return {
      rooms,
      sortedRooms,
      loading,
      error,
      showAddForm,
      newRoom,
      adding,
      addError,
      editingId,
      editForm,
      updating,
      deleting,
      loadRooms,
      addRoom,
      startEdit,
      cancelEdit,
      saveRoom,
      deleteRoom
    }
  }
}
</script>

<style scoped>
.admin-panel {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.admin-panel h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.admin-actions {
  margin-bottom: 2rem;
}

.btn-add {
  padding: 0.8rem 1.5rem;
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-add:hover {
  transform: translateY(-2px);
}

.add-form {
  background: #f8f9fa;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.add-form h3 {
  color: #333;
  margin-bottom: 1.5rem;
}

.room-form {
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

.btn-submit {
  padding: 0.8rem;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #229954;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.error {
  color: #e74c3c;
  background: #fee;
  border-radius: 8px;
}

.rooms-table {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
}

.table-header,
.table-row {
  display: grid;
  grid-template-columns: 150px 1fr 120px 100px 100px 200px;
  gap: 1rem;
  padding: 1rem;
  align-items: center;
}

.table-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
}

.table-row {
  border-bottom: 1px solid #e0e0e0;
  transition: background 0.2s;
}

.table-row:hover {
  background: #f8f9fa;
}

.table-row.editing {
  background: #fff9e6;
}

.table-row:last-child {
  border-bottom: none;
}

.col-id code {
  background: #f0f0f0;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #667eea;
}

.location-badge {
  background: #667eea;
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.capacity-badge {
  background: #ecf0f1;
  padding: 0.3rem 0.6rem;
  border-radius: 8px;
  font-weight: 600;
  color: #555;
}

.col-actions {
  display: flex;
  gap: 0.5rem;
}

.col-actions input,
.col-actions select {
  width: 100%;
  padding: 0.4rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-edit-small,
.btn-delete-small,
.btn-save-small,
.btn-cancel-small {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-edit-small {
  background: #3498db;
  color: white;
}

.btn-edit-small:hover {
  background: #2980b9;
}

.btn-delete-small {
  background: #e74c3c;
  color: white;
}

.btn-delete-small:hover:not(:disabled) {
  background: #c0392b;
}

.btn-delete-small:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-save-small {
  background: #27ae60;
  color: white;
}

.btn-save-small:hover:not(:disabled) {
  background: #229954;
}

.btn-save-small:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel-small {
  background: #ecf0f1;
  color: #555;
}

.btn-cancel-small:hover {
  background: #bdc3c7;
}

@media (max-width: 1024px) {
  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }

  .col-actions {
    justify-content: flex-start;
  }
}
</style>
