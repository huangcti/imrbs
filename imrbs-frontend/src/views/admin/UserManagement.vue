<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 頁面標題 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900">
        使用者管理
      </h1>
      <p class="mt-2 text-gray-600">
        管理系統使用者及其權限
      </p>
    </div>

    <!-- 搜尋過濾 -->
    <div class="mb-6 flex justify-between items-center">
      <div class="flex gap-3">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜尋使用者..."
          data-testid="search-input"
          class="px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
        <select
          v-model="roleFilter"
          data-testid="role-filter"
          class="px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">
            所有角色
          </option>
          <option value="EMPLOYEE">
            一般員工
          </option>
          <option value="ROOM_ADMIN">
            會議室管理員
          </option>
          <option value="SYSTEM_ADMIN">
            系統管理員
          </option>
        </select>
      </div>
    </div>

    <!-- 使用者列表 -->
    <div v-if="loading" class="text-center py-12">
      <p class="text-gray-500">
        載入中...
      </p>
    </div>

    <div v-else-if="filteredUsers.length === 0" class="text-center py-12">
      <p class="text-gray-500">
        目前沒有符合條件的使用者
      </p>
    </div>

    <div v-else class="bg-white shadow-sm rounded-lg overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              使用者
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              電子郵件
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              角色
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              狀態
            </th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              操作
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr 
            v-for="user in filteredUsers" 
            :key="user.id"
            :data-testid="`user-row-${user.id}`"
          >
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                  <div class="h-10 w-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-medium">
                    {{ user.name?.charAt(0) || user.username?.charAt(0) || '?' }}
                  </div>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900">
                    {{ user.name || user.username }}
                  </div>
                  <div class="text-sm text-gray-500">
                    {{ user.username }}
                  </div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              {{ user.email || '-' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="getRoleBadgeClass(user.role)">
                {{ getRoleLabel(user.role) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span 
                :class="user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full"
              >
                {{ user.enabled ? '啟用' : '停用' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button
                class="text-blue-600 hover:text-blue-900 mr-3"
                data-testid="edit-user-btn"
                @click="openEditModal(user)"
              >
                編輯
              </button>
              <button
                :class="user.enabled ? 'text-red-600 hover:text-red-900' : 'text-green-600 hover:text-green-900'"
                data-testid="toggle-status-btn"
                @click="toggleUserStatus(user)"
              >
                {{ user.enabled ? '停用' : '啟用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 編輯使用者 Modal -->
    <div 
      v-if="showEditModal" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      data-testid="edit-modal"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <h2 class="text-xl font-bold mb-4">
          編輯使用者
        </h2>
        
        <form @submit.prevent="saveUser">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">使用者名稱</label>
            <input
              v-model="editingUser.username"
              type="text"
              disabled
              class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-100"
            >
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">顯示名稱</label>
            <input
              v-model="editingUser.name"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
            >
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">電子郵件</label>
            <input
              v-model="editingUser.email"
              type="email"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
            >
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-1">角色</label>
            <select
              v-model="editingUser.role"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
            >
              <option value="EMPLOYEE">
                一般員工
              </option>
              <option value="ROOM_ADMIN">
                會議室管理員
              </option>
              <option value="SYSTEM_ADMIN">
                系統管理員
              </option>
            </select>
          </div>

          <div class="flex justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
              @click="closeEditModal"
            >
              取消
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              儲存
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Toast 通知 -->
    <div
      v-if="toast.show"
      :class="toast.type === 'success' ? 'bg-green-500' : 'bg-red-500'"
      class="fixed bottom-4 right-4 px-6 py-3 rounded-md text-white shadow-lg"
    >
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// Types
interface User {
  id: string
  username: string
  name?: string
  email?: string
  role: 'EMPLOYEE' | 'ROOM_ADMIN' | 'SYSTEM_ADMIN'
  enabled: boolean
}

// State
const loading = ref(true)
const users = ref<User[]>([])
const searchQuery = ref('')
const roleFilter = ref('')
const showEditModal = ref(false)
const editingUser = ref<Partial<User>>({})
const toast = ref({ show: false, message: '', type: 'success' })

// Mock data for development
const mockUsers: User[] = [
  { id: '1', username: 'admin', name: '系統管理員', email: 'admin@company.com', role: 'SYSTEM_ADMIN', enabled: true },
  { id: '2', username: 'room_admin', name: '會議室管理員', email: 'room@company.com', role: 'ROOM_ADMIN', enabled: true },
  { id: '3', username: 'employee1', name: '張三', email: 'zhang@company.com', role: 'EMPLOYEE', enabled: true },
  { id: '4', username: 'employee2', name: '李四', email: 'li@company.com', role: 'EMPLOYEE', enabled: false },
  { id: '5', username: 'employee3', name: '王五', email: 'wang@company.com', role: 'EMPLOYEE', enabled: true },
]

// Computed
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchesSearch = !searchQuery.value || 
      user.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      user.name?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchQuery.value.toLowerCase())
    
    const matchesRole = !roleFilter.value || user.role === roleFilter.value

    return matchesSearch && matchesRole
  })
})

// Methods
const getRoleBadgeClass = (role: string) => {
  const baseClass = 'px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full'
  switch (role) {
    case 'SYSTEM_ADMIN':
      return `${baseClass} bg-purple-100 text-purple-800`
    case 'ROOM_ADMIN':
      return `${baseClass} bg-blue-100 text-blue-800`
    default:
      return `${baseClass} bg-gray-100 text-gray-800`
  }
}

const getRoleLabel = (role: string) => {
  switch (role) {
    case 'SYSTEM_ADMIN':
      return '系統管理員'
    case 'ROOM_ADMIN':
      return '會議室管理員'
    default:
      return '一般員工'
  }
}

const openEditModal = (user: User) => {
  editingUser.value = { ...user }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingUser.value = {}
}

const saveUser = async () => {
  // TODO: Implement API call
  const index = users.value.findIndex(u => u.id === editingUser.value.id)
  if (index !== -1) {
    users.value[index] = { ...users.value[index], ...editingUser.value } as User
  }
  showToast('使用者資訊已更新', 'success')
  closeEditModal()
}

const toggleUserStatus = async (user: User) => {
  // TODO: Implement API call
  user.enabled = !user.enabled
  showToast(`使用者已${user.enabled ? '啟用' : '停用'}`, 'success')
}

const showToast = (message: string, type: 'success' | 'error') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// Lifecycle
onMounted(async () => {
  // TODO: Replace with actual API call
  // const response = await userService.getUsers()
  // users.value = response.data
  
  // For now, use mock data in development
  setTimeout(() => {
    users.value = mockUsers
    loading.value = false
  }, 500)
})
</script>
