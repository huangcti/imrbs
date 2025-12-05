<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  type ChartData,
  type ChartOptions
} from 'chart.js'
import { Bar, Line, Doughnut } from 'vue-chartjs'

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
)

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

interface TimeSlotStats {
  hour: number
  timeSlot: string
  bookingCount: number
  isPeak: boolean
}

interface DayStats {
  dayOfWeek: string
  dayName: string
  bookingCount: number
}

interface Props {
  roomStats?: RoomStats[]
  hourlyStats?: TimeSlotStats[]
  dayStats?: DayStats[]
  overallUsageRate?: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  roomStats: () => [],
  hourlyStats: () => [],
  dayStats: () => [],
  overallUsageRate: 0,
  loading: false
})

// Chart type selection
type ChartType = 'room-usage' | 'hourly-trend' | 'day-distribution'
const activeChart = ref<ChartType>('room-usage')

const chartTabs = computed(() => [
  { key: 'room-usage' as ChartType, label: t('report.chart.roomUsage') },
  { key: 'hourly-trend' as ChartType, label: t('report.chart.hourlyTrend') },
  { key: 'day-distribution' as ChartType, label: t('report.chart.dayDistribution') }
])

// Room usage bar chart data
const roomUsageChartData = computed<ChartData<'bar'>>(() => ({
  labels: props.roomStats.map(r => r.roomName),
  datasets: [
    {
      label: t('report.chart.usageRate'),
      data: props.roomStats.map(r => r.usageRate),
      backgroundColor: props.roomStats.map(r => 
        r.usageRate >= 70 ? 'rgba(239, 68, 68, 0.7)' :  // Red for high usage
        r.usageRate >= 40 ? 'rgba(245, 158, 11, 0.7)' : // Orange for medium
        'rgba(34, 197, 94, 0.7)'  // Green for low
      ),
      borderColor: props.roomStats.map(r => 
        r.usageRate >= 70 ? 'rgb(239, 68, 68)' :
        r.usageRate >= 40 ? 'rgb(245, 158, 11)' :
        'rgb(34, 197, 94)'
      ),
      borderWidth: 1
    }
  ]
}))

const roomUsageChartOptions = computed<ChartOptions<'bar'>>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top'
    },
    title: {
      display: true,
      text: t('report.chart.roomUsageTitle')
    },
    tooltip: {
      callbacks: {
        label: (context) => `${context.parsed.y.toFixed(1)}%`
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      max: 100,
      title: {
        display: true,
        text: t('report.chart.usageRatePercent')
      },
      ticks: {
        callback: (value) => `${value}%`
      }
    },
    x: {
      title: {
        display: true,
        text: t('report.chart.roomName')
      }
    }
  }
}))

// Hourly trend line chart data
const hourlyTrendChartData = computed<ChartData<'line'>>(() => ({
  labels: props.hourlyStats.map(h => h.timeSlot),
  datasets: [
    {
      label: t('report.chart.bookingCount'),
      data: props.hourlyStats.map(h => h.bookingCount),
      borderColor: 'rgb(59, 130, 246)',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      fill: true,
      tension: 0.3,
      pointBackgroundColor: props.hourlyStats.map(h => 
        h.isPeak ? 'rgb(239, 68, 68)' : 'rgb(59, 130, 246)'
      ),
      pointRadius: props.hourlyStats.map(h => h.isPeak ? 6 : 4),
      pointHoverRadius: 8
    }
  ]
}))

const hourlyTrendChartOptions = computed<ChartOptions<'line'>>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top'
    },
    title: {
      display: true,
      text: t('report.chart.hourlyTrendTitle')
    },
    tooltip: {
      callbacks: {
        afterLabel: (context) => {
          const dataIndex = context.dataIndex
          const stats = props.hourlyStats[dataIndex]
          return stats?.isPeak ? `⚠️ ${t('report.chart.peakHour')}` : ''
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      title: {
        display: true,
        text: t('report.chart.reservationCount')
      }
    },
    x: {
      title: {
        display: true,
        text: t('report.chart.timeSlot')
      }
    }
  }
}))

