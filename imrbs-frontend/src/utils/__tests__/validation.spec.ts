import { describe, it, expect } from 'vitest'
import {
  isValidEmail,
  validateParticipants,
  validateCapacity
} from '../validation'

describe('validation utils', () => {
  describe('isValidEmail()', () => {
    it('should return true for valid email', () => {
      expect(isValidEmail('test@example.com')).toBe(true)
      expect(isValidEmail('user.name@domain.org')).toBe(true)
      expect(isValidEmail('user+tag@company.co.uk')).toBe(true)
    })

    it('should return false for invalid email', () => {
      expect(isValidEmail('')).toBe(false)
      expect(isValidEmail('invalid')).toBe(false)
      expect(isValidEmail('no@domain')).toBe(false)
      expect(isValidEmail('@nodomain.com')).toBe(false)
      expect(isValidEmail('spaces in@email.com')).toBe(false)
    })
  })

  describe('validateParticipants()', () => {
    it('should return valid for empty array', () => {
      const result = validateParticipants([])
      expect(result.valid).toBe(true)
      expect(result.errors).toHaveLength(0)
    })

    it('should return valid for array of valid emails', () => {
      const result = validateParticipants([
        'user1@example.com',
        'user2@example.com',
        'user3@example.com'
      ])
      expect(result.valid).toBe(true)
      expect(result.errors).toHaveLength(0)
    })

    it('should return error for invalid email in array', () => {
      const result = validateParticipants([
        'valid@example.com',
        'invalid-email',
        'another@valid.com'
      ])
      expect(result.valid).toBe(false)
      expect(result.errors.length).toBeGreaterThan(0)
      expect(result.errors[0]).toContain('Email 格式無效')
    })

    it('should return error when participants exceed 50', () => {
      const participants = Array.from({ length: 51 }, (_, i) => `user${i}@example.com`)
      const result = validateParticipants(participants)
      expect(result.valid).toBe(false)
      expect(result.errors).toContain('參與者人數不能超過 50 人')
    })

    it('should report multiple invalid emails', () => {
      const result = validateParticipants([
        'invalid1',
        'valid@example.com',
        'invalid2'
      ])
      expect(result.valid).toBe(false)
      expect(result.errors.length).toBe(2)
    })
  })

  describe('validateCapacity()', () => {
    it('should return valid when room capacity is sufficient', () => {
      const result = validateCapacity(5, 10)
      expect(result.valid).toBe(true)
    })

    it('should return valid when room capacity equals required', () => {
      const result = validateCapacity(10, 10)
      expect(result.valid).toBe(true)
    })

    it('should return invalid when room capacity is insufficient', () => {
      const result = validateCapacity(15, 10)
      expect(result.valid).toBe(false)
      expect(result.message).toContain('容量不足')
    })
  })
})
