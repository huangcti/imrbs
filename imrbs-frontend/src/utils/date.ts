/**
 * T074 [P] [US1] 實作日期時間工具函式
 * 提供日期時間格式化與驗證功能
 */

import { format, parse, isValid, isBefore, isAfter, addDays } from 'date-fns'
import { zhTW } from 'date-fns/locale'

/**
 * 格式化日期為 YYYY-MM-DD
 */
export function formatDate(date: Date | string): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return format(dateObj, 'yyyy-MM-dd')
}

/**
 * 格式化時間為 HH:mm
 */
export function formatTime(date: Date | string): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return format(dateObj, 'HH:mm')
}

/**
 * 格式化日期時間為 YYYY-MM-DD HH:mm
 */
export function formatDateTime(date: Date | string): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return format(dateObj, 'yyyy-MM-dd HH:mm')
}

/**
 * 格式化日期為中文顯示 (例: 2025年11月20日 星期三)
 */
export function formatDateChinese(date: Date | string): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return format(dateObj, 'yyyy年MM月dd日 EEEE', { locale: zhTW })
}

/**
 * 解析日期字串
 */
export function parseDate(dateStr: string, formatStr: string = 'yyyy-MM-dd'): Date | null {
  try {
    const parsed = parse(dateStr, formatStr, new Date())
    return isValid(parsed) ? parsed : null
  } catch {
    return null
  }
}

/**
 * 驗證日期是否有效
 */
export function isValidDate(date: Date | string): boolean {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return isValid(dateObj)
}

/**
 * 驗證時間範圍 (開始時間必須早於結束時間)
 */
export function isValidTimeRange(startTime: string, endTime: string): boolean {
  const start = new Date(startTime)
  const end = new Date(endTime)
  return isValid(start) && isValid(end) && isBefore(start, end)
}

/**
 * 檢查日期是否在今天之後
 */
export function isAfterToday(date: Date | string): boolean {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return isAfter(dateObj, today)
}

/**
 * 取得今天的日期
 */
export function getToday(): string {
  return formatDate(new Date())
}

/**
 * 取得明天的日期
 */
export function getTomorrow(): string {
  return formatDate(addDays(new Date(), 1))
}

/**
 * 取得 N 天後的日期
 */
export function getDateAfterDays(days: number): string {
  return formatDate(addDays(new Date(), days))
}

/**
 * 合併日期與時間為 ISO 字串
 * @param date 日期 (YYYY-MM-DD)
 * @param time 時間 (HH:mm)
 * @returns ISO 8601 格式字串 (YYYY-MM-DDTHH:mm:ss)
 */
export function combineDateAndTime(date: string, time: string): string {
  return `${date}T${time}:00`
}

/**
 * 檢查日期時間是否為未來
 */
export function isUpcoming(dateTime: Date | string): boolean {
  const dateObj = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const now = new Date()
  return isAfter(dateObj, now)
}

/**
 * 檢查日期時間是否為過去
 */
export function isPast(dateTime: Date | string): boolean {
  const dateObj = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const now = new Date()
  return isBefore(dateObj, now)
}

/**
 * 計算距離指定時間還有多少小時
 */
export function getHoursUntil(dateTime: Date | string): number {
  const dateObj = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const now = new Date()
  const diffMs = dateObj.getTime() - now.getTime()
  return diffMs / (1000 * 60 * 60)
}
