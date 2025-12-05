/**
 * T110 [P] [US3] Axios API 客戶端配置
 * 自動附加 JWT Token、處理 Token 刷新與錯誤處理
 */

import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import { authService } from './auth.service'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

// 建立 Axios 實例
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 正在刷新 Token 的標記
let isRefreshing = false
// 等待刷新完成的請求佇列
let refreshSubscribers: ((token: string) => void)[] = []

/**
 * 訂閱 Token 刷新完成事件
 */
function subscribeTokenRefresh(callback: (token: string) => void): void {
  refreshSubscribers.push(callback)
}

/**
 * 通知所有訂閱者 Token 已刷新
 */
function onTokenRefreshed(token: string): void {
  refreshSubscribers.forEach((callback) => callback(token))
  refreshSubscribers = []
}

// ==================== 請求攔截器 ====================
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()

    // 自動附加 Access Token
    if (authStore.accessToken && config.headers) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }

    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// ==================== 回應攔截器 ====================
apiClient.interceptors.response.use(
  // 成功回應直接返回
  (response) => response,

  // 錯誤處理
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    const authStore = useAuthStore()

    // 處理 401 未授權錯誤 (Token 過期)
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 避免重複刷新
      if (isRefreshing) {
        // 等待刷新完成後重試
        return new Promise((resolve) => {
          subscribeTokenRefresh((token: string) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`
            }
            resolve(apiClient(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // 嘗試刷新 Token
        if (authStore.refreshToken) {
          const refreshResponse = await authService.refreshToken({
            refreshToken: authStore.refreshToken
          })

          // 更新 Token
          authStore.updateAccessToken(refreshResponse)

          // 通知等待中的請求
          onTokenRefreshed(refreshResponse.accessToken)

          // 重試原始請求
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${refreshResponse.accessToken}`
          }
          return apiClient(originalRequest)
        } else {
          // 沒有 Refresh Token,重定向到登入頁
          authStore.clearAuth()
          window.location.href = '/login'
          return Promise.reject(error)
        }
      } catch (refreshError) {
        // Token 刷新失敗,清除認證並重定向
        authStore.clearAuth()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 處理 403 權限不足錯誤
    if (error.response?.status === 403) {
      console.error('權限不足:', error.response.data)
      // 可選: 顯示提示訊息或重定向到 403 頁面
      window.location.href = '/403'
    }

    // 處理 404 未找到錯誤
    if (error.response?.status === 404) {
      console.error('資源未找到:', error.config?.url)
    }

    // 處理 500 伺服器錯誤
    if (error.response?.status === 500) {
      console.error('伺服器錯誤:', error.response.data)
    }

    // 處理網路錯誤
    if (error.message === 'Network Error') {
      console.error('網路連線失敗,請檢查網路狀態')
    }

    // 處理請求逾時
    if (error.code === 'ECONNABORTED') {
      console.error('請求逾時,請稍後再試')
    }

    return Promise.reject(error)
  }
)

export default apiClient
