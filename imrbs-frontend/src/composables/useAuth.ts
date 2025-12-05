/**
 * T106 [P] [US3] 實作認證 Composable
 * 提供認證相關的便捷方法和響應式狀態
 * 
 * 功能:
 * - 登入/登出邏輯封裝
 * - Token 自動刷新
 * - OAuth 2.0 流程處理
 * - 角色權限檢查
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */

import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authService } from '@/services/auth.service'
import { useToast } from './useToast'
import type { UserRole } from '@/types/auth'

/**
 * OAuth 2.0 配置
 * 實際部署時應從環境變數讀取
 */
const OAUTH_CONFIG = {
  clientId: import.meta.env.VITE_OAUTH_CLIENT_ID || 'imrbs-web',
  redirectUri: import.meta.env.VITE_OAUTH_REDIRECT_URI || 'http://localhost:5173/auth/callback',
  authorizationEndpoint:
    import.meta.env.VITE_OAUTH_AUTHORIZATION_ENDPOINT ||
    'http://localhost:8080/oauth2/authorize'
}

/**
 * 認證 Composable
 */
export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()
  const toast = useToast()

  // ==================== 響應式狀態 ====================
  const user = computed(() => authStore.user)
  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const isLoading = computed(() => authStore.isLoading)
  const error = computed(() => authStore.error)
  const userRoles = computed(() => authStore.userRoles)

  // ==================== 認證方法 ====================

  /**
   * 發起 OAuth 2.0 登入流程
   * 重定向到 Keycloak 授權頁面
   */
  function login(): void {
    console.log('[useAuth] 發起 OAuth 登入流程')
    console.log('[useAuth] OAuth 配置:', OAUTH_CONFIG)
    console.log('[useAuth] 環境變數檢查:', {
      VITE_OAUTH_CLIENT_ID: import.meta.env.VITE_OAUTH_CLIENT_ID,
      VITE_OAUTH_REDIRECT_URI: import.meta.env.VITE_OAUTH_REDIRECT_URI,
      VITE_OAUTH_AUTHORIZATION_ENDPOINT: import.meta.env.VITE_OAUTH_AUTHORIZATION_ENDPOINT
    })
    authService.initiateOAuthFlow(
      OAUTH_CONFIG.clientId,
      OAUTH_CONFIG.redirectUri,
      OAUTH_CONFIG.authorizationEndpoint
    )
  }

  /**
   * 處理 OAuth 回調
   * 從 URL 提取 Authorization Code 並交換 Token
   * 
   * @returns 是否成功處理回調
   */
  async function handleOAuthCallback(): Promise<boolean> {
    console.log('[useAuth] 開始處理 OAuth 回調')
    console.log('[useAuth] 當前 URL:', window.location.href)
    console.log('[useAuth] URL 參數:', window.location.search)
    
    authStore.setLoading(true)
    authStore.setError(null)

    try {
      // 檢查是否有錯誤
      const oauthError = authService.extractOAuthError()
      if (oauthError) {
        console.error('[useAuth] OAuth 錯誤:', oauthError)
        authStore.setError(`OAuth 授權失敗: ${oauthError}`)
        toast.error(`登入失敗: ${oauthError}`)
        return false
      }

      // 提取 Authorization Code
      const code = authService.extractAuthorizationCode()
      console.log('[useAuth] Authorization Code:', code ? '存在' : '不存在')
      
      if (!code) {
        console.error('[useAuth] URL 中未找到 code 參數')
        authStore.setError('未找到授權碼。請確認已完成 Keycloak 登入流程。')
        return false
      }

      // 交換 Token
      const loginResponse = await authService.login({
        code,
        redirectUri: OAUTH_CONFIG.redirectUri
      })

      // 獲取使用者資訊
      const userInfo = await authService.getCurrentUser()

      // 設定認證狀態
      authStore.setAuth(loginResponse, userInfo)

      toast.success(`歡迎回來,${userInfo.fullName}!`)
      return true
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '登入失敗'
      authStore.setError(errorMessage)
      toast.error(errorMessage)
      return false
    } finally {
      authStore.setLoading(false)
    }
  }

  /**
   * 登出
   * 清除認證狀態並重定向到登入頁
   */
  async function logout(): Promise<void> {
    try {
      await authService.logout()
      authStore.clearAuth()
      toast.info('已成功登出')
      await router.push('/login')
    } catch (err) {
      console.error('登出失敗:', err)
      // 即使登出失敗也要清除本地狀態
      authStore.clearAuth()
      await router.push('/login')
    }
  }

  /**
   * 刷新 Access Token
   * 
   * @returns 是否成功刷新
   */
  async function refreshAccessToken(): Promise<boolean> {
    if (!authStore.refreshToken) {
      return false
    }

    try {
      const refreshResponse = await authService.refreshToken({
        refreshToken: authStore.refreshToken
      })

      authStore.updateAccessToken(refreshResponse)
      return true
    } catch (err) {
      console.error('刷新 Token 失敗:', err)
      // Token 刷新失敗,清除認證狀態
      authStore.clearAuth()
      await router.push('/login')
      return false
    }
  }

  /**
   * 自動檢查並刷新 Token (如果即將過期)
   */
  async function autoRefreshToken(): Promise<void> {
    if (authStore.isTokenExpiringSoon && authStore.refreshToken) {
      await refreshAccessToken()
    }
  }

  // ==================== 角色權限方法 ====================

  /**
   * 檢查是否擁有特定角色
   */
  function hasRole(role: UserRole): boolean {
    return authStore.hasRole(role)
  }

  /**
   * 檢查是否擁有任一角色
   */
  function hasAnyRole(roles: UserRole[]): boolean {
    return authStore.hasAnyRole(roles)
  }

  /**
   * 檢查是否擁有所有角色
   */
  function hasAllRoles(roles: UserRole[]): boolean {
    return authStore.hasAllRoles(roles)
  }

  /**
   * 檢查是否可以修改預約
   */
  function canModifyReservation(reservationUserId: number): boolean {
    return authStore.canModifyReservation(reservationUserId)
  }

  /**
   * 檢查是否可以管理會議室
   */
  function canManageRooms(): boolean {
    return authStore.canManageRooms()
  }

  /**
   * 檢查是否可以管理使用者
   */
  function canManageUsers(): boolean {
    return authStore.canManageUsers()
  }

  // ==================== 自動 Token 刷新 ====================

  // 監聽認證狀態,定期檢查 Token 是否即將過期
  watch(
    () => authStore.isAuthenticated,
    (authenticated) => {
      if (authenticated) {
        // 每 2 分鐘檢查一次 Token 狀態
        const interval = setInterval(autoRefreshToken, 120000)

        // 組件卸載時清除定時器
        return () => clearInterval(interval)
      }
    },
    { immediate: true }
  )

  return {
    // 狀態
    user,
    isAuthenticated,
    isLoading,
    error,
    userRoles,

    // 認證方法
    login,
    logout,
    handleOAuthCallback,
    refreshAccessToken,

    // 角色權限方法
    hasRole,
    hasAnyRole,
    hasAllRoles,
    canModifyReservation,
    canManageRooms,
    canManageUsers
  }
}
