<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// Props
interface RoomStats {
  roomId: number
  roomName: string
  location?: string
  usageRate: number
  usageHours: number
  reservationCount: number
}

interface Props {
  roomStats?: RoomStats[]
  totalUsageHours?: number
  totalReservations?: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  roomStats: () => [],
  totalUsageHours: 0,
  totalReservations: 0,
  loading: false
})

// Sorting
type SortField = 'roomName' | 'usageRate' | 'usageHours' | 'reservationCount'
type SortOrder = 'asc' | 'desc'

const sortField = ref<SortField>('usageRate')
const sortOrder = ref<SortOrder>('desc')

const sortedRoomStats = computed(() => {
  const sorted = [...props.roomStats]
  sorted.sort((a, b) => {
    let comparison = 0
    switch (sortField.value) {
      case 'roomName':
        comparison = a.roomName.localeCompare(b.roomName)
        break
      case 'usageRate':
        comparison = a.usageRate - b.usageRate
        break
      case 'usageHours':
        comparison = a.usageHours - b.usageHours
        break
      case 'reservationCount':
        comparison = a.reservationCount - b.reservationCount
        break
    }
    return sortOrder.value === 'asc' ? comparison : -comparison
  })
  return sorted
})

// Handle column header click for sorting
const handleSort = (field: SortField) => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'desc'
  }
}

// Get sort icon
const getSortIcon = (field: SortField) => {
  if (sortField.value !== field) return '↕️'
  return sortOrder.value === 'asc' ? '↑' : '↓'
}

// Usage rate color coding
const getUsageRateClass = (rate: number) => {
  if (rate >= 70) return 'text-red-600 bg-red-50'
  if (rate >= 40) return 'text-yellow-600 bg-yellow-50'
  return 'text-green-600 bg-green-50'
}

// Progress bar for usage rate
const getProgressBarColor = (rate: number) => {
  if (rate >= 70) return 'bg-red-500'
  if (rate >= 40) return 'bg-yellow-500'
  return 'bg-green-500'
}

// Format hours
const formatHours = (hours: number) => {
  return hours.toFixed(1)
}
</script>

<template>
  <div class="usage-table bg-white rounded-lg shadow-sm overflow-hidden">
    <!-- Table Header -->
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-semibold text-gray-800">
        {{ t('report.table.title') }}
      </h3>
    </div>

    <!-- Summary Row -->
    <div class="px-6 py-3 bg-gray-50 border-b border-gray-200 flex flex-wrap gap-6">
      <div class="flex items-center gap-2">
        <span class="text-gray-500">{{ t('report.table.totalRooms') }}:</span>
        <span class="font-semibold text-gray-800" data-testid="total-rooms">
          {{ roomStats.length }}
        </span>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-gray-500">{{ t('report.table.totalHours') }}:</span>
        <span class="font-semibold text-gray-800" data-testid="total-hours">
          {{ formatHours(totalUsageHours) }} {{ t('common.hours') }}
        </span>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-gray-500">{{ t('report.table.totalReservations') }}:</span>
        <span class="font-semibold text-gray-800" data-testid="total-reservations">
          {{ totalReservations }} {{ t('common.count') }}
        </span>
      </div>
    </div>

    <!-- Loading State -->
    <div
      v-if="loading"
      class="p-8 flex items-center justify-center"
      data-testid="table-loading"
    >
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
    </div>

    <!-- Table -->
    <div v-else class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200" data-testid="usage-table">
        <thead class="bg-gray-50">
          <tr>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
              data-testid="sort-roomName"
              @click="handleSort('roomName')"
            >
              <div class="flex items-center gap-1">
                {{ t('report.table.roomName') }}
                <span class="text-xs">{{ getSortIcon('roomName') }}</span>
              </div>
            </th>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
            >
              {{ t('report.table.location') }}
            </th>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
              data-testid="sort-usageRate"
              @click="handleSort('usageRate')"
            >
              <div class="flex items-center gap-1">
                {{ t('report.table.usageRate') }}
                <span class="text-xs">{{ getSortIcon('usageRate') }}</span>
              </div>
            </th>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
              data-testid="sort-usageHours"
              @click="handleSort('usageHours')"
            >
              <div class="flex items-center gap-1">
                {{ t('report.table.usageHours') }}
                <span class="text-xs">{{ getSortIcon('usageHours') }}</span>
              </div>
            </th>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
              data-testid="sort-reservationCount"
              @click="handleSort('reservationCount')"
            >
              <div class="flex items-center gap-1">
                {{ t('report.table.reservations') }}
                <span class="text-xs">{{ getSortIcon('reservationCount') }}</span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr
            v-for="room in sortedRoomStats"
            :key="room.roomId"
            class="hover:bg-gray-50"
            :data-testid="`room-row-${room.roomId}`"
          >
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">
                {{ room.roomName }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-500">
                {{ room.location || '-' }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center gap-2">
                <!-- Progress Bar -->
                <div class="w-24 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    class="h-full rounded-full transition-all duration-300"
                    :class="getProgressBarColor(room.usageRate)"
                    :style="{ width: `${Math.min(room.usageRate, 100)}%` }"
                  />
                </div>
                <!-- Percentage Badge -->
                <span
                  class="px-2 py-1 text-xs font-medium rounded-full"
                  :class="getUsageRateClass(room.usageRate)"
                >
                  {{ room.usageRate.toFixed(1) }}%
                </span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">
                {{ formatHours(room.usageHours) }} {{ t('common.hours') }}
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm text-gray-900">
                {{ room.reservationCount }} {{ t('common.count') }}
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Empty State -->
      <div
        v-if="!loading && roomStats.length === 0"
        class="p-8 text-center text-gray-500"
        data-testid="empty-state"
      >
        <span class="text-4xl mb-2 block">📋</span>
        <p>{{ t('report.table.noData') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.usage-table {
  border: 1px solid #e5e7eb;
}
</style>
