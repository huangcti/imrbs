/**
 * 認證相關型別定義
 * T104 [P] [US3] 實作認證 Pinia Store
 */

/**
 * 使用者角色
 */
export type UserRole = 'EMPLOYEE' | 'ROOM_ADMIN' | 'SYSTEM_ADMIN'

/**
 * 使用者資訊
 */
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  department: string
  roles: UserRole[]
  createdAt: string
  updatedAt: string
}

/**
 * 登入請求
 */
export interface LoginRequest {
  code: string
  redirectUri: string
}

/**
 * 登入回應
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
}

/**
 * 刷新 Token 請求
 */
export interface RefreshTokenRequest {
  refreshToken: string
}

/**
 * 刷新 Token 回應
 */
export interface RefreshTokenResponse {
  accessToken: string
  expiresIn: number
}

/**
 * 認證狀態
 */
export interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
  expiresAt: number | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
}