// Day distribution doughnut chart data
const dayDistributionChartData = computed<ChartData<'doughnut'>>(() => ({
  labels: props.dayStats.map(d => d.dayName),
  datasets: [
    {
      data: props.dayStats.map(d => d.bookingCount),
      backgroundColor: [
        'rgba(239, 68, 68, 0.7)',   // 週一
        'rgba(245, 158, 11, 0.7)', // 週二
        'rgba(34, 197, 94, 0.7)',  // 週三
        'rgba(59, 130, 246, 0.7)', // 週四
        'rgba(139, 92, 246, 0.7)', // 週五
        'rgba(107, 114, 128, 0.7)', // 週六
        'rgba(75, 85, 99, 0.7)'    // 週日
      ],
      borderColor: [
        'rgb(239, 68, 68)',
        'rgb(245, 158, 11)',
        'rgb(34, 197, 94)',
        'rgb(59, 130, 246)',
        'rgb(139, 92, 246)',
        'rgb(107, 114, 128)',
        'rgb(75, 85, 99)'
      ],
      borderWidth: 1
    }
  ]
}))

const dayDistributionChartOptions = computed<ChartOptions<'doughnut'>>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'right'
    },
    title: {
      display: true,
      text: t('report.chart.dayDistributionTitle')
    },
    tooltip: {
      callbacks: {
        label: (context) => {
          const total = props.dayStats.reduce((sum, d) => sum + d.bookingCount, 0)
          const percentage = total > 0 ? ((context.parsed / total) * 100).toFixed(1) : 0
          return `${context.label}: ${context.parsed} (${percentage}%)`
        }
      }
    }
  }
}))

// Overall usage gauge visualization
const gaugeColor = computed(() => {
  if (props.overallUsageRate >= 70) return 'text-red-600'
  if (props.overallUsageRate >= 40) return 'text-yellow-600'
  return 'text-green-600'
})

const gaugeBgColor = computed(() => {
  if (props.overallUsageRate >= 70) return 'bg-red-100'
  if (props.overallUsageRate >= 40) return 'bg-yellow-100'
  return 'bg-green-100'
})
</script>

<template>
  <div class="usage-chart bg-white rounded-lg shadow-sm p-6">
    <!-- Overall Usage Summary -->
    <div
      class="flex items-center justify-center mb-6 p-4 rounded-lg"
      :class="gaugeBgColor"
      data-testid="overall-usage-summary"
    >
      <div class="text-center">
        <div class="text-sm text-gray-600 mb-1">
          {{ t('report.chart.overallUsageRate') }}
        </div>
        <div class="text-4xl font-bold" :class="gaugeColor">
          {{ overallUsageRate.toFixed(1) }}%
        </div>
      </div>
    </div>

    <!-- Chart Tabs -->
    <div class="border-b border-gray-200 mb-4">
      <nav class="flex space-x-4" aria-label="Chart tabs">
        <button
          v-for="tab in chartTabs"
          :key="tab.key"
          @click="activeChart = tab.key"
          :class="[
            'px-4 py-2 text-sm font-medium rounded-t-lg transition-colors',
            activeChart === tab.key
              ? 'text-blue-600 border-b-2 border-blue-600 bg-blue-50'
              : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
          ]"
          :data-testid="`chart-tab-${tab.key}`"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Loading State -->
    <div
      v-if="loading"
      class="h-80 flex items-center justify-center"
      data-testid="chart-loading"
    >
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
    </div>

    <!-- Charts -->
    <div v-else class="h-80" data-testid="chart-container">
      <!-- Room Usage Bar Chart -->
      <Bar
        v-if="activeChart === 'room-usage'"
        :data="roomUsageChartData"
        :options="roomUsageChartOptions"
        data-testid="room-usage-chart"
      />

      <!-- Hourly Trend Line Chart -->
      <Line
        v-else-if="activeChart === 'hourly-trend'"
        :data="hourlyTrendChartData"
        :options="hourlyTrendChartOptions"
        data-testid="hourly-trend-chart"
      />

      <!-- Day Distribution Doughnut Chart -->
      <Doughnut
        v-else-if="activeChart === 'day-distribution'"
        :data="dayDistributionChartData"
        :options="dayDistributionChartOptions"
        data-testid="day-distribution-chart"
      />
    </div>

    <!-- No Data State -->
    <div
      v-if="!loading && roomStats.length === 0"
      class="h-80 flex items-center justify-center text-gray-500"
      data-testid="no-data"
    >
      <div class="text-center">
        <span class="text-4xl mb-2 block">📊</span>
        <p>{{ t('report.chart.noData') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.usage-chart {
  border: 1px solid #e5e7eb;
}
</style>
