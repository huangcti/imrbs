<template>
  <div class="app">
    <header class="header">
      <div class="container">
        <h1>🏢 會議室預約系統</h1>
        <nav>
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            :class="['nav-btn', { active: currentTab === tab.id }]"
            @click="currentTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </nav>
      </div>
    </header>

    <main class="container main-content">
      <component :is="currentComponent" @refresh="handleRefresh" />
    </main>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import RoomList from './components/RoomList.vue'
import ReservationForm from './components/ReservationForm.vue'
import MyReservations from './components/MyReservations.vue'
import AdminPanel from './components/AdminPanel.vue'

export default {
  name: 'App',
  components: {
    RoomList,
    ReservationForm,
    MyReservations,
    AdminPanel
  },
  setup() {
    const currentTab = ref('rooms')
    
    const tabs = [
      { id: 'rooms', label: '會議室查詢', component: 'RoomList' },
      { id: 'reserve', label: '建立預約', component: 'ReservationForm' },
      { id: 'my-reservations', label: '我的預約', component: 'MyReservations' },
      { id: 'admin', label: '管理會議室', component: 'AdminPanel' }
    ]

    const currentComponent = computed(() => {
      return tabs.find(t => t.id === currentTab.value)?.component || 'RoomList'
    })

    const handleRefresh = () => {
      // Trigger refresh if needed
    }

    return {
      currentTab,
      tabs,
      currentComponent,
      handleRefresh
    }
  }
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.header h1 {
  margin-bottom: 1rem;
  font-size: 1.8rem;
}

nav {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.nav-btn {
  padding: 0.6rem 1.2rem;
  border: 2px solid rgba(255,255,255,0.3);
  background: transparent;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.3s;
  font-weight: 500;
}

.nav-btn:hover {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.5);
}

.nav-btn.active {
  background: white;
  color: #667eea;
  border-color: white;
}

.main-content {
  padding: 2rem;
}
</style>
