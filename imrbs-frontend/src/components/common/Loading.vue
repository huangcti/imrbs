<!--
T049 [P] 建立 Loading 通用元件
顯示載入狀態的元件,支援全螢幕或內聯模式
-->

<script setup lang="ts">
interface Props {
  fullscreen?: boolean
  size?: 'small' | 'medium' | 'large'
  message?: string
}

const props = withDefaults(defineProps<Props>(), {
  fullscreen: false,
  size: 'medium',
  message: '載入中...'
})

const sizeClasses = {
  small: 'w-6 h-6 border-2',
  medium: 'w-12 h-12 border-4',
  large: 'w-16 h-16 border-4'
}
</script>

<template>
  <div
    :class="[
      'loading-container flex flex-col items-center justify-center',
      fullscreen
        ? 'fixed inset-0 bg-white bg-opacity-90 z-[9999]'
        : 'inline-flex p-4'
    ]"
    data-testid="loading-indicator"
  >
    <!-- Spinner -->
    <div
      :class="[
        'spinner border-blue-600 border-t-transparent rounded-full animate-spin',
        sizeClasses[size]
      ]"
    />

    <!-- 訊息 -->
    <p
      v-if="message"
      :class="[
        'mt-4 text-gray-600',
        size === 'small' ? 'text-xs' : size === 'medium' ? 'text-sm' : 'text-base'
      ]"
    >
      {{ message }}
    </p>
  </div>
</template>

<style scoped>
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
