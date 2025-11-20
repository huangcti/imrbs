/**
 * 預約相關型別定義
 */

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED'

export interface Reservation {
  id: number
  roomId: number
  roomName?: string
  userId: string
  userName?: string
  startTime: string
  endTime: string
  purpose: string
  participants: string[]
  status: ReservationStatus
  createdAt: string
  updatedAt: string
}

export interface CreateReservationRequest {
  roomId: number
  startTime: string
  endTime: string
  purpose: string
  participants: string[]
}

export interface CreateReservationResponse {
  id: number
  message: string
}
