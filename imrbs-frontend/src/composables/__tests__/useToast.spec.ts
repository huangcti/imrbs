import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useToast } from '../useToast'

describe('useToast', () => {
  beforeEach(() => {
    // Clear all toast messages before each test
    const { clear } = useToast()
    clear()
    vi.useFakeTimers()
  })

  describe('show()', () => {
    it('should add a toast message with positional arguments', () => {
      const { show, messages } = useToast()
      
      show('success', 'Test message')
      
      expect(messages.length).toBe(1)
      expect(messages[0].type).toBe('success')
      expect(messages[0].message).toBe('Test message')
    })

    it('should add a toast message with object argument', () => {
      const { show, messages } = useToast()
      
      show({ type: 'error', message: 'Error message' })
      
      expect(messages.length).toBe(1)
      expect(messages[0].type).toBe('error')
      expect(messages[0].message).toBe('Error message')
    })

    it('should use default duration of 3000ms', () => {
      const { show, messages } = useToast()
      
      show('info', 'Test message')
      
      expect(messages[0].duration).toBe(3000)
    })

    it('should use custom duration when provided', () => {
      const { show, messages } = useToast()
      
      show('info', 'Test message', 5000)
      
      expect(messages[0].duration).toBe(5000)
    })

    it('should auto-remove toast after duration + 500ms', () => {
      const { show, messages } = useToast()
      
      show('success', 'Test message', 1000)
      expect(messages.length).toBe(1)
      
      vi.advanceTimersByTime(1500)
      expect(messages.length).toBe(0)
    })
  })

  describe('success()', () => {
    it('should add a success toast', () => {
      const { success, messages } = useToast()
      
      success('Success message')
      
      expect(messages.length).toBe(1)
      expect(messages[0].type).toBe('success')
    })
  })

  describe('error()', () => {
    it('should add an error toast', () => {
      const { error, messages } = useToast()
      
      error('Error message')
      
      expect(messages.length).toBe(1)
      expect(messages[0].type).toBe('error')
    })
  })

  describe('warning()', () => {
    it('should add a warning toast', () => {
      const { warning, messages } = useToast()
      
      warning('Warning message')
      
      expect(messages.length).toBe(1)
      expect(messages[0].type).toBe('warning')
    })
  })

  describe('info()', () => {
    it('should add an info toast', () => {
      const { info, messages } = useToast()
      
      info('Info message')
      
      expect(messages.length).toBe(1)
      expect(messages[0].type).toBe('info')
    })
  })

  describe('remove()', () => {
    it('should remove a specific toast by id', () => {
      const { show, remove, messages } = useToast()
      
      show('success', 'First message')
      show('error', 'Second message')
      
      expect(messages.length).toBe(2)
      
      const firstId = messages[0].id
      remove(firstId)
      
      expect(messages.length).toBe(1)
      expect(messages[0].message).toBe('Second message')
    })

    it('should do nothing if toast id not found', () => {
      const { show, remove, messages } = useToast()
      
      show('success', 'Test message')
      expect(messages.length).toBe(1)
      
      remove(999)
      expect(messages.length).toBe(1)
    })
  })

  describe('clear()', () => {
    it('should remove all toast messages', () => {
      const { show, clear, messages } = useToast()
      
      show('success', 'First')
      show('error', 'Second')
      show('info', 'Third')
      
      expect(messages.length).toBe(3)
      
      clear()
      
      expect(messages.length).toBe(0)
    })
  })

  describe('multiple toasts', () => {
    it('should handle multiple concurrent toasts', () => {
      const { show, messages } = useToast()
      
      show('success', 'Message 1')
      show('error', 'Message 2')
      show('warning', 'Message 3')
      
      expect(messages.length).toBe(3)
      expect(messages.map(m => m.type)).toEqual(['success', 'error', 'warning'])
    })
  })
})
