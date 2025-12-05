/**
 * T159 訪客預約 API 服務
 * 處理訪客預約申請提交、狀態查詢、管理員審核等 API
 */

import axios from 'axios'
import apiClient from './api'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// 公開端點 (無需認證)
const publicClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// ==================== 類型定義 ====================

export interface GuestRequestData {
  guestName: string
  guestEmail: string
  guestPhone?: string
  guestCompany?: string
  roomId: number
  meetingTitle: string
  requestedStartTime: string // ISO 8601 格式
  requestedEndTime: string
  attendeeCount?: number
  purpose?: string
}

export interface GuestRequestResponse {
  id: number
  guestName: string
  guestEmail: string
  guestPhone?: string
  guestCompany?: string
  roomId: number
  meetingTitle: string
  requestedStartTime: string
  requestedEndTime: string
  meetingPurpose?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  reviewedBy?: number
  reviewedAt?: string
  rejectionReason?: string
  reservationId?: number
  createdAt: string
}

export interface RejectRequestData {
  reason: string
}

// ==================== 公開 API (訪客端) ====================

/**
 * 訪客提交預約申請
 */
export async function submitGuestRequest(data: GuestRequestData): Promise<GuestRequestResponse> {
  const response = await publicClient.post<GuestRequestResponse>('/guest/requests', data)
  return response.data
}

/**
 * 訪客查詢申請狀態
 */
export async function getGuestRequestStatus(
  requestId: number,
  email: string
): Promise<GuestRequestResponse> {
  const response = await publicClient.get<GuestRequestResponse>(`/guest/requests/${requestId}`, {
    params: { email }
  })
  return response.data
}

// ==================== 管理 API (管理員端) ====================

/**
 * 查詢訪客申請清單
 */
export async function getGuestRequests(
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
): Promise<GuestRequestResponse[]> {
  const params: Record<string, string> = {}
  if (status) {
    params.status = status
  }
  const response = await apiClient.get<GuestRequestResponse[]>('/admin/guest-requests', { params })
  return response.data
}

/**
 * 批准訪客申請
 */
export async function approveGuestRequest(requestId: number): Promise<GuestRequestResponse> {
  const response = await apiClient.post<GuestRequestResponse>(
    `/admin/guest-requests/${requestId}/approve`
  )
  return response.data
}

/**
 * 拒絕訪客申請
 */
export async function rejectGuestRequest(
  requestId: number,
  data: RejectRequestData
): Promise<GuestRequestResponse> {
  const response = await apiClient.post<GuestRequestResponse>(
    `/admin/guest-requests/${requestId}/reject`,
    data
  )
  return response.data
}

// ==================== 導出服務物件 ====================

export const guestService = {
  submitGuestRequest,
  getGuestRequestStatus,
  getGuestRequests,
  approveGuestRequest,
  rejectGuestRequest
}

export default guestService
