/**
 * T068 [P] [US1] 實作預約 API 服務
 * 負責呼叫後端預約相關 API
 */

import apiClient from './api'
import type {
  Reservation,
  CreateReservationRequest,
  CreateReservationResponse
} from '@/types/reservation'

export const reservationService = {
  /**
   * 創建預約
   * POST /reservations
   */
  async createReservation(
    request: CreateReservationRequest
  ): Promise<CreateReservationResponse> {
    const response = await apiClient.post<CreateReservationResponse>('/reservations', request)
    return response.data
  },

  /**
   * 取得我的預約清單
   * GET /reservations
   */
  async getMyReservations(): Promise<Reservation[]> {
    const response = await apiClient.get<Reservation[]>('/reservations')
    return response.data
  },

  /**
   * 取得預約詳情
   * GET /reservations/{id}
   */
  async getReservationById(id: number): Promise<Reservation> {
    const response = await apiClient.get<Reservation>(`/reservations/${id}`)
    return response.data
  }
}
