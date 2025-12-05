import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import {
  formatDate,
  formatTime,
  formatDateTime,
  formatDateChinese,
  parseDate,
  isValidDate,
  isValidTimeRange,
  isAfterToday,
  getToday,
  getTomorrow,
  getDateAfterDays,
  combineDateAndTime,
  isUpcoming,
  isPast,
  getHoursUntil
} from '../date'

describe('date utils', () => {
  // Mock current date for consistent tests
  const mockDate = new Date('2025-06-15T10:00:00')
  
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(mockDate)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('formatDate()', () => {
    it('should format Date object to YYYY-MM-DD', () => {
      const date = new Date('2025-06-15')
      expect(formatDate(date)).toBe('2025-06-15')
    })

    it('should format date string to YYYY-MM-DD', () => {
      expect(formatDate('2025-06-15T10:30:00')).toBe('2025-06-15')
    })
  })

  describe('formatTime()', () => {
    it('should format Date object to HH:mm', () => {
      const date = new Date('2025-06-15T14:30:00')
      expect(formatTime(date)).toBe('14:30')
    })

    it('should format date string to HH:mm', () => {
      expect(formatTime('2025-06-15T09:05:00')).toBe('09:05')
    })
  })

  describe('formatDateTime()', () => {
    it('should format Date object to YYYY-MM-DD HH:mm', () => {
      const date = new Date('2025-06-15T14:30:00')
      expect(formatDateTime(date)).toBe('2025-06-15 14:30')
    })
  })

  describe('formatDateChinese()', () => {
    it('should format date to Chinese format', () => {
      const date = new Date('2025-06-15')
      const result = formatDateChinese(date)
      expect(result).toContain('2025年')
      expect(result).toContain('06月')
      expect(result).toContain('15日')
    })
  })

  describe('parseDate()', () => {
    it('should parse valid date string', () => {
      const result = parseDate('2025-06-15')
      expect(result).toBeInstanceOf(Date)
      expect(result?.getFullYear()).toBe(2025)
      expect(result?.getMonth()).toBe(5) // 0-indexed
      expect(result?.getDate()).toBe(15)
    })

    it('should return null for invalid date string', () => {
      expect(parseDate('invalid-date')).toBeNull()
    })

    it('should parse date with custom format', () => {
      const result = parseDate('15/06/2025', 'dd/MM/yyyy')
      expect(result).toBeInstanceOf(Date)
      expect(result?.getDate()).toBe(15)
    })
  })

  describe('isValidDate()', () => {
    it('should return true for valid Date object', () => {
      expect(isValidDate(new Date('2025-06-15'))).toBe(true)
    })

    it('should return true for valid date string', () => {
      expect(isValidDate('2025-06-15')).toBe(true)
    })

    it('should return false for invalid date string', () => {
      expect(isValidDate('invalid')).toBe(false)
    })
  })

  describe('isValidTimeRange()', () => {
    it('should return true when start is before end', () => {
      expect(isValidTimeRange('2025-06-15T09:00:00', '2025-06-15T10:00:00')).toBe(true)
    })

    it('should return false when start is after end', () => {
      expect(isValidTimeRange('2025-06-15T10:00:00', '2025-06-15T09:00:00')).toBe(false)
    })

    it('should return false when start equals end', () => {
      expect(isValidTimeRange('2025-06-15T10:00:00', '2025-06-15T10:00:00')).toBe(false)
    })
  })

  describe('isAfterToday()', () => {
    it('should return true for future date', () => {
      expect(isAfterToday('2025-06-20')).toBe(true)
    })

    it('should return false for today when date is same day', () => {
      // Note: isAfterToday compares with start of today (00:00:00)
      // 2025-06-15 00:00:00 is NOT after 2025-06-15 00:00:00 (equal)
      const todayMidnight = new Date('2025-06-15T00:00:00')
      expect(isAfterToday(todayMidnight)).toBe(false)
    })

    it('should return false for past date', () => {
      expect(isAfterToday('2025-06-10')).toBe(false)
    })
  })

  describe('getToday()', () => {
    it('should return today as YYYY-MM-DD', () => {
      expect(getToday()).toBe('2025-06-15')
    })
  })

  describe('getTomorrow()', () => {
    it('should return tomorrow as YYYY-MM-DD', () => {
      expect(getTomorrow()).toBe('2025-06-16')
    })
  })

  describe('getDateAfterDays()', () => {
    it('should return date N days from today', () => {
      expect(getDateAfterDays(5)).toBe('2025-06-20')
    })

    it('should handle negative days', () => {
      expect(getDateAfterDays(-3)).toBe('2025-06-12')
    })
  })

  describe('combineDateAndTime()', () => {
    it('should combine date and time into ISO format', () => {
      expect(combineDateAndTime('2025-06-15', '14:30')).toBe('2025-06-15T14:30:00')
    })
  })

  describe('isUpcoming()', () => {
    it('should return true for future datetime', () => {
      expect(isUpcoming('2025-06-15T12:00:00')).toBe(true)
    })

    it('should return false for past datetime', () => {
      expect(isUpcoming('2025-06-15T08:00:00')).toBe(false)
    })
  })

  describe('isPast()', () => {
    it('should return true for past datetime', () => {
      expect(isPast('2025-06-15T08:00:00')).toBe(true)
    })

    it('should return false for future datetime', () => {
      expect(isPast('2025-06-15T12:00:00')).toBe(false)
    })
  })

  describe('getHoursUntil()', () => {
    it('should return hours until future datetime', () => {
      // mockDate is 10:00, target is 14:00 = 4 hours
      const hours = getHoursUntil('2025-06-15T14:00:00')
      expect(hours).toBe(4)
    })

    it('should return negative hours for past datetime', () => {
      // mockDate is 10:00, target is 08:00 = -2 hours
      const hours = getHoursUntil('2025-06-15T08:00:00')
      expect(hours).toBe(-2)
    })
  })
})
