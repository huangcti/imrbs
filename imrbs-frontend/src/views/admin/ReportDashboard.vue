<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'
import ReportFilter from '@/components/admin/ReportFilter.vue'
import UsageChart from '@/components/admin/UsageChart.vue'
import UsageTable from '@/components/admin/UsageTable.vue'
import { reportService, type UsageReport, type ReportFilter as FilterParams } from '@/services/report.service'
import { roomService } from '@/services/room.service'

const { t } = useI18n()
const { showSuccess, showError } = useToast()

// State
const loading = ref(false)
const exporting = ref(false)
const report = ref<UsageReport | null>(null)
const rooms = ref<Array<{ id: number; name: string }>>([])
const currentFilter = ref<FilterParams>({
  startDate: '',
  endDate: '',
  roomIds: [],
  periodType: 'monthly'
})

// Computed values for chart
const roomStats = computed(() => report.value?.roomStats ?? [])
const hourlyStats = computed(() => report.value?.hourlyStats ?? [])
const dayStats = computed(() => report.value?.dayStats ?? [])
const overallUsageRate = computed(() => report.value?.overallUsageRate ?? 0)
const totalUsageHours = computed(() => report.value?.totalUsageHours ?? 0)
const totalReservations = computed(() => report.value?.totalReservations ?? 0)

// Peak hours display
const peakHoursDisplay = computed(() => {
  if (!report.value?.peakHours || report.value.peakHours.length === 0) {
    return t('report.noPeakHours')
  }
  return report.value.peakHours.join(', ')
})

// Off-peak hours display
const offPeakHoursDisplay = computed(() => {
  if (!report.value?.offPeakHours || report.value.offPeakHours.length === 0) {
    return t('report.noOffPeakHours')
  }
  return report.value.offPeakHours.join(', ')
})

// Load rooms for filter
const loadRooms = async () => {
  try {
    const response = await roomService.getAllRooms()
    rooms.value = response.map((r) => ({
      id: r.id,
      name: r.name
    }))
  } catch (error) {
    console.error('Failed to load rooms:', error)
  }
}

// Handle filter change
const handleFilterChange = async (filter: FilterParams) => {
  currentFilter.value = filter
  await loadReport()
}

// Load report data
const loadReport = async () => {
  if (!currentFilter.value.startDate || !currentFilter.value.endDate) {
    return
  }

  loading.value = true
  try {
    report.value = await reportService.getUsageReport(currentFilter.value)
  } catch (error) {
    console.error('Failed to load report:', error)
    showError(t('report.error.loadFailed'))
  } finally {
    loading.value = false
  }
}

// Handle Excel export
const handleExportExcel = async () => {
  if (!currentFilter.value.startDate || !currentFilter.value.endDate) {
    showError(t('report.error.selectDateRange'))
    return
  }

  exporting.value = true
  try {
    const blob = await reportService.exportExcel(currentFilter.value)
    const filename = `usage-report_${currentFilter.value.startDate}_${currentFilter.value.endDate}.xlsx`
    reportService.downloadExcel(blob, filename)
    showSuccess(t('report.success.exported'))
  } catch (error) {
    console.error('Failed to export Excel:', error)
    showError(t('report.error.exportFailed'))
  } finally {
    exporting.value = false
  }
}

// Initialize
onMounted(async () => {
  await loadRooms()
  
  // Set default date range (this month)
  const today = new Date()
  const firstOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  
  currentFilter.value = {
    startDate: firstOfMonth.toISOString().split('T')[0] ?? '',
    endDate: today.toISOString().split('T')[0] ?? '',
    roomIds: [],
    periodType: 'monthly'
  }
  
  await loadReport()
})
</script>

<template>
  <div class="report-dashboard min-h-screen bg-gray-100 py-6">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Page Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900" data-testid="page-title">
          {{ t('report.title') }}
        </h1>
        <p class="mt-1 text-sm text-gray-500">
          {{ t('report.subtitle') }}
        </p>
      </div>

      <!-- Filter Section -->
      <ReportFilter
        :rooms="rooms"
        :loading="loading"
        data-testid="report-filter"
        @filter-change="handleFilterChange"
        @export-excel="handleExportExcel"
      />

      <!-- Report Period Info -->
      <div
        v-if="report"
        class="bg-white rounded-lg shadow-sm p-4 mb-6"
        data-testid="report-period"
      >
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div>
            <span class="text-gray-500">{{ t('report.reportPeriod') }}:</span>
            <span class="ml-2 font-medium text-gray-900">
              {{ report.startDate }} ~ {{ report.endDate }}
            </span>
          </div>
          <div class="flex gap-4">
            <div class="text-center">
              <div class="text-sm text-gray-500">
                {{ t('report.peakHours') }}
              </div>
              <div class="font-medium text-red-600">
                {{ peakHoursDisplay }}
              </div>
            </div>
            <div class="text-center">
              <div class="text-sm text-gray-500">
                {{ t('report.offPeakHours') }}
              </div>
              <div class="font-medium text-green-600">
                {{ offPeakHoursDisplay }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="mb-6">
        <UsageChart
          :room-stats="roomStats"
          :hourly-stats="hourlyStats"
          :day-stats="dayStats"
          :overall-usage-rate="overallUsageRate"
          :loading="loading"
          data-testid="usage-chart"
        />
      </div>

      <!-- Table Section -->
      <div class="mb-6">
        <UsageTable
          :room-stats="roomStats"
          :total-usage-hours="totalUsageHours"
          :total-reservations="totalReservations"
          :loading="loading"
          data-testid="usage-table"
        />
      </div>

      <!-- Export Status -->
      <div
        v-if="exporting"
        class="fixed bottom-4 right-4 bg-blue-600 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2"
        data-testid="export-status"
      >
        <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white" />
        <span>{{ t('report.exporting') }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.report-dashboard {
  /* Page-level styles */
}
</style>
