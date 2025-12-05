/**
 * T066 [P] [US1] 實作預約 Pinia Store
 * 管理預約創建與狀態
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { reservationService } from '@/services/reservation.service'
import type { Reservation, CreateReservationRequest } from '@/types/reservation'
import type { UpdateReservationRequest } from '@/types/reservation'

export const useReservationStore = defineStore('reservation', () => {
  // State
  const reservations = ref<Reservation[]>([])
  const currentReservation = ref<Reservation | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const hasReservations = computed(() => reservations.value.length > 0)
  const isLoading = computed(() => loading.value)
  const errorMessage = computed(() => error.value)

  // Actions

  /**
   * 創建預約
   */
  async function createReservation(request: CreateReservationRequest): Promise<number> {
    loading.value = true
    error.value = null
    try {
      const response = await reservationService.createReservation(request)
      return response.id
    } catch (err) {
      error.value = err instanceof Error ? err.message : '創建預約失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 取得我的預約清單
   */
  async function fetchMyReservations(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      reservations.value = await reservationService.getMyReservations()
    } catch (err) {
      error.value = err instanceof Error ? err.message : '取得預約清單失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 取得預約詳情
   */
  async function fetchReservationById(id: number): Promise<Reservation> {
    loading.value = true
    error.value = null
    try {
      const reservation = await reservationService.getReservationById(id)
      currentReservation.value = reservation
      return reservation
    } catch (err) {
      error.value = err instanceof Error ? err.message : '取得預約詳情失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 清除預約清單
   */
  function clearReservations(): void {
    reservations.value = []
    error.value = null
  }

  /**
   * 清除當前預約
   */
  function clearCurrentReservation(): void {
    currentReservation.value = null
  }

  /**
   * 更新預約
   */
  async function updateReservation(
    id: number,
    request: UpdateReservationRequest
  ): Promise<Reservation> {
    loading.value = true
    error.value = null
    try {
      const updated = await reservationService.updateReservation(id, request)
      // 更新清單中的預約
      const index = reservations.value.findIndex((r) => r.id === id)
      if (index !== -1) {
        reservations.value[index] = updated
      }
      // 更新當前預約
      if (currentReservation.value?.id === id) {
        currentReservation.value = updated
      }
      return updated
    } catch (err) {
      error.value = err instanceof Error ? err.message : '更新預約失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消預約
   */
  async function cancelReservation(id: number, reason: string): Promise<void> {
    loading.value = true
    error.value = null
    try {
      await reservationService.cancelReservation(id, { cancellationReason: reason })
      // 從清單中移除或更新狀態為 CANCELLED
      const index = reservations.value.findIndex((r) => r.id === id)
      if (index !== -1) {
        const reservation = reservations.value[index]
        if (reservation) {
          reservation.status = 'CANCELLED'
        }
      }
      // 更新當前預約狀態
      if (currentReservation.value?.id === id) {
        currentReservation.value.status = 'CANCELLED'
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '取消預約失敗'
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    reservations,
    currentReservation,
    loading,
    error,
    // Getters
    hasReservations,
    isLoading,
    errorMessage,
    // Actions
    createReservation,
    fetchMyReservations,
    fetchReservationById,
    updateReservation,
    cancelReservation,
    clearReservations,
    clearCurrentReservation
  }
})
