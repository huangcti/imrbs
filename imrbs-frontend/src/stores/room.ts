/**
 * T065 [P] [US1] 實作會議室查詢 Pinia Store
 * 管理會議室查詢狀態與快取
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { roomService } from '@/services/room.service'
import type { Room, RoomSearchParams, RoomAvailability } from '@/types/room'

export const useRoomStore = defineStore('room', () => {
  // State
  const rooms = ref<Room[]>([])
  const currentRoom = ref<Room | null>(null)
  const currentAvailability = ref<RoomAvailability | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const totalRooms = ref(0)

  // Getters
  const hasRooms = computed(() => rooms.value.length > 0)
  const isLoading = computed(() => loading.value)
  const errorMessage = computed(() => error.value)

  // Actions

  /**
   * 搜尋可用會議室
   */
  async function searchRooms(params: RoomSearchParams): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const result = await roomService.searchRooms(params)
      rooms.value = result.data
      totalRooms.value = result.total
    } catch (err) {
      error.value = err instanceof Error ? err.message : '查詢會議室失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 取得會議室詳情
   */
  async function fetchRoomById(id: number): Promise<Room> {
    loading.value = true
    error.value = null
    try {
      const room = await roomService.getRoomById(id)
      currentRoom.value = room
      return room
    } catch (err) {
      error.value = err instanceof Error ? err.message : '取得會議室詳情失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 取得會議室可用時段
   */
  async function fetchRoomAvailability(id: number, date: string): Promise<RoomAvailability> {
    loading.value = true
    error.value = null
    try {
      const availability = await roomService.getRoomAvailability(id, date)
      currentAvailability.value = availability
      return availability
    } catch (err) {
      error.value = err instanceof Error ? err.message : '取得可用時段失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 清除搜尋結果
   */
  function clearRooms(): void {
    rooms.value = []
    totalRooms.value = 0
    error.value = null
  }

  /**
   * 清除當前會議室
   */
  function clearCurrentRoom(): void {
    currentRoom.value = null
    currentAvailability.value = null
  }

  return {
    // State
    rooms,
    currentRoom,
    currentAvailability,
    loading,
    error,
    totalRooms,
    // Getters
    hasRooms,
    isLoading,
    errorMessage,
    // Actions
    searchRooms,
    fetchRoomById,
    fetchRoomAvailability,
    clearRooms,
    clearCurrentRoom
  }
})
