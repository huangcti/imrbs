/**
 * T104 [P] [US3] 實作認證 Pinia Store
 * 管理使用者認證狀態、Token 與角色權限
 * 
 * 功能:
 * - 使用者登入/登出
 * - Token 管理 (Access Token + Refresh Token)
 * - 自動刷新 Token
 * - 角色權限檢查
 * - 狀態持久化 (localStorage)
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  User,
  LoginResponse,
  RefreshTokenResponse,
  UserRole
} from '@/types/auth'

// Storage keys
const STORAGE_KEYS = {
  ACCESS_TOKEN: 'imrbs_access_token',
  REFRESH_TOKEN: 'imrbs_refresh_token',
  EXPIRES_AT: 'imrbs_expires_at',
  USER: 'imrbs_user'
}

export const useAuthStore = defineStore('auth', () => {
  // ==================== State ====================
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const expiresAt = ref<number | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // ==================== Getters ====================
  
  /**
   * 是否已認證 (Token 有效且未過期)
   */
  const isAuthenticated = computed(() => {
    if (!accessToken.value || !expiresAt.value) {
      return false
    }
    // 檢查 Token 是否過期 (預留 60 秒緩衝)
    return Date.now() < expiresAt.value - 60000
  })

  /**
   * Token 是否即將過期 (5分鐘內)
   */
  const isTokenExpiringSoon = computed(() => {
    if (!expiresAt.value) return false
    return Date.now() > expiresAt.value - 300000 // 5 分鐘 = 300000 毫秒
  })

  /**
   * 取得使用者角色清單
   */
  const userRoles = computed(() => user.value?.roles || [])

  /**
   * 是否為一般員工
   */
  const isEmployee = computed(() => userRoles.value.includes('EMPLOYEE'))

  /**
   * 是否為會議室管理員
   */
  const isRoomAdmin = computed(() => userRoles.value.includes('ROOM_ADMIN'))

  /**
   * 是否為系統管理員
   */
  const isSystemAdmin = computed(() => userRoles.value.includes('SYSTEM_ADMIN'))

  // ==================== Actions ====================

  /**
   * 開發模式自動登入
   * 當 VITE_DEV_MODE=true 時，自動使用模擬使用者登入
   */
  function initDevMode(): boolean {
    const isDevMode = import.meta.env.VITE_DEV_MODE === 'true'
    
    if (isDevMode) {
      console.log('🔧 開發模式啟用 - 自動登入模擬使用者')
      
      const devUser: User = {
        id: Number(import.meta.env.VITE_DEV_USER_ID) || 1,
        username: 'dev-user',
        fullName: import.meta.env.VITE_DEV_USER_NAME || '開發者',
        email: import.meta.env.VITE_DEV_USER_EMAIL || 'dev@example.com',
        roles: (import.meta.env.VITE_DEV_USER_ROLES || 'EMPLOYEE,ROOM_ADMIN').split(',') as UserRole[],
        department: '開發部門',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }

      // 設定模擬 Token (1小時有效)
      accessToken.value = 'dev-mock-access-token'
      refreshToken.value = 'dev-mock-refresh-token'
      expiresAt.value = Date.now() + 3600000 // 1 小時
      user.value = devUser
      
      return true
    }
    
    return false
  }

  /**
   * 從 localStorage 恢復認證狀態
   */
  function restoreAuth(): void {
    // 開發模式：自動登入
    if (initDevMode()) {
      return
    }

    try {
      const storedToken = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
      const storedRefreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
      const storedExpiresAt = localStorage.getItem(STORAGE_KEYS.EXPIRES_AT)
      const storedUser = localStorage.getItem(STORAGE_KEYS.USER)

      if (storedToken && storedExpiresAt && storedUser) {
        accessToken.value = storedToken
        refreshToken.value = storedRefreshToken
        expiresAt.value = parseInt(storedExpiresAt, 10)
        user.value = JSON.parse(storedUser)

        // 如果 Token 已過期,自動清除
        if (!isAuthenticated.value) {
          clearAuth()
        }
      }
    } catch (err) {
      console.error('恢復認證狀態失敗:', err)
      clearAuth()
    }
  }

  /**
   * 設定認證資訊
   */
  function setAuth(loginResponse: LoginResponse, userInfo: User): void {
    const expiresAtTimestamp = Date.now() + loginResponse.expiresIn * 1000

    // 更新 State
    accessToken.value = loginResponse.accessToken
    refreshToken.value = loginResponse.refreshToken
    expiresAt.value = expiresAtTimestamp
    user.value = userInfo

    // 持久化到 localStorage
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, loginResponse.accessToken)
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, loginResponse.refreshToken)
    localStorage.setItem(STORAGE_KEYS.EXPIRES_AT, expiresAtTimestamp.toString())
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userInfo))

    error.value = null
  }

  /**
   * 更新 Access Token (刷新後)
   */
  function updateAccessToken(refreshResponse: RefreshTokenResponse): void {
    const expiresAtTimestamp = Date.now() + refreshResponse.expiresIn * 1000

    accessToken.value = refreshResponse.accessToken
    expiresAt.value = expiresAtTimestamp

    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, refreshResponse.accessToken)
    localStorage.setItem(STORAGE_KEYS.EXPIRES_AT, expiresAtTimestamp.toString())
  }

  /**
   * 清除認證資訊
   */
  function clearAuth(): void {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    expiresAt.value = null
    error.value = null

    // 清除 localStorage
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
    localStorage.removeItem(STORAGE_KEYS.EXPIRES_AT)
    localStorage.removeItem(STORAGE_KEYS.USER)
  }

  /**
   * 設定載入狀態
   */
  function setLoading(loading: boolean): void {
    isLoading.value = loading
  }

  /**
   * 設定錯誤訊息
   */
  function setError(errorMessage: string | null): void {
    error.value = errorMessage
  }

  /**
   * 檢查使用者是否擁有特定角色
   */
  function hasRole(role: UserRole): boolean {
    return userRoles.value.includes(role)
  }

  /**
   * 檢查使用者是否擁有任一角色
   */
  function hasAnyRole(roles: UserRole[]): boolean {
    return roles.some((role) => userRoles.value.includes(role))
  }

  /**
   * 檢查使用者是否擁有所有角色
   */
  function hasAllRoles(roles: UserRole[]): boolean {
    return roles.every((role) => userRoles.value.includes(role))
  }

  /**
   * 檢查使用者是否可以修改預約
   * (擁有者本人或管理員)
   */
  function canModifyReservation(reservationUserId: number): boolean {
    if (!user.value) return false
    return user.value.id === reservationUserId || isRoomAdmin.value || isSystemAdmin.value
  }

  /**
   * 檢查使用者是否可以管理會議室
   */
  function canManageRooms(): boolean {
    return isRoomAdmin.value || isSystemAdmin.value
  }

  /**
   * 檢查使用者是否可以管理使用者
   */
  function canManageUsers(): boolean {
    return isSystemAdmin.value
  }

  // 初始化時恢復認證狀態
  restoreAuth()

  return {
    // State
    user,
    accessToken,
    refreshToken,
    expiresAt,
    isLoading,
    error,

    // Getters
    isAuthenticated,
    isTokenExpiringSoon,
    userRoles,
    isEmployee,
    isRoomAdmin,
    isSystemAdmin,

    // Actions
    setAuth,
    updateAccessToken,
    clearAuth,
    setLoading,
    setError,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    canModifyReservation,
    canManageRooms,
    canManageUsers,
    restoreAuth
  }
})
