import api from './api'

export interface RoomUsageStats {
  roomId: number
  roomName: string
  location?: string
  usageRate: number
  usageHours: number
  reservationCount: number
}

export interface TimeSlotStats {
  hour: number
  timeSlot: string
  bookingCount: number
  isPeak: boolean
}

export interface DayStats {
  dayOfWeek: string
  dayName: string
  bookingCount: number
}

export interface UsageReport {
  startDate: string
  endDate: string
  overallUsageRate: number
  totalUsageHours: number
  totalReservations: number
  roomStats: RoomUsageStats[]
  hourlyStats: TimeSlotStats[]
  dayStats: DayStats[]
  peakHours: string[]
  offPeakHours: string[]
}

export interface ReportSummary {
  startDate: string
  endDate: string
  overallUsageRate: number
  totalUsageHours: number
  totalReservations: number
  peakHoursCount: number
  busiestDay?: string
}

export interface ReportFilter {
  startDate: string
  endDate: string
  roomIds?: number[]
  periodType?: 'daily' | 'weekly' | 'monthly' | 'custom'
}

class ReportService {
  /**
   * 取得使用率報告
   */
  async getUsageReport(filter: ReportFilter): Promise<UsageReport> {
    const params = new URLSearchParams()
    params.append('startDate', filter.startDate)
    params.append('endDate', filter.endDate)
    
    if (filter.periodType) {
      params.append('periodType', filter.periodType)
    }
    
    if (filter.roomIds && filter.roomIds.length > 0) {
      filter.roomIds.forEach(id => params.append('roomIds', id.toString()))
    }

    const response = await api.get<UsageReport>(
      `/api/admin/reports/usage?${params.toString()}`
    )
    return response.data
  }

  /**
   * 取得報告摘要
   */
  async getReportSummary(startDate: string, endDate: string): Promise<ReportSummary> {
    const response = await api.get<ReportSummary>(
      `/api/admin/reports/usage/summary?startDate=${startDate}&endDate=${endDate}`
    )
    return response.data
  }

  /**
   * 取得今日報告
   */
  async getTodayReport(): Promise<UsageReport> {
    const response = await api.get<UsageReport>('/api/admin/reports/usage/today')
    return response.data
  }

  /**
   * 取得本週報告
   */
  async getThisWeekReport(): Promise<UsageReport> {
    const response = await api.get<UsageReport>('/api/admin/reports/usage/this-week')
    return response.data
  }

  /**
   * 取得本月報告
   */
  async getThisMonthReport(): Promise<UsageReport> {
    const response = await api.get<UsageReport>('/api/admin/reports/usage/this-month')
    return response.data
  }

  /**
   * 匯出 Excel 報告
   */
  async exportExcel(filter: ReportFilter): Promise<Blob> {
    const params = new URLSearchParams()
    params.append('startDate', filter.startDate)
    params.append('endDate', filter.endDate)
    
    if (filter.roomIds && filter.roomIds.length > 0) {
      filter.roomIds.forEach(id => params.append('roomIds', id.toString()))
    }

    const response = await api.post<Blob>(
      `/api/admin/reports/export/excel?${params.toString()}`,
      {},
      {
        responseType: 'blob',
        headers: {
          'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        }
      }
    )
    return response.data
  }

  /**
   * 下載 Excel 檔案
   */
  downloadExcel(blob: Blob, filename?: string): void {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename || `usage-report-${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
}

export const reportService = new ReportService()
export default reportService
