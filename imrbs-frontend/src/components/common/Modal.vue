<!--
T049 [P] 建立 Modal 通用元件
可重用的對話框元件,支援自訂內容和動作按鈕
-->

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

interface Props {
  show: boolean
  title?: string
  width?: string
  closable?: boolean
  maskClosable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  show: false,
  title: '',
  width: '500px',
  closable: true,
  maskClosable: true
})

const emit = defineEmits<{
  close: []
  confirm: []
  cancel: []
}>()

function handleClose() {
  if (props.closable) {
    emit('close')
  }
}

function handleMaskClick() {
  if (props.maskClosable) {
    handleClose()
  }
}

function handleEscape(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.show && props.closable) {
    handleClose()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})
</script>

<template>
  <Transition name="modal">
    <div
      v-if="show"
      class="fixed inset-0 z-[9998] flex items-center justify-center p-4"
      @click.self="handleMaskClick"
    >
      <!-- 遮罩 -->
      <div class="absolute inset-0 bg-black bg-opacity-50" />

      <!-- Modal 內容 -->
      <div
        :style="{ width, maxWidth: '90vw' }"
        class="relative bg-white rounded-lg shadow-2xl max-h-[90vh] overflow-y-auto z-[9999]"
        @click.stop
      >
        <!-- Header -->
        <div
          v-if="title || closable"
          class="flex items-center justify-between px-6 py-4 border-b border-gray-200"
        >
          <h3 class="text-xl font-semibold text-gray-800">
            {{ title }}
          </h3>
          <button
            v-if="closable"
            class="text-gray-400 hover:text-gray-600 transition-colors"
            @click="handleClose"
          >
            <i class="pi pi-times text-xl" />
          </button>
        </div>

        <!-- Body -->
        <div class="px-6 py-4">
          <slot />
        </div>

        <!-- Footer -->
        <div v-if="$slots.footer" class="px-6 py-4 border-t border-gray-200 bg-gray-50">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
/* Modal 動畫 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .relative,
.modal-leave-active .relative {
  transition: transform 0.3s ease;
}

.modal-enter-from .relative {
  transform: scale(0.9);
}

.modal-leave-to .relative {
  transform: scale(0.9);
}
</style>
