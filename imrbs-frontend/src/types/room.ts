/**
 * 會議室相關型別定義
 */

export type RoomStatus = 'AVAILABLE' | 'UNAVAILABLE' | 'MAINTENANCE'

export interface Room {
  id: number
  name: string
  capacity: number
  floor: number
  building: string
  equipment: string[]
  features: Record<string, unknown>
  photos: string[]
  status: RoomStatus
  description?: string
}

export interface TimeSlot {
  startTime: string
  endTime: string
  available: boolean
}

export interface RoomAvailability {
  date: string
  roomId: number
  availableSlots: TimeSlot[]
}

export interface RoomSearchParams {
  date: string
  startTime: string
  endTime: string
  capacity?: number
  equipment?: string[]
  building?: string
  floor?: number
  name?: string
}

export interface RoomSearchResult {
  data: Room[]
  total: number
}
