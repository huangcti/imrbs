/**
 * Toast 通知服務
 * 提供全域的 Toast 通知功能
 */

import { reactive } from 'vue'
import type { ToastType } from '@/components/common/Toast.vue'

interface ToastMessage {
  id: number
  type: ToastType
  message: string
  duration: number
}

const state = reactive<{
  messages: ToastMessage[]
}>({
  messages: []
})

let messageId = 0

// Object-style toast options
interface ToastOptions {
  type: ToastType
  message: string
  duration?: number
}

export function useToast() {
  function show(type: ToastType, message: string, duration?: number): void
  function show(options: ToastOptions): void
  function show(typeOrOptions: ToastType | ToastOptions, message?: string, duration = 3000) {
    let toastType: ToastType
    let toastMessage: string
    let toastDuration: number

    if (typeof typeOrOptions === 'object') {
      toastType = typeOrOptions.type
      toastMessage = typeOrOptions.message
      toastDuration = typeOrOptions.duration ?? 3000
    } else {
      toastType = typeOrOptions
      toastMessage = message!
      toastDuration = duration
    }

    const id = messageId++
    const toast: ToastMessage = {
      id,
      type: toastType,
      message: toastMessage,
      duration: toastDuration
    }

    state.messages.push(toast)

    // 自動移除
    setTimeout(() => {
      remove(id)
    }, toastDuration + 500) // 多 500ms 等待動畫
  }

  function remove(id: number) {
    const index = state.messages.findIndex((m) => m.id === id)
    if (index > -1) {
      state.messages.splice(index, 1)
    }
  }

  function success(message: string, duration?: number) {
    show('success', message, duration)
  }

  function error(message: string, duration?: number) {
    show('error', message, duration)
  }

  function warning(message: string, duration?: number) {
    show('warning', message, duration)
  }

  function info(message: string, duration?: number) {
    show('info', message, duration)
  }

  function clear() {
    state.messages.length = 0
  }

  return {
    messages: state.messages,
    show,
    remove,
    success,
    error,
    warning,
    info,
    clear,
    // Aliases for compatibility
    showToast: show,
    showSuccess: success,
    showError: error,
    showWarning: warning,
    showInfo: info
  }
}
