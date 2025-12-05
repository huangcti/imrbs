/**
 * T105 [P] [US3] 實作認證 API 服務
 * 處理 OAuth 2.0 登入、Token 刷新、使用者資訊查詢
 * 
 * API 端點:
 * - POST /api/v1/auth/login - OAuth 2.0 Authorization Code 交換
 * - POST /api/v1/auth/refresh - 刷新 Access Token
 * - GET /api/v1/auth/me - 獲取當前使用者資訊
 * 
 * @author IMRBS Team
 * @since 2025-11-24
 */

import apiClient from './api'
import type {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  User
} from '@/types/auth'

/**
 * 認證服務
 */
class AuthService {
  private readonly AUTH_BASE = '/auth'

  /**
   * OAuth 2.0 登入
   * 使用 Authorization Code 交換 Access Token
   * 
   * @param loginRequest - 登入請求 (包含 code 和 redirect_uri)
   * @returns 登入回應 (包含 access_token, refresh_token)
   * @throws Error 如果登入失敗
   */
  async login(loginRequest: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<LoginResponse>(
        `${this.AUTH_BASE}/login`,
        {
          code: loginRequest.code,
          redirectUri: loginRequest.redirectUri
        }
      )

      return {
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        expiresIn: response.data.expiresIn,
        tokenType: response.data.tokenType || 'Bearer'
      }
    } catch (error) {
      console.error('登入失敗:', error)
      throw new Error('登入失敗,請稍後再試')
    }
  }

  /**
   * 刷新 Access Token
   * 使用 Refresh Token 獲取新的 Access Token
   * 
   * @param refreshTokenRequest - 刷新 Token 請求
   * @returns 刷新 Token 回應 (包含新的 access_token)
   * @throws Error 如果刷新失敗
   */
  async refreshToken(refreshTokenRequest: RefreshTokenRequest): Promise<RefreshTokenResponse> {
    try {
      const response = await apiClient.post<RefreshTokenResponse>(
        `${this.AUTH_BASE}/refresh`,
        {
          refresh_token: refreshTokenRequest.refreshToken
        }
      )

      return {
        accessToken: response.data.accessToken,
        expiresIn: response.data.expiresIn
      }
    } catch (error) {
      console.error('刷新 Token 失敗:', error)
      throw new Error('刷新 Token 失敗,請重新登入')
    }
  }

  /**
   * 獲取當前使用者資訊
   * 需要有效的 Access Token
   * 
   * @returns 當前使用者資訊
   * @throws Error 如果獲取失敗
   */
  async getCurrentUser(): Promise<User> {
    try {
      const response = await apiClient.get<User>(`${this.AUTH_BASE}/me`)
      return response.data
    } catch (error) {
      console.error('獲取使用者資訊失敗:', error)
      throw new Error('獲取使用者資訊失敗')
    }
  }

  /**
   * 登出 (僅清除前端狀態)
   * 注意: 後端可能需要額外實作 Token 撤銷端點
   */
  async logout(): Promise<void> {
    // 如果後端有 /auth/logout 端點,可以在這裡調用
    // await apiClient.post(`${this.AUTH_BASE}/logout`)
    
    // 清除前端狀態將由 AuthStore 處理
  }

  /**
   * 發起 OAuth 2.0 授權流程
   * 重定向到 Keycloak 登入頁面
   * 
   * @param clientId - OAuth 2.0 Client ID
   * @param redirectUri - 登入後的回調 URI
   * @param authorizationEndpoint - Keycloak 授權端點
   */
  initiateOAuthFlow(
    clientId: string,
    redirectUri: string,
    authorizationEndpoint: string
  ): void {
    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      response_type: 'code',
      scope: 'openid profile email'
    })

    const authUrl = `${authorizationEndpoint}?${params.toString()}`
    console.log('[AuthService] 準備重定向到授權端點')
    console.log('[AuthService] Client ID:', clientId)
    console.log('[AuthService] Redirect URI:', redirectUri)
    console.log('[AuthService] 授權 URL:', authUrl)
    window.location.href = authUrl
  }

  /**
   * 從 URL 查詢參數中提取 Authorization Code
   * 
   * @returns Authorization Code 或 null
   */
  extractAuthorizationCode(): string | null {
    const urlParams = new URLSearchParams(window.location.search)
    return urlParams.get('code')
  }

  /**
   * 檢查 URL 是否包含 OAuth 錯誤
   * 
   * @returns 錯誤訊息或 null
   */
  extractOAuthError(): string | null {
    const urlParams = new URLSearchParams(window.location.search)
    const error = urlParams.get('error')
    const errorDescription = urlParams.get('error_description')

    if (error) {
      return errorDescription || error
    }
    return null
  }
}

// 匯出單例實例
export const authService = new AuthService()
