package tw.huangcti.imrbs.domain.service;

import lombok.RequiredArgsConstructor;
import tw.huangcti.imrbs.domain.model.Reservation;
import tw.huangcti.imrbs.domain.repository.ReservationRepository;
import tw.huangcti.imrbs.domain.repository.RoomRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 熱門時段分析服務
 * 職責: 分析預約熱門時段、星期分佈、尖峰/離峰時段
 * 
 * 分析維度:
 * - 按小時分析 (08:00 - 18:00)
 * - 按星期分析 (週一至週五)
 * - 尖峰時段識別 (使用率 > 70%)
 */
@RequiredArgsConstructor
public class PopularTimeSlotsService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    // 營業時間配置
    private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);
    private static final LocalTime BUSINESS_END = LocalTime.of(18, 0);
    
    // 尖峰時段閾值
    private static final double PEAK_THRESHOLD = 0.7; // 70%

    /**
     * 分析每小時的預約頻率
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return Map<hour, reservationCount>
     */
    public Map<Integer, Integer> analyzeHourlyBookings(LocalDate startDate, LocalDate endDate) {
        Map<Integer, Integer> hourlyBookings = new HashMap<>();
        
        // 初始化營業時間內的每個小時
        for (int hour = BUSINESS_START.getHour(); hour < BUSINESS_END.getHour(); hour++) {
            hourlyBookings.put(hour, 0);
        }

        LocalDateTime queryStart = startDate.atTime(BUSINESS_START);
        LocalDateTime queryEnd = endDate.atTime(BUSINESS_END);

        // 取得所有會議室的預約
        List<Long> roomIds = roomRepository.findAll().stream()
                .map(room -> room.getId())
                .toList();

        for (Long roomId : roomIds) {
            List<Reservation> reservations = reservationRepository.findByRoomIdAndTimeRange(
                    roomId, queryStart, queryEnd);

            for (Reservation reservation : reservations) {
                if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
                    continue;
                }

                // 計算此預約覆蓋的每個小時
                int startHour = Math.max(reservation.getStartTime().getHour(), BUSINESS_START.getHour());
                int endHour = Math.min(reservation.getEndTime().getHour(), BUSINESS_END.getHour());
                
                // 如果結束時間恰好在整點，不計入該小時
                if (reservation.getEndTime().getMinute() == 0 && endHour > startHour) {
                    endHour--;
                }

                for (int hour = startHour; hour <= endHour && hour < BUSINESS_END.getHour(); hour++) {
                    hourlyBookings.merge(hour, 1, Integer::sum);
                }
            }
        }

        return hourlyBookings;
    }

    /**
     * 分析每個星期幾的預約頻率
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return Map<dayOfWeek, reservationCount>
     */
    public Map<DayOfWeek, Integer> analyzeDailyBookings(LocalDate startDate, LocalDate endDate) {
        Map<DayOfWeek, Integer> dailyBookings = new HashMap<>();
        
        // 初始化週一至週日
        for (DayOfWeek day : DayOfWeek.values()) {
            dailyBookings.put(day, 0);
        }

        LocalDateTime queryStart = startDate.atTime(BUSINESS_START);
        LocalDateTime queryEnd = endDate.atTime(BUSINESS_END);

        List<Long> roomIds = roomRepository.findAll().stream()
                .map(room -> room.getId())
                .toList();

        for (Long roomId : roomIds) {
            List<Reservation> reservations = reservationRepository.findByRoomIdAndTimeRange(
                    roomId, queryStart, queryEnd);

            for (Reservation reservation : reservations) {
                if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
                    continue;
                }

                DayOfWeek dayOfWeek = reservation.getStartTime().getDayOfWeek();
                dailyBookings.merge(dayOfWeek, 1, Integer::sum);
            }
        }

        return dailyBookings;
    }

    /**
     * 識別尖峰時段 (使用率 > 閾值)
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 尖峰時段列表 (小時)
     */
    public List<Integer> identifyPeakHours(LocalDate startDate, LocalDate endDate) {
        Map<Integer, Integer> hourlyBookings = analyzeHourlyBookings(startDate, endDate);
        
        // 計算平均預約數
        int totalBookings = hourlyBookings.values().stream().mapToInt(Integer::intValue).sum();
        int hourCount = hourlyBookings.size();
        double averageBookings = hourCount > 0 ? (double) totalBookings / hourCount : 0;

        if (averageBookings == 0) {
            return new ArrayList<>();
        }

        // 找出超過閾值的小時
        return hourlyBookings.entrySet().stream()
                .filter(entry -> entry.getValue() > averageBookings * PEAK_THRESHOLD)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /**
     * 識別離峰時段 (使用率低於平均)
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 離峰時段列表 (小時)
     */
    public List<Integer> identifyOffPeakHours(LocalDate startDate, LocalDate endDate) {
        Map<Integer, Integer> hourlyBookings = analyzeHourlyBookings(startDate, endDate);
        
        // 計算平均預約數
        int totalBookings = hourlyBookings.values().stream().mapToInt(Integer::intValue).sum();
        int hourCount = hourlyBookings.size();
        double averageBookings = hourCount > 0 ? (double) totalBookings / hourCount : 0;

        // 找出低於平均的小時
        return hourlyBookings.entrySet().stream()
                .filter(entry -> entry.getValue() < averageBookings)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /**
     * 取得最熱門的時段 (Top N)
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @param topN 前 N 名
     * @return 熱門時段列表
     */
    public List<TimeSlotStats> getTopPopularTimeSlots(
            LocalDate startDate, LocalDate endDate, int topN) {
        
        Map<Integer, Integer> hourlyBookings = analyzeHourlyBookings(startDate, endDate);
        
        return hourlyBookings.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN)
                .map(entry -> TimeSlotStats.builder()
                        .hour(entry.getKey())
                        .timeSlot(formatTimeSlot(entry.getKey()))
                        .bookingCount(entry.getValue())
                        .build())
                .toList();
    }

    /**
     * 取得最熱門的星期幾 (Top N)
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @param topN 前 N 名
     * @return 熱門星期列表
     */
    public List<DayStats> getTopPopularDays(
            LocalDate startDate, LocalDate endDate, int topN) {
        
        Map<DayOfWeek, Integer> dailyBookings = analyzeDailyBookings(startDate, endDate);
        
        return dailyBookings.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN)
                .map(entry -> DayStats.builder()
                        .dayOfWeek(entry.getKey())
                        .dayName(entry.getKey().getDisplayName(TextStyle.FULL, Locale.TAIWAN))
                        .bookingCount(entry.getValue())
                        .build())
                .toList();
    }

    /**
     * 產生完整的時段分析報告
     * 
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 時段分析報告
     */
    public TimeSlotAnalysisReport generateAnalysisReport(LocalDate startDate, LocalDate endDate) {
        Map<Integer, Integer> hourlyBookings = analyzeHourlyBookings(startDate, endDate);
        Map<DayOfWeek, Integer> dailyBookings = analyzeDailyBookings(startDate, endDate);
        
        List<Integer> peakHours = identifyPeakHours(startDate, endDate);
        List<Integer> offPeakHours = identifyOffPeakHours(startDate, endDate);
        
        // 轉換為 DTO 列表
        List<TimeSlotStats> hourlyStats = hourlyBookings.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> TimeSlotStats.builder()
                        .hour(entry.getKey())
                        .timeSlot(formatTimeSlot(entry.getKey()))
                        .bookingCount(entry.getValue())
                        .isPeak(peakHours.contains(entry.getKey()))
                        .build())
                .toList();

        List<DayStats> dayStats = dailyBookings.entrySet().stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .map(entry -> DayStats.builder()
                        .dayOfWeek(entry.getKey())
                        .dayName(entry.getKey().getDisplayName(TextStyle.FULL, Locale.TAIWAN))
                        .bookingCount(entry.getValue())
                        .build())
                .toList();

        return TimeSlotAnalysisReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .hourlyStats(hourlyStats)
                .dayStats(dayStats)
                .peakHours(peakHours.stream().map(this::formatTimeSlot).toList())
                .offPeakHours(offPeakHours.stream().map(this::formatTimeSlot).toList())
                .build();
    }

    /**
     * 格式化時段顯示
     */
    private String formatTimeSlot(int hour) {
        return String.format("%02d:00 - %02d:00", hour, hour + 1);
    }

    /**
     * 時段統計 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class TimeSlotStats {
        private int hour;
        private String timeSlot;
        private int bookingCount;
        private boolean isPeak;
    }

    /**
     * 星期統計 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class DayStats {
        private DayOfWeek dayOfWeek;
        private String dayName;
        private int bookingCount;
    }

    /**
     * 時段分析報告 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class TimeSlotAnalysisReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<TimeSlotStats> hourlyStats;
        private List<DayStats> dayStats;
        private List<String> peakHours;
        private List<String> offPeakHours;
    }
}
