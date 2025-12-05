<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// Props
interface Props {
  rooms?: Array<{ id: number; name: string }>
  loading?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  rooms: () => [],
  loading: false
})

// Emits
const emit = defineEmits<{
  (_e: 'filter-change', _filters: FilterParams): void
  (_e: 'export-excel'): void
}>()

// Filter state
interface FilterParams {
  startDate: string
  endDate: string
  roomIds: number[]
  periodType: 'daily' | 'weekly' | 'monthly' | 'custom'
}

const startDate = ref<string>('')
const endDate = ref<string>('')
const selectedRoomIds = ref<number[]>([])
const periodType = ref<'daily' | 'weekly' | 'monthly' | 'custom'>('monthly')

// Period type options
const periodTypeOptions = computed(() => [
  { value: 'daily', label: t('report.periodType.daily') },
  { value: 'weekly', label: t('report.periodType.weekly') },
  { value: 'monthly', label: t('report.periodType.monthly') },
  { value: 'custom', label: t('report.periodType.custom') }
])

// Initialize dates
const initializeDates = () => {
  const today = new Date()
  endDate.value = today.toISOString().split('T')[0] ?? ''
  
  // Default to this month
  const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  startDate.value = firstOfMonth.toISOString().split('T')[0] ?? ''
}

// Auto-adjust dates based on period type
watch(periodType, (newType) => {
  const today = new Date()
  endDate.value = today.toISOString().split('T')[0] ?? ''
  
  switch (newType) {
    case 'daily':
      startDate.value = today.toISOString().split('T')[0] ?? ''
      break
    case 'weekly': {
      const dayOfWeek = today.getDay()
      const monday = new Date(today)
      monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
      startDate.value = monday.toISOString().split('T')[0] ?? ''
      break
    }
    case 'monthly': {
      const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
      startDate.value = firstOfMonth.toISOString().split('T')[0] ?? ''
      break
    }
    // custom: keep current dates
  }
  
  emitFilterChange()
})

// Date validation
const isValidDateRange = computed(() => {
  if (!startDate.value || !endDate.value) return false
  return new Date(startDate.value) <= new Date(endDate.value)
})

// Emit filter change
const emitFilterChange = () => {
  if (!isValidDateRange.value) return
  
  emit('filter-change', {
    startDate: startDate.value,
    endDate: endDate.value,
    roomIds: selectedRoomIds.value,
    periodType: periodType.value
  })
}

// Handle search button click
const handleSearch = () => {
  emitFilterChange()
}

// Handle export button click
const handleExport = () => {
  emit('export-excel')
}

// Room selection toggle
const _toggleRoom = (roomId: number) => {
  const index = selectedRoomIds.value.indexOf(roomId)
  if (index > -1) {
    selectedRoomIds.value.splice(index, 1)
  } else {
    selectedRoomIds.value.push(roomId)
  }
}

// Select all rooms
const selectAllRooms = () => {
  selectedRoomIds.value = props.rooms.map(r => r.id)
}

// Clear room selection
const clearRoomSelection = () => {
  selectedRoomIds.value = []
}

// Initialize on mount
initializeDates()
</script>

<template>
  <div class="report-filter bg-white rounded-lg shadow-sm p-6 mb-6">
    <h3 class="text-lg font-semibold text-gray-800 mb-4">
      {{ t('report.filter.title') }}
    </h3>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Period Type -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          {{ t('report.filter.periodType') }}
        </label>
        <select
          v-model="periodType"
          class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          data-testid="period-type-select"
        >
          <option
            v-for="option in periodTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>

      <!-- Start Date -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          {{ t('report.filter.startDate') }}
        </label>
        <input
          v-model="startDate"
          type="date"
          :disabled="periodType !== 'custom'"
          class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
          data-testid="start-date-input"
        >
      </div>

      <!-- End Date -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          {{ t('report.filter.endDate') }}
        </label>
        <input
          v-model="endDate"
          type="date"
          :disabled="periodType !== 'custom'"
          :min="startDate"
          class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
          data-testid="end-date-input"
        >
      </div>

      <!-- Room Filter -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          {{ t('report.filter.rooms') }}
        </label>
        <div class="relative">
          <select
            v-model="selectedRoomIds"
            multiple
            class="w-full h-10 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            data-testid="room-select"
          >
            <option v-for="room in rooms" :key="room.id" :value="room.id">
              {{ room.name }}
            </option>
          </select>
          <div class="mt-1 flex gap-2">
            <button
              type="button"
              class="text-xs text-blue-600 hover:text-blue-800"
              @click="selectAllRooms"
            >
              {{ t('common.selectAll') }}
            </button>
            <button
              type="button"
              class="text-xs text-gray-600 hover:text-gray-800"
              @click="clearRoomSelection"
            >
              {{ t('common.clear') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Date Range Validation Error -->
    <div
      v-if="!isValidDateRange && startDate && endDate"
      class="mt-2 text-sm text-red-600"
      data-testid="date-error"
    >
      {{ t('report.filter.invalidDateRange') }}
    </div>

    <!-- Action Buttons -->
    <div class="mt-4 flex flex-wrap gap-3">
      <button
        type="button"
        :disabled="!isValidDateRange || loading"
        class="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        data-testid="search-button"
        @click="handleSearch"
      >
        <svg
          v-if="loading"
          class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        <span v-else class="mr-2">🔍</span>
        {{ t('report.filter.search') }}
      </button>

      <button
        type="button"
        :disabled="!isValidDateRange || loading"
        class="inline-flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        data-testid="export-button"
        @click="handleExport"
      >
        <span class="mr-2">📊</span>
        {{ t('report.filter.exportExcel') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.report-filter {
  border: 1px solid #e5e7eb;
}
</style>
