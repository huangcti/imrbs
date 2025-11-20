<!--
T049 [P] 建立 Toast 通用元件
顯示暫時性訊息通知的元件,支援成功/錯誤/警告/資訊類型
-->

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

interface Props {
  show: boolean
  type?: ToastType
  message: string
  duration?: number
  position?: 'top' | 'top-right' | 'top-left' | 'bottom' | 'bottom-right' | 'bottom-left'
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  type: 'info',
  message: '',
  duration: 3000,
  position: 'top-right'
})

const emit = defineEmits<{
  close: []
}>()

const visible = ref(props.show)
let timer: ReturnType<typeof setTimeout> | null = null

// 根據類型決定顏色和圖示
const typeConfig = computed(() => {
  const configs = {
    success: {
      bgColor: 'bg-green-500',
      icon: 'pi pi-check-circle',
      textColor: 'text-white'
    },
    error: {
      bgColor: 'bg-red-500',
      icon: 'pi pi-times-circle',
      textColor: 'text-white'
    },
    warning: {
      bgColor: 'bg-yellow-500',
      icon: 'pi pi-exclamation-triangle',
      textColor: 'text-white'
    },
    info: {
      bgColor: 'bg-blue-500',
      icon: 'pi pi-info-circle',
      textColor: 'text-white'
    }
  }
  return configs[props.type]
})

// 根據位置決定定位樣式
const positionClasses = computed(() => {
  const positions = {
    top: 'top-4 left-1/2 -translate-x-1/2',
    'top-right': 'top-4 right-4',
    'top-left': 'top-4 left-4',
    bottom: 'bottom-4 left-1/2 -translate-x-1/2',
    'bottom-right': 'bottom-4 right-4',
    'bottom-left': 'bottom-4 left-4'
  }
  return positions[props.position]
})

function close() {
  visible.value = false
  emit('close')
}

function startTimer() {
  if (props.duration > 0) {
    timer = setTimeout(() => {
      close()
    }, props.duration)
  }
}

function clearTimer() {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

watch(
  () => props.show,
  (newVal) => {
    visible.value = newVal
    if (newVal) {
      clearTimer()
      startTimer()
    }
  },
  { immediate: true }
)
</script>

<template>
  <Transition name="toast">
    <div
      v-if="visible"
      :class="[
        'fixed z-[9999] flex items-center gap-3 px-6 py-4 rounded-lg shadow-lg min-w-[300px] max-w-md',
        typeConfig.bgColor,
        typeConfig.textColor,
        positionClasses
      ]"
      @mouseenter="clearTimer"
      @mouseleave="startTimer"
    >
      <!-- 圖示 -->
      <i :class="[typeConfig.icon, 'text-2xl']" />

      <!-- 訊息 -->
      <div class="flex-1">
        <p class="font-medium">
          {{ message }}
        </p>
      </div>

      <!-- 關閉按鈕 -->
      <button class="hover:opacity-80 transition-opacity" @click="close">
        <i class="pi pi-times" />
      </button>
    </div>
  </Transition>
</template>

<style scoped>
/* Toast 動畫 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
