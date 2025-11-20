/**
 * T075 [P] [US1] 實作表單驗證工具函式
 * 提供預約表單的驗證規則
 */

import * as yup from 'yup'
import { isValidTimeRange, isAfterToday } from './date'

/**
 * 會議室搜尋參數驗證 Schema
 */
export const roomSearchSchema = yup.object({
  date: yup
    .string()
    .required('請選擇日期')
    .test('is-after-today', '日期必須是今天或之後', (value) => {
      if (!value) return false
      return isAfterToday(value) || value === new Date().toISOString().split('T')[0]
    }),
  startTime: yup.string().required('請選擇開始時間'),
  endTime: yup
    .string()
    .required('請選擇結束時間')
    .test('is-after-start', '結束時間必須晚於開始時間', function (value) {
      const { date, startTime } = this.parent
      if (!date || !startTime || !value) return false
      const start = `${date}T${startTime}:00`
      const end = `${date}T${value}:00`
      return isValidTimeRange(start, end)
    }),
  capacity: yup.number().min(1, '容量必須至少為 1').nullable(),
  building: yup.string().nullable(),
  floor: yup.number().nullable()
})

/**
 * 預約創建表單驗證 Schema
 */
export const reservationCreateSchema = yup.object({
  roomId: yup.number().required('請選擇會議室').min(1, '會議室 ID 無效'),
  startTime: yup.string().required('請選擇開始時間'),
  endTime: yup
    .string()
    .required('請選擇結束時間')
    .test('is-after-start', '結束時間必須晚於開始時間', function (value) {
      const { startTime } = this.parent
      if (!startTime || !value) return false
      return isValidTimeRange(startTime, value)
    }),
  purpose: yup
    .string()
    .required('請輸入會議目的')
    .min(5, '會議目的至少需要 5 個字元')
    .max(200, '會議目的不能超過 200 個字元'),
  participants: yup
    .array()
    .of(yup.string().email('參與者必須是有效的 Email 地址'))
    .min(0, '參與者清單不能為空')
    .max(50, '參與者人數不能超過 50 人')
})

/**
 * 驗證 Email 格式
 */
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 驗證參與者 Email 清單
 */
export function validateParticipants(participants: string[]): { valid: boolean; errors: string[] } {
  const errors: string[] = []

  if (participants.length === 0) {
    return { valid: true, errors: [] } // 允許空清單
  }

  if (participants.length > 50) {
    errors.push('參與者人數不能超過 50 人')
  }

  participants.forEach((email, index) => {
    if (!isValidEmail(email)) {
      errors.push(`第 ${index + 1} 個參與者的 Email 格式無效: ${email}`)
    }
  })

  return {
    valid: errors.length === 0,
    errors
  }
}

/**
 * 驗證會議室容量是否足夠
 */
export function validateCapacity(
  requiredCapacity: number,
  roomCapacity: number
): { valid: boolean; message: string } {
  if (requiredCapacity > roomCapacity) {
    return {
      valid: false,
      message: `會議室容量不足。需要: ${requiredCapacity} 人，可容納: ${roomCapacity} 人`
    }
  }
  return { valid: true, message: '' }
}
