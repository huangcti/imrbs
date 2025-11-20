/**
 * T067 [P] [US1] 實作會議室 API 服務
 * 負責呼叫後端會議室相關 API
 */

import apiClient from './api'
import type { Room, RoomSearchParams, RoomSearchResult, RoomAvailability } from '@/types/room'

export const roomService = {
  /**
   * 查詢可用會議室
   * GET /rooms
   */
  async searchRooms(params: RoomSearchParams): Promise<RoomSearchResult> {
    const response = await apiClient.get<RoomSearchResult>('/rooms', { params })
    return response.data
  },

  /**
   * 取得會議室詳情
   * GET /rooms/{id}
   */
  async getRoomById(id: number): Promise<Room> {
    const response = await apiClient.get<Room>(`/rooms/${id}`)
    return response.data
  },

  /**
   * 取得會議室可用時段
   * GET /rooms/{id}/availability
   */
  async getRoomAvailability(id: number, date: string): Promise<RoomAvailability> {
    const response = await apiClient.get<RoomAvailability>(`/rooms/${id}/availability`, {
      params: { date }
    })
    return response.data
  }
}
