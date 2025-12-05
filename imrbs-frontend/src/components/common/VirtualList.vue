<script setup lang="ts" generic="T">
/**
 * VirtualList - 虛擬滾動列表組件
 * 
 * 用於大量數據的高效能列表渲染，只渲染可視區域內的項目。
 * 支援固定高度和動態高度項目。
 * 
 * @example
 * <VirtualList
 *   :items="rooms"
 *   :item-height="80"
 *   :buffer-size="5"
 *   v-slot="{ item, index }"
 * >
 *   <RoomCard :room="item" />
 * </VirtualList>
 */

import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

interface Props {
  /** 資料列表 */
  items: T[]
  /** 每個項目的高度 (px) */
  itemHeight: number
  /** 容器高度 (px)，若不設定則自動計算 */
  containerHeight?: number
  /** 緩衝區大小 (上下各額外渲染的項目數) */
  bufferSize?: number
  /** 自定義容器 CSS 類別 */
  containerClass?: string
  /** 是否啟用平滑滾動 */
  smoothScroll?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  bufferSize: 5,
  containerClass: '',
  smoothScroll: true
})

const emit = defineEmits<{
  /** 捲動到底部事件 */
  (_e: 'scroll-end'): void
  /** 捲動事件 */
  (_e: 'scroll', _scrollTop: number): void
  /** 可見項目變更事件 */
  (_e: 'visible-change', _startIndex: number, _endIndex: number): void
}>()

// 容器 ref
const containerRef = ref<HTMLDivElement | null>(null)

// 滾動位置
const scrollTop = ref(0)

// 計算容器實際高度
const actualContainerHeight = computed(() => {
  if (props.containerHeight) {
    return props.containerHeight
  }
  // 預設高度
  return 400
})

// 總列表高度
const totalHeight = computed(() => {
  return props.items.length * props.itemHeight
})

// 可見項目數量
const visibleCount = computed(() => {
  return Math.ceil(actualContainerHeight.value / props.itemHeight)
})

// 起始索引 (含緩衝)
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight)
  return Math.max(0, index - props.bufferSize)
})

// 結束索引 (含緩衝)
const endIndex = computed(() => {
  const index = startIndex.value + visibleCount.value + props.bufferSize * 2
  return Math.min(props.items.length, index)
})

// 可見項目列表
const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value).map((item, index) => ({
    item,
    index: startIndex.value + index,
    style: {
      position: 'absolute' as const,
      top: `${(startIndex.value + index) * props.itemHeight}px`,
      left: 0,
      right: 0,
      height: `${props.itemHeight}px`
    }
  }))
})

// 佔位元素樣式 (用於維持總高度)
const placeholderStyle = computed(() => ({
  height: `${totalHeight.value}px`,
  position: 'relative' as const
}))

// 處理滾動事件
const handleScroll = (event: Event) => {
  const target = event.target as HTMLDivElement
  scrollTop.value = target.scrollTop
  emit('scroll', scrollTop.value)

  // 檢查是否滾動到底部
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  const currentScrollTop = target.scrollTop

  if (scrollHeight - currentScrollTop - clientHeight < props.itemHeight) {
    emit('scroll-end')
  }
}

// 監視可見範圍變化
watch([startIndex, endIndex], ([newStart, newEnd]) => {
  emit('visible-change', newStart, newEnd)
})

// 滾動到指定索引
const scrollToIndex = (index: number, behavior: 'auto' | 'smooth' = 'smooth') => {
  if (containerRef.value) {
    const targetScrollTop = index * props.itemHeight
    containerRef.value.scrollTo({
      top: targetScrollTop,
      behavior: props.smoothScroll ? behavior : 'auto'
    })
  }
}

// 滾動到頂部
const scrollToTop = (behavior: 'auto' | 'smooth' = 'smooth') => {
  scrollToIndex(0, behavior)
}

// 滾動到底部
const scrollToBottom = (behavior: 'auto' | 'smooth' = 'smooth') => {
  if (containerRef.value) {
    containerRef.value.scrollTo({
      top: totalHeight.value,
      behavior: props.smoothScroll ? behavior : 'auto'
    })
  }
}

// 取得當前滾動位置
const getScrollTop = () => scrollTop.value

// 取得當前可見範圍
const getVisibleRange = () => ({
  start: startIndex.value,
  end: endIndex.value
})

// 暴露方法給父組件
defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom,
  getScrollTop,
  getVisibleRange,
  containerRef
})

// 節流處理滾動
let scrollRAF: number | null = null
const throttledScroll = (event: Event) => {
  if (scrollRAF) {
    cancelAnimationFrame(scrollRAF)
  }
  scrollRAF = requestAnimationFrame(() => {
    handleScroll(event)
  })
}

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.addEventListener('scroll', throttledScroll, { passive: true })
  }
})

onUnmounted(() => {
  if (containerRef.value) {
    containerRef.value.removeEventListener('scroll', throttledScroll)
  }
  if (scrollRAF) {
    cancelAnimationFrame(scrollRAF)
  }
})

// 監視 items 變化，重置滾動
watch(() => props.items.length, async (newLength, oldLength) => {
  if (newLength !== oldLength) {
    await nextTick()
    // 如果項目減少且當前滾動位置超出範圍，調整滾動位置
    if (containerRef.value) {
      const maxScrollTop = Math.max(0, totalHeight.value - actualContainerHeight.value)
      if (scrollTop.value > maxScrollTop) {
        containerRef.value.scrollTop = maxScrollTop
      }
    }
  }
})
</script>

<template>
  <div
    ref="containerRef"
    :class="['virtual-list-container', containerClass]"
    :style="{
      height: `${actualContainerHeight}px`,
      overflow: 'auto',
      position: 'relative'
    }"
  >
    <!-- 佔位元素維持總高度 -->
    <div :style="placeholderStyle">
      <!-- 可見項目 -->
      <div
        v-for="{ item, index, style } in visibleItems"
        :key="index"
        :style="style"
        class="virtual-list-item"
      >
        <slot :item="item" :index="index" />
      </div>
    </div>

    <!-- 空狀態 -->
    <div
      v-if="items.length === 0"
      class="virtual-list-empty"
    >
      <slot name="empty">
        <div class="text-center text-gray-500 py-8">
          {{ $t('common.noData') }}
        </div>
      </slot>
    </div>
  </div>
</template>

<style scoped>
.virtual-list-container {
  /* 自定義滾動條樣式 */
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.virtual-list-container::-webkit-scrollbar {
  width: 8px;
}

.virtual-list-container::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.virtual-list-container::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

.virtual-list-container::-webkit-scrollbar-thumb:hover {
  background-color: #94a3b8;
}

.virtual-list-item {
  box-sizing: border-box;
}

.virtual-list-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
