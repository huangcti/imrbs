<template>
  <div class="reservation-form">
    <h2>📝 建立預約</h2>

    <form @submit.prevent="submitReservation" class="form">
      <div class="form-row">
        <div class="form-group">
          <label>會議室 *</label>
          <select v-model="form.roomId" required>
            <option value="">請選擇會議室</option>
            <option v-for="room in rooms" :key="room.id" :value="room.id">
              {{ room.name }} - {{ room.location }} {{ room.floor }} ({{ room.capacity }}人)
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>預約日期 *</label>
          <input 
            type="date" 
            v-model="form.date" 
            :min="minDate"
            required 
          />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>開始時間 *</label>
          <input 
            type="time" 
            v-model="form.startTime" 
            required 
          />
        </div>

        <div class="form-group">
          <label>結束時間 *</label>
          <input 
            type="time" 
            v-model="form.endTime" 
            required 
          />
        </div>
      </div>

      <div class="form-group">
        <label>會議主題 *</label>
        <input 
          type="text" 
          v-model="form.title" 
          placeholder="例如：Sprint Planning Meeting"
          required 
        />
      </div>

      <div class="form-group">
        <label>聯絡 Email *</label>
        <input 
          type="email" 
          v-model="form.organizerEmail" 
          placeholder="your.email@example.com"
          required 
        />
      </div>

      <div class="form-group">
        <label>參與者 Email (選填，一行一個)</label>
        <textarea 
          v-model="participantsText"
          placeholder="alice@example.com&#10;bob@example.com"
          rows="4"
        ></textarea>
        <small>每行輸入一個 email 地址</small>
      </div>

      <div v-if="error" class="error-message">
        ❌ {{ error }}
      </div>

      <div v-if="success" class="success-message">
        ✅ {{ success }}
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '處理中...' : '🎯 確認預約' }}
        </button>
        <button type="button" class="btn-reset" @click="resetForm">
          🔄 清除表單
        </button>
      </div>
    </form>

    <div class="tips">
      <h3>💡 預約小提示</h3>
      <ul>
        <li>請至少提前 1 小時預約</li>
        <li>系統會自動檢查時間衝突</li>
        <li>預約成功後會寄送確認信至聯絡 Email</li>
        <li>可在「我的預約」頁面修改或取消</li>
      </ul>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'ReservationForm',
  setup() {
    const rooms = ref([])
    const loading = ref(false)
    const error = ref(null)
    const success = ref(null)
    const participantsText = ref('')

    const form = ref({
      roomId: '',
      date: '',
      startTime: '',
      endTime: '',
      title: '',
      organizerEmail: '',
      participants: []
    })

    const minDate = computed(() => {
      return new Date().toISOString().split('T')[0]
    })

    async function loadRooms() {
      try {
        const response = await axios.get('/api/rooms')
        rooms.value = response.data
      } catch (err) {
        console.error('載入會議室列表失敗:', err)
      }
    }

    async function submitReservation() {
      error.value = null
      success.value = null
      loading.value = true

      try {
        // Parse participants from textarea
        const participants = participantsText.value
          .split('\n')
          .map(email => email.trim())
          .filter(email => email && email.includes('@'))

        const payload = {
          roomId: form.value.roomId,
          date: form.value.date,
          startTime: form.value.startTime + ':00',
          endTime: form.value.endTime + ':00',
          title: form.value.title,
          organizerEmail: form.value.organizerEmail,
          participants: participants.length > 0 ? participants : undefined
        }

        const response = await axios.post('/api/reservations', payload)
        
        success.value = `預約成功！預約編號: ${response.data.id.substring(0, 8)}...`
        
        // Reset form after 2 seconds
        setTimeout(() => {
          resetForm()
          success.value = null
        }, 3000)

      } catch (err) {
        if (err.response?.status === 409) {
          error.value = '此時段已被預約，請選擇其他時間'
        } else if (err.response?.status === 400) {
          const details = err.response.data.details
          if (details) {
            error.value = Object.values(details).join('; ')
          } else {
            error.value = err.response.data.error || '輸入資料有誤'
          }
        } else {
          error.value = '預約失敗: ' + (err.response?.data?.message || err.message)
        }
      } finally {
        loading.value = false
      }
    }

    function resetForm() {
      form.value = {
        roomId: '',
        date: '',
        startTime: '',
        endTime: '',
        title: '',
        organizerEmail: '',
        participants: []
      }
      participantsText.value = ''
      error.value = null
      success.value = null
    }

    onMounted(() => {
      loadRooms()
    })

    return {
      rooms,
      form,
      participantsText,
      loading,
      error,
      success,
      minDate,
      submitReservation,
      resetForm
    }
  }
}
</script>

<style scoped>
.reservation-form {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  max-width: 800px;
}

.reservation-form h2 {
  color: #333;
  margin-bottom: 2rem;
  font-size: 1.5rem;
}

.form {
  margin-bottom: 2rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #555;
  font-size: 0.95rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.8rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group small {
  color: #999;
  font-size: 0.85rem;
}

.form-group textarea {
  resize: vertical;
}

.error-message,
.success-message {
  padding: 1rem;
  border-radius: 8px;
  margin: 1rem 0;
  font-weight: 500;
}

.error-message {
  background: #fee;
  color: #c0392b;
  border-left: 4px solid #e74c3c;
}

.success-message {
  background: #d4edda;
  color: #155724;
  border-left: 4px solid #27ae60;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-submit,
.btn-reset {
  flex: 1;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-reset {
  background: #ecf0f1;
  color: #555;
}

.btn-reset:hover {
  background: #bdc3c7;
}

.tips {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  border-left: 4px solid #3498db;
}

.tips h3 {
  color: #3498db;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.tips ul {
  list-style: none;
  padding: 0;
}

.tips li {
  padding: 0.5rem 0;
  color: #666;
  position: relative;
  padding-left: 1.5rem;
}

.tips li:before {
  content: "→";
  position: absolute;
  left: 0;
  color: #3498db;
  font-weight: bold;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>
